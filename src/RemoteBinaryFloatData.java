import java.util.Vector;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RemoteBinaryFloatData extends RemoteBinaryData {

    private int valuesPerEntry;

    RemoteBinaryFloatData(DataClient dc, SampleData sd, MarkerData md, String collection, int vals) {
        super(dc, sd, md, collection);
        this.valuesPerEntry = vals;
        bytesPerRecord = valuesPerEntry * 4 * numInds;
    }

    public Vector<float[]> getRecord(String name) {
        try{
            BufferedInputStream bntIS = new BufferedInputStream(
                    new FileInputStream(dc.getLocalDir()+ File.separator+collection+"."+name+".bnt"),8192);

            //read raw snp data
            byte[] rawSnpData = new byte[bytesPerRecord];
            bntIS.read(rawSnpData, 0, bytesPerRecord);


            ByteBuffer rawDataBuffer = ByteBuffer.wrap(rawSnpData);
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
        }catch (IOException e){
            System.out.println("e = " + e);
        }
        return (null);
    }
}
