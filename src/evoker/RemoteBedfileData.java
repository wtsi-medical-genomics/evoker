package evoker;

import java.util.ArrayList;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;

public class RemoteBedfileData extends RemoteBinaryData {

    RemoteBedfileData(DataClient dc, int numInds, MarkerData md, String collection, String name, String chromosome) throws IOException {
        super(dc, numInds, md, collection, chromosome);
        bytesPerRecord = (int)Math.ceil(((double)numInds)/4);
        checkFile(name, bedMagic);
    }
    
    RemoteBedfileData(DataClient dc, int numInds, MarkerData md, String collection, String chromosome) {
        super(dc, numInds, md, collection, chromosome);
        bytesPerRecord = (int)Math.ceil(((double)numInds)/4);
    }


    public ArrayList<Byte> getRecord(String name)throws IOException{
        int snpIndex = md.getIndex(name,md.getSampleCollectionIndex(collection));

        if (snpIndex > -1){
            //ask data client to get this SNP
            dc.getSNPFiles(name,md.getChrom(name),collection,snpIndex,numInds,totNumSNPs);

            BedfileDataFile bed = new BedfileDataFile(dc.getLocalDir()+ File.separator+collection+"."+name+".bed",
                    this);

            return bed.getRecord(0);
        }else{
            return null;
        }

    }
}