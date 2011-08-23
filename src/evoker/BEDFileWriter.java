package evoker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/** Write BED-Files */
public class BEDFileWriter {

    private BufferedOutputStream out;  // the output stream
    private byte buffer = 0;
    private byte placesLeftInBuffer = 4;

    /**
     * Write to a specified file
     * @param file to write to
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public BEDFileWriter(File file) throws FileNotFoundException, IOException {
        assert file != null;
        out = new BufferedOutputStream(new FileOutputStream(file));
        writeHeader();
    }

    /**
     * Writes bytes to the file
     * @param i
     * @throws IOException
     */
    public void write(byte[] i) throws IOException {
        out.write(i);
    }

    /**
     * Writes an array of genotypes (internal notation!) to the file
     * @param genotypes 
     */
    public void writeGenotypes(int[] genotypes) throws IOException {
        for (int geno : genotypes) {
            writeGenotype(geno);
        }
    }

    /**
     * Writes the magic numbers to the file
     * @throws IOException 
     */
    private void writeHeader() throws IOException {
        out.write(Integer.parseInt("01101100", 2));
        out.write(Integer.parseInt("00011011", 2));
        out.write(Integer.parseInt("00000001", 2));
    }

    /**
     * Writes single genotype to file
     * 
     * 00  Homozygote "1"/"1"   -> internal notation: 0
     * 01  Heterozygote         -> internal notation: 2
     * 11  Homozygote "2"/"2"   -> internal notation: 3
     * 10  Missing genotype     -> internal notation: 1
     */
    public void writeGenotype(int type) throws IOException {
        
        buffer = (byte) (buffer >>> 2);
        
        switch (type) {
            case 0:
                break;
            case 1:
                buffer = (byte) (buffer | 0x40);
                break;
            case 2:
                buffer = (byte) (buffer | 0x80);
                break;
            case 3:
                buffer = (byte) (buffer | 0xc0);
                break;
        }
        
        placesLeftInBuffer--;
        
        if (placesLeftInBuffer == 0) {
            clearBuffer();
            placesLeftInBuffer = 4;
        }
    }

    /**
     * Pads the buffer with zeros and flushes it
     * 
     * @throws IOException 
     */
    private void clearBuffer() throws IOException {
        
    }
    
    /**
     * Pads the buffer with zeros and flushes it
     * @throws IOException 
     */
    public void flush() throws IOException {
        clearBuffer();
    }

    /**
     * Closes the file (flushes it beforehand)
     * @throws IOException 
     */
    public void close() throws IOException {
        out.flush();
        out.close();
    }
}
