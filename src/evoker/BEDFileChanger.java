package evoker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import evoker.DataDirectory.ExtensionFilter;

/**
 * Change the genotype information within a bed file (and write to a new one)
 */
public class BEDFileChanger {

    /** Holds the list of inds (in correct order) */
    Vector<String> inds = null;
    /** Holds the changes made to the collection (_not_ in correct order)*/
    HashMap<String, HashMap<String, HashMap<String, Byte>>> changes;  // chromosome -> snp -> [ind -> change]
    /**  */
    HashMap<String, Marker> markerTable;
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
    //TP CHANGED THIS
    /** DATA DIRECTORY, required for looking up the number of SNPs associated with collections and chromosomes,
     * stored in a hashmap in MarkerData*/
    DataDirectory db;
    /**a boolean to store whether to print .bim and .fam files*/
    boolean printFullFileset;
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
            HashMap<String, Marker> markerTable, HashMap<String, HashMap<String, HashMap<String, Byte>>> changes,
            String toWriteTo, DataDirectory db, boolean printFullFileset) throws IOException {
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
        //TP CHANGED THIS
        this.db = db;
        this.printFullFileset = printFullFileset;
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
    //TP CHANGED THIS
    BEDFileChanger(DataDirectory db, String collection, String file, boolean printFullFileset) throws IOException {
        new BEDFileChanger(db.getMarkerData().collectionIndices.get(collection), collection, db.getDisplayName(),
                db.samplesByCollection.get(collection).inds, db.getMarkerData().getNumSNPs(collection),
                db.getMarkerData().getMarkerTable(), db.changesByCollection.get(collection), file, db, printFullFileset);
    }

    /**
     * Sets the connection between the internal genotype coding and the bed-file-one
     */
    private void setGenotypeCoding() {
        genotpeCoding.put((byte) 0, 0x00); //homo1
        genotpeCoding.put((byte) 1, 0x40); //missing
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

            //TP CHANGED THIS
            //output a .fam and .bim file for each .bed file
            //search through the current directory for files with the right names to copy
            if (printFullFileset) {File dir = new File(path);
            	File dirList[] = dir.listFiles();
            	for (File thisFile : dirList) {
            		String fileName = thisFile.getName();
            		String bimFileExt = (collection + "." + chromosome + ".bim");
            		String famFileExt = (collection + ".fam");
            	
            		if (fileName.endsWith(bimFileExt))
            		{
            			File destination = new File(path + "/" + collection + "mod." + chromosome + ".bim");
            			copyFile(thisFile, destination);
             			}
            	
            		else if (fileName.endsWith(famFileExt))
            		{
            			File destination = new File(path + "/" + collection + "mod." + chromosome + ".fam");
            			copyFile(thisFile, destination);
            		}
            	}
            }
            
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
                }
                bfw.write(rawSnpData);
                snpAt++;

                // remove snp from the todo list
                changes.get(chromosome).remove(nextSnpToStopAt);
            }
            //TP changed this
            // there is nothing to change anymore, but there are still snps to copy.
            for (; snpAt < db.getMarkerData().getNumSNPs(collection+chromosome); snpAt++) {
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
    
    
    //a function to copy a file
    //source help: http://blog-en.openalfa.com/how-to-rename-move-or-copy-a-file-in-java
    @SuppressWarnings("resource")
	private static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }
     
        FileChannel origin = null;
        FileChannel destination = null;
        try {
            origin = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
     
            long count = 0;
            long size = origin.size();              
            while((count += destination.transferFrom(origin, count, size-count))<size);
        }
        finally {
            if(origin != null) {
                origin.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }
}
