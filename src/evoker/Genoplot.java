package evoker;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.Box;

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
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.jfree.ui.ExtensionFileFilter;

import evoker.Types.*;

public class Genoplot extends JFrame implements ActionListener {

    private DataDirectory db;
    /** Mode, determining the way, the Diagram responses to mouse gestures.  
     * <code>true</code> means lasso select, <code>false</code> means zooming in.*/
    boolean MOUSE_MODE = false;
    String plottedSNP = null;
    private JTextField snpField;
    private JButton goButton;
    private JPanel plotArea;
    private File output;
    private LinkedList<String> snpList;
    private String currentSNPinList;
    private String currentSNP = null;
    private int currentSNPindex;
    private int displaySNPindex;
    private boolean backSNP = false;
    private boolean historySNP = false;
    private boolean markerList = false;
    private boolean endOfList;
    private boolean saveBedBimFamFiles = false;
    private HashMap<String, Integer> listScores = new HashMap<String, Integer>();
    private Hashtable<String, String> pdfScores;
    JFileChooser jfc;
    OpenDirectoryDialog odd;
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
    private JMenuItem saveAllPlots;
    private JMenuItem saveAllBeds;
    private JMenuItem exportPDF;
    private JMenu viewMenu;
    private JMenu coordinateMenu;
    private JMenuItem viewPolar;
    private JMenuItem viewCart;
    private JMenuItem viewUKBiobank;
	private JMenu sortMenu;
	private JMenuItem collectionBatchAscend;
	private JMenuItem collectionBatchDescend;
	private JMenuItem mafAscend;
	private JMenuItem mafDescend;
	private JMenuItem gpcAscend;
	private JMenuItem gpcDescend;
	private JMenuItem hwePValueAscend;
	private JMenuItem hwePValueDescend;
	private JMenu historyMenu;
    private ButtonGroup snpGroup;
    private JMenuItem returnToListPosition;
    private JMenuItem showLogItem;
    private JMenu settingsMenu;
    private JMenuItem plotSize;
    private JMenuItem longStats;
    private JMenuItem saveBedBimFam;
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
    private CoordinateSystem coordSystem = CoordinateSystem.CART;
    private int plotHeight = 300;
    private int plotWidth = 300;

    public enum MouseMode {LASSO,ZOOM};
    private MouseMode mouseMode;

    private FileFormat fileFormat;
    private String UKBIOBANK = "UKBIOBANK";

    private SortBy sortBy = SortBy.COLLECTIONBATCH_ASCEND;

    public static void main(String[] args) {

        new Genoplot();

    }

    Genoplot() {
        super("Evoke...");
        jfc = new JFileChooser("user.dir");
        odd = new evoker.OpenDirectoryDialog(this);
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

        saveAllBeds = new JMenuItem("Save All BEDs");
        saveAllBeds.setEnabled(false);
        saveAllBeds.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                menumask));
        saveAllBeds.addActionListener(this);
        fileMenu.add(saveAllBeds);
        
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
        saveAllPlots = new JMenuItem("Save SNP Plots");
        saveAllPlots.addActionListener(this);
        saveAllPlots.setEnabled(false);
        toolsMenu.add(saveAllPlots);
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



        coordinateMenu = new JMenu("Coordinates");

        ButtonGroup coordGroup = new ButtonGroup();
        
        viewCart = new JCheckBoxMenuItem("Cartesian coordinates");
        viewCart.addActionListener(this);
		viewCart.setEnabled(false);
		coordGroup.add(viewCart);
		coordinateMenu.add(viewCart);
        
        viewPolar = new JCheckBoxMenuItem("Polar coordinates");
        viewPolar.addActionListener(this);
        viewPolar.setEnabled(false);
		coordGroup.add(viewPolar);
		coordinateMenu.add(viewPolar);
        
        viewUKBiobank = new JCheckBoxMenuItem("Affymetrix Axiom UK Biobank");
        viewUKBiobank.addActionListener(this);
        viewUKBiobank.setEnabled(false);
		coordGroup.add(viewUKBiobank);
		coordinateMenu.add(viewUKBiobank);

		sortMenu = new JMenu("Sort");
		ButtonGroup sortGroup = new ButtonGroup();

		collectionBatchAscend = new JCheckBoxMenuItem("↓ Collection/Batch");
		collectionBatchAscend.addActionListener(this);
		collectionBatchAscend.setEnabled(false);
		collectionBatchAscend.setSelected(true);
		sortGroup.add(collectionBatchAscend);
		sortMenu.add(collectionBatchAscend);

		collectionBatchDescend = new JCheckBoxMenuItem("↑ Collection/Batch");
		collectionBatchDescend.addActionListener(this);
		collectionBatchDescend.setEnabled(false);
		sortGroup.add(collectionBatchDescend);
		sortMenu.add(collectionBatchDescend);

		mafAscend  = new JCheckBoxMenuItem("↓ MAF");
		mafAscend .addActionListener(this);
		mafAscend .setEnabled(false);
		sortGroup.add(mafAscend );
		sortMenu.add(mafAscend );

		mafDescend = new JCheckBoxMenuItem("↑ MAF");
		mafDescend.addActionListener(this);
		mafDescend.setEnabled(false);
		sortGroup.add(mafDescend);
		sortMenu.add(mafDescend);

		gpcAscend = new JCheckBoxMenuItem("↓ GPC");
		gpcAscend.addActionListener(this);
		gpcAscend.setEnabled(false);
		sortGroup.add(gpcAscend);
		sortMenu.add(gpcAscend);

		gpcDescend = new JCheckBoxMenuItem("↑ GPC");
		gpcDescend.addActionListener(this);
		gpcDescend.setEnabled(false);
		sortGroup.add(gpcDescend);
		sortMenu.add(gpcDescend);

		hwePValueAscend = new JCheckBoxMenuItem("↓ HWE p-value");
		hwePValueAscend.addActionListener(this);
		hwePValueAscend.setEnabled(false);
		sortGroup.add(hwePValueAscend);
		sortMenu.add(hwePValueAscend);

		hwePValueDescend = new JCheckBoxMenuItem("↑ HWE p-value");
		hwePValueDescend.addActionListener(this);
		hwePValueDescend.setEnabled(false);
		sortGroup.add(hwePValueDescend);
		sortMenu.add(hwePValueDescend);

		viewMenu = new JMenu("View");
		viewMenu.add(coordinateMenu);
		viewMenu.add(sortMenu);
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

        settingsMenu = new JMenu("Settings");
        plotSize = new JMenuItem("Plot settings");
        plotSize.addActionListener(this);
        plotSize.setEnabled(true);
        settingsMenu.add(plotSize);
        longStats = new JCheckBoxMenuItem("Long Stats");
        longStats.addActionListener(this);
        longStats.setSelected(true);
        longStats.setEnabled(true);
        saveBedBimFam = new JCheckBoxMenuItem("Output .bed/.bim/.fam");
        saveBedBimFam.addActionListener(this);
        saveBedBimFam.setSelected(false);
        saveBedBimFam.setEnabled(true);
        
        settingsMenu.add(longStats);
        settingsMenu.add(saveBedBimFam);
        
        mb.add(settingsMenu);

		setJMenuBar(mb);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

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

//        plotArea = new JPanel(new GridLayout());
		plotArea = new JPanel();
        plotArea.setPreferredSize(new Dimension(700, 350));

        plotArea.setBorder(new LineBorder(Color.BLACK));
        plotArea.setBackground(Color.WHITE);

        mouseMode = MouseMode.ZOOM;

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
		String goCommand                     = "Go";
		String noCommand                     = "No";
		String maybeCommand                  = "Maybe";
		String yesCommand                    = "Yes";
		String backCommand                   = "Back";
		String currentCommand                = "Return to current list position";
		String randomCommand                 = "Random";
		String plotCommand                   = "PLOTSNP";
		String openDirCommand                = "Open directory";
		String openRemoteCommand             = "Connect to remote server";
		String markerCommand                 = "Load marker list";
		String excludeCommand                = "Load sample exclude list";
		String filterCommand                 = "Filter samples";
		String savePlotsCommand              = "Save SNP Plots";
		String generatePlotsCommand          = "Generate PDF from scores";
		String cartesianCommand              = "Cartesian coordinates";
		String ukBiobankCommand              = "Affymetrix Axiom UK Biobank";
		String polarCommand                  = "Polar coordinates";
		String collectionBatchAscendCommand  = "↓ Collection/Batch";
		String collectionBatchDescendCommand = "↑ Collection/Batch";
		String mafAscendCommand              = "↓ MAF";
		String mafDescendCommand             = "↑ MAF";
		String gpcAscendCommand              = "↓ GPC";
		String gpcDescendCommand             = "↑ GPC";
		String hwePValueAscendCommand        = "↓ HWE p-value";
		String hwePValueDescendCommand       = "↑ HWE p-value";
		String plotSettingsCommand           = "Plot settings";
		String longStatsCommand              = "Long Stats";
		String saveBedBimFamCommand          = "Output .bed/.bim/.fam";
		String showLogCommand                = "Show Evoker log";
		String quitCommand                   = "Quit";
		String saveBedsCommand               = "Save All BEDs";
                
        try {
            String command = actionEvent.getActionCommand();
            if (command.equals(goCommand)) {
                if (markerListLoaded()) {
                    if (snpList.contains(snpField.getText().trim())) {
                        setHistorySNP(true);
                        displaySNPindex = snpList.indexOf(snpField.getText().trim());
                    }
                }
                plotIntensitas(snpField.getText().trim());
            } else if (command.equals(noCommand)) {
                noButton.requestFocusInWindow();
                recordVerdict(-1);
            } else if (command.equals(maybeCommand)) {
                maybeButton.requestFocusInWindow();
                recordVerdict(0);
            } else if (command.equals(yesCommand)) {
                yesButton.requestFocusInWindow();
                recordVerdict(1);
            } else if (command.equals(backCommand)) {
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
            } else if (command.equals(currentCommand)) {
                plotIntensitas(snpList.get(currentSNPindex));
            } else if (command.equals(randomCommand)) {
                plotIntensitas(db.getRandomSNP());
            } else if (command.startsWith(plotCommand)) {
                String[] bits = command.split("\\s");
                if (markerListLoaded()) {
                    if (snpList.contains(bits[1])) {
                        setHistorySNP(true);
                        displaySNPindex = snpList.indexOf(bits[1]);
                    }
                }
                plotIntensitas(bits[1]);
            } else if (command.equals(openDirCommand)) {

                odd.pack();
                odd.setVisible(true);
                if (odd.success()) {
                    String directory = odd.getDirectory();
                    fileFormat = odd.getFileFormat();
                    try {
                        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        if (!new File(directory).exists()) {
                            throw new IOException("Directory does not exist!");
                        }
                        setStandardCoordSystem(fileFormat);
                        db = new DataDirectory(directory, fileFormat);
                        plottedSNP = null;
                        finishLoadingDataSource();
                        refreshSaveMenu();
                    } finally {
                        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            } else if (command.equals(openRemoteCommand)) {

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
								fileFormat = dcd.getFileFormat();
								setStandardCoordSystem(fileFormat);
                                db = new DataDirectory(dc, fileFormat);
                                finishLoadingDataSource();
                                refreshSaveMenu();
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

            } else if (command.equals(markerCommand)) {
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
                            output = outfile;
                        } else {
                            throw new IOException();
                        }
                    } catch (IOException ioe) {
                        throw new IOException("No score file selected");
                    }
                }
            } else if (command.equals(excludeCommand)) {
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
            } else if (command.equals(filterCommand)) {
                // turn filtering on/off
                if (filterData.isSelected()) {
                    db.setFilterState(true);
                } else {
                    db.setFilterState(false);
                }
                if (currentSNP != null) {
                    plotIntensitas(currentSNP);
                }
            } else if (command.equals(savePlotsCommand)) {
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
            } else if (command.equals(generatePlotsCommand)) {
                pdfd.pack();
                pdfd.setVisible(true);
                if (pdfd.success()) {
                    generatePDFs();
                }
            } else if (command.equals(cartesianCommand)) {
                setCoordSystem(CoordinateSystem.CART);
                refreshPlot();
            } else if (command.equals(polarCommand)) {
                setCoordSystem(CoordinateSystem.POLAR);
                refreshPlot();
            } else if (command.equals(ukBiobankCommand)) {
                setCoordSystem(CoordinateSystem.UKBIOBANK);
                refreshPlot();
            } else if (command.equals(plotSettingsCommand)) {
                sd.pack();
                sd.setVisible(true);
			} else if (command.equals(collectionBatchAscendCommand)) {
				setSort(SortBy.COLLECTIONBATCH_ASCEND);
				refreshPlot();
			} else if (command.equals(collectionBatchDescendCommand)) {
				setSort(SortBy.COLLECTIONBATCH_DESCEND);
				refreshPlot();
			} else if (command.equals(mafAscendCommand)) {
				setSort(SortBy.MAF_ASCEND);
				refreshPlot();
			} else if (command.equals(mafDescendCommand)) {
				setSort(SortBy.MAF_DESCEND);
				refreshPlot();
			} else if (command.equals(gpcAscendCommand)) {
				setSort(SortBy.GPC_ASCEND);
				refreshPlot();
			} else if (command.equals(gpcDescendCommand)) {
				setSort(SortBy.GPC_DESCEND);
				refreshPlot();
			} else if (command.equals(hwePValueAscendCommand)) {
				setSort(SortBy.HWEPVAL_ASCEND);
				refreshPlot();
			} else if (command.equals(hwePValueDescendCommand)) {
				setSort(SortBy.HWEPVAL_DESCEND);
				refreshPlot();
            } else if (command.equals(longStatsCommand)) {
                refreshPlot();
            } else if (command.equals(saveBedBimFamCommand)) {
                saveBedBimFamFiles = !saveBedBimFamFiles;
            } else if (command.equals(showLogCommand)) {
                ld.setVisible(true);
            } else if (command.equals(quitCommand)) {
                System.exit(0);
            } else if (command.equals(saveBedsCommand)) {
                ArrayList<String> collections = db.getCollections();
                for (String s : collections) {
                    dumpChanges();
                    if (db.changesByCollection.containsKey(s)) {
                        save(s,db.getDataPath()+s+"mod");
                    }                        
                }
            } else {
                ArrayList<String> collections = db.getCollections();
                for (String s : collections) {
                    if (command.equals(s)) {
                        dumpChanges();
                        if (!db.changesByCollection.containsKey(s)) {
                            throw new IOException("Not made any changes to that collection!");
                        }
                        save(s,null);
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
        saveAllPlots.setEnabled(false);
        saveAllBeds.setEnabled(false);
        exportPDF.setEnabled(false);

        viewCart.setEnabled(false);
        viewPolar.setEnabled(false);
        viewUKBiobank.setEnabled(false);

		collectionBatchAscend.setEnabled(false);
		collectionBatchDescend.setEnabled(false);
		mafAscend.setEnabled(false);
		mafDescend.setEnabled(false);
		gpcAscend.setEnabled(false);
		gpcDescend.setEnabled(false);
		hwePValueAscend.setEnabled(false);
		hwePValueDescend.setEnabled(false);

        returnToListPosition.setEnabled(false);
        // clear history items
        snpGroup.clearSelection();
    }

    private void generatePDFs() throws DocumentException, IOException {

        loadScores(pdfd.getscoresFile());

        pm = new ProgressMonitor(this.getContentPane(), "Exporting plots to PDF", null, 0, pdfScores.size());

        PDFWorker pdfWorker = new PDFWorker(this);
        pdfWorker.execute();
    }

    public void printPDFsInBackground() throws IOException{

        Enumeration<String> keys = pdfScores.keys();
        try {
            openPDFs();
        } catch (DocumentException/*|IOException ex*/ ex) {
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

                ArrayList<Image> images = new ArrayList<Image>();
                ArrayList<String> stats = new ArrayList<String>();

                // loop through all the collections to get the average maf
                double totalMaf = 0;
                int totalSamples = 0;
                for (String collection : db.getCollections()) {
                    PlotData pd = db.getRecord(snp, collection, coordSystem);
                    pd.computeSummary();
                    int sampleNum = db.samplesByCollection.get(collection).getNumInds();
                    totalMaf += pd.getMaf() * sampleNum;
                    totalSamples += sampleNum;
                }

                for (String collection : db.getCollections()) {
                    PlotPanel pp = new PlotPanel(this, collection,
                            db.getRecord(snp, collection, coordSystem),
                            plotWidth, plotHeight, longStats.isSelected(), totalMaf, totalSamples);
                    pp.refresh();
                    if (pp.hasData()) {
                        pp.setDimensions();
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
    }



    private void printScores() throws FileNotFoundException {
        ListIterator<String> snpi = snpList.listIterator();
        PrintStream ps = new PrintStream(new FileOutputStream(output));
        
        while (snpi.hasNext()) {
            String snp = (String) snpi.next();
            if (listScores.get(snp) != null) {              
                ps.println(snp + "\t" + listScores.get(snp));
            }            
        }        
        ps.close();
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

    private void setCoordSystem(CoordinateSystem  coordSystem) {
        this.coordSystem = coordSystem;
        switch (coordSystem) {
            case CART:
                viewCart.setSelected(true);
                break;
            case POLAR:
                viewPolar.setSelected(true);
                break;
            case UKBIOBANK:
                viewUKBiobank.setSelected(true);
                break;
        }
    }

    private void setStandardCoordSystem(FileFormat fileFormat) {
        switch (fileFormat) {
            case UKBIOBANK:
                setCoordSystem(CoordinateSystem.UKBIOBANK);
                break;
            default:
                setCoordSystem(CoordinateSystem.CART);
                break;
        }
    }

//    private CoordinateSystem getCoordSystem() {
//        return coordSystem;
//    }

    protected MouseMode getMouseMode(){
        return mouseMode;
    }
    
    protected void setMouseMode(MouseMode mm){
        this.mouseMode=mm;
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

    private void recordVerdict(int v) throws DocumentException, FileNotFoundException {
        if (isBackSNP()) {
            listScores.put(snpList.get(displaySNPindex), v);
            printScores();
            setBackSNP(false);
            if (displaySNPindex == 0) {
                backButton.setEnabled(true);
            }
            displaySNPindex = currentSNPindex;
            plotIntensitas(snpList.get(currentSNPindex));
        } else if (isHistorySNP()) {
            listScores.put(snpList.get(displaySNPindex), v);
            printScores();
            setHistorySNP(false);
            if (displaySNPindex == 0) {
                backButton.setEnabled(true);
            }
            displaySNPindex = currentSNPindex;
            plotIntensitas(snpList.get(currentSNPindex));
        } else {
            listScores.put(snpList.get(currentSNPindex), v);
            printScores();
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
                        "Would you like to finish scoring the current list?",
                        "Finish scoring list?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                // n 0 = yes 1 = no
                if (n == 0) {                    
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
            saveAllPlots.setEnabled(true);
            viewCart.setEnabled(true);
            viewPolar.setEnabled(true);
            viewUKBiobank.setEnabled(true);

			collectionBatchAscend.setEnabled(true);
			collectionBatchDescend.setEnabled(true);
			mafAscend.setEnabled(true);
			mafDescend.setEnabled(true);
			gpcAscend.setEnabled(true);
			gpcDescend.setEnabled(true);
			hwePValueAscend.setEnabled(true);
			hwePValueDescend.setEnabled(true);

            exportPDF.setEnabled(true);
                        
            // if the data source is local then enable the saving of manual calls
            if (db.isLocal()) {
                saveMenu.setEnabled(true);
                saveAllBeds.setEnabled(true);
            } 
            
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
        plotArea.setLayout(new BoxLayout(plotArea, BoxLayout.PAGE_AXIS));
        if (name != null) {
            if (!name.equals("ENDLIST")) {
                currentSNP = name;
				JLabel jLabelName = new JLabel(name);
				jLabelName.setAlignmentX(Component.CENTER_ALIGNMENT);
				plotArea.add(jLabelName);
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
                endOfList = false;
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
                endOfList = true;
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
            JScrollPane jsp = (JScrollPane) plotArea.getComponent(1);
            JPanel jp = (JPanel) jsp.getViewport().getView();
            // Exception in thread "AWT-EventQueue-0" java.lang.ClassCastException: javax.swing.JScrollPane$ScrollBar cannot be cast to javax.swing.JPanel
            // at evoker.Genoplot.dumpChanges(Genoplot.java:1109)
            
            //check if this is the end of a marker list
            // if it is there will be no changes to commit
            if (!endOfList) {
                ArrayList<String> collections = db.getCollections();
                for (int i = 0; i < collections.size(); i++) {
                    PlotPanel plotPanel = (PlotPanel) jp.getComponent(i);
                    PlotData plotData = plotPanel.getPlotData();
                    if (plotData.changed) {
                        db.commitGenotypeChange(collections.get(i), plottedSNP, plotData.getGenotypeChanges());
                    }
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
            plotHolder.setLayout(new WrapLayout());
            JScrollPane scrollPane = new JScrollPane(plotHolder);

            plotArea.add(scrollPane);

            ArrayList<PlotPanel> plots = new ArrayList<PlotPanel>();
            double maxdim = -100000;
            double mindim = 100000;

            // loop through all the collections to get the average maf
            double totalMaf = 0;
            int totalSamples = 0;

            // UKB is stored as a single collection within Evoker but in fact the ~150 batches
            // are encoded with the same fam file so these need to be pulled out one at a time.
            if (fileFormat == FileFormat.UKBIOBANK) {
				plots = db.getUKBPlotPanels(this, name, coordSystem, plotHeight, plotWidth, longStats.isSelected());
            } else { // Default or Oxford format
                ArrayList<String> v = db.getCollections();
                for (String collection : v) {
                    PlotData pd = db.getRecord(name, collection, coordSystem);
                    if (pd.generatePoints() != null) {
                    	// DR This has already been called in generatePoints
                        // pd.computeSummary();
                        int sampleNum = db.samplesByCollection.get(collection).getNumInds();
                        totalMaf += pd.getMaf() * sampleNum;
                        totalSamples += sampleNum;
                    }
                }

                for (String c : v) {
                    PlotData pd = db.getRecord(name, c, coordSystem);
                    PlotPanel pp = new PlotPanel(this, c, pd, plotHeight, plotWidth, longStats.isSelected(), totalMaf, totalSamples);
                    pp.refresh();
                    if (pp.getMaxDim() > maxdim) { maxdim = pp.getMaxDim(); }
                    if (pp.getMinDim() < mindim) { mindim = pp.getMinDim(); }
                    plots.add(pp);
                }

				for (PlotPanel pp : plots) {
					pp.setDimensions(mindim, maxdim);
				}
			}



			// Sort on collection/batch
			switch (sortBy) {
				case COLLECTIONBATCH_ASCEND:
					Collections.sort(plots, new NaturalOrderComparator());
//					plots.sort((o1, o2) -> o1.getTitle().compareTo(o2.getTitle()));
					break;
				case COLLECTIONBATCH_DESCEND:
					Collections.sort(plots, new NaturalOrderComparator());
					Collections.reverse(plots);
					break;
				case MAF_ASCEND:
					plots.sort((o1, o2) -> Double.compare(o1.getPlotData().getMaf(), o2.getPlotData().getMaf()));
					break;
				case MAF_DESCEND:
					plots.sort((o1, o2) -> Double.compare(o2.getPlotData().getMaf(), o1.getPlotData().getMaf()));
					break;
				case GPC_ASCEND:
					plots.sort((o1, o2) -> Double.compare(o1.getPlotData().getGenopc(), o2.getPlotData().getGenopc()));
					break;
				case GPC_DESCEND:
					plots.sort((o1, o2) -> Double.compare(o2.getPlotData().getGenopc(), o1.getPlotData().getGenopc()));
					break;
				case HWEPVAL_ASCEND:
					plots.sort((o1, o2) -> Double.compare(o1.getPlotData().getHwpval(), o2.getPlotData().getHwpval()));
					break;
				case HWEPVAL_DESCEND:
					plots.sort((o1, o2) -> Double.compare(o2.getPlotData().getHwpval(), o1.getPlotData().getHwpval()));
					break;
				default:
					ld.log("Unknown sort method encountered: " + sortBy);
					break;
			}

			for (PlotPanel pp : plots) {
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
        ArrayList<String> v = db.getCollections();
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
                windowWidth = (plotWidth * 3) + 250;
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

    private void save(String collection, String filename) throws FileNotFoundException, IOException {        
        JFileChooser fileChooser = new JFileChooser();
        ExtensionFileFilter filter = new ExtensionFileFilter("BED Binary Files", ".bed");
        fileChooser.addChoosableFileFilter(filter);

        if (filename == null){
            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                filename = fileChooser.getSelectedFile().getAbsolutePath();
            }else{
                return;
            }
        }
        //TP CHANGED THIS
        BEDFileChanger bfc = new BEDFileChanger(db.getMarkerData().collectionIndices.get(collection),
                    collection, db.getDisplayName(), db.samplesByCollection.get(collection).inds,
                    db.getMarkerData().snpsPerCollection.get(collection), db.getMarkerData().getMarkerTable(),
                    db.changesByCollection.get(collection), filename, db, saveBedBimFamFiles);
        
    }

	private void setSort(SortBy sortBy) { this.sortBy = sortBy;	}

}
