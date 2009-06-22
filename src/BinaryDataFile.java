import java.io.File;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Vector;

public abstract class BinaryDataFile extends BinaryData{

    File file;

    BinaryDataFile(String filename, int numInds, MarkerData md, String collection){
        super(numInds,md,collection);
        this.file = new File(filename);
    }

    public void checkFile(byte[] headers) throws IOException{

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

        if (file != null){
            if (file.length() != (numSNPs*bytesPerRecord) + headers.length){
                throw new IOException(file +
                        " is not properly formatted.\n(Incorrect length.)");
            }
        }else{
            //this is a useless message, but it implies badness 10000
            throw new IOException("File is null?!?");
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
