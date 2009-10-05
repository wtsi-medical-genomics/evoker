import java.io.File;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

public abstract class BinaryDataFile extends BinaryData{

    File file;

    BinaryDataFile(String filename, int numInds, MarkerData md, String collection){
        super(numInds,md,collection);
        this.file = new File(filename);
    }

    public void checkFile(byte[] headers) throws IOException{

        if (file != null){
            if (file.length() != (numSNPs*bytesPerRecord) + headers.length){
                if (file.length() == (numSNPs*bytesPerRecord) + 8){
                    //alternate Oxford format
                    //Change headers byte[] to be a new byte[] of the correct things as specified by numSNPs and numInds.
                    ByteBuffer buf = ByteBuffer.allocate(8);
                    buf.order(ByteOrder.LITTLE_ENDIAN);
                    // in the case of remote data we need to compare the total snps as this is the value in the header
                    buf.putInt(this.totNumSNPs);
                    if (file.getName().endsWith("bed")){
                        // the inds value in the header is the number of columns--three values per ind
                        buf.putInt(this.numInds*3);
                    }else if (file.getName().endsWith("bnt")){
                        // the inds value in the header is the number of columns--two values per ind
                        buf.putInt(this.numInds*2);
                    }
                    buf.clear();
                    headers = new byte[8];
                    buf.get(headers, 0, 8);
                    
                    bntHeaderOffset = 8;
                } else{
                    throw new IOException(file +
                        " is not properly formatted.\n(Incorrect length.)");
                }
            }
        } else{
            //this is a useless message, but it implies badness 10000
            throw new IOException("File is null?!?");
        }

        //are the headers acceptable for this file?
        BufferedInputStream binaryIS = new BufferedInputStream(new FileInputStream(file),8192);
        byte[] fromFile = new byte[headers.length];
        binaryIS.read(fromFile,0,headers.length);

        for (int i = 0; i < headers.length; i++){
            if (fromFile[i] != headers[i]){
                throw new IOException(file +
                        " is not properly formatted.\n(Magic number is incorrect.)");
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
    }

    abstract Vector getRecord(int index) throws IOException;
    
}
