package evoker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Change the genotype information within a bed file (and write to a new one)
 */
public class BEDFileChanger {

    /** Holds the list of inds (in correct order) */
    Vector<String> inds = null;
    /** Holds the changes made to the collection (_not_ in correct order)*/
    HashMap<String, HashMap<String, HashMap<String, Byte>>> changes;  // chromosome -> snp -> [ind -> change]
    /**  */
    Hashtable<String, Marker> markerTable;
    /** Name of the collection to save */
    String collection = null;
    /** Path for file to be read */
    String path = null;
    /** internal ID of collection */
    int collectionID = -1;
    /** Absolute path of output file */
    String toWriteTo = null;
    /** Information about conversion from the internal genotype notation to the bed-one. */
    HashMap<Byte, Integer> genotpeCoding = new HashMap<Byte, Integer>();
    /** Number of SNPs contained in this collection */
    int noOfSnps = -1;

    /**
     * Create a bed file from a given one according to the specified changes
     * 
     * @param collectionID
     * @param collection
     * @param path
     * @param sd
     * @param noOfSnps
     * @param markerTable
     * @param changes
     * @param toWriteTo
     * @throws IOException 
     */
    BEDFileChanger(int collectionID, String collection, String path, Vector<String> inds, int noOfSnps,
            Hashtable<String, Marker> markerTable, HashMap<String, HashMap<String, HashMap<String, Byte>>> changes,
            String toWriteTo) throws IOException {
        assert collectionID >= 0 && collection != null && path != null && inds != null && 
                noOfSnps != 0 && markerTable != null && changes != null &&  !changes.keySet().isEmpty() && 
                toWriteTo != null;
        
        this.inds = inds;
        this.changes = (HashMap<String, HashMap<String, HashMap<String, Byte>>>) changes.clone();
        this.markerTable = markerTable;
        this.collection = collection;
        this.collectionID = collectionID;
        this.path = path;
        this.toWriteTo = toWriteTo;
        this.noOfSnps = noOfSnps;

        setGenotypeCoding();
        write();
    }

    /**
     * Create a bed file from a given one according to the specified changes.  
     * Same as the other constructor, but collects the information itself.
     * 
     * @param containing related information
     * @param collection name to save
     * @param absolute file(path) to write to
     * @throws IOException 
     */
    BEDFileChanger(DataDirectory db, String collection, String file) throws IOException {
        new BEDFileChanger(db.getMarkerData().collectionIndices.get(collection), collection, db.getDisplayName(),
                db.samplesByCollection.get(collection).inds, db.getMarkerData().getNumSNPs(collection),
                db.getMarkerData().getMarkerTable(), db.changesByCollection.get(collection), file);
    }

    /**
     * Sets the connection between the internal genotype coding and the bed-file-one
     */
    private void setGenotypeCoding() {
        genotpeCoding.put((byte) 0, 0x00); //homo1
        genotpeCoding.put((byte) 2, 0x80); //hetero
        genotpeCoding.put((byte) 3, 0xc0); //homo2
    }

    /**
     * The writing algorithm.
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void write() throws FileNotFoundException, IOException {
        int bytesPerSnp = (int) Math.ceil(((double) inds.size()) / 4);

        // for all the changed chromosomes.
        for (String chromosome : changes.keySet()) {
            //read file
            File f = new File(path + "/" + collection + "." + chromosome + ".bed");
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f), 8192);

            //write file
            File f_write = new File(toWriteTo + "." + chromosome + ".bed");
            if(f_write.exists()) f_write.delete();
            BEDFileWriter bfw = new BEDFileWriter(f_write);

            //skip header
            long toskip = 3;
            while ((toskip = toskip - bis.skip(toskip)) > 0);
            long snpAt = 0;

            byte[] rawSnpData = null;

            // until there is no snp left for this chromosome to change.
            while (changes.get(chromosome).keySet().size() > 0) {
                String nextSnpToStopAt = null;
                long nextSnpIndex = Long.MAX_VALUE;

                // find the first snip to be changed
                for (String s : changes.get(chromosome).keySet()) {
                    Marker m = markerTable.get(s);
                    int index = m.getIndex(collectionID);
                    if (index < nextSnpIndex) {
                        nextSnpIndex = index;
                        nextSnpToStopAt = s;
                    }
                }

                // read in snp per snp, write it to file until we reach a changed snp
                long skip = nextSnpIndex - snpAt;
                for (int i = 0; i < skip; i++) {
                    rawSnpData = new byte[bytesPerSnp];
                    bis.read(rawSnpData, 0, bytesPerSnp);
                    bfw.write(rawSnpData);
                    snpAt++;
                }

                // read that whole snp in
                rawSnpData = new byte[bytesPerSnp];
                bis.read(rawSnpData, 0, bytesPerSnp);

                // find changed inds
                for (String ind : changes.get(chromosome).get(nextSnpToStopAt).keySet()) {
                    long indexOfInd = inds.indexOf(ind);
                    int indexOfIndInArray = (int) (indexOfInd / 4);
                    int posInTheByteFromBeginning = (int) (3 - (indexOfInd % 4));  // still to be used as index, as it is turned around, big endian >.<
                    rawSnpData[indexOfIndInArray] = changeByte(rawSnpData[indexOfIndInArray], posInTheByteFromBeginning, changes.get(chromosome).get(nextSnpToStopAt).get(ind));
                    rawSnpData[0] = 0x00;
                }
                bfw.write(rawSnpData);
                snpAt++;

                // remove snp from the todo list
                changes.get(chromosome).remove(nextSnpToStopAt);
            }

            // there is nothing to change anymore, but there are still snps to coppy.
            for (; snpAt < noOfSnps; snpAt++) {
                rawSnpData = new byte[bytesPerSnp];
                bis.read(rawSnpData, 0, bytesPerSnp);
                bfw.write(rawSnpData);
            }

            bfw.flush();
            bfw.close();
        }
    }

    /**
     * Change genotype within a byte
     * 
     * @param byte to change
     * @param potition of the double-bit to change
     * @param genotype to change to (internal id-notation)
     * @return changed bit
     */
    private byte changeByte(byte b, int posInTheByteFromBeginning, byte changeTo) {
        int byteToChange = b & 0xff;  // java seems to convert bytes to ints while processing them, that'd give problems... (no, java does not suck, java is great.)
        int toOrWith = genotpeCoding.get(changeTo);
        int toResetTo0 = 0xc0;

        toOrWith = toOrWith >>> (posInTheByteFromBeginning * 2);
        toResetTo0 = toResetTo0 >>> (posInTheByteFromBeginning * 2);

        return (byte) ((byteToChange & ~toResetTo0) | toOrWith);
    }
}
