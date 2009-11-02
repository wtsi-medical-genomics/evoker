import java.util.Hashtable;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

//This class holds all the data about a set of SNPs, usually for one chromosome.

public class MarkerData {

    //Hashtable<String,Integer> snpIndexTable;
    //Hashtable<String,String> snpAlleleATable;
    //Hashtable<String,String> snpAlleleBTable;

    //while this seems ridiculous, it is a considerable memory savings which is now not exposed anywhere
    //outside this class. Instead of having a hash keyed on strings of chroms, taking up something like
    //20 bytes per key, even though there are only a few possibilities. we do this dance to simultaneously
    //avoid the memory overhead for millions of entries while allowing "chrom" to be anything, rather than
    //just numbers 1..22 etc.

    Hashtable<String,Byte> chromosomeLookup;
    Hashtable<Byte,String> chromosomeBackLookup;

    Hashtable<String,Marker> markerTable;
    Hashtable<String,Integer> collectionIndices;
    Hashtable<String,Integer> snpsPerCollection;
    private int numCollections;
    private int runningCount;

    public MarkerData(int numCollections){
        this.numCollections = numCollections;
        markerTable = new Hashtable<String,Marker>();
        collectionIndices = new Hashtable<String,Integer>();
        snpsPerCollection = new Hashtable<String,Integer>();
        chromosomeLookup = new Hashtable<String,Byte>();
        chromosomeBackLookup = new Hashtable<Byte,String>();
        runningCount = -1;
    }


    public int getSampleCollectionIndex(String collection){
        return collectionIndices.get(collection);
    }

    public String getRandomSNP(){
        Vector v  = new Vector(markerTable.keySet());
        return (String)v.get((int)(Math.random()*markerTable.keySet().size()));
    }


    public void addFile(String bimFile, String collection, String chromosome,
                        boolean isOx) throws IOException {
        if (collectionIndices.get(collection) == null){
            runningCount++;
            collectionIndices.put(collection,runningCount);
        }
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
            if(bits.length == 5) {
            	snpid = new StringBuffer(bits[1]);
            	if (isOx){
                    a = bits[3].toCharArray()[0];
                    b = bits[4].toCharArray()[0];
                }else{
                    a = bits[4].toCharArray()[0];
                    b = bits[5].toCharArray()[0];
                }
            } else if(bits.length == 3) {
            	// if there are 3 columns assume the file contains name, id and position
            	missingAlleles = true;
            	snpid = new StringBuffer(bits[1]);
            } else if (bits.length == 1){
            	// if there is just 1 column assume the file contains only an id
            	missingAlleles = true;
            	snpid = new StringBuffer(bits[0]);
            } 
            if (markerTable.get(snpid.toString()) ==  null){
                markerTable.put(snpid.toString(), new Marker(numCollections,a,b,chrom));
            }
            markerTable.get(snpid.toString()).addSampleCollection(runningCount,index++,a,b,snpid.toString());
        }

        snpsPerCollection.put(collection,index);
        
        if (missingAlleles) {
        	Genoplot.ld.log("WARNING: SNP file does not contain allele information");
        }
    }

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
