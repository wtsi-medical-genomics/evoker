import java.io.IOException;
import java.math.BigInteger;

public abstract class RemoteBinaryData extends BinaryData{

    DataClient dc;

    RemoteBinaryData(DataClient dc, int numInds, MarkerData md, String collection){
        super (numInds,md,collection);
        this.dc = dc;
    }
    public void checkFile(byte[] headers) throws IOException{
    	throw new IOException("checkFile() for remote files requires a filename)");
    }
    public void checkFile(String filename, byte[] headers) throws IOException{
        //TODO: check the magic numbers
    	String fileSizeString = dc.getFTP().stat(filename).getSize().toString();
    	// All of the details in the Spec concerning overflow are ignored, as BigIntegers are made as large as necessary to accommodate the results of an operation.
    	BigInteger fileSize       = new BigInteger(fileSizeString);
    	BigInteger biOxHeaderSize = new BigInteger("8");
    	BigInteger biHeaderSize   = BigInteger.valueOf(new Long(headers.length));
    	BigInteger biNumSNPs      = BigInteger.valueOf(new Long(numSNPs));
    	BigInteger biBPR          = BigInteger.valueOf(new Long(bytesPerRecord));
    	BigInteger checkSize      = biNumSNPs.multiply(biBPR);
    	    	    	
    	if (!fileSize.equals(checkSize.add(biHeaderSize))) {
    		if (!fileSize.equals(checkSize.add(biOxHeaderSize))){
    			throw new IOException(filename + " is not properly formatted.\n(Incorrect length.)");
    		}
    	}
    }        
}
