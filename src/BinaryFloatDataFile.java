import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BinaryFloatDataFile extends BinaryDataFile{

    private int valuesPerEntry;

    BinaryFloatDataFile(String filename, int numInds, MarkerData md, String collection, int vals)
            throws IOException{
        super(filename, numInds, md,collection);
        this.valuesPerEntry = vals;
        bytesPerRecord = valuesPerEntry * 4 * numInds;

        checkFile(bntMagic);
    }
    
    BinaryFloatDataFile(String filename, int numInds, MarkerData md, String collection, int vals, boolean zipped)
    		throws IOException{
    	super(filename, numInds, md,collection);
    	this.valuesPerEntry = vals;
    	bytesPerRecord = valuesPerEntry * 4 * numInds;
    	compressed = true;
    	bntHeaderOffset = 8;
    	// compressed file - do not use checkFile()
        // TODO: method for checking compressed files

    }

    BinaryFloatDataFile(String filename, RemoteBinaryFloatData rbfd) throws IOException{
        super(filename,rbfd.numInds,rbfd.md,rbfd.collection);

        this.valuesPerEntry = rbfd.valuesPerEntry;
        bytesPerRecord = rbfd.bytesPerRecord;
        this.numSNPs = 1;
        
        checkFile(bntMagic);
    }

    public Vector<float[]> getRecord(String name){
        //we subclass this so we can force the type of data in the Vector
        return super.getRecord(name);
    }

    Vector<float[]> getRecord(int snpIndex) throws IOException{
        BufferedInputStream intIS;
        
        if (this.isCompressed()){	
        	intIS = new BufferedInputStream(new GZIPInputStream(new FileInputStream(file),8192));
        } else{
        	intIS = new BufferedInputStream(new FileInputStream(file),8192);
        }
        
        //skip to SNP of interest
        //sometimes the skip() method doesn't skip as far as you ask, so you have to keep flogging it
        //java sux.
        long remaining = (snpIndex * bytesPerRecord)+bntHeaderOffset;
        while ((remaining = remaining - intIS.skip(remaining)) > 0){
        }

        //read raw snp data
        byte[] rawData = new byte[bytesPerRecord];
        intIS.read(rawData, 0, bytesPerRecord);
        ByteBuffer rawDataBuffer = ByteBuffer.wrap(rawData);
        rawDataBuffer.order(ByteOrder.LITTLE_ENDIAN);

        Vector<float[]> record = new Vector<float[]>();
        for (int i = 0; i < numInds; i++){
            float[] send = new float[valuesPerEntry];
            for (int j = 0; j < valuesPerEntry; j++){
                send[j] = rawDataBuffer.getFloat();
            }
            record.add(send);
        }

        return record;
    }
}

