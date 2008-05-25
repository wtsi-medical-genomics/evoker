import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Pattern;

public class DataDirectory {

    Hashtable<String,Hashtable<Integer,BinaryFloatData>> intensityDataByCollectionChrom;
//    Hashtable<String,Hashtable<String,BinaryFloatData>> probabilityDataByCollectionChrom;
    Hashtable<String,Hashtable<Integer,BedfileData>> genotypeDataByCollectionChrom;

    Hashtable<String,SampleData> samplesByCollection;
    Hashtable<String,Integer> sampleOrder;

    Hashtable<String,Integer> chromosomeBySNP;

    DataDirectory(String filename) throws IOException{

        intensityDataByCollectionChrom = new Hashtable<String,Hashtable<Integer,BinaryFloatData>>();
        //probabilityDataByCollectionChrom = new Hashtable<String, Hashtable<String, BinaryFloatData>>();
        genotypeDataByCollectionChrom = new Hashtable<String, Hashtable<Integer, BedfileData>>();
        File directory = new File(filename);
        if (!directory.exists()){
            throw new IOException(directory.getName() + " does not exist!");
        }

        samplesByCollection = new Hashtable<String,SampleData>();
        sampleOrder = new Hashtable<String,Integer>();
        int index=0;
        File[] fams = directory.listFiles(new ExtensionFilter(".fam"));
        for (File famFile : fams){
            //stash all sample data in Hashtable keyed on collection name.
            String name = famFile.getName().substring(0,famFile.getName().length()-4);
            samplesByCollection.put(name, new SampleData(famFile.getAbsolutePath()));
            sampleOrder.put(name,index++);
            System.out.println("Found collection: " + name);
        }

        MarkerData md = new MarkerData(index);
        chromosomeBySNP = new Hashtable<String, Integer>();

        File[] bims = directory.listFiles(new ExtensionFilter(".bim"));

        Vector<String> fileStems = new Vector<String>();
        for (File bimFile : bims){
            String[] chunks = bimFile.getName().split("\\.");
            md.addFile(bimFile.getAbsolutePath(),sampleOrder.get(chunks[0]),Integer.valueOf(chunks[1]));
            fileStems.add(new String(chunks[0]+"."+chunks[1]));
            System.out.println("Found bim: " + bimFile.getName());
        }

        Hashtable<Integer,Boolean> knownChroms  = new Hashtable<Integer,Boolean>();
        //we need to remember where each SNP is:
        for (String SNP : md.getSNPs()){
            chromosomeBySNP.put(SNP,md.getChrom(SNP));
            knownChroms.put(md.getChrom(SNP),true);
        }


        for(String collection : samplesByCollection.keySet()){
            Hashtable<Integer,BinaryFloatData> tmpIntensity = new Hashtable<Integer,BinaryFloatData>();
            Hashtable<Integer,BedfileData> tmpGenotypes = new Hashtable<Integer,BedfileData>();
            //Hashtable<String,BinaryFloatData> tmpProbability = new Hashtable<String,BinaryFloatData>();
            String root =  directory.getAbsolutePath() + File.separator + collection;
            for (Integer chrom : knownChroms.keySet()){
                String name = root + "." + chrom;
                System.out.println("Found: " + name);
                tmpIntensity.put(chrom,new BinaryFloatData(name+".bnt",samplesByCollection.get(collection),
                        md,2));
                //tmpProbability.put(chrom,new BinaryFloatData(name+".bpr",samplesByCollection.get(collection),
                  //      markersByChromosome.get(chrom),3));
                tmpGenotypes.put(chrom,new BedfileData(name+".bed",samplesByCollection.get(collection),
                        md));
            }
            intensityDataByCollectionChrom.put(collection,tmpIntensity);
            //probabilityDataByCollectionChrom.put(collection,tmpProbability);
            genotypeDataByCollectionChrom.put(collection,tmpGenotypes);
        }
    }

    public String getRandomSNP(){
        Vector v  = new Vector(chromosomeBySNP.keySet());
        return (String)v.get((int)(Math.random()*chromosomeBySNP.keySet().size()));
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
        /*if (!(new File(stem+".bpr").exists())){
            System.out.println("Missing file: " + stem+".bpr");
            all = false;
        }*/
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
        Integer chrom = chromosomeBySNP.get(snp);
        if (chrom != null){
            return new PlotData(
                    genotypeDataByCollectionChrom.get(collection).get(chrom).getRecord(snp,sampleOrder.get(collection)),
                    intensityDataByCollectionChrom.get(collection).get(chrom).getRecord(snp,sampleOrder.get(collection)));
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
        Integer chrom = chromosomeBySNP.get(snp);
        Vector v = new Vector(samplesByCollection.keySet());
        PlotData pd = new PlotData(
                genotypeDataByCollectionChrom.get(v.get(0)).get(chrom).getRecord(snp,0),
                intensityDataByCollectionChrom.get(v.get(0)).get(chrom).getRecord(snp,0));
        for (Object col : v){
            pd.add(
                    genotypeDataByCollectionChrom.get(col).get(chrom).getRecord(snp,sampleOrder.get(col)),
                    intensityDataByCollectionChrom.get(col).get(chrom).getRecord(snp,sampleOrder.get(col)));
        }

        return pd;
    }
}
