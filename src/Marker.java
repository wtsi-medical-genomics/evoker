
public class Marker {
    int[] indices;
    byte chrom;
    char[] alleles;

    public Marker (int numCollections, char alleleA, char alleleB, byte chrom){
        this.indices = new int[numCollections];
        for (int i = 0; i < numCollections; i++){
            indices[i] = -1;
        }
        this.alleles = new char[]{alleleA,alleleB};
        this.chrom = chrom;
    }

    public void addSampleCollection(int sampleIndex, int markerIndex, char alleleA, char alleleB, String snp){
        if (alleleA != alleles[0] || alleleB != alleles[1]){
            Genoplot.ld.log("Warning, alleles do not match across collections for "+snp);
        }
        indices[sampleIndex] = markerIndex;
    }

    public int getIndex(int i){
        return indices[i];
    }

    public byte getChrom(){
        return chrom;
    }

    public char[] getAlleles(){
        return alleles;
    }
}


