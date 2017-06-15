package evoker;

import java.util.ArrayList;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import evoker.Types.FileFormat;

public class RemoteBinaryFloatData extends RemoteBinaryData {

    protected int valuesPerEntry;

    RemoteBinaryFloatData(DataClient dc, int numInds, MarkerData md, String collection, int vals, String name, String chromosome, FileFormat fileFormat) throws IOException {
        super(dc, numInds, md, collection, chromosome);
        this.valuesPerEntry = vals;
        bytesPerRecord = valuesPerEntry * 4 * numInds;
        switch (fileFormat) {
            case UKBIOBANK:
                checkFile(name, bntMagic);
                break;
            case DEFAULT:
                checkFile(name, bntUKBiobankMagic);
                break;
        }
    }
    
    RemoteBinaryFloatData(DataClient dc, int numInds, MarkerData md, String collection, int vals, String chromosome) {
        super(dc, numInds, md, collection, chromosome);
        this.valuesPerEntry = vals;
        bytesPerRecord = valuesPerEntry * 4 * numInds;
    }

    public ArrayList<float[]> getRecord(String name) throws IOException{
        int snpIndex = md.getIndex(name,md.getSampleCollectionIndex(collection));

        if (snpIndex > -1){
            BinaryFloatDataFile bnt = new BinaryFloatDataFile(
                    dc.getLocalDir()+ File.separator+collection+"."+name+".bnt",
                    this);

            return bnt.getRecord(0);
        }else{
            return null;
        }
    }
}
