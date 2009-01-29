import java.io.*;
import java.util.Vector;

public abstract class BinaryData {

    File file;
    MarkerData md;
    String collection;
    int numInds;
    int numSNPs;
    int bytesPerRecord;

    BinaryData(String filename, SampleData sd, MarkerData md, String collection){
        this.file = new File(filename);
        this.numInds = sd.getNumInds();
        this.numSNPs = md.getNumSNPs();
        this.md = md;
        this.collection = collection;
    }

    void checkFileLength(){
        if (file != null){
            if (file.length() != numSNPs*bytesPerRecord){
                //todo: eek, wrong file size
            }
        }
    }

    public Vector getRecord(String markerName){
        //do some checks on getting the data and handle errors centrally
        int snpIndex;
        try {
            if((snpIndex = md.getIndex(markerName,md.getSampleCollectionIndex(collection))) >= 0) {
                return getRecord(snpIndex);
            }
        }catch(IOException ioe) {
            //TODO: handle me
            //TODO: I don't know anything about that SNP?
        }
        return(null);
        //todo: if I return null, calling class should complain.
    }

    abstract Vector getRecord(int index) throws IOException;
}
