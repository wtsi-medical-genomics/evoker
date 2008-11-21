import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: jcbarret
 * Date: May 22, 2008
 * Time: 2:15:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class Marker {
    int[] indices;
    int chrom;
    char alleleA;
    char alleleB;

    public Marker (int numCollections, char alleleA, char alleleB, int chrom){
        this.indices = new int[numCollections];
        for (int i = 0; i < numCollections; i++){
            indices[i] = -1;
        }
        this.alleleA = alleleA;
        this.alleleB = alleleB;
        this.chrom = chrom;
    }

    public void addSample(int sampleIndex, int markerIndex){
        indices[sampleIndex] = markerIndex;
    }

    public int getIndex(int i){
        return indices[i];
    }

    public int getChrom(){
        return chrom;
    }

    public String getAlleleA(){
        return String.valueOf(alleleA);
    }

    public String getAlleleB(){
        return String.valueOf(alleleB);
    }
}


