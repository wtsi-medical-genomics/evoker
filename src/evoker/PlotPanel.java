package evoker;

import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.util.Locale;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class PlotPanel extends JPanel {

    /** Mode, determining the way, the Diagram responses to mouse gestures.  
     * <code>true</code> means lasso select, <code>false</code> means zooming in.*/
    private boolean MOUSE_MODE = false;
    
    ChartPanel generatePlot;
    
    private JFreeChart jfc;
    private PlotData data;
    private String title, xlab, ylab;
    private boolean foundData;
    static NumberFormat nf = NumberFormat.getInstance(Locale.US);
    
    JPanel statistics = null;

    static {
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
    }

    PlotPanel(String title, PlotData pd, int plotHeight, int plotWidth, boolean MOUSE_MODE) {
        this.title = title;
        this.data = pd;
        this.MOUSE_MODE = MOUSE_MODE;

        if (pd.getCoordSystem().matches("POLAR")) {
            this.xlab = String.valueOf("\u03F4");
            this.ylab = String.valueOf("r");
        } else {
            this.xlab = String.valueOf("X");
            this.ylab = String.valueOf("Y");
        }

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setPreferredSize(new Dimension(plotHeight, plotWidth));
        this.setMaximumSize(new Dimension(plotHeight, plotWidth));
    }

    protected void refresh() {
        this.removeAll();
        XYSeriesCollection xysc = data.generatePoints();
        if (xysc != null) {
            setFoundData(true);
            generatePlot = generatePlot(xysc);
            add(generatePlot);
            statistics = new JPanel();
            add(generateInfo());
        } else {
            setFoundData(false);
            this.setBackground(Color.WHITE);
            add(Box.createVerticalGlue());
            JLabel l = new JLabel("No data found for " + title);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(l);
            add(Box.createVerticalGlue());
        }
    }

    private void setFoundData(boolean b) {
        foundData = b;
    }

    void saveToFile(File f) throws IOException {
        ChartUtilities.saveChartAsPNG(f, jfc, 400, 400);
    }

    public JPanel generateInfo() {
        statistics.setBackground(Color.white);
        statistics.add(new JLabel("MAF: " + nf.format(data.getMaf())));
        statistics.add(new JLabel("GPC: " + nf.format(data.getGenopc())));
        statistics.add(new JLabel("HWE pval: " + formatPValue(data.getHwpval())));

        return statistics;
    }
    
    public void updateInfo(){
        statistics.removeAll();
        generateInfo();
        statistics.revalidate();
    }

    public String generateInfoStr() {
        return "MAF: " + nf.format(data.getMaf()) + "\tGPC: " + nf.format(data.getGenopc()) + "\tHWE pval: " + formatPValue(data.getHwpval());
    }

    private ChartPanel generatePlot(XYSeriesCollection xysc) {

        jfc = ChartFactory.createScatterPlot(title, xlab, ylab, xysc,
                PlotOrientation.VERTICAL, false, false, false);
        jfc.addSubtitle(new TextTitle("(n=" + data.getSampleNum() + ")"));

        XYPlot thePlot = jfc.getXYPlot();
        thePlot.setBackgroundPaint(Color.white);
        thePlot.setOutlineVisible(false);

        XYItemRenderer xyd = thePlot.getRenderer();
        Shape dot = new Ellipse2D.Double(-1.5, -1.5, 3, 3);
        xyd.setSeriesShape(0, dot);
        xyd.setSeriesShape(1, dot);
        xyd.setSeriesShape(2, dot);
        xyd.setSeriesShape(3, dot);
        xyd.setSeriesPaint(0, Color.BLUE);
        xyd.setSeriesPaint(1, new Color(180, 180, 180));
        xyd.setSeriesPaint(2, Color.GREEN);
        xyd.setSeriesPaint(3, Color.RED);

        xyd.setBaseToolTipGenerator(new ZitPlotToolTipGenerator());

        EvokerChartPanel cp = new EvokerChartPanel(jfc, data, this);
        cp.setDisplayToolTips(true);
        cp.setDismissDelay(10000);
        cp.setInitialDelay(0);
        cp.setReshowDelay(0);

        return cp;
    }

    public double getMaxDim() {
        double range = data.getMaxDim() - data.getMinDim();
        return data.getMaxDim() + 0.05 * range;
    }

    public double getMinDim() {
        double range = data.getMaxDim() - data.getMinDim();
        return data.getMinDim() - 0.05 * range;
    }

    public void setDimensions(double min, double max) {
        if (jfc != null) {
            jfc.getXYPlot().setRangeAxis(new LinkedAxis(ylab, min, max));
            jfc.getXYPlot().getRangeAxis().setRange(min, max);
            if (data.getCoordSystem().matches("POLAR")) {
                jfc.getXYPlot().setDomainAxis(new LinkedAxis(xlab, 0, 2));
                jfc.getXYPlot().getDomainAxis().setRange(0, 2);
            } else {
                jfc.getXYPlot().setDomainAxis(new LinkedAxis(xlab, min, max));
                jfc.getXYPlot().getDomainAxis().setRange(min, max);
            }

        }
    }

    public static String formatPValue(double pval) {
        DecimalFormat df;
        //java truly sucks for simply restricting the number of sigfigs but still
        //using scientific notation when appropriate
        if (pval < 0.0001) {
            df = new DecimalFormat("0.0E0", new DecimalFormatSymbols(Locale.US));
        } else {
            df = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.US));
        }
        return df.format(pval, new StringBuffer(), new FieldPosition(NumberFormat.INTEGER_FIELD)).toString();
    }

    class ZitPlotToolTipGenerator extends StandardXYToolTipGenerator {

        public double round3(double n) {
            double result = n * 100000;
            result = Math.round(result);
            result = result / 100000;
            return result;
        }

        public ZitPlotToolTipGenerator() {
            super();
        }

        public String generateToolTip(XYDataset dataset, int series, int item) {
            return data.getIndInClass(series, item) + " ("
                    + round3(dataset.getXValue(series, item)) + ", "
                    + round3(dataset.getYValue(series, item)) + ")";
                  //  + dataset.getXValue(series, item) + ", "
                  //  + dataset.getYValue(series, item) + ")";
        }
    }

    public JFreeChart getChart() {
        return jfc;
    }
    
    public PlotData getPlotData(){
        return data;
    }
    
    public boolean getMouseMode(){
        return MOUSE_MODE;
    }
    
    public void setMouseMode(boolean s) {
        MOUSE_MODE = s;
    }

    public boolean hasData() {
        return foundData;
    }
}
