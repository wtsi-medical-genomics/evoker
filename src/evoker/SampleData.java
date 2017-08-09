package evoker;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import evoker.Types.*;

/**
 * Holds the SampleIDs of a given fam file in a Vector
 */

public class SampleData {
    Vector<String> inds;
    private HashMap<String, Vector<Integer>> ukbBatchSampleIndices;
    private Vector<String> ukbBatchMembership;
    private Vector<Sex> sexByIndex;

    // If UK Biobank, we need to keep track of any sample IDs with a negative sign as these
    // must not be reported
    private QCFilterData ukbExclude;
    private FileFormat fileFormat;


    SampleData(String famFilename, FileFormat fileFormat) throws IOException{

        this.inds = new Vector<String>();
        this.fileFormat = fileFormat;
        if (fileFormat == FileFormat.UKBIOBANK) {
            ukbBatchSampleIndices = new HashMap<String, Vector<Integer>>();
            ukbBatchMembership = new Vector<String>();
            sexByIndex = new Vector<Sex>();
            ukbExclude = new QCFilterData();
        }

        BufferedReader famReader = new BufferedReader(new FileReader(famFilename));
        String currentLine;
        String[] tokens;
        if (fileFormat == FileFormat.OXFORD){
            //strip headers
            famReader.readLine();
            famReader.readLine();
        }
        int index = -1;
        while ((currentLine = famReader.readLine()) != null) {
            index++;
            tokens = currentLine.split("\\s");
            String sample = tokens[1];
            inds.add(sample);

            if (fileFormat == FileFormat.UKBIOBANK) {
                if (sample.charAt(0) == '-') {
                    ukbExclude.add(sample);
                }

                if (tokens.length == 5) {
                    throw new IOException("UK Biobank fam file ill-formed: requires a sixth column indicating the batch.");
                }
                if (tokens.length != 6) {
                    throw new IOException("UK Biobank fam file ill-formed: requires six columns.");
                }
                String sexCode = tokens[4];
                // TODO assert that sex is either ('1' = male, '2' = female, '0' = unknown)

                Sex sex;
                switch (sexCode) {
                    case "1":
                        sex = Sex.MALE;
                        break;
                    case "2":
                        sex = Sex.FEMALE;
                        break;
                    default:
                        sex = Sex.UNKNOWN;
                        break;
                }

                sexByIndex.add(sex);
                String batch = tokens[5];

                //ukbBatchMembership.add(batch);
                ukbBatchMembership.add(batch);
                if (ukbBatchSampleIndices.containsKey(batch)) {
                    ukbBatchSampleIndices.get(batch).add(index);
                } else {
                    Vector<Integer> v = new Vector<Integer>(1);
                    v.add(index);
                    ukbBatchSampleIndices.put(batch, v);
                }
            }
        }
        famReader.close();
    }

    SampleData(Vector<String> inds) {
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

    public HashMap<String, Vector<Integer>> getUkbBatchSampleIndices() { return ukbBatchSampleIndices; }

    public int getNumUkbBatches() { return  ukbBatchSampleIndices.size(); }

    public Vector<String> getUkbBatchMembership() { return ukbBatchMembership; }

    public Sex getSexByIndex(Integer index) { return sexByIndex.get(index); }

    public FileFormat getFileFormat() {
        return fileFormat;
    }

    public void setUkbExclude(QCFilterData ukbExclude) {
        this.ukbExclude = ukbExclude;
    }

    public QCFilterData getUkbExclude() {
        return ukbExclude;
    }
}

