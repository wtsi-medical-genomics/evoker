package evoker;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeriesCollection;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Vector;
import java.util.LinkedList;
import org.jfree.ui.ExtensionFileFilter;

public class Genoplot extends JFrame implements ActionListener {

    private DataDirectory db;
    String plottedSNP = null;
    private JTextField snpField;
    private JButton goButton;
    private JPanel plotArea;
    private PrintStream output;
    private LinkedList<String> snpList;
    private String currentSNPinList;
    private String currentSNP = null;
    private int currentSNPindex;
    private int displaySNPindex;
    private boolean backSNP = false;
    private boolean historySNP = false;
    private boolean markerList = false;
    private Hashtable<String, Integer> listScores = new Hashtable<String, Integer>();
    private Hashtable<String, String> pdfScores;
    JFileChooser jfc;
    DataConnectionDialog dcd;
    PDFDialog pdfd;
    ProgressMonitor pm;
    SettingsDialog sd;
    private JPanel scorePanel;
    private JPanel messagePanel;
    private JLabel message;
    private JButton yesButton;
    private JButton maybeButton;
    private JButton noButton;
    private JButton backButton;
    private JMenu fileMenu;
    private JMenu saveMenu;
    private JMenuItem openDirectory;
    private JMenuItem openRemote;
    private JMenuItem loadList;
    private JMenuItem loadExclude;
    private JMenu toolsMenu;
    private JCheckBoxMenuItem filterData;
    private JMenuItem saveAll;
    private JMenuItem exportPDF;
    private JMenu viewMenu;
    private JMenuItem viewPolar;
    private JMenuItem viewCart;
    private JMenu historyMenu;
    private ButtonGroup snpGroup;
    private JMenuItem returnToListPosition;
    private JMenuItem showLogItem;
    private JMenu settingsMenu;
    private JMenuItem plotSize;
    public static LoggingDialog ld;
    private JButton randomSNPButton;
    private EvokerPDF allPDF = null;
    private EvokerPDF yesPDF = null;
    private EvokerPDF maybePDF = null;
    private EvokerPDF noPDF = null;
    private int yesPlotNum;
    private int maybePlotNum;
    private int noPlotNum;
    // default values
    private String coordSystem = "CART";
    private int plotHeight = 300;
    private int plotWidth = 300;

    public static void main(String[] args) {

        new Genoplot();

    }

    Genoplot() {
        super("Evoke...");

        jfc = new JFileChooser("user.dir");
        dcd = new DataConnectionDialog(this);
        pdfd = new PDFDialog(this);
        sd = new SettingsDialog(this);
        JMenuBar mb = new JMenuBar();

        int menumask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        fileMenu = new JMenu("File");
        openDirectory = new JMenuItem("Open directory");
        openDirectory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                menumask));
        openDirectory.addActionListener(this);
        fileMenu.add(openDirectory);

        openRemote = new JMenuItem("Connect to remote server");
        openRemote.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                menumask));
        openRemote.addActionListener(this);
        fileMenu.add(openRemote);

        saveMenu = new JMenu("Save BED...");
        saveMenu.setEnabled(false);
        fileMenu.add(saveMenu);

        loadList = new JMenuItem("Load marker list");
        loadList.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, menumask));
        loadList.addActionListener(this);
        loadList.setEnabled(false);
        fileMenu.add(loadList);
        loadExclude = new JMenuItem("Load sample exclude list");
        loadExclude.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
                menumask));
        loadExclude.addActionListener(this);
        loadExclude.setEnabled(false);
        fileMenu.add(loadExclude);

        mb.add(fileMenu);

        toolsMenu = new JMenu("Tools");
        filterData = new JCheckBoxMenuItem("Filter samples");
        filterData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                menumask));
        filterData.addActionListener(this);
        filterData.setEnabled(false);
        toolsMenu.add(filterData);
        saveAll = new JMenuItem("Save SNP Plots");
        saveAll.addActionListener(this);
        saveAll.setEnabled(false);
        toolsMenu.add(saveAll);
        exportPDF = new JMenuItem("Generate PDF from scores");
        exportPDF.addActionListener(this);
        exportPDF.setEnabled(false);
        toolsMenu.add(exportPDF);

        if (!(System.getProperty("os.name").toLowerCase().contains("mac"))) {
            JMenuItem quitItem = new JMenuItem("Quit");
            quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                    menumask));
            quitItem.addActionListener(this);
            fileMenu.add(quitItem);
        }

        mb.add(toolsMenu);

        viewMenu = new JMenu("View");
        ButtonGroup viewGroup = new ButtonGroup();
        viewCart = new JCheckBoxMenuItem("Cartesian coordinates");
        viewCart.addActionListener(this);
        viewCart.setEnabled(false);
        viewGroup.add(viewCart);
        viewMenu.add(viewCart);
        viewPolar = new JCheckBoxMenuItem("Polar coordinates");
        viewPolar.addActionListener(this);
        viewPolar.setEnabled(false);
        viewGroup.add(viewPolar);
        viewMenu.add(viewPolar);

        mb.add(viewMenu);

        historyMenu = new JMenu("History");
        returnToListPosition = new JMenuItem("Return to current list position");
        snpGroup = new ButtonGroup();
        returnToListPosition.setEnabled(false);
        returnToListPosition.addActionListener(this);
        historyMenu.add(returnToListPosition);
        historyMenu.addSeparator();
        mb.add(historyMenu);

        JMenu logMenu = new JMenu("Log");
        showLogItem = new JMenuItem("Show Evoker log");
        showLogItem.addActionListener(this);
        logMenu.add(showLogItem);
        mb.add(logMenu);

        setJMenuBar(mb);

        settingsMenu = new JMenu("Settings");
        plotSize = new JMenuItem("Plot settings");
        plotSize.addActionListener(this);
        plotSize.setEnabled(true);
        settingsMenu.add(plotSize);
        mb.add(settingsMenu);

        JPanel controlsPanel = new JPanel();

        snpField = new JTextField(10);
        snpField.setEnabled(false);
        JPanel snpPanel = new JPanel();
        snpPanel.add(new JLabel("SNP:"));
        snpPanel.add(snpField);
        goButton = new JButton("Go");
        goButton.addActionListener(this);
        goButton.setEnabled(false);
        snpPanel.add(goButton);
        randomSNPButton = new JButton("Random");
        randomSNPButton.addActionListener(this);
        randomSNPButton.setEnabled(false);
        snpPanel.add(randomSNPButton);
        controlsPanel.add(snpPanel);

        controlsPanel.add(Box.createRigidArea(new Dimension(50, 1)));

        scorePanel = new JPanel();
        scorePanel.add(new JLabel("Approve?"));

        yesButton = new JButton("Yes");
        scorePanel.registerKeyboardAction(this, "Yes", KeyStroke.getKeyStroke(
                KeyEvent.VK_Y, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        yesButton.addActionListener(this);
        yesButton.setEnabled(false);
        scorePanel.add(yesButton);

        maybeButton = new JButton("Maybe");
        scorePanel.registerKeyboardAction(this, "Maybe", KeyStroke.getKeyStroke(KeyEvent.VK_M, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        maybeButton.addActionListener(this);
        maybeButton.setEnabled(false);
        scorePanel.add(maybeButton);

        noButton = new JButton("No");
        scorePanel.registerKeyboardAction(this, "No", KeyStroke.getKeyStroke(
                KeyEvent.VK_N, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        noButton.addActionListener(this);
        noButton.setEnabled(false);
        scorePanel.add(noButton);

        backButton = new JButton("Back");
        scorePanel.registerKeyboardAction(this, "Back", KeyStroke.getKeyStroke(
                KeyEvent.VK_B, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        backButton.addActionListener(this);
        backButton.setEnabled(false);
        scorePanel.add(backButton);

        messagePanel = new JPanel();
        message = new JLabel("");
        message.setEnabled(false);
        messagePanel.add(message);
        message.setVisible(false);

        JPanel rightPanel = new JPanel();
        rightPanel.add(scorePanel);
        rightPanel.add(messagePanel);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        controlsPanel.add(rightPanel);

        controlsPanel.setMaximumSize(new Dimension(2000, (int) controlsPanel.getPreferredSize().getHeight()));
        controlsPanel.setMinimumSize(new Dimension(10, (int) controlsPanel.getPreferredSize().getHeight()));

        plotArea = new JPanel();
        plotArea.setPreferredSize(new Dimension(700, 350));

        plotArea.setBorder(new LineBorder(Color.BLACK));
        plotArea.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(plotArea);
        contentPanel.add(controlsPanel);

        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        this.setContentPane(contentPanel);
        this.pack();
        this.setVisible(true);

        ld = new LoggingDialog(this);
        ld.pack();
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            String command = actionEvent.getActionCommand();
            if (command.equals("Go")) {
                if (markerListLoaded()) {
                    if (snpList.contains(snpField.getText().trim())) {
                        setHistorySNP(true);
                        displaySNPindex = snpList.indexOf(snpField.getText().trim());
                    }
                }
                plotIntensitas(snpField.getText().trim());
            } else if (command.equals("No")) {
                noButton.requestFocusInWindow();
                recordVerdict(-1);
            } else if (command.equals("Maybe")) {
                maybeButton.requestFocusInWindow();
                recordVerdict(0);
            } else if (command.equals("Yes")) {
                yesButton.requestFocusInWindow();
                recordVerdict(1);
            } else if (command.equals("Back")) {
                setBackSNP(true);
                displaySNPindex--;
                if (displaySNPindex == 0) {
                    backButton.setEnabled(false);
                }
                int lastCall = listScores.get(snpList.get(displaySNPindex));
                if (lastCall == 1) {
                    yesButton.requestFocusInWindow();
                } else if (lastCall == 0) {
                    maybeButton.requestFocusInWindow();
                } else {
                    noButton.requestFocusInWindow();
                }
                plotIntensitas(snpList.get(displaySNPindex));
            } else if (command.equals("Return to current list position")) {
                plotIntensitas(snpList.get(currentSNPindex));
            } else if (command.equals("Random")) {
                plotIntensitas(db.getRandomSNP());
            } else if (command.startsWith("PLOTSNP")) {
                String[] bits = command.split("\\s");
                if (markerListLoaded()) {
                    if (snpList.contains(bits[1])) {
                        setHistorySNP(true);
                        displaySNPindex = snpList.indexOf(bits[1]);
                    }
                }
                plotIntensitas(bits[1]);
            } else if (command.equals("Open directory")) {
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        if (!new File(jfc.getSelectedFile().getAbsolutePath()).exists()) {
                            throw new IOException("Directory does not exist!");
                        }
                        db = new DataDirectory(jfc.getSelectedFile().getAbsolutePath());
                        plottedSNP = null;
                        finishLoadingDataSource();
                        refreshSaveMenu();
                    } finally {
                        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            } else if (command.equals("Connect to remote server")) {

                final Genoplot gp = this;

                // put loading data into a swing worker so that evoker does not hang when loading lots of data
                new SwingWorker() {

                    Exception pendingException = null;

                    public Object doInBackground() throws Exception {
                        try {
                            dcd.pack();
                            dcd.setVisible(true);

                            DataClient dc = new DataClient(dcd, gp);

                            if (dc.getConnectionStatus()) {
                                // when loading only allow the user to view the log
                                disableAllActions();
                                showLogItem.setEnabled(true);
                                db = new DataDirectory(dc);
                                finishLoadingDataSource();
                            }
                        } catch (Exception e) {
                            pendingException = e;
                        }
                        return null;
                    }

                    protected void done() {
                        if (pendingException != null) {
                            JOptionPane.showMessageDialog(gp, pendingException.getMessage());
                        }
                    }
                }.execute();

            } else if (command.equals("Load marker list")) {
                currentSNPindex = 0;
                displaySNPindex = currentSNPindex;
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    String markerList = jfc.getSelectedFile().getAbsolutePath();
                    File outfile = checkOverwriteFile(new File(jfc.getSelectedFile().getAbsolutePath()
                            + ".scores"));
                    try {
                        if (outfile != null) {
                            loadList(markerList);
                            FileOutputStream fos = new FileOutputStream(outfile);
                            output = new PrintStream(fos);
                        } else {
                            throw new IOException();
                        }
                    } catch (IOException ioe) {
                        throw new IOException("No score file selected");
                    }
                }
            } else if (command.equals("Load sample exclude list")) {
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    // TODO: before setting as the new list check it is a valid
                    // not empty exclude file
                    db.setExcludeList(new QCFilterData(jfc.getSelectedFile().getAbsolutePath()));
                    db.setFilterState(true);
                    filterData.setEnabled(true);
                    filterData.setSelected(true);
                    Genoplot.ld.log("Loaded exclude file: "
                            + jfc.getSelectedFile().getName());
                    if (currentSNP != null) {
                        plotIntensitas(currentSNP);
                    }
                }
            } else if (command.equals("Filter samples")) {
                // turn filtering on/off
                if (filterData.isSelected()) {
                    db.setFilterState(true);
                } else {
                    db.setFilterState(false);
                }
                if (currentSNP != null) {
                    plotIntensitas(currentSNP);
                }
            } else if (command.equals("Save SNP Plots")) {
                File defaultFileName = new File(plottedSNP + ".png");
                jfc.setSelectedFile(defaultFileName);
                if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File png;
                    if (!jfc.getSelectedFile().toString().endsWith(".png")) {
                        png = new File(jfc.getSelectedFile().toString()
                                + ".png");
                    } else {
                        png = jfc.getSelectedFile();
                    }
                    BufferedImage image = new BufferedImage(
                            plotArea.getWidth(), plotArea.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = image.createGraphics();
                    plotArea.paint(g2);
                    g2.dispose();
                    try {
                        ImageIO.write(image, "png", png);
                    } catch (IOException ioe) {
                        System.out.println(ioe.getMessage());
                    }
                }
            } else if (command.equals("Generate PDF from scores")) {
                pdfd.pack();
                pdfd.setVisible(true);
                if (pdfd.success()) {
                    generatePDFs();
                }
            } else if (command.equals("Cartesian coordinates")) {
                setCoordSystem("CART");
                refreshPlot();
            } else if (command.equals("Polar coordinates")) {
                setCoordSystem("POLAR");
                refreshPlot();
            } else if (command.equals("Plot settings")) {
                sd.pack();
                sd.setVisible(true);

            } else if (command.equals("Show Evoker log")) {
                ld.setVisible(true);
            } else if (command.equals("Quit")) {
                System.exit(0);
            } else {
                Vector<String> collections = db.getCollections();
                for (String s : collections) {
                    if (command.equals(s)) {
                        dumpChanges();
                        if (!db.changesByCollection.containsKey(s)) {
                            throw new IOException("Not made any changes to that collection!");
                        }
                        save(s);
                    }
                }
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, ioe.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void disableAllActions() {

        // disable all actions while loading data		
        goButton.setEnabled(false);
        randomSNPButton.setEnabled(false);
        snpField.setEnabled(false);

        yesButton.setEnabled(false);
        noButton.setEnabled(false);
        maybeButton.setEnabled(false);
        backButton.setEnabled(false);
        scorePanel.setEnabled(false);

        openDirectory.setEnabled(false);
        openRemote.setEnabled(false);
        loadList.setEnabled(false);
        loadExclude.setEnabled(false);

        filterData.setEnabled(false);
        saveAll.setEnabled(false);
        exportPDF.setEnabled(false);

        viewCart.setEnabled(false);
        viewPolar.setEnabled(false);

        returnToListPosition.setEnabled(false);
        // clear history items
        snpGroup.clearSelection();
    }

    private void generatePDFs() throws DocumentException, IOException {

        loadScores(pdfd.getscoresFile());

        pm = new ProgressMonitor(this.getContentPane(), "Exporting plots to PDF", null, 0, pdfScores.size());

        SwingWorker pdfWorker = new SwingWorker() {

            public Object doInBackground() throws IOException {

                Enumeration<String> keys = pdfScores.keys();
                try {
                    openPDFs();
                } catch (DocumentException/*|IOException ex*/) {
                    ex.printStackTrace();
                    throw new IOException("Could not write PDF");
                } catch (IOException ex){
            	    ex.printStackTrace();
            	    throw new IOException("Could not write PDF");
                }

                File tempFile = null;
                try {
                    tempFile = File.createTempFile("temp", "png");
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                tempFile.deleteOnExit();

                int progressCounter = 0;
                try {
                    while (keys.hasMoreElements() && !pm.isCanceled()) {

                        String snp = (String) keys.nextElement();
                        String score = (String) pdfScores.get(snp);

                        Vector<Image> images = new Vector<Image>();
                        Vector<String> stats = new Vector<String>();

                        for (String collection : db.getCollections()) {
                            PlotPanel pp = new PlotPanel(collection, db.getRecord(snp, collection, getCoordSystem()), plotWidth, plotHeight);
                            pp.refresh();
                            if (pp.hasData()) {
                                pp.setDimensions(pp.getMinDim(), pp.getMaxDim());
                                ChartUtilities.saveChartAsPNG(tempFile, pp.getChart(), 400, 400);
                                images.add(Image.getInstance(tempFile.getAbsolutePath()));
                                stats.add(pp.generateInfoStr());
                            } else {
                                // print the jpanel displaying the no data
                                // message
                                BufferedImage noSNP = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
                                Graphics2D g2 = noSNP.createGraphics();
                                pp.paint(g2);
                                g2.dispose();
                                ImageIO.write(noSNP, "png", tempFile);
                                images.add(Image.getInstance(tempFile.getAbsolutePath()));
                                stats.add("");
                            }
                        }

                        PdfPTable table = new PdfPTable(images.size());
                        PdfPCell snpCell = new PdfPCell(new Paragraph(snp));
                        snpCell.setColspan(images.size());
                        snpCell.setBorder(0);
                        snpCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        snpCell.setVerticalAlignment(Element.ALIGN_TOP);
                        table.addCell(snpCell);

                        Iterator<Image> ii = images.iterator();
                        while (ii.hasNext()) {
                            PdfPCell imageCell = new PdfPCell((Image) ii.next());
                            imageCell.setBorder(0);
                            table.addCell(imageCell);
                        }

                        Iterator<String> si = stats.iterator();
                        while (si.hasNext()) {
                            PdfPCell statsCell = new PdfPCell(new Paragraph(
                                    (String) si.next()));
                            statsCell.setBorder(0);
                            statsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            table.addCell(statsCell);
                        }

                        if (pdfd.allPlots()) {
                            allPDF.getDocument().add(table);
                            allPDF.getDocument().newPage();
                        }
                        if (score.matches("1") && pdfd.yesPlots()) {
                            yesPDF.getDocument().add(table);
                            yesPDF.getDocument().newPage();
                            yesPlotNum++;
                        }
                        if (score.matches("0") && pdfd.maybePlots()) {
                            maybePDF.getDocument().add(table);
                            maybePDF.getDocument().newPage();
                            maybePlotNum++;
                        }
                        if (score.matches("-1") && pdfd.noPlots()) {
                            noPDF.getDocument().add(table);
                            noPDF.getDocument().newPage();
                            noPlotNum++;
                        }
                        progressCounter++;
                        pm.setProgress(progressCounter);
                    }
                    closePDFs();
                } catch (Exception e) {
                    //TODO: catch this
                }
                return null;
            }
        };

        pdfWorker.execute();

    }

    private void printScores() {
        ListIterator<String> snpi = snpList.listIterator();

        while (snpi.hasNext()) {
            String snp = (String) snpi.next();
            output.println(snp + "\t" + listScores.get(snp));
        }
        output.close();
    }

    private void setMarkerList(boolean marker) {
        markerList = marker;
    }

    private boolean markerListLoaded() {
        return markerList;
    }

    private void activeScorePanel(boolean score) {
        if (score) {
            yesButton.setEnabled(true);
            noButton.setEnabled(true);
            maybeButton.setEnabled(true);
            scorePanel.setEnabled(true);
            if (currentSNPindex == 0 || displaySNPindex == 0) {
                backButton.setEnabled(false);
            } else {
                backButton.setEnabled(true);
            }
        } else {
            yesButton.setEnabled(false);
            noButton.setEnabled(false);
            maybeButton.setEnabled(false);
            backButton.setEnabled(false);
            scorePanel.setEnabled(false);
        }
    }

    private void setBackSNP(boolean back) {
        backSNP = back;
    }

    private boolean isBackSNP() {
        return backSNP;
    }

    private void setHistorySNP(boolean history) {
        historySNP = history;
    }

    private boolean isHistorySNP() {
        return historySNP;
    }

    private void setCoordSystem(String s) {
        coordSystem = s;
    }

    private String getCoordSystem() {
        return coordSystem;
    }

    private File checkOverwriteFile(File file) {

        if (file.exists()) {
            int n = JOptionPane.showConfirmDialog(
                    this.getContentPane(),
                    "The file " + file.getName() + " already exists\n would you like to overwrite this file?",
                    "Overwrite file?", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            // n 0 = yes 1 = no
            if (n == 1) {
                if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    file = new File(jfc.getSelectedFile().getAbsolutePath());
                } else {
                    file = null;
                }
            }
        }
        return file;
    }

    private void recordVerdict(int v) throws DocumentException {
        if (isBackSNP()) {
            listScores.put(snpList.get(displaySNPindex), v);
            setBackSNP(false);
            if (displaySNPindex == 0) {
                backButton.setEnabled(true);
            }
            displaySNPindex = currentSNPindex;
            plotIntensitas(snpList.get(currentSNPindex));
        } else if (isHistorySNP()) {
            listScores.put(snpList.get(displaySNPindex), v);
            setHistorySNP(false);
            if (displaySNPindex == 0) {
                backButton.setEnabled(true);
            }
            displaySNPindex = currentSNPindex;
            plotIntensitas(snpList.get(currentSNPindex));
        } else {
            listScores.put(snpList.get(currentSNPindex), v);
            if (currentSNPindex < (snpList.size() - 1)) {
                currentSNPindex++;
                displaySNPindex = currentSNPindex;
                backButton.setEnabled(true);
                plotIntensitas(snpList.get(currentSNPindex));
            } else {
                plotIntensitas("ENDLIST");
                activeScorePanel(false);
                returnToListPosition.setEnabled(false);
                int n = JOptionPane.showConfirmDialog(
                        this.getContentPane(),
                        "Would you like to save the scores you have generated?",
                        "Finish scoring list?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                // n 0 = yes 1 = no
                if (n == 0) {
                    printScores();
                    setMarkerList(false);
                    randomSNPButton.setEnabled(true);
                    snpList.clear();
                } else {
                    activeScorePanel(true);
                    plotIntensitas(snpList.get(currentSNPindex));
                }
            }
        }
    }

    private void printMessage(String string) {
        message.setText(string);
        message.setVisible(true);
    }

    private void openPDFs() throws DocumentException, IOException {

        if (pdfd.allPlots()) {
            allPDF = new EvokerPDF(checkOverwriteFile(new File(pdfd.getPdfDir()
                    + "/all.pdf")), db.getNumCollections());
        }
        if (pdfd.yesPlots()) {
            yesPDF = new EvokerPDF(checkOverwriteFile(new File(pdfd.getPdfDir()
                    + "/yes.pdf")), db.getNumCollections());
            yesPlotNum = 0;
        }
        if (pdfd.maybePlots()) {
            maybePDF = new EvokerPDF(checkOverwriteFile(new File(pdfd.getPdfDir()
                    + "/maybe.pdf")), db.getNumCollections());
            maybePlotNum = 0;
        }
        if (pdfd.noPlots()) {
            noPDF = new EvokerPDF(checkOverwriteFile(new File(pdfd.getPdfDir()
                    + "/no.pdf")), db.getNumCollections());
            noPlotNum = 0;
        }
    }

    private void closePDFs() throws DocumentException {
        if (pdfd.allPlots() && allPDF.isFileOpen()) {
            allPDF.getDocument().close();
        }
        if (pdfd.yesPlots() && yesPDF.isFileOpen()) {
            if (yesPlotNum == 0) {
                yesPDF.getDocument().add(new Paragraph("No Yes plots recorded"));
            }
            yesPDF.getDocument().close();
        }
        if (pdfd.maybePlots() && maybePDF.isFileOpen()) {
            if (maybePlotNum == 0) {
                maybePDF.getDocument().add(
                        new Paragraph("No Maybe plots recorded"));
            }
            maybePDF.getDocument().close();
        }
        if (pdfd.noPlots() && noPDF.isFileOpen()) {
            if (noPlotNum == 0) {
                noPDF.getDocument().add(new Paragraph("No No plots recorded"));
            }
            noPDF.getDocument().close();
        }
    }

    private void viewedSNP(String name) {

        boolean inHistory = false;
        for (Component i : historyMenu.getMenuComponents()) {
            if (i instanceof JMenuItem) {
                if (((JMenuItem) i).getText().equals(name)) {
                    ((JRadioButtonMenuItem) i).setSelected(true);
                    inHistory = true;
                    break;
                }
            }
        }

        if (!inHistory) {
            // new guy
            JRadioButtonMenuItem snpItem = new JRadioButtonMenuItem(name);
            snpItem.setActionCommand("PLOTSNP " + name);
            snpItem.addActionListener(this);
            snpGroup.add(snpItem);
            snpItem.setSelected(true);
            historyMenu.add(snpItem, 2);

            // only track last ten
            if (historyMenu.getMenuComponentCount() > 12) {
                historyMenu.remove(12);
            }
        }
    }

    /**
     * Sets all menu entries enabled
     */
    private void finishLoadingDataSource() {
        if (db != null) {
            setPlotAreaSize();
            goButton.setEnabled(true);
            randomSNPButton.setEnabled(true);
            snpField.setEnabled(true);
            openDirectory.setEnabled(true);
            openRemote.setEnabled(true);
            loadList.setEnabled(true);
            loadExclude.setEnabled(true);
            saveAll.setEnabled(true);
            viewCart.setEnabled(true);
            viewPolar.setEnabled(true);
            exportPDF.setEnabled(true);
            saveMenu.setEnabled(true);
            while (historyMenu.getMenuComponentCount() > 2) {
                historyMenu.remove(2);
            }
            if (db.qcList() != null) {
                // if a exclude file is loaded from the directory enable
                // filtering
                filterData.setEnabled(true);
                filterData.setSelected(true);
            }

            this.setTitle("Evoke...[" + db.getDisplayName() + "]");

            plotArea.removeAll();
            plotArea.repaint();
        }
    }

    private void plotIntensitas(String name) {
        dumpChanges();

        plottedSNP = name;
        plotArea.removeAll();
        plotArea.setLayout(new BoxLayout(plotArea, BoxLayout.Y_AXIS));
        if (name != null) {
            if (!name.equals("ENDLIST")) {
                currentSNP = name;
                plotArea.add(new JLabel(name));
                fetchRecord(name);
                viewedSNP(name);
                if (markerListLoaded()) {
                    if (snpList.contains((String) name)) {
                        activeScorePanel(true);
                        plotArea.add(new JLabel((currentSNPindex + 1) + "/"
                                + snpList.size()));
                    }
                } else {
                    activeScorePanel(false);
                }

            } else {
                // I tried very hard to get the label right in the middle and
                // failed because java layouts blow
                plotArea.add(Box.createVerticalGlue());
                JPanel p = new JPanel();
                p.add(new JLabel("End of list."));
                p.setBackground(Color.WHITE);
                plotArea.add(p);
                plotArea.add(Box.createVerticalGlue());
                activeScorePanel(false);
            }
        }

        // seems to need both of these to avoid floating old crud left behind
        plotArea.revalidate();
        plotArea.repaint();
    }

    private void loadList(String filename) throws IOException {
        snpList = new LinkedList<String>();
        BufferedReader listReader = new BufferedReader(new FileReader(filename));
        String currentLine;
        while ((currentLine = listReader.readLine()) != null) {
            String[] bits = currentLine.split("\n");
            snpList.add(bits[0]);
        }
        listReader.close();

        setMarkerList(true);
        listScores.clear();

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            db.listNotify((LinkedList) snpList.clone());
            displaySNPindex = currentSNPindex;
            randomSNPButton.setEnabled(false);
            plotIntensitas(snpList.get(currentSNPindex));
            returnToListPosition.setEnabled(true);
            yesButton.requestFocusInWindow();
        } finally {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void loadScores(String filename) throws IOException {
        BufferedReader listReader = new BufferedReader(new FileReader(filename));
        pdfScores = new Hashtable<String, String>();
        String currentLine;
        while ((currentLine = listReader.readLine()) != null) {
            String[] bits = currentLine.split("\t");
            pdfScores.put(bits[0], bits[1]);
        }
    }

    private void dumpChanges() {
        if (plottedSNP != null) {
            JPanel jp = (JPanel) plotArea.getComponent(1);
            
            Vector<String> collections = db.getCollections();
            for (int i = 0; i < collections.size(); i++) {
                PlotPanel plotPanel = (PlotPanel) jp.getComponent(i);
                PlotData plotData = plotPanel.getPlotData();
                if (plotData.changed) {
                    db.commitGenotypeChange(collections.get(i), plottedSNP, plotData.getGenotypeChanges());
                }
            }
        }
    }

    private void fetchRecord(String name) {

        try {
            if (db.isRemote()) {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
            JPanel plotHolder = new JPanel();
            plotHolder.setBackground(Color.WHITE);
            plotArea.add(plotHolder);

            Vector<String> v = db.getCollections();
            Vector<PlotPanel> plots = new Vector<PlotPanel>();
            double maxdim = -100000;
            double mindim = 100000;

            for (String c : v) {
                PlotPanel pp = new PlotPanel(c, db.getRecord(name, c, getCoordSystem()), plotWidth, plotHeight);

                pp.refresh();
                if (pp.getMaxDim() > maxdim) {
                    maxdim = pp.getMaxDim();
                }
                if (pp.getMinDim() < mindim) {
                    mindim = pp.getMinDim();
                }
                plots.add(pp);
            }

            for (PlotPanel pp : plots) {
                pp.setDimensions(mindim, maxdim);
                plotHolder.add(pp);
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, ioe.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void refreshSaveMenu() {
        saveMenu.removeAll();
        Vector<String> v = db.getCollections();
        for (String c : v) {
            JMenuItem collectionEntry = new JMenuItem(c);
            collectionEntry.addActionListener(this);
            saveMenu.add(collectionEntry);
        }
    }

    public void setPlotHeight(int ph) {
        plotHeight = ph;
    }

    public void setPlotWidth(int pw) {
        plotWidth = pw;

    }

    public void setPlotAreaSize() {
        int windowWidth = 0;
        int windowHeight = 0;

        if (db != null) {
            if (db.getCollections().size() == 3) {
                windowWidth = (plotWidth * 3) + 2500;
                windowHeight = plotHeight + 150;
            } else if (db.getCollections().size() == 4) {
                windowWidth = (plotWidth * 2) + 250;
                windowHeight = (plotHeight * 2) + 150;
            } else {
                windowWidth = (plotWidth * 2) + 250;
                windowHeight = plotHeight + 150;
            }
        } else {
            windowWidth = (plotWidth * 2) + 250;
            windowHeight = plotHeight + 150;
        }
        this.setSize(new Dimension(windowWidth, windowHeight));
    }

    public void refreshPlot() {
        if (currentSNP != null) {
            plotIntensitas(currentSNP);
        }
    }

    private void save(String collection) throws FileNotFoundException, IOException {
        JFileChooser fileChooser = new JFileChooser();
        ExtensionFileFilter filter = new ExtensionFileFilter("BED Binary Files", ".bed");
        fileChooser.addChoosableFileFilter(filter);

        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getAbsolutePath();

            BEDFileChanger bfc = new BEDFileChanger(db.getMarkerData().collectionIndices.get(collection),
                    collection, db.getDisplayName(), db.samplesByCollection.get(collection).inds,
                    db.getMarkerData().snpsPerCollection.get(collection), db.getMarkerData().getMarkerTable(),
                    db.changesByCollection.get(collection), filename);
        }
    }
}
