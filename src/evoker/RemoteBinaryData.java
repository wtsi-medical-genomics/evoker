package evoker;

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
    	
    	BigInteger fileSize       = new BigInteger(dc.getFTP().stat(filename).getSize().toString());
    	BigInteger checkSize      = BigInteger.valueOf(new Long(numSNPs)).multiply(BigInteger.valueOf(new Long(bytesPerRecord)));
    	    	    	
    	if (!fileSize.equals(checkSize.add(BigInteger.valueOf(new Long(headers.length))))) {
    		if (!fileSize.equals(checkSize.add(new BigInteger("8")))){
    			throw new IOException(filename + " is not properly formatted.\n(Incorrect length.)");
    		}
    	}
    }        
}
