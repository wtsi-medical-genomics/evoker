import java.io.*;
import java.util.Vector;

public class BedfileDataFile extends BinaryDataFile{


    static String[] alleleMap = {"N","A","C","G","T"};

    BedfileDataFile(String filename, int numInds, MarkerData md, String collection) throws IOException{
        super(filename, numInds, md, collection);

        bytesPerRecord = (int)Math.ceil(((double)numInds)/4);
        checkFile(bedMagic);
    }

    BedfileDataFile(String filename, RemoteBedfileData rbd) throws IOException{
        super(filename, rbd.numInds, rbd.md, rbd.collection);

        bytesPerRecord = (int)Math.ceil(((double)numInds)/4);
        numSNPs = 1;
        
        checkFile(bedMagic);
    }

    public Vector<Byte> getRecord(String name){
        //we subclass this so we can force the type of data in the Vector
        return super.getRecord(name);
    }

    public Vector<Byte> getRecord(int snpIndex) throws IOException{
        //have index, now load bed file
        BufferedInputStream bedIS = new BufferedInputStream(new FileInputStream(file),8192);


        //skip to SNP of interest
        //sometimes the skip() method doesn't skip as far as you ask, so you have to keep flogging it
        //java sux.
        long remaining = (snpIndex * bytesPerRecord)+3;
        while ((remaining = remaining - bedIS.skip(remaining)) > 0){
        }

        //read raw snp data
        byte[] rawSnpData = new byte[bytesPerRecord];
        bedIS.read(rawSnpData, 0, bytesPerRecord);

        //convert into array of genotypes
        //genotype code is:
        //0 == homo 1
        //1 == missing
        //2 == hetero
        //3 == homo 2
        byte[] snpData = new byte[numInds+3];
        int genoCount = 0;
        for (byte aRawSnpData : rawSnpData) {
            snpData[genoCount++] = (byte) ((aRawSnpData & (byte) 0x03) >>> 0);
            snpData[genoCount++] = (byte) ((aRawSnpData & (byte) 0x0c) >>> 2);
            snpData[genoCount++] = (byte) ((aRawSnpData & (byte) 0x30) >>> 4);
            snpData[genoCount++] = (byte) (((int)aRawSnpData & (int)0xc0) >>> 6);
            //note: may be up to 3 extra entries at end of snpData array     -- don't use snpData.length!
        }


        Vector<Byte> genos = new Vector<Byte>();
        for (int i = 0; i < numInds; i++){
            genos.add(snpData[i]);
        }

        return genos;
    }
}
