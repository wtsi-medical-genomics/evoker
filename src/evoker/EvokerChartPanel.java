package evoker;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ExtensionFileFilter;

public class EvokerChartPanel extends ChartPanel {

    /** Holding the last Point, the mouse was dragged to (for drawing a line while selecting) */
    Point lastDragPoint = null;
    /** Will hold the lasso Object,  while the selection is being made*/
    Lasso lasso = null;
    /** Enable mouse listeners to perform zoom*/
    public static final String ZOOM_ENABLE_COMMAND = "ZOOM_ENABLE";
    /** Enable mouse listeners to perform lasso select*/
    public static final String LASSO_SELECT_ENABLE_COMMAND = "LASSO_SELECT_ENABLE";
    /*The genotype selection menu*/
    private JPopupMenu genotypeSelectPopup;
    /** The genotype data being displayed */
    private PlotData plotData = null;
    /** The panel calling the ChartPanel*/
    private PlotPanel plotPanel = null;

    EvokerChartPanel(JFreeChart jfc, PlotData pdata, PlotPanel ppanel) {
        super(jfc);
        // set up genotype select menu (... :] )        
        this.genotypeSelectPopup = createGenotypeSelectPopup();
        this.plotData = pdata;
        this.plotPanel = ppanel;

        createPopupMenu_();
    }

    /**
     * Handles a 'mouse pressed' event.
     * <P>
     * This event is the popup trigger on Unix/Linux.  For Windows, the popup
     * trigger is the 'mouse released' event.
     *
     * @param e  The mouse event.
     */
    public void mousePressed(MouseEvent e) {
        if (plotPanel.getMouseMode()) {
            lastDragPoint = e.getPoint();
            lasso = new Lasso();
            lasso.addPoint(e.getX(), e.getY());
        }
        else {
            super.mousePressed(e);
        }
        if (e.isPopupTrigger()) {
            if (popup != null) {
                displayPopupMenu(e.getX(), e.getY());
            }
        }
    }

    /**
     * Handles a 'mouse dragged' event.
     *
     * @param e  the mouse event.
     */
    public void mouseDragged(MouseEvent e) {
        if (plotPanel.getMouseMode()) {
            // draw line from last Point to the current one
            if (lastDragPoint != null) {
                Graphics2D g2 = (Graphics2D) getGraphics();
                g2.draw(new Line2D.Double(lastDragPoint.getX(), lastDragPoint.getY(), e.getX(), e.getY()));
            }
            lastDragPoint = new Point(e.getX(), e.getY());

            lasso.addPoint(e.getX(), e.getY());
        }
        else {
            super.mouseDragged(e);
        }
    }

    /**
     * Handles a 'mouse released' event.  On Windows, we need to check if this
     * is a popup trigger, but only if we haven't already been tracking a zoom
     * rectangle.
     *
     * @param e  information about the event.
     */
    public void mouseReleased(MouseEvent e) {

        if (plotPanel.getMouseMode()) {
            lastDragPoint = null;

            lasso.addPoint(e.getX(), e.getY());

            if (lasso != null && lasso.getNumberOfEdges() > 3) {
                this.genotypeSelectPopup.show(this, e.getX(), e.getY());
            }

        }
        else {
            super.mouseReleased(e);
        }
    }

    /**
     * Creates a popup menu for the panel.
     *
     * @param properties  include a menu item for the chart property editor.
     * @param copy include a menu item for copying to the clipboard.
     * @param save  include a menu item for saving the chart.
     * @param print  include a menu item for printing the chart.
     * @param zoom  include menu items for zooming.
     *
     * @return The popup menu.
     *
     * @since 1.0.13
     */
    protected void createPopupMenu_() {
        ButtonGroup group = new ButtonGroup();

        JRadioButtonMenuItem jrbMenIt = new JRadioButtonMenuItem("Zoom");
        if (!plotPanel.getMouseMode()) jrbMenIt.setSelected(true);
        jrbMenIt.setMnemonic(KeyEvent.VK_O);
        jrbMenIt.setActionCommand(ZOOM_ENABLE_COMMAND);
        jrbMenIt.addActionListener(this);
        group.add(jrbMenIt);
        popup.add(jrbMenIt, 0);

        jrbMenIt = new JRadioButtonMenuItem("Lasso Select");
        if (plotPanel.getMouseMode()) jrbMenIt.setSelected(true);
        jrbMenIt.setMnemonic(KeyEvent.VK_R);
        jrbMenIt.setActionCommand(LASSO_SELECT_ENABLE_COMMAND);
        jrbMenIt.addActionListener(this);
        group.add(jrbMenIt);
        popup.add(jrbMenIt, 1);
    }

    protected JPopupMenu createGenotypeSelectPopup() {
        JPopupMenu result = new JPopupMenu("Chart:");
        ButtonGroup group = new ButtonGroup();

        JRadioButtonMenuItem jrbMenIt = new JRadioButtonMenuItem("Y Homozygous");
        jrbMenIt.setSelected(true);
        jrbMenIt.setMnemonic(KeyEvent.VK_O);
        jrbMenIt.setActionCommand("GENOTYPE_YY");
        jrbMenIt.addActionListener(this);
        group.add(jrbMenIt);
        result.add(jrbMenIt);

        jrbMenIt = new JRadioButtonMenuItem("Heterozygous");
        jrbMenIt.setMnemonic(KeyEvent.VK_R);
        jrbMenIt.setActionCommand("GENOTYPE_XY");
        jrbMenIt.addActionListener(this);
        group.add(jrbMenIt);
        result.add(jrbMenIt);

        jrbMenIt = new JRadioButtonMenuItem("X Homozygous");
        jrbMenIt.setMnemonic(KeyEvent.VK_R);
        jrbMenIt.setActionCommand("GENOTYPE_XX");
        jrbMenIt.addActionListener(this);
        group.add(jrbMenIt);
        result.add(jrbMenIt);

        jrbMenIt = new JRadioButtonMenuItem("Unknown");
        jrbMenIt.setMnemonic(KeyEvent.VK_R);
        jrbMenIt.setActionCommand("GENOTYPE_UNKNOWN");
        jrbMenIt.addActionListener(this);
        group.add(jrbMenIt);
        result.add(jrbMenIt);

        return result;
    }

    /**
     * Handles action events generated by the popup menu.
     *
     * @param event  the event.
     */
    public void actionPerformed(ActionEvent event) {

        String command = event.getActionCommand();
        if (command.equals("GENOTYPE_YY")) {
            adjustDataSeries(0);
        }
        else if (command.equals("GENOTYPE_UNKNOWN")) {
            adjustDataSeries(1);
        }
        else if (command.equals("GENOTYPE_XY")) {
            adjustDataSeries(2);
        }
        else if (command.equals("GENOTYPE_XX")) {
            adjustDataSeries(3);
        }
        else if (command.equals(ZOOM_ENABLE_COMMAND)) {
            plotPanel.setMouseMode(false);
        }
        else if (command.equals(LASSO_SELECT_ENABLE_COMMAND)) {
            plotPanel.setMouseMode(true);
        }
        else {
            super.actionPerformed(event);
        }

    }

    public double round3(double n) {
        double result = n * 100000;
        result = Math.round(result);
        result = result / 100000;
        return result;
    }

    void adjustDataSeries(int genotype) {
        //ArrayList<EvokerPoint2D> containedPoints = lasso.getContainedPoints(super.info.getEntityCollection());
        HashMap<EvokerPoint2D, String> containedPointsInd = lasso.getContainedPointsInd(super.info.getEntityCollection());
        ArrayList<String> al_s = new ArrayList<String>();

        XYPlot plot = (XYPlot) super.getChart().getPlot();
        XYSeriesCollection xyseriescoll = (XYSeriesCollection) plot.getDataset();

        ArrayList<XYDataItem> l_di = new ArrayList<XYDataItem>();

        int seriesCount = xyseriescoll.getSeriesCount();
        for (int a = 0; a < seriesCount; a++) {
            XYSeries series = xyseriescoll.getSeries(a);

            List<XYDataItem> items = (List<XYDataItem>) series.getItems();
            int itemsLength = items.size();
            for (int b = 0; b < itemsLength; b++) {
                XYDataItem xydi = items.get(b);
                Point2D p = new Point2D.Double(round3(Double.parseDouble(xydi.getX() + "")),
                        round3(Double.parseDouble(xydi.getY() + "")));

                for (EvokerPoint2D p_ : containedPointsInd.keySet()) {
                    if (p.getX() == p_.getX() && p.getY() == p_.getY()) {
                        if (al_s.contains(containedPointsInd.get(p_))) continue;
                        plotData.moveIndToClass(containedPointsInd.get(p_), a, b, genotype);
                        al_s.add(containedPointsInd.get(p_));
                        l_di.add(xydi);
                        items.remove(b);
                        b--;
                        itemsLength--;
                        break;
                    }
                }
            }
        }

        XYSeries series = xyseriescoll.getSeries(genotype);
        List<XYDataItem> items = (List<XYDataItem>) series.getItems();
        for (XYDataItem xydi : l_di) {
            items.add(xydi);
        }

        lasso = null;
        this.chart.setNotify(true); // last thing, redraw.  Applies changes and gets rid of the line.
        this.plotData.computeSummary();
        this.plotPanel.updateInfo();

        this.plotData.changed = true;
    }

    private void save() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(super.defaultDirectoryForSaveAs);
        ExtensionFileFilter filter = new ExtensionFileFilter("BED Binary Files", ".bed");
        fileChooser.addChoosableFileFilter(filter);

        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getPath();
            if (isEnforceFileExtensions()) {
                if (!filename.endsWith(".bed")) {
                    filename = filename + ".bed";
                }
            }
            try {
                BEDFileWriter bfw = new BEDFileWriter(new File(filename));
            }
            catch (IOException ex) {
                throw new IOException("Could not write file.");
            }
        }
    }
}
