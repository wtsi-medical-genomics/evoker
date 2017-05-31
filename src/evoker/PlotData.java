package evoker;

import java.util.ArrayList;
import java.util.HashMap;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;

import evoker.Types.CoordinateSystem;

public class PlotData {

    private ArrayList<Byte> calledGenotypes;
    private ArrayList<float[]> intensities;
    private double maf, genopc, hwpval, maxDim, minDim;
    private SampleData samples;
    private QCFilterData exclude;
    private int sampleNum;
    private CoordinateSystem coordSystem;
    private ArrayList<ArrayList<String>> indsInClasses;
    private HashMap<String, Integer> indexInArrayListByInd;
    private char[] alleles;
    private HashMap<String, Byte> genotypeChanges = new HashMap<String, Byte>();
    public boolean changed = false;

    PlotData(ArrayList<Byte> calledGenotypes, ArrayList<float[]> intensities, SampleData samples, QCFilterData exclude, char[] alleles, CoordinateSystem coordSystem) {
        this.calledGenotypes = calledGenotypes;
        this.intensities = intensities;
        this.samples = samples;
        this.exclude = exclude;
        this.maxDim = -100000;
        this.minDim = 100000;
        this.alleles = alleles;
        this.setCoordSystem(coordSystem);
    }

    public void add(ArrayList<Byte> calledgenotypes, ArrayList<float[]> intensities) {
        this.calledGenotypes.addAll(calledgenotypes);
        this.intensities.addAll(intensities);
    }


    XYSeriesCollection generatePoints() {
        if (intensities == null || calledGenotypes == null) {
            return null;
        }

        computeSummary();


        XYSeries intensityDataSeriesHomo1 = new XYSeries(0, false);
        XYSeries intensityDataSeriesMissing = new XYSeries(1, false);
        XYSeries intensityDataSeriesHetero = new XYSeries(2, false);
        XYSeries intensityDataSeriesHomo2 = new XYSeries(3, false);

        indsInClasses = new ArrayList<ArrayList<String>>();
        for (int i = 0; i < 4; i++) {
            indsInClasses.add(new ArrayList<String>());
        }
        
        indexInArrayListByInd = new HashMap<String, Integer>();

        sampleNum = 0;

        for (int i = 0; i < intensities.size(); i++) {
            float[] intens = intensities.get(i);

            if (getCoordSystem() == CoordinateSystem.POLAR) {
                float x = intens[0];
                float y = intens[1];

                float r = (float) Math.sqrt(Math.pow(y, 2) + Math.pow(x, 2));
                float theta = (float) Math.asin(y / r);

                intens[0] = theta;
                intens[1] = r;
            } else if (getCoordSystem() == CoordinateSystem.UKBIOBANK) {
                float a = intens[0];
                float b = intens[1];

                // Contrast (x-axis) = log2(A/B)
                intens[0] = (float) log2(a/b);

                // Strength (y-axis) = (log2(A*B))/2
                intens[1] = (float) log2(a*b)/2;
             }

            // check if there is a valid exclude file loaded
            if (exclude != null) {
                // check if the sample should be excluded before adding points
                if (!exclude.isExcluded(samples.getInd(i))) {
                    if (calledGenotypes.get(i) != null) {
                        sampleNum++;
                        switch (calledGenotypes.get(i)) {
                            case 0:
                                intensityDataSeriesHomo1.add(intens[0], intens[1]);
                                indsInClasses.get(0).add(samples.getInd(i));
                                indexInArrayListByInd.put(samples.getInd(i), indsInClasses.get(0).size() -1);
                                break;
                            case 1:
                                intensityDataSeriesMissing.add(intens[0], intens[1]);
                                indsInClasses.get(1).add(samples.getInd(i));
                                indexInArrayListByInd.put(samples.getInd(i), indsInClasses.get(1).size() -1);
                                break;
                            case 2:
                                intensityDataSeriesHetero.add(intens[0], intens[1]);
                                indsInClasses.get(2).add(samples.getInd(i));
                                indexInArrayListByInd.put(samples.getInd(i), indsInClasses.get(2).size() -1);
                                break;
                            case 3:
                                intensityDataSeriesHomo2.add(intens[0], intens[1]);
                                indsInClasses.get(3).add(samples.getInd(i));
                                indexInArrayListByInd.put(samples.getInd(i), indsInClasses.get(3).size() -1);
                                break;
                            default:
                                //TODO: this is very bad
                                break;
                        }
                    }
                }
            } else {
                if (calledGenotypes.get(i) != null) {
                    sampleNum++;
                    switch (calledGenotypes.get(i)) {
                        case 0:
                            intensityDataSeriesHomo1.add(intens[0], intens[1]);
                            indsInClasses.get(0).add(samples.getInd(i));
                            indexInArrayListByInd.put(samples.getInd(i), indsInClasses.get(0).size() -1);
                            break;
                        case 1:
                            intensityDataSeriesMissing.add(intens[0], intens[1]);
                            indsInClasses.get(1).add(samples.getInd(i));
                            indexInArrayListByInd.put(samples.getInd(i), indsInClasses.get(1).size() -1);
                            break;
                        case 2:
                            intensityDataSeriesHetero.add(intens[0], intens[1]);
                            indsInClasses.get(2).add(samples.getInd(i));
                            indexInArrayListByInd.put(samples.getInd(i), indsInClasses.get(2).size() -1);
                            break;
                        case 3:
                            intensityDataSeriesHomo2.add(intens[0], intens[1]);
                            indsInClasses.get(3).add(samples.getInd(i));
                            indexInArrayListByInd.put(samples.getInd(i), indsInClasses.get(3).size() -1);
                            break;
                        default:
                            //TODO: this is very bad
                            break;
                    }
                }
            }


            //illuminus uses [-1,-1] as a flag for missing data. technically we don't want to make it impossible
            //for such a datapoint to exist, but we won't let this exact data point adjust the bounds of the plot.
            //if it really is intentional, there will almost certainly be other nearby, negative points
            //which will resize the bounds appropriately.
            if (!(intens[0] == -1 && intens[1] == -1)) {
                if (intens[0] > maxDim) {
                    maxDim = intens[0];
                }
                if (intens[0] < minDim) {
                    minDim = intens[0];
                }

                if (intens[1] > maxDim) {
                    maxDim = intens[1];
                }
                if (intens[1] < minDim) {
                    minDim = intens[1];
                }
            }

        }

        XYSeriesCollection xysc = new XYSeriesCollection(intensityDataSeriesHomo1);
        xysc.addSeries(intensityDataSeriesMissing);
        xysc.addSeries(intensityDataSeriesHetero);
        xysc.addSeries(intensityDataSeriesHomo2);
        return xysc;
    }

    public String getIndInClass(int cl, int i) {
        return indsInClasses.get(cl).get(i);
    }

    /**
     * Moves an IND to another (internal) genotype class
     * 
     * @param ind name
     * @param class it is from
     * @param index of the genotype in that class
     * @param class it should be in
     */
    public void moveIndToClass(String ind, int fromCl, int fromI, int to) {
        indsInClasses.get(fromCl).remove(fromI);
        indsInClasses.get(to).add(ind);

        int index = samples.getIndex(ind);
        calledGenotypes.set(index, (byte) to);
        genotypeChanges.put(ind, (byte) to);
    }

    protected void computeSummary() {
        double hom1 = 0, het = 0, hom2 = 0, missing = 0;
        for (int i = 0; i < calledGenotypes.size(); i++) {
            byte geno = calledGenotypes.get(i);
            // check if there is a valid exclude file loaded
            if (exclude != null) {
                // check if the sample should be excluded before adding points
                if (!exclude.isExcluded(samples.getInd(i))) {
                    if (geno == 0) {
                        hom1++;
                    } else if (geno == 2) {
                        het++;
                    } else if (geno == 3) {
                        hom2++;
                    } else {
                        missing++;
                    }
                    genopc = 1 - (missing / (missing + hom1 + het + hom2));
                    double tmpmaf = ((2 * hom1) + het) / ((2 * het) + (2 * hom1) + (2 * hom2));
                    if (tmpmaf < 0.5) {
                        maf = tmpmaf;
                    } else {
                        maf = 1 - tmpmaf;
                    }
                    hwpval = hwCalculate((int) hom1, (int) het, (int) hom2);
                }
            } else {
                if (geno == 0) {
                    hom1++;
                } else if (geno == 2) {
                    het++;
                } else if (geno == 3) {
                    hom2++;
                } else {
                    missing++;
                }
                genopc = 1 - (missing / (missing + hom1 + het + hom2));
                double tmpmaf = ((2 * hom1) + het) / ((2 * het) + (2 * hom1) + (2 * hom2));
                if (tmpmaf < 0.5) {
                    maf = tmpmaf;
                } else {
                    maf = 1 - tmpmaf;
                }
                hwpval = hwCalculate((int) hom1, (int) het, (int) hom2);
            }
        }
    }

    private double hwCalculate(int obsAA, int obsAB, int obsBB) {
        //Calculates exact two-sided hardy-weinberg p-value. Parameters
        //are number of genotypes, number of rare alleles observed and
        //number of heterozygotes observed.
        //
        // (c) 2003 Jan Wigginton, Goncalo Abecasis
        return 0.5;
    //     int diplotypes = obsAA + obsAB + obsBB;
    //     if (diplotypes == 0) {
    //         return 0;
    //     }
    //     int rare = (obsAA * 2) + obsAB;
    //     int hets = obsAB;


    //     //make sure "rare" allele is really the rare allele
    //     if (rare > diplotypes) {
    //         rare = 2 * diplotypes - rare;
    //     }

    //     double[] tailProbs = new double[rare + 1];
    //     for (int z = 0; z < tailProbs.length; z++) {
    //         tailProbs[z] = 0;
    //     }

    //     //start at midpoint
    //     int mid = rare * (2 * diplotypes - rare) / (2 * diplotypes);

    //     //check to ensure that midpoint and rare alleles have same parity
    //     if (((rare & 1) ^ (mid & 1)) != 0) {
    //         mid++;
    //     }
    //     int het = mid;
    //     int hom_r = (rare - mid) / 2;
    //     int hom_c = diplotypes - het - hom_r;

    //     //Calculate probability for each possible observed heterozygote
    //     //count up to a scaling constant, to avoid underflow and overflow
    //     tailProbs[mid] = 1.0;
    //     double sum = tailProbs[mid];
    //     for (het = mid; het > 1; het -= 2) {
    //         tailProbs[het - 2] = (tailProbs[het] * het * (het - 1.0)) / (4.0 * (hom_r + 1.0) * (hom_c + 1.0));
    //         sum += tailProbs[het - 2];
    //         //2 fewer hets for next iteration -> add one rare and one common homozygote
    //         hom_r++;
    //         hom_c++;
    //     }

    //     het = mid;
    //     hom_r = (rare - mid) / 2;
    //     hom_c = diplotypes - het - hom_r;
    //     for (het = mid; het <= rare - 2; het += 2) {
    //         tailProbs[het + 2] = (tailProbs[het] * 4.0 * hom_r * hom_c) / ((het + 2.0) * (het + 1.0));
    //         sum += tailProbs[het + 2];
    //         //2 more hets for next iteration -> subtract one rare and one common homozygote
    //         hom_r--;
    //         hom_c--;
    //     }

    //     for (int z = 0; z < tailProbs.length; z++) {
    //         tailProbs[z] /= sum;
    //     }

    //     double top = tailProbs[hets];
    //     for (int i = hets + 1; i <= rare; i++) {
    //         top += tailProbs[i];
    //     }
    //     double otherSide = tailProbs[hets];
    //     for (int i = hets - 1; i >= 0; i--) {
    //         otherSide += tailProbs[i];
    //     }

    //     if (top > 0.5 && otherSide > 0.5) {
    //         return 1.0;
    //     } else {
    //         if (top < otherSide) {
    //             return top * 2;
    //         } else {
    //             return otherSide * 2;
    //         }
    //     }
    }

    public HashMap<String, Byte> getGenotypeChanges() {
        return genotypeChanges;
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

    public double getMaxDim() {
        return maxDim;
    }

    public double getMinDim() {
        return minDim;
    }

    public char[] getAlleles() {
        if (alleles != null) {
            return alleles;
        } else {
            return new char[]{' ', ' '};
        }
    }

    public int getSampleNum() {
        return sampleNum;
    }

    private void setCoordSystem(CoordinateSystem coordSystem) {
        this.coordSystem = coordSystem;
    }
    
    public CoordinateSystem getCoordSystem() {
        return coordSystem;
    }
    
    public byte getCalledGenotype(String ind){
        return calledGenotypes.get(samples.getIndex(ind));
    }
    
    public int getIndexInArrayList(String ind){
        return indexInArrayListByInd.get(ind);
    }

    private double log2(double x) {
        return Math.log(x) / Math.log(2);
    }
}
