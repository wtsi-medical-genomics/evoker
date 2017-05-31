package evoker;

import java.io.*;
import java.util.ArrayList;

public abstract class BinaryData {

    protected static final byte[] bedMagic = {0x6c, 0x1b, 0x01};
    protected static final byte[] bntMagic = {0x1a, 0x31};

    protected MarkerData md;
    protected String collection;
    protected String chromosome;
    protected int numInds;
    protected int numSNPs;
    protected int bytesPerRecord;

    /** default header offset for bnt files */
    protected int bntHeaderOffset = 2;
    /** default header offset for bed files */
    protected int bedHeaderOffset = 3;
     /** to hold the total number of snps when using remote data, as the total number of snps is required for checking Oxformat header information*/
    protected int totNumSNPs;

    BinaryData(int numInds, MarkerData md, String collection, String chromosome){
        this.numInds = numInds;
        this.numSNPs = md.getNumSNPs(collection + chromosome);
        this.totNumSNPs = md.getNumSNPs(collection);
        this.md = md;
        this.collection = collection;
        this.chromosome = chromosome;
    }

    public abstract ArrayList getRecord(String markerName) throws IOException;

    public abstract void checkFile(byte[] headers) throws IOException;

}
