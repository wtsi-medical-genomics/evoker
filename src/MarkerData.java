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
    Hashtable<String,Marker> markerTable;
    int numCollections;

    public MarkerData(int numCollections){
        this.numCollections = numCollections;
        markerTable = new Hashtable<String,Marker>();
    }

    public MarkerData (String bimFilename)throws IOException{
        this.numCollections = 1;
        markerTable = new Hashtable<String,Marker>();
        addFile(bimFilename);
    }

    public void addFile(String bimFilename) throws IOException{
        addFile(bimFilename,0,0);
    }

    public void addFile(String bimFilename, int sampleIndex, int chrom) throws IOException {
        String currentLine;
        StringTokenizer st;
        BufferedReader bimReader =  new BufferedReader(new FileReader(bimFilename));

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
            markerTable.get(snpid).addSample(sampleIndex,index++);
        }
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

    public int getChrom(String name){
        return markerTable.get(name).getChrom();
    }

    public int getNumSNPs() {
        return getSNPs().size();
    }

    public Integer getIndex(String name, int index){
        if (markerTable.get(name) != null){
            return markerTable.get(name).getIndex(index);
        }else{
            return -1;
        }
    }
}
