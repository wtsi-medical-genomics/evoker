package evoker;

import java.util.Hashtable;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Holds all the data about a set of SNPs, usually for one chromosome.
 * 
 */
public class MarkerData {

    //Hashtable<String,Integer> snpIndexTable;
    //Hashtable<String,String> snpAlleleATable;
    //Hashtable<String,String> snpAlleleBTable;

    //while this seems ridiculous, it is a considerable memory savings which is now not exposed anywhere
    //outside this class. Instead of having a hash keyed on strings of chroms, taking up something like
    //20 bytes per key, even though there are only a few possibilities. we do this dance to simultaneously
    //avoid the memory overhead for millions of entries while allowing "chrom" to be anything, rather than
    //just numbers 1..22 etc.

    HashMap<String,Byte> chromosomeLookup;    // chromosome -> id
    HashMap<Byte,String> chromosomeBackLookup;//  id -> chromosome

    HashMap<String,Marker> markerTable;       // SNP_Name -> Marker
    HashMap<String,Integer> collectionIndices;// Collection -> ID (from collectionIndices after first chromosome file)
    HashMap<String,Integer> snpsPerCollection;// Collection -> Number of SNPs
    private int numCollections;                 // Number of Collections
    private int runningCount;                   // Number of BimFiles so far
    
    //HashMap<String, HashMap<String, Vector<String>>> snpDB = new HashMap<String, HashMap<String, Vector<String>>>();

    public MarkerData(int numCollections){
        this.numCollections = numCollections;
        markerTable = new HashMap<String,Marker>();
        collectionIndices = new HashMap<String,Integer>();
        snpsPerCollection = new HashMap<String,Integer>();
        chromosomeLookup = new HashMap<String,Byte>();
        chromosomeBackLookup = new HashMap<Byte,String>();
        runningCount = -1;
    }


    public int getSampleCollectionIndex(String collection){
        return collectionIndices.get(collection);
    }

    /**
     * Returns a Random SNP ID from markerTable (all IDs as key)
     * @return SNP ID
     */
    public String getRandomSNP(){
        Vector v  = new Vector(markerTable.keySet());
        return (String)v.get((int)(Math.random()*markerTable.keySet().size()));
    }


    /**
     * Adds information of a bim file
     * @param bimFile
     * @param collection
     * @param chromosome
     * @param isOx
     * @throws IOException 
     */
    public void addFile(String bimFile, String collection, String chromosome,
                        boolean isOx) throws IOException {

    	// All of the chromosomes for one collection are loaded at once in DataDirectory
		// which is why we can increase runningCount once the collection doesn't exist in
		// collectionIndices as a key and not worry about returning to it later.
        if (!collectionIndices.containsKey(collection)){
            runningCount++;
            collectionIndices.put(collection,runningCount);
        }

        // We shouldn't get a NullPointerException here because addChromToLookup has been called
		// in DataDirectory when the bim or snp file is parsed.
        byte chrom = chromosomeLookup.get(chromosome);
        String currentLine;
        BufferedReader bimReader =  new BufferedReader(new FileReader(bimFile));

        //read through bim file to record marker order so we can quickly index
        //into binary files
        int index = 0;
        String[] bits;
        boolean missingAlleles = false;
        while ((currentLine = bimReader.readLine()) != null){
            bits = currentLine.split("\\s");
            StringBuffer snpid = null;
            char a = 'A',b = 'B';
            // check the size of the bits array
            if(bits.length >= 5) {
            	snpid = new StringBuffer(bits[1]);
            	if (isOx){
                    a = bits[3].toCharArray()[0];
                    b = bits[4].toCharArray()[0];
                }else{
                    a = bits[4].toCharArray()[0];
                    b = bits[5].toCharArray()[0];
                }
            } else if (bits.length == 1){
            	// if there is just 1 column assume the file contains only a SNP id
            	missingAlleles = true;
            	snpid = new StringBuffer(bits[0]);
            }  
            
            // not sure if these files exist            
//            else if(bits.length == 3) {
//            	// if there are 3 columns assume the file contains name, id and position
//            	missingAlleles = true;
//            	snpid = new StringBuffer(bits[1]);
//            } 
            String stringSnpid = snpid.toString();
            if (!markerTable.containsKey(stringSnpid))
                markerTable.put(stringSnpid, new Marker(numCollections,a,b,chrom));

            //TP: only the first two args are used in addSampleCollection
            markerTable.get(stringSnpid).addSampleCollection(runningCount,index++,a,b,stringSnpid);
            
//            if(! snpDB.containsKey(collection)) snpDB.put(collection, new HashMap<String, Vector<String>>());
//            if(! snpDB.get(collection).containsKey(chromosome)) snpDB.get(collection).put(chromosome, new Vector<String>());
//            snpDB.get(collection).get(chromosome).add(snpid.toString());
            
        }
        //TP CHANGED THIS
        int snpsSoFar = 0;	
        if (snpsPerCollection.containsKey(collection))
        	snpsSoFar = snpsPerCollection.get(collection);
        
        snpsPerCollection.put(collection,index + snpsSoFar);
        snpsPerCollection.put(collection+chromosome,index);
        
        if (missingAlleles) {
        	Genoplot.ld.log("WARNING: SNP file does not contain allele information");
        }
    }

//    public HashMap<String, Vector<String>> getSnpInfo(String collection){
//        return snpDB.get(collection);
//    }
    
    public char[] getAlleles(String snp){
        return markerTable.get(snp).getAlleles();
    }

    public String getChrom(String name){
        if (markerTable.get(name) != null){
            return chromosomeBackLookup.get(markerTable.get(name).getChrom());
        }else{
            return null;
        }
    }
    
    public HashMap<String, Marker> getMarkerTable(){
        return markerTable;
    }

    /**
     * Returns the number of SNPs contained by a Collection
     * @param collectionName
     * @return SNP number
     */
    public int getNumSNPs(String collection) {
        return snpsPerCollection.get(collection);
    }

    public Integer getIndex(String markerName, int sampleIndex){
        if (markerTable.get(markerName) != null){
            return markerTable.get(markerName).getIndex(sampleIndex);
        }else{
            return -1;
        }
    }
    
    public void addChromToLookup(String chrom, byte counter) {
        chromosomeLookup.put(chrom,counter);
        chromosomeBackLookup.put(counter,chrom);
    }
}
