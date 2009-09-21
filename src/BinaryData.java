import java.io.*;
import java.util.Vector;

public abstract class BinaryData {

    protected static final byte[] bedMagic = {0x6c, 0x1b, 0x01};
    protected static final byte[] bntMagic = {0x1a, 0x31};

    protected MarkerData md;
    protected String collection;
    protected int numInds;
    protected int numSNPs;
    protected int bytesPerRecord;

    // default header offset for the 2 byte magic number
    protected int bntHeaderOffset = 2;
     // to hold the total number of snps when using remote data
    protected int totNumSNPs;

    BinaryData(int numInds, MarkerData md, String collection){
        this.numInds = numInds;
        this.numSNPs = md.getNumSNPs(collection);
        this.totNumSNPs = md.getNumSNPs(collection);
        this.md = md;
        this.collection = collection;
    }
                                                                                                                    


    public abstract Vector getRecord(String markerName) throws IOException;

    public abstract void checkFile(byte[] headers) throws IOException;

}
