import java.io.*;
import java.util.Vector;

public abstract class BinaryData {

    MarkerData md;
    String collection;
    int numInds;
    int numSNPs;
    int bytesPerRecord;

    BinaryData(SampleData sd, MarkerData md, String collection){
        this.numInds = sd.getNumInds();
        this.numSNPs = md.getNumSNPs();
        this.md = md;
        this.collection = collection;
    }



    abstract Vector getRecord(String markerName);


}
