import java.util.Hashtable;
import java.util.StringTokenizer;
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
    int numCollections;
    int runningCount;

    public MarkerData(int numCollections){
        this.numCollections = numCollections;
        markerTable = new Hashtable<String,Marker>();
        collectionIndices = new Hashtable<String,Integer>();
        chromosomeLookup = new Hashtable<String,Byte>();
        chromosomeBackLookup = new Hashtable<Byte,String>();
        runningCount = 0;
    }

    /*public MarkerData (String bimFilename)throws IOException{
        this.numCollections = 1;
        markerTable = new Hashtable<String,Marker>();
        collectionIndices = new Hashtable<String,Integer>();
        runningCount = 0;
        addFile(bimFilename);
    } */

    public int getSampleCollectionIndex(String collection){
        return collectionIndices.get(collection);
    }

    public String getRandomSNP(){
        Vector v  = new Vector(markerTable.keySet());
        return (String)v.get((int)(Math.random()*markerTable.keySet().size()));
    }


    public void addFile(String bimFile, String collection, String chromosome) throws IOException {
        byte chrom = chromosomeLookup.get(chromosome);
        collectionIndices.put(collection,runningCount);
        String currentLine;
        StringTokenizer st;
        BufferedReader bimReader =  new BufferedReader(new FileReader(bimFile));

        //read through bim file to record marker order so we can quickly index
        //into binary files
        int index = 0;
        while ((currentLine = bimReader.readLine()) != null){
            st = new StringTokenizer(currentLine);
            String chromVal = st.nextToken();
            String snpid = st.nextToken(); //snpid
            st.nextToken(); //gendist
            st.nextToken(); //physical position
            char a = st.nextToken().toCharArray()[0];
            char b = st.nextToken().toCharArray()[0];
            if (markerTable.get(snpid) ==  null){
                markerTable.put(snpid, new Marker(numCollections,a,b,chrom));
            }
            markerTable.get(snpid).addSampleCollection(runningCount,index++);
        }
        runningCount++;
    }

    public Vector<String> getSNPs(){
        return new Vector<String>(markerTable.keySet());
    }

    public String getAlleleA(String name){
        return markerTable.get(name).getAlleleA();
    }

    public String getAlleleB(String name){
        return markerTable.get(name).getAlleleB();
    }

    public String getChrom(String name){
        return chromosomeBackLookup.get(markerTable.get(name).getChrom());
    }

    public int getNumSNPs() {
        return getSNPs().size();
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
