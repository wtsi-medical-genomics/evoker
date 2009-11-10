import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.sshtools.j2ssh.sftp.SftpFile;


public abstract class RemoteBinaryData extends BinaryData{

    DataClient dc;

    RemoteBinaryData(DataClient dc, int numInds, MarkerData md, String collection){
        super (numInds,md,collection);
        this.dc = dc;
    }

//    public void checkFile(String filename){
//        //TODO: how to be able to run this check without unnecessarily slowing down the system?
//    	// get the size of the bed or bnt file
//    	// compare this to what we are expecting
//    	
//    	// first get the file name 
//    	// use evoker-helper.pl to get the size of the file?
//    	String[] out = new String[1];
//    	
//    	
//    	out[i] = ((SftpFile)files.get(i)).;
//    	
//    	if (file.length() != (numSNPs*bytesPerRecord) + headers.length){
//    		if (file.length() == (numSNPs*bytesPerRecord) + 8){
//    				
//    		} else{
//    			throw new IOException(file + " is not properly formatted.\n(Incorrect length.)");
//    		}
//    	}    	
//    }        
}
