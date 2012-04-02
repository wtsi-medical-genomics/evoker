package evoker;

import javax.swing.*;

//import com.sun.tools.javac.util.List;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Manages all file information
 */
public class DataDirectory {
    HashMap<String,HashMap<String,? extends BinaryData>> intensityDataByCollectionChrom;  // fileName -> [chromosome -> BinaryFloatDataFile]
    HashMap<String,HashMap<String, ? extends BinaryData>> genotypeDataByCollectionChrom;  // fileName -> [chromosome -> Bed/GenfileDataFile]
    HashMap<String,SampleData> samplesByCollection; // fileName -> SampleData
    
    /** Holds the changes being made to die data at Runtime */
    HashMap<String, HashMap<String, HashMap<String, HashMap<String, Byte>>>> changesByCollection = new HashMap<String, HashMap<String, HashMap<String, HashMap<String, Byte>>>>();   // collection -> chromosome -> snp -> [ind -> internal notation of genotype change]
    
    QCFilterData filterList;
    boolean filterState;
    String oxPlatform = "";
    
    MarkerData md;
    DataClient dc;
    String displayName;
    String dataPath;

    DataDirectory(DataClient dc) throws IOException{
        boolean success = true;

        this.dc = dc;
        File directory = dc.prepMetaFiles();
        HashMap<String,Boolean> knownChroms = parseMetaFiles(directory);

        if (dc.isOxFormat()){
            int i = 0,a = 0;
            for (String s : dc.getFilesInRemoteDir()){
                if (s.contains("illumina")){
                    oxPlatform = "illumina";
                    i = 1;
                }else if (s.contains("affymetrix")){
                    oxPlatform = "affymetrix";
                    a = 1;
                }else if (s.contains("affy")){
                    oxPlatform = "affy";
                    a = 1;
                }
            }
            if (i+a == 0){
                throw new IOException("Cannot find either *affy or *illumina Oxford files.");
            }
            if (i+a > 1){
                throw new IOException("Found both *affy and *illumina Oxford files. Please use one or the other.");
            }
            dc.setOxPlatform(oxPlatform);
        }
        
        for(String collection : samplesByCollection.keySet()){
            HashMap<String, RemoteBinaryFloatData> tmpIntensity = new HashMap<String, RemoteBinaryFloatData>();
            HashMap<String, RemoteBedfileData> tmpGenotypes = new HashMap<String, RemoteBedfileData>();
            for (String chrom : knownChroms.keySet()){
            	String name;
            	if (dc.isOxFormat()){
                    name = collection + "_" + chrom + "_" + oxPlatform + ".snp";
                    name = directory.getAbsolutePath() + File.separator + name;
                    md.addFile(name,collection,chrom,true);
                    //data files for this collection and chromosome:
                    tmpIntensity.put(chrom,new RemoteBinaryFloatData(dc,
                            samplesByCollection.get(collection).getNumInds(),
                            md,collection,2));
                    tmpGenotypes.put(chrom,new RemoteBedfileData(dc,
                            samplesByCollection.get(collection).getNumInds(),
                            md,collection));
                }else{
                    name = collection + "." + chrom;
                    success &= checkFile(dc.getFilesInRemoteDir(),name);
                    name =  directory.getAbsolutePath() + File.separator + name;
                    //we require a bimfile for this collection and chromosome:
                    md.addFile(name+".bim",collection,chrom,false);
                    //data files for this collection and chromosome:
                    tmpIntensity.put(chrom,new RemoteBinaryFloatData(dc,
                            samplesByCollection.get(collection).getNumInds(),
                            md,collection,2,collection + "." + chrom + ".bnt"));
                    tmpGenotypes.put(chrom,new RemoteBedfileData(dc,
                            samplesByCollection.get(collection).getNumInds(),
                            md,collection,collection + "." + chrom + ".bed"));
                }
            }
            intensityDataByCollectionChrom.put(collection,tmpIntensity);
            genotypeDataByCollectionChrom.put(collection,tmpGenotypes);
        }

        if (!success){
            throw new IOException("Could not find all required files!\nSee log for details.");
        }

        Genoplot.ld.log("Using files in " + dc.getDisplayName());
        
        double freeMem = Runtime.getRuntime().freeMemory();
        double totlMem = Runtime.getRuntime().totalMemory(); 
        Genoplot.ld.log("Memory used:" + ((totlMem - freeMem)/1048576) + "MB");
        
        displayName = dc.getDisplayName();
    }

    DataDirectory(String filename) throws IOException{
        boolean success = true;
        boolean oxFiles = false;
        
        File directory = new File(filename);
        dataPath = directory.getAbsolutePath()+File.separator;
        String[] filesInDir = directory.list();
        
        // check if the directory contains Oxford format files
        if (directory.listFiles(new ExtensionFilter(".snp")).length > 0) {
        	oxFiles = true;
        }
        
        //markerId
        HashMap<String,Boolean> knownChroms = parseMetaFiles(directory);
        
        if (oxFiles){
            int i = 0,a = 0;
            for (String s : filesInDir){
                if (s.contains("illumina")){
                    oxPlatform = "illumina";
                    i = 1;
                }else if (s.contains("affymetrix")){
                    oxPlatform = "affymetrix";
                    a = 1;
                }else if (s.contains("affy")){
                    oxPlatform = "affy";
                    a = 1;
                }
            }
            if (i+a == 0){
                throw new IOException("Cannot find either *affy or *illumina Oxford files.");
            }
            if (i+a > 1){
                throw new IOException("Found both *affy and *illumina Oxford files. Please use one or the other.");
            }
        }
        
        for(String collection : samplesByCollection.keySet()){
            HashMap<String, BinaryFloatDataFile> tmpIntensity = new HashMap<String, BinaryFloatDataFile>();
            HashMap<String, BinaryDataFile> tmpGenotypes = new HashMap<String, BinaryDataFile>();       
            for (String chrom : knownChroms.keySet()){
            	String name;
            	if (oxFiles){
                    name = collection + "_" + chrom + "_" + oxPlatform + ".snp";
                    name = directory.getAbsolutePath() + File.separator + name;
                    md.addFile(name,collection,chrom,true);
                }else{
                    name = collection + "." + chrom;
                    success &= checkFile(filesInDir,name);

                    name =  directory.getAbsolutePath() + File.separator + name;
                    //we require a bimfile for this collection and chromosome:
                    md.addFile(name+".bim",collection,chrom,false);
                }
            	
                //even though we know that something is amiss, we want to keep cdring through the list so that
                //we can log all the missing files at once.
                if (success){
                    //data files for this collection and chromosome:
                    if (oxFiles){
                    	name = collection + "_" + chrom + "_" + oxPlatform;
                        name = directory.getAbsolutePath() + File.separator + name;
                        boolean zipped = true;
                        // check if the oxford int and gen files are in a compressed format
                        if (new File(name+".int.bin.gz").exists()) {
                        	tmpIntensity.put(chrom,new BinaryFloatDataFile(name+".int.bin.gz",
                                    samplesByCollection.get(collection).getNumInds(),
                                    md,collection,2,zipped));
                        } else {
                        	tmpIntensity.put(chrom,new BinaryFloatDataFile(name+".int.bin",
                                    samplesByCollection.get(collection).getNumInds(),
                                    md,collection,2));
                        }
                        if (new File(name+".gen.bin.gz").exists()) {
                        	tmpGenotypes.put(chrom,new GenfileDataFile(name+".gen.bin.gz",
                                    samplesByCollection.get(collection).getNumInds(),
                                    md,collection,zipped));                     
                        } else {
                        	tmpGenotypes.put(chrom,new GenfileDataFile(name+".gen.bin",
                                    samplesByCollection.get(collection).getNumInds(),
                                    md,collection));
                        }
                    }else{
                    	tmpIntensity.put(chrom,new BinaryFloatDataFile(name+".bnt",
                                samplesByCollection.get(collection).getNumInds(),
                                md,collection,2));
                        tmpGenotypes.put(chrom,new BedfileDataFile(name+".bed",
                                samplesByCollection.get(collection).getNumInds(),
                                md,collection));
                    }
                }
            }
            intensityDataByCollectionChrom.put(collection,tmpIntensity);
            genotypeDataByCollectionChrom.put(collection,tmpGenotypes);
        }

        if (!success){
            throw new IOException("Could not find all required files!\nSee log for details.");
        }

        Genoplot.ld.log("Using files in " + filename);   
        
        double freeMem = Runtime.getRuntime().freeMemory();
        double totlMem = Runtime.getRuntime().totalMemory(); 
        Genoplot.ld.log("Memory used:" + ((totlMem - freeMem)/1048576) + "MB");
        
        displayName = filename;

    }

    /**
     * Announces a change at a specific position for a specific collection
     * 
     * @param collection    collection name
     * @param snp           snp name
     * @param changes       in the form HashMap <ind , targetID>
     */
    protected void commitGenotypeChange(String collection, String snp, HashMap<String, Byte> changes){
        if(! changesByCollection.containsKey(collection)) changesByCollection.put(collection, new HashMap<String, HashMap<String, HashMap<String, Byte>>>());
        String chromosome = null;
        chromosome = md.getChrom(snp);
        if(! changesByCollection.get(collection).containsKey(chromosome)) changesByCollection.get(collection).put(chromosome, new HashMap<String, HashMap<String, Byte>>());
        if(! changesByCollection.get(collection).get(chromosome).containsKey(snp)) changesByCollection.get(collection).get(chromosome).put(snp, new HashMap<String, Byte>());

        changesByCollection.get(collection).get(chromosome).get(snp).putAll(changes);
    }
    
    /**
     * Analyze filenames in given directory
     * 
     * collection-names (by .fam/.sample files) are put into samplesByCollection
     * chromosome names (by .bim/.snp files) are put into knownChromosomes
     * 
     * @param directory            Directory containing files
     * @return  knownChromosomes   Chromosomes being contained
     * @throws IOException
     */
    private HashMap<String,Boolean> parseMetaFiles(File directory) throws IOException{

        if (!directory.exists()){
            throw new IOException(directory.getName() + " does not exist!");
        }

        intensityDataByCollectionChrom = new HashMap<String, HashMap<String, ? extends BinaryData>>();
        genotypeDataByCollectionChrom = new HashMap<String, HashMap<String, ? extends BinaryData>>();

        samplesByCollection = new HashMap<String,SampleData>();
        int numberofCollections=0;
        File[] fams = directory.listFiles(new ExtensionFilter(".fam"));
        for (File famFile : fams){
            //stash all sample data in HashMap keyed on collection name.
            String name = famFile.getName().substring(0,famFile.getName().length()-4);
            samplesByCollection.put(name, new SampleData(famFile.getAbsolutePath(),false));
            numberofCollections++;
            Genoplot.ld.log("Found collection: " + name);
        }

        File[] oxFams = directory.listFiles(new ExtensionFilter(".sample"));
        for (File sampleFile : oxFams){
            //see if we have oxford style sample files. yeesh.
            String name = sampleFile.getName().split("_")[0];
            samplesByCollection.put(name, new SampleData(sampleFile.getAbsolutePath(),true));
            numberofCollections++;
            Genoplot.ld.log("Found collection: " + name);
        }

        if (numberofCollections == 0){
            throw new IOException("Zero sample collection (.fam) files found in " + directory.getName());
        }

        md = new MarkerData(numberofCollections);
        
        // is there a ".qc" file in the directory?
        File[] qcfiles = directory.listFiles(new ExtensionFilter(".qc"));
        if (qcfiles.length > 0) {
        	// for now just take the first qc file found, later load all qc files and list in the menu with the ability to select which file to use
        	File qcFile = qcfiles[0];
        	String name = qcFile.getName();
        	// parse the qc file and store all the samples to exclude in a ArrayList
        	this.setExcludeList(new QCFilterData(qcFile.getAbsolutePath()));
        	// turn filtering on
        	this.setFilterState(true);
        	Genoplot.ld.log("Loaded exclude file: " + name);
        }

        //what chromosomes do we have here?
        File[] bims = directory.listFiles(new ExtensionFilter(".bim"));
        HashMap<String,Boolean> knownChroms  = new HashMap<String,Boolean>();
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

        //see if we have oxford style snp files...
        //TODO: do this in a less heinous way? should probably feed in the boolean and separate the ox/non-ox searches
        File [] snpFiles = directory.listFiles(new ExtensionFilter(".snp"));
        for (File snpFile : snpFiles){
            String[] chunks = snpFile.getName().split("_");
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

    private boolean checkFile(String[] filesInDir, String stem) throws IOException{
        if (filesInDir != null){
            boolean bed = false;
            boolean bnt = false;
            boolean bim = false;
            for (String s : filesInDir){
                if (s.equals(stem+".bed")){
                    bed = true;
                }else if (s.equals(stem+".bnt")){
                    bnt = true;
                }else if (s.equals(stem+".bim")){
                    bim = true;
                }
            }

            if (!bed){
                Genoplot.ld.log("Missing file: " + stem+".bed!");
            }
            if (!bnt){
                Genoplot.ld.log("Missing file: " + stem+".bnt!");
            }
            if (!bim){
                Genoplot.ld.log("Missing file: " + stem+".bim!");
            }
            return bed & bnt & bim;
        }else{
            throw new IOException("Could not get list of files in data directory.");
        }
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
    
    /**
     * Generates a PlotData Object for a given position
     * 
     * @param snp           SNP Name
     * @param collection    Name of collection
     * @param coordSystem   Tye of coordinate system
     * @return  representative PlotData Object
     * @throws IOException 
     */
    public PlotData getRecord(String snp, String collection, String coordSystem) throws IOException{
        /**if (collection.equals("ALL")){
            return getRecord(snp);
        }**/
        String chrom = md.getChrom(snp);
        if (chrom != null){
        	
        	// time how long this takes to get a benchmark for improving compressed file response
        	long start = System.currentTimeMillis();
                
                // get the previously made changes and apply them.
                ArrayList record = genotypeDataByCollectionChrom.get(collection).get(chrom).getRecord(snp);
                SampleData samples = samplesByCollection.get(collection);
                
                if(changesByCollection.containsKey(collection)){
                    if(changesByCollection.get(collection).containsKey(chrom)){
                        if(changesByCollection.get(collection).get(chrom).containsKey(snp)){
                        HashMap<String, Byte> changes = changesByCollection.get(collection).get(chrom).get(snp);
                            for(String s : changes.keySet()){
                                record.set(samples.inds.indexOf(s), changes.get(s));
                            }
                        }
                    }
                }
                
        	PlotData pd = new PlotData(
                    record,
                    intensityDataByCollectionChrom.get(collection).get(chrom).getRecord(snp),
                    samples,
                    qcList(),
                    md.getAlleles(snp),
                    coordSystem);
        	double time = ((double)(System.currentTimeMillis() - start))/1000;
            Genoplot.ld.log(snp +" for "+ collection +" was fetched in "+ time + "s.");
            return pd;
        	
        }else{
            return new PlotData(null,null,null,null,null,"null");
        }
    }

    public ArrayList<String> getCollections(){
        ArrayList<String> r = new ArrayList<String>(samplesByCollection.keySet());
        return r;
    }
    
    public int getNumCollections() {
    	return samplesByCollection.size();
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
    
    public boolean isLocal(){
        return (dc == null);
    }

    public QCFilterData qcList() {
    	if (filterState == true) {
    		return this.getExcludeList();
    	} else {
    		return null;
    	}
    }

    public boolean getFilterState() {
    	return filterState;
    }
	  	
    public void setFilterState(boolean state) {
    	this.filterState = state;
    }
	
    public QCFilterData getExcludeList() {
		return filterList;
	}
    
    public MarkerData getMarkerData(){
        return md;
    }
    
    public void setExcludeList(QCFilterData qc) {
		this.filterList = qc; 
	}	    
    
    public String getDataPath(){
    	return dataPath;
    }
   
}
