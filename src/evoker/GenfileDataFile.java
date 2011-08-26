package evoker;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;


public class GenfileDataFile extends BinaryDataFile{
	
	GenfileDataFile(String filename, int numInds, MarkerData md, String collection) throws IOException{
        super(filename, numInds, md, collection);
       
        // there are 3 four byte values for each ind in the ox format files
        bytesPerRecord = 3 * 4 * numInds;
        
        checkFile(bedMagic);
    }
	
	GenfileDataFile(String filename, int numInds, MarkerData md, String collection, boolean zipped) throws IOException{
        super(filename, numInds, md, collection);
       
        // there are 3 four byte values for each ind in the ox format files
        bytesPerRecord = 3 * 4 * numInds;
        compressed = true;
        // set the header offset here for now as it would normally be set in the checkfile method
        bedHeaderOffset = 8;
        // compressed file - do not use checkFile()
        // TODO: method for checking compressed files
        
        
    }

	public ArrayList<Byte> getRecord(long snpIndex) throws IOException{
        //have index, now load gen file
		BufferedInputStream genIS;
		ArrayList<Byte> genos = new ArrayList<Byte>();
		
        if (this.isCompressed()){
        	
        	genIS = new BufferedInputStream(new GZIPInputStream(new FileInputStream(file),8192));
        	
        } else{
        	
        	genIS = new BufferedInputStream(new FileInputStream(file),8192);
        	
        }
                
        //skip to SNP of interest
        //sometimes the skip() method doesn't skip as far as you ask, so you have to keep flogging it
        //java sux. 
        long remaining = (snpIndex * bytesPerRecord)+bedHeaderOffset;
        while ((remaining = remaining - genIS.skip(remaining)) > 0){
        }

        //read raw snp data
        byte[] binSnpData = new byte[bytesPerRecord];
        genIS.read(binSnpData, 0, bytesPerRecord);
        // close the input stream
        genIS.close();
        // convert the binary data array into a float array
        float[] floatSnpData = new float[numInds*3];
        int count = 0;
        for (int start = 0; start < bytesPerRecord; start = start + 4) {
        	floatSnpData[count] = arr2float(binSnpData, start);
        	count++;
        }
            
        // loop through each set of three values and then decide on the genotype
        for (int loop = 0; loop < floatSnpData.length; loop = loop + 3) {
        	
        	float aa = floatSnpData[loop];
        	float ab = floatSnpData[loop+1];
        	float bb = floatSnpData[loop+2];
              
        	//convert into array of genotypes
        	//genotype code is:
        	//0 == homo 1
        	//1 == missing
        	//2 == hetero
        	//3 == homo 2
        	if (aa > 0.9) {
        		genos.add((byte)0);
        	} else if (ab >0.9){
        		genos.add((byte)2);
        	} else if (bb > 0.9) {
            	  genos.add((byte)3);
        	} else {
        		genos.add((byte)1);
            }
        }
                   
        return genos;
        
	}
	public static float arr2float (byte[] arr, int start) {
		int i = 0;
		int len = 4;
		int cnt = 0;
		byte[] tmp = new byte[len];
		for (i = start; i < (start + len); i++) {
			tmp[cnt] = arr[i];
			cnt++;
		}
		int accum = 0;
		i = 0;
		for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 ) {
			accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
			i++;
		}
		return Float.intBitsToFloat(accum);
	}


}
