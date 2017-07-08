package evoker;

import java.util.HashMap;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import evoker.Types.FileFormat;

/**
 * Holds the SampleIDs of a given fam file in a Vector
 */

public class SampleData {
    Vector<String> inds;
//    Vector<String> ukbBatchMembership;
    HashMap<String, Vector<String>> ukbSamplesByBatch;

    SampleData(String famFilename, FileFormat fileFormat) throws IOException{

        this.inds = new Vector<String>();
        if (fileFormat == FileFormat.UKBIOBANK) {
            ukbSamplesByBatch = new HashMap<String, Vector<String>>();
        }

        BufferedReader famReader = new BufferedReader(new FileReader(famFilename));
        String currentLine;
        String[] tokens;
        if (fileFormat == FileFormat.OXFORD){
            //strip headers
            famReader.readLine();
            famReader.readLine();
        }



        while ((currentLine = famReader.readLine()) != null) {
            tokens = currentLine.split("\\s");
            String sample = tokens[1];
            inds.add(sample);

            if (fileFormat == FileFormat.UKBIOBANK) {
                if (tokens.length == 5) {
                    throw new IOException("UK Biobank fam file ill-formed: requires a sixth column indicating the batch.");
                }
                if (tokens.length != 6) {
                    throw new IOException("UK Biobank fam file ill-formed: requires six columns.");
                }
                String batch = tokens[5];

                //ukbBatchMembership.add(batch);

                if (ukbSamplesByBatch.containsKey(batch)) {
                    ukbSamplesByBatch.get(batch).add(sample);
                } else {
                    Vector<String> v = new Vector<String>(1);
                    v.add(sample);
                    ukbSamplesByBatch.put(batch, v);
                }
            }
        }
        famReader.close();
    }

    SampleData(Vector<String> inds) throws IOException{
        this.inds = inds;
    }
    
    /**
     * Returns the index of a given ind
     * @param ind
     * @return 
     */
    public int getIndex(String ind){
        for(int i = 0; i < inds.size(); i++){
            String s = inds.get(i);
            if(s.equals(ind)){
               return i;
            }
        }
        return -1;
    }

    public String getInd(int i){
        return inds.get(i);
    }

    public int getNumInds(){
        return inds.size();
    }

}
