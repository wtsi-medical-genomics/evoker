import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;

import java.util.Vector;

public class PlotData {

    
    private Vector<Byte> calledGenotypes;
    private Vector<float[]> genotypeProbabilities;
    private Vector<float[]> intensities;
    private double maf, genopc, hwpval, maxDim;
    private SampleData samples;
    Vector<Vector<String>> indsInClasses;

    PlotData(Vector<float[]> genotypeProbabilities, Vector<float[]> intensities, Vector<Byte>calledGenotypes,
             SampleData samples){
        this.calledGenotypes = calledGenotypes;
        this.genotypeProbabilities = genotypeProbabilities;
        this.intensities = intensities;
        this.samples = samples;
        this.maxDim = 0;
    }

    PlotData(Vector<Byte>calledGenotypes, Vector<float[]> intensities){
        this.calledGenotypes = calledGenotypes;
        this.intensities = intensities;
        this.maxDim = 0;
    }

    /*PlotData(Vector<float[]> genotypeProbabilities, Vector<float[]> intensities){
        this.genotypeProbabilities = genotypeProbabilities;
        this.intensities = intensities;
    }*/

    public void add(Vector<Byte> calledgenotypes, Vector<float[]> intensities){
        this.calledGenotypes.addAll(calledgenotypes);
        //this.genotypeProbabilities.addAll(genotypeProbabilities);
        this.intensities.addAll(intensities);
    }

    XYSeriesCollection callGenotypes(float cutoff){
        if (intensities == null || (genotypeProbabilities == null && calledGenotypes == null)){
            return null;
        }

        Vector<Byte> genotypes = new Vector<Byte>();

        if (calledGenotypes != null){
            genotypes = calledGenotypes;
        }else{
            for (float[] prob : genotypeProbabilities) {
                if (prob[0] >= cutoff){
                    genotypes.add((byte) 0);
                }else if (prob[1] >= cutoff){
                    genotypes.add((byte) 2);
                }else if (prob[2] >= cutoff){
                    genotypes.add((byte) 3);
                }else{
                    genotypes.add((byte) 1);
                }
            }
        }

        computeSummary(genotypes);


        XYSeries intensityDataSeriesHomo1 = new XYSeries(0,false);
        XYSeries intensityDataSeriesMissing = new XYSeries(1,false);
        XYSeries intensityDataSeriesHetero = new XYSeries(2,false);
        XYSeries intensityDataSeriesHomo2 = new XYSeries(3,false);

        indsInClasses = new Vector<Vector<String>>();
        for (int i = 0; i <4; i++){
            indsInClasses.add(new Vector<String>());
        }

        for (int i = 0; i < intensities.size(); i++){
            if (genotypes.get(i) != null){
                switch(genotypes.get(i)) {
                    case 0:
                        intensityDataSeriesHomo1.add(intensities.get(i)[0],intensities.get(i)[1]);
                        //indsInClasses.get(0).add(samples.getInd(i));
                        break;
                    case 1:
                        intensityDataSeriesMissing.add(intensities.get(i)[0],intensities.get(i)[1]);
                        //indsInClasses.get(1).add(samples.getInd(i));
                        break;
                    case 2:
                        intensityDataSeriesHetero.add(intensities.get(i)[0],intensities.get(i)[1]);
                        //indsInClasses.get(2).add(samples.getInd(i));
                        break;
                    case 3:
                        intensityDataSeriesHomo2.add(intensities.get(i)[0],intensities.get(i)[1]);
                        //indsInClasses.get(3).add(samples.getInd(i));
                        break;
                    default:
                        //TODO: this is very bad
                        break;
                }
            }

            if (intensities.get(i)[0] > maxDim){
                maxDim = intensities.get(i)[0];
            }

            if (intensities.get(i)[1] > maxDim){
                maxDim = intensities.get(i)[1];
            }

        }

        XYSeriesCollection xysc = new XYSeriesCollection(intensityDataSeriesHomo1);
        xysc.addSeries(intensityDataSeriesMissing);
        xysc.addSeries(intensityDataSeriesHetero);
        xysc.addSeries(intensityDataSeriesHomo2);
        return xysc;
    }

    public String getIndInClass(int cl, int i){
        return indsInClasses.get(cl).get(i);
    }

    private void computeSummary(Vector<Byte> genos){
        double hom1 = 0, het = 0, hom2 = 0, missing = 0;
        for (byte geno : genos){
            if (geno == 0){
                hom1++;
            }else if (geno == 2){
                het++;
            }else if (geno == 3){
                hom2++;
            }else{
                missing++;
            }
        }
        genopc = 1 - (missing / (missing+hom1+het+hom2));
        double tmpmaf = ((2*hom1) + het) / ((2*het) + (2*hom1) + (2*hom2));
        if (tmpmaf < 0.5){
            maf = tmpmaf;
        }else{
            maf = 1 - tmpmaf;
        }

        hwpval = hwCalculate((int)hom1,(int)het,(int)hom2);
    }

    private double hwCalculate(int obsAA, int obsAB, int obsBB){
        //Calculates exact two-sided hardy-weinberg p-value. Parameters
        //are number of genotypes, number of rare alleles observed and
        //number of heterozygotes observed.
        //
        // (c) 2003 Jan Wigginton, Goncalo Abecasis
        int diplotypes =  obsAA + obsAB + obsBB;
        if (diplotypes == 0){
            return 0;
        }
        int rare = (obsAA*2) + obsAB;
        int hets = obsAB;


        //make sure "rare" allele is really the rare allele
        if (rare > diplotypes){
            rare = 2*diplotypes-rare;
        }

        double[] tailProbs = new double[rare+1];
        for (int z = 0; z < tailProbs.length; z++){
            tailProbs[z] = 0;
        }

        //start at midpoint
        int mid = rare * (2 * diplotypes - rare) / (2 * diplotypes);

        //check to ensure that midpoint and rare alleles have same parity
        if (((rare & 1) ^ (mid & 1)) != 0){
            mid++;
        }
        int het = mid;
        int hom_r = (rare - mid) / 2;
        int hom_c = diplotypes - het - hom_r;

        //Calculate probability for each possible observed heterozygote
        //count up to a scaling constant, to avoid underflow and overflow
        tailProbs[mid] = 1.0;
        double sum = tailProbs[mid];
        for (het = mid; het > 1; het -=2){
            tailProbs[het-2] = (tailProbs[het] * het * (het-1.0))/(4.0*(hom_r + 1.0) * (hom_c + 1.0));
            sum += tailProbs[het-2];
            //2 fewer hets for next iteration -> add one rare and one common homozygote
            hom_r++;
            hom_c++;
        }

        het = mid;
        hom_r = (rare - mid) / 2;
        hom_c = diplotypes - het - hom_r;
        for (het = mid; het <= rare - 2; het += 2){
            tailProbs[het+2] = (tailProbs[het] * 4.0 * hom_r * hom_c) / ((het+2.0)*(het+1.0));
            sum += tailProbs[het+2];
            //2 more hets for next iteration -> subtract one rare and one common homozygote
            hom_r--;
            hom_c--;
        }

        for (int z = 0; z < tailProbs.length; z++){
            tailProbs[z] /= sum;
        }

        double top = tailProbs[hets];
        for (int i = hets+1; i <= rare; i++){
            top += tailProbs[i];
        }
        double otherSide = tailProbs[hets];
        for (int i = hets-1; i >= 0; i--){
            otherSide += tailProbs[i];
        }

        if (top > 0.5 && otherSide > 0.5){
            return 1.0;
        }else{
            if (top < otherSide){
                return top * 2;
            }else{
                return otherSide * 2;
            }
        }
    }

    public double getMaf() {
        return maf;
    }

    public double getGenopc() {
        return genopc;
    }

    public double getHwpval() {
        return hwpval;
    }

    public double getMaxDim(){
        return maxDim;
    }
}
