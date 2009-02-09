import org.jfree.chart.axis.NumberAxis;


public class LinkedAxis extends NumberAxis {

    private double min;
    private double max;

    public LinkedAxis(double min, double max){
        this.min = min;
        this.max = max;
    }

    public void setAutoRange(boolean auto){
        //we want the automatic range on this puppy to be linked to all other graphs,
        //which we set in the constructor. schweet.
        if (auto){
            setLowerBound(min);
            setUpperBound(max);
        }
    }
    
}
