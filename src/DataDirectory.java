import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Pattern;

public class DataDirectory {

    Hashtable<String,Hashtable<String,BinaryFloatData>> intensityDataByCollectionChrom;
    Hashtable<String,Hashtable<String,BedfileData>> genotypeDataByCollectionChrom;
    Hashtable<String,SampleData> samplesByCollection;
    MarkerData md;

    DataDirectory(DataClient dc) throws IOException{
        intensityDataByCollectionChrom = new Hashtable<String,Hashtable<String,BinaryFloatData>>();
        genotypeDataByCollectionChrom = new Hashtable<String, Hashtable<String, BedfileData>>();
        samplesByCollection = new Hashtable<String,SampleData>();
        samplesByCollection.put("NBS",new SampleData("/Users/jcbarret/NBS.fam"));

        md = new MarkerData(1);
        md.addChromToLookup("18",(byte)0);
        md.addFile("/Users/jcbarret/18.bim","NBS","18");
        Hashtable<String,BinaryFloatData> tmpIntensity = new Hashtable<String,BinaryFloatData>();
        Hashtable<String,BedfileData> tmpGenotypes = new Hashtable<String,BedfileData>();
        tmpGenotypes.put("18",new RemoteBedfileData(dc,samplesByCollection.get("NBS"),md,"NBS"));
        tmpIntensity.put("18",new RemoteBinaryFloatData("flu",samplesByCollection.get("NBS"),md,2,"NBS"));
        intensityDataByCollectionChrom.put("NBS",tmpIntensity);
        genotypeDataByCollectionChrom.put("NBS",tmpGenotypes);
        
    }

    DataDirectory(String filename) throws IOException{

        intensityDataByCollectionChrom = new Hashtable<String,Hashtable<String,BinaryFloatData>>();
        genotypeDataByCollectionChrom = new Hashtable<String, Hashtable<String, BedfileData>>();
        File directory = new File(filename);
        if (!directory.exists()){
            throw new IOException(directory.getName() + " does not exist!");
        }

        samplesByCollection = new Hashtable<String,SampleData>();
        int numberofCollections=0;
        File[] fams = directory.listFiles(new ExtensionFilter(".fam"));
        for (File famFile : fams){
            //stash all sample data in Hashtable keyed on collection name.
            String name = famFile.getName().substring(0,famFile.getName().length()-4);
            samplesByCollection.put(name, new SampleData(famFile.getAbsolutePath()));
            numberofCollections++;
            System.out.println("Found collection: " + name);
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
                System.out.println("Found chromosome: " + chunks[1]);
            }
        }

        for(String collection : samplesByCollection.keySet()){
            Hashtable<String,BinaryFloatData> tmpIntensity = new Hashtable<String,BinaryFloatData>();
            Hashtable<String,BedfileData> tmpGenotypes = new Hashtable<String,BedfileData>();
            String root =  directory.getAbsolutePath() + File.separator + collection;
            for (String chrom : knownChroms.keySet()){
                String name = root + "." + chrom;
                //TODO: handle checking for missing files better
                //we require a bimfile for this collection and chromosome:
                md.addFile(name+".bim",collection,chrom);

                //data files for this collection and chromosome:
                tmpIntensity.put(chrom,new BinaryFloatData(name+".bnt",samplesByCollection.get(collection),
                        md,2,collection));
                tmpGenotypes.put(chrom,new BedfileData(name+".bed",samplesByCollection.get(collection),
                        md,collection));
            }
            intensityDataByCollectionChrom.put(collection,tmpIntensity);
            genotypeDataByCollectionChrom.put(collection,tmpGenotypes);
        }
    }

    public String getRandomSNP(){
        return md.getRandomSNP();
    }

    private boolean checkFile(String stem){
        boolean all = true;
        if (!(new File(stem+".bed").exists())){
            System.out.println("Missing file: " + stem+".bed");
            all = false;
        }
        if (!(new File(stem+".bnt").exists())){
            System.out.println("Missing file: " + stem+".bnt");
            all = false;
        }
        if (!(new File(stem+".bim").exists())){
            System.out.println("Missing file: " + stem+".bim");
            all = false;
        }
        return all;
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

    public PlotData getRecord(String snp, String collection){
        if (collection.equals("ALL")){
            return getRecord(snp);
        }
        String chrom = md.getChrom(snp);
        if (chrom != null){
            return new PlotData(
                    genotypeDataByCollectionChrom.get(collection).get(chrom).getRecord(snp),
                    intensityDataByCollectionChrom.get(collection).get(chrom).getRecord(snp));
        }else{
            return new PlotData(null,null);
        }
    }

    public Vector<String> getCollections(){
        Vector<String> r = new Vector<String>(samplesByCollection.keySet());
        //r.add("ALL");
        return r;
    }

    public PlotData getRecord(String snp){
        String chrom = md.getChrom(snp);
        Vector v = new Vector(samplesByCollection.keySet());
        PlotData pd = new PlotData(
                genotypeDataByCollectionChrom.get(v.get(0)).get(chrom).getRecord(snp),
                intensityDataByCollectionChrom.get(v.get(0)).get(chrom).getRecord(snp));
        for (Object col : v){
            pd.add(
                    genotypeDataByCollectionChrom.get(col).get(chrom).getRecord(snp),
                    intensityDataByCollectionChrom.get(col).get(chrom).getRecord(snp));
        }

        return pd;
    }
}
