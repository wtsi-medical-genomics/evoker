import java.util.Vector;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BinaryFloatData extends BinaryData{

    private int valuesPerEntry;

    BinaryFloatData(String filename, SampleData sd, MarkerData md, int valuesPerEntry) {
        super(filename, sd, md);
        this.valuesPerEntry = valuesPerEntry;
        bytesPerRecord = valuesPerEntry * 4 * numInds;

        checkFileLength();
    }

    public Vector<float[]> getRecord(String name, int index){
        //we subclass this so we can force the type of data in the Vector
        return super.getRecord(name, index);
    }

    Vector<float[]> getRecord(int snpIndex) throws IOException{
        //have index, now load float file
        //todo: network seems to block on opening FIS sometimes?
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis,8192);
        //todo: should probably put a magic number at front of float files?

        //skip to SNP of interest
        //sometimes the skip() method doesn't skip as far as you ask, so you have to keep flogging it
        //java sux.
        long remaining = snpIndex * bytesPerRecord;
        while ((remaining = remaining - bis.skip(remaining)) > 0){
        }

        byte[] rawData = new byte[bytesPerRecord];
        bis.read(rawData,0,bytesPerRecord);
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

