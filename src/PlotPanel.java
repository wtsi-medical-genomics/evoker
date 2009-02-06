import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.renderer.xy.XYItemRenderer;
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

public class PlotPanel extends JPanel {

    private JFreeChart jfc;
    private PlotData data;
    private String title, xlab, ylab;
    static NumberFormat nf = NumberFormat.getInstance(Locale.US);
    static {
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
    }


    PlotPanel(String title, String xlab, String ylab, PlotData pd){
        this.title = title;
        this.xlab = xlab;
        this.ylab = ylab;
        this.data = pd;

        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setPreferredSize(new Dimension(300,300));
        this.setMaximumSize(new Dimension(300,300));
    }

    void refresh(){
        this.removeAll();
        XYSeriesCollection xysc = data.generatePoints();
        if (xysc != null){
            add(generatePlot(xysc));
            add(generateInfo());
        }else{
            this.setBackground(Color.WHITE);
            add(Box.createVerticalGlue());
            JLabel l = new JLabel("No data found for "+title);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(l);
            add(Box.createVerticalGlue());
        }
    }

    void saveToFile(File f) throws IOException {
        ChartUtilities.saveChartAsPNG(f, jfc,400,400);
    }

    private JPanel generateInfo(){
        JPanel foo = new JPanel();
        foo.setBackground(Color.white);
        foo.add(new JLabel("MAF: " + nf.format(data.getMaf())));
        foo.add(new JLabel("GPC: " + nf.format(data.getGenopc())));
        foo.add(new JLabel("HWE pval: " + formatPValue(data.getHwpval())));

        return foo;
    }

    private ChartPanel generatePlot(XYSeriesCollection xysc) {

        jfc = ChartFactory.createScatterPlot(title, xlab, ylab, xysc,
                PlotOrientation.VERTICAL, false, false, false);

        XYPlot thePlot = jfc.getXYPlot();
        //BasicStroke dash = new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,
        //        1,new float[]{10,10},0);
        //BasicStroke solid = new BasicStroke(1);
        thePlot.setBackgroundPaint(Color.white);
        //thePlot.setDomainZeroBaselineVisible(true);
        //thePlot.setDomainZeroBaselineStroke(solid);
        //thePlot.setRangeZeroBaselineVisible(true);
        //thePlot.setRangeZeroBaselineStroke(solid);
        thePlot.setOutlineVisible(false);
        //thePlot.getDomainAxis().setAxisLineVisible(false);
        //thePlot.getDomainAxis().setTickMarksVisible(false);
        //thePlot.getRangeAxis().setAxisLineVisible(false);
        //thePlot.getRangeAxis().setTickMarksVisible(false);

        XYItemRenderer xyd = thePlot.getRenderer();
        Shape dot = new Ellipse2D.Double(-1.5,-1.5,3,3);
        xyd.setSeriesShape(0, dot);
        xyd.setSeriesShape(1, dot);
        xyd.setSeriesShape(2, dot);
        xyd.setSeriesShape(3, dot);
        xyd.setSeriesPaint(0, Color.BLUE);
        xyd.setSeriesPaint(1, new Color(180,180,180));
        xyd.setSeriesPaint(2, Color.GREEN);
        xyd.setSeriesPaint(3, Color.RED);

        xyd.setBaseToolTipGenerator(new ZitPlotToolTipGenerator());

        ChartPanel cp = new ChartPanel(jfc);
        cp.setDisplayToolTips(true);

        return new ChartPanel(jfc);
    }

    public double getMaxDim(){
        return 1.05*data.getMaxDim();
    }

    public void setMaxDim(double val){
        if (jfc != null){
            jfc.getXYPlot().getDomainAxis().setRange(-0.1,val);
            jfc.getXYPlot().getRangeAxis().setRange(-0.1,val);
        }
    }

    public static String formatPValue(double pval){
         DecimalFormat df;
        //java truly sucks for simply restricting the number of sigfigs but still
        //using scientific notation when appropriate
        if (pval < 0.0001){
            df = new DecimalFormat("0.0E0", new DecimalFormatSymbols(Locale.US));
        }else{
            df = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.US));
        }
        return df.format(pval, new StringBuffer(), new FieldPosition(NumberFormat.INTEGER_FIELD)).toString();
    }

    class ZitPlotToolTipGenerator extends StandardXYToolTipGenerator {

        public ZitPlotToolTipGenerator(){
            super();
        }

        public String generateToolTip(XYDataset dataset, int series, int item){
            return data.getIndInClass(series,item);
        }
    }
}
