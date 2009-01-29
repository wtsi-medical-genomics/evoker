import java.io.*;
import java.util.Vector;

public class BedfileDataFile extends BinaryDataFile{


    static String[] alleleMap = {"N","A","C","G","T"};

    BedfileDataFile(String filename, SampleData sd, MarkerData md, String collection) {
        super(filename, sd, md,collection);

        bytesPerRecord = (int)Math.ceil(((double)numInds)/4);
        checkFileLength();
    }

    public Vector<Byte> getRecord(String name){
        //we subclass this so we can force the type of data in the Vector
        return super.getRecord(name);
    }

    public Vector<Byte> getRecord(int snpIndex) throws IOException{
        //have index, now load bed file
        BufferedInputStream bedIS = new BufferedInputStream(new FileInputStream(file),8192);

        //read headers
        byte[] magicNumber = new byte[2];
        bedIS.read(magicNumber,0, 2);
        byte[] mode = new byte[1];
        bedIS.read(mode, 0, 1);

        //verify headers
        if(!(magicNumber[0]==0x6c && magicNumber[1]==0x1b)) {
            //Error: BED file version is not supported
            //TODO: handle error
        }
        if(!(mode[0]==0x01)) {
            //Error: only SNP-major mode supported
            //TODO: handle error
        }

        //skip to SNP of interest
        //sometimes the skip() method doesn't skip as far as you ask, so you have to keep flogging it
        //java sux.
        long remaining = snpIndex * bytesPerRecord;
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
