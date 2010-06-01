import javax.swing.*;

import com.sun.tools.javac.util.List;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.LinkedList;

public class DataDirectory {

    Hashtable<String,Hashtable<String,? extends BinaryData>> intensityDataByCollectionChrom;
    Hashtable<String,Hashtable<String, ? extends BinaryData>> genotypeDataByCollectionChrom;
    Hashtable<String,SampleData> samplesByCollection;
    
    QCFilterData filterList;
    boolean filterState;
    String oxPlatform = "";
    
    MarkerData md;
    DataClient dc;
    String displayName;

    DataDirectory(DataClient dc) throws IOException{
        boolean success = true;

        this.dc = dc;
        File directory = dc.prepMetaFiles();
        Hashtable<String,Boolean> knownChroms = parseMetaFiles(directory);

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
            Hashtable<String, RemoteBinaryFloatData> tmpIntensity = new Hashtable<String, RemoteBinaryFloatData>();
            Hashtable<String, RemoteBedfileData> tmpGenotypes = new Hashtable<String, RemoteBedfileData>();
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
        displayName = dc.getDisplayName();
    }

    DataDirectory(String filename) throws IOException{
        boolean success = true;
        boolean oxFiles = false;
        
        File directory = new File(filename);
        String[] filesInDir = directory.list();
        
        // check if the directory contains Oxford format files
        if (directory.listFiles(new ExtensionFilter(".snp")).length > 0) {
        	oxFiles = true;
        }
        
        Hashtable<String,Boolean> knownChroms = parseMetaFiles(directory);
        
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
            Hashtable<String, BinaryFloatDataFile> tmpIntensity = new Hashtable<String, BinaryFloatDataFile>();
            Hashtable<String, BinaryDataFile> tmpGenotypes = new Hashtable<String, BinaryDataFile>();       
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
        	// parse the qc file and store all the samples to exclude in a vector
        	this.setExcludeList(new QCFilterData(qcFile.getAbsolutePath()));
        	// turn filtering on
        	this.setFilterState(true);
        	Genoplot.ld.log("Loaded exclude file: " + name);
        }

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
    
    public PlotData getRecord(String snp, String collection, String coordSystem) throws IOException{
        /**if (collection.equals("ALL")){
            return getRecord(snp);
        }**/
        String chrom = md.getChrom(snp);
        if (chrom != null){
        	
        	// time how long this takes to get a benchmark for improving compressed file response
        	long start = System.currentTimeMillis();
        	PlotData pd = new PlotData(
                    genotypeDataByCollectionChrom.get(collection).get(chrom).getRecord(snp),
                    intensityDataByCollectionChrom.get(collection).get(chrom).getRecord(snp),
                    samplesByCollection.get(collection),
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

    public Vector<String> getCollections(){
        Vector<String> r = new Vector<String>(samplesByCollection.keySet());
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
    
    public void setExcludeList(QCFilterData qc) {
		this.filterList = qc; 
	}	    
   
}
