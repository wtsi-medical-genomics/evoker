import java.io.*;
import java.util.Vector;

public abstract class BinaryData {

    File file;
    MarkerData md;
    int numInds;
    int numSNPs;
    int bytesPerRecord;

    BinaryData(String filename, SampleData sd, MarkerData md){
        this.file = new File(filename);
        this.numInds = sd.getNumInds();
        this.numSNPs = md.getNumSNPs();
        this.md = md;
    }

    void checkFileLength(){
        if (file.length() != numSNPs*bytesPerRecord){
            //todo: eek, wrong file size
        }
    }

    public Vector getRecord(String name, int index){
        //do some checks on getting the data and handle errors centrally
        int snpIndex;
        try {
            if((snpIndex = md.getIndex(name,index)) >= 0) {
                return getRecord(snpIndex);
            }
        }catch(IOException ioe) {
            //TODO: handle me
        }
        return(null);
        //todo: if I return null, calling class should complain.
    }

    abstract Vector getRecord(int index) throws IOException;
}
