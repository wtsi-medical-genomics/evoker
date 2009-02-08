import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.LinkedList;

public class DataDirectory {

    Hashtable<String,Hashtable<String,? extends BinaryData>> intensityDataByCollectionChrom;
    Hashtable<String,Hashtable<String, ? extends BinaryData>> genotypeDataByCollectionChrom;
    Hashtable<String,SampleData> samplesByCollection;
    MarkerData md;
    DataClient dc;
    String displayName;

    DataDirectory(DataClient dc) throws IOException{

        this.dc = dc;
        File directory = dc.prepMetaFiles();
        Hashtable<String,Boolean> knownChroms = parseMetaFiles(directory);

        for(String collection : samplesByCollection.keySet()){
            Hashtable<String, RemoteBinaryFloatData> tmpIntensity = new Hashtable<String, RemoteBinaryFloatData>();
            Hashtable<String, RemoteBedfileData> tmpGenotypes = new Hashtable<String, RemoteBedfileData>();
            String root =  directory.getAbsolutePath() + File.separator + collection;
            for (String chrom : knownChroms.keySet()){
                String name = root + "." + chrom;
                //TODO: handle checking for missing files better
                //we require a bimfile for this collection and chromosome:
                md.addFile(name+".bim",collection,chrom);

                //data files for this collection and chromosome:
                tmpIntensity.put(chrom,new RemoteBinaryFloatData(dc,
                        samplesByCollection.get(collection).getNumInds(),
                        md,collection,2));
                tmpGenotypes.put(chrom,new RemoteBedfileData(dc,
                        samplesByCollection.get(collection).getNumInds(),
                        md,collection));
            }
            intensityDataByCollectionChrom.put(collection,tmpIntensity);
            genotypeDataByCollectionChrom.put(collection,tmpGenotypes);
        }

        displayName = dc.getDisplayName();        
    }

    DataDirectory(String filename) throws IOException{
        boolean success = true;

        File directory = new File(filename);

        Hashtable<String,Boolean> knownChroms = parseMetaFiles(directory);

        for(String collection : samplesByCollection.keySet()){
            Hashtable<String, BinaryFloatDataFile> tmpIntensity = new Hashtable<String, BinaryFloatDataFile>();
            Hashtable<String, BedfileDataFile> tmpGenotypes = new Hashtable<String, BedfileDataFile>();
            String root =  directory.getAbsolutePath() + File.separator + collection;
            for (String chrom : knownChroms.keySet()){
                String name = root + "." + chrom;

                success &= checkFile(name);

                //even though we know that something is amiss, we want to keep cdring through the list so that
                //we can log all the missing files at once.
                if (success){
                    //we require a bimfile for this collection and chromosome:
                    md.addFile(name+".bim",collection,chrom);

                    //data files for this collection and chromosome:
                    tmpIntensity.put(chrom,new BinaryFloatDataFile(name+".bnt",
                            samplesByCollection.get(collection).getNumInds(),
                            md,collection,2));
                    tmpGenotypes.put(chrom,new BedfileDataFile(name+".bed",
                            samplesByCollection.get(collection).getNumInds(),
                            md,collection));
                }
            }
            intensityDataByCollectionChrom.put(collection,tmpIntensity);
            genotypeDataByCollectionChrom.put(collection,tmpGenotypes);
        }

        if (!success){
            throw new IOException("Could not find all required files!\nSee log for details.");
        }

        displayName = filename;

    }

    private Hashtable<String,Boolean> parseMetaFiles(File directory) throws IOException{

        if (!directory.exists()){
            throw new IOException(directory.getName() + " does not exist!");
        }

        intensityDataByCollectionChrom = new Hashtable<String, Hashtable<String, ? extends BinaryData>>();
        genotypeDataByCollectionChrom = new Hashtable<String, Hashtable<String, ? extends BinaryData>>();

        samplesByCollection = new Hashtable<String,SampleData>();
        int numberofCollections=0;
        File[] fams = directory.listFiles(new ExtensionFilter(".fam"));
        for (File famFile : fams){
            //stash all sample data in Hashtable keyed on collection name.
            String name = famFile.getName().substring(0,famFile.getName().length()-4);
            samplesByCollection.put(name, new SampleData(famFile.getAbsolutePath()));
            numberofCollections++;
            Genoplot.ld.log("Found collection: " + name);
        }

        if (numberofCollections == 0){
            throw new IOException("Zero sample collection (.fam) files found in " + directory.getName());
        }

        md = new MarkerData(numberofCollections);

        //what chromosomes do we have here?
        File[] bims = directory.listFiles(new ExtensionFilter(".bim"));
        Hashtable<String,Boolean> knownChroms  = new Hashtable<String,Boolean>();
        byte counter = 0;
        for (File bimFile : bims){
            String[] chunks = bimFile.getName().split("\\.");
            if (knownChroms.get(chunks[1]) == null){
                knownChroms.put(chunks[1],true);
                md.addChromToLookup(chunks[1],counter);
                counter++;
                Genoplot.ld.log("Found chromosome: " + chunks[1]);
            }
        }

        if (knownChroms.keySet().size() == 0){
            throw new IOException("Zero SNP information (.bim) files found in " + directory.getName());
        }

        return knownChroms;
    }

    public String getRandomSNP(){
        return md.getRandomSNP();
    }

    private boolean checkFile(String stem){
        boolean all = true;
        if (!(new File(stem+".bed").exists())){
            Genoplot.ld.log("Missing file: " + stem+".bed!");
            all = false;
        }
        if (!(new File(stem+".bnt").exists())){
            Genoplot.ld.log("Missing file: " + stem+".bnt!");
            all = false;
        }
        if (!(new File(stem+".bim").exists())){
            Genoplot.ld.log("Missing file: " + stem+".bim!");
            all = false;
        }
        return all;
    }

    public String getDisplayName() {
        return displayName;
    }

    class ExtensionFilter implements FilenameFilter{

        String extension;

        ExtensionFilter(String extension){
            this.extension = extension;
        }

        public boolean accept(File file, String string) {
            return string.endsWith(extension);
        }
    }

    public PlotData getRecord(String snp, String collection) throws IOException{
        /**if (collection.equals("ALL")){
            return getRecord(snp);
        }**/
        String chrom = md.getChrom(snp);
        if (chrom != null){
            return new PlotData(
                    genotypeDataByCollectionChrom.get(collection).get(chrom).getRecord(snp),
                    intensityDataByCollectionChrom.get(collection).get(chrom).getRecord(snp),
                    samplesByCollection.get(collection));
        }else{
            return new PlotData(null,null,null);
        }
    }

    public Vector<String> getCollections(){
        Vector<String> r = new Vector<String>(samplesByCollection.keySet());
        return r;
    }

    public void listNotify(final LinkedList<String> list) throws IOException{
        if (dc != null){
            //we need to fetch the first one in this thread so we can plot it as soon as it arrives
            String firstSNP = list.removeFirst();
            String chrom = md.getChrom(firstSNP);
            if (chrom != null){
                for (String collection : samplesByCollection.keySet()){
                    genotypeDataByCollectionChrom.get(collection).get(chrom).getRecord(firstSNP);
                    intensityDataByCollectionChrom.get(collection).get(chrom).getRecord(firstSNP);
                }
            }

            class BackgroundFetcher implements Runnable {
                public void run() {
                    try{
                        for (String snp : list){
                            String chrom = md.getChrom(snp);
                            if (chrom != null){
                                for (String collection : samplesByCollection.keySet()){
                                    genotypeDataByCollectionChrom.get(collection).get(chrom).getRecord(snp);
                                    intensityDataByCollectionChrom.get(collection).get(chrom).getRecord(snp);
                                }
                            }
                        }
                    }catch (IOException ioe){
                        JOptionPane.showMessageDialog(null,ioe.getMessage(),"File error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            (new Thread(new BackgroundFetcher())).start();
        }
    }

    public boolean isRemote(){
        return (dc != null);
    }

}
