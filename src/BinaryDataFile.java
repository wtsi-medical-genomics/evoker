import java.io.File;
import java.io.IOException;
import java.util.Vector;

public abstract class BinaryDataFile extends BinaryData{

    File file;

    BinaryDataFile(String filename, SampleData sd, MarkerData md, String collection){
        super(sd,md,collection);
        this.file = new File(filename);
    }

    void checkFileLength(){
        if (file != null){
            if (file.length() != numSNPs*bytesPerRecord){
                //todo: eek, wrong file size
            }
        }else{
            //todo: eek, file is null!
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
