package evoker;


/**
 * Holds information about the line the SNP information is hold at for each bim file ID
 */

public class Marker {
    int[] indices;
    byte chrom;
    char alleleA;
    char alleleB;

    public Marker (int numCollections, char alleleA, char alleleB, byte chrom){
        this.indices = new int[numCollections];
        for (int i = 0; i < numCollections; i++){
            indices[i] = -1;
        }
        this.alleleA = alleleA;
        this.alleleB = alleleB;
        this.chrom = chrom;
    }

    public void addSampleCollection(int sampleIndex, int markerIndex, char alleleA, char alleleB, String snp){
        if ((alleleA == this.alleleA || alleleA == this.alleleB) && (alleleB == this.alleleA || alleleB == this.alleleB) ){
            
        } else{
        	Genoplot.ld.log("Warning, alleles do not match across collections for "+snp);
            //TODO: if they are just flipped around, should be able to figure that out and swap 'em
        }
        indices[sampleIndex] = markerIndex;
    }

    /**
     * Returns the Index of a given SNP within a collection. (or: at which position it is)
     * 
     * @param collection id bzw number
     * @return the index
     */
    public int getIndex(int i){
        return indices[i];
    }

    public byte getChrom(){
        return chrom;
    }

    public char[] getAlleles(){
        return new char[]{alleleA,alleleB};
    }
}


