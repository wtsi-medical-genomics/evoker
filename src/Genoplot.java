import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

import com.itextpdf.text.DocumentException;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.*;
import java.util.Vector;
import java.util.LinkedList;

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

    JFileChooser jfc;
    DataConnectionDialog dcd;
    MarkerListDialog mld;
    
    private JPanel scorePanel;
    private JButton yesButton;
    private JButton maybeButton;
    private JButton noButton;
    private JMenu fileMenu;
    private JMenuItem loadList;
    private JMenuItem loadExclude;
    private JCheckBoxMenuItem filterData;
    private JMenuItem saveAll;
    private JMenu historyMenu;
    private ButtonGroup snpGroup;
    private JMenuItem returnToListPosition;
   
    public static LoggingDialog ld;
    private JButton randomSNPButton;

	private PDFFile allPDF = null;
	private PDFFile yesPDF = null;
	private PDFFile maybePDF = null;
	private PDFFile noPDF = null;
	
    public static void main(String[] args){

        new Genoplot();

    }

    Genoplot(){
        super("Evoke...");

        jfc = new JFileChooser("user.dir");
        dcd = new DataConnectionDialog(this);
        mld = new MarkerListDialog(this);

        JMenuBar mb = new JMenuBar();

        int menumask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        fileMenu = new JMenu("File");
        JMenuItem openDirectory = new JMenuItem("Open directory");
        openDirectory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, menumask));
        openDirectory.addActionListener(this);
        fileMenu.add(openDirectory);
        JMenuItem openRemote = new JMenuItem("Connect to remote server");
        openRemote.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, menumask));
        openRemote.addActionListener(this);
        fileMenu.add(openRemote);
        loadList = new JMenuItem("Load marker list");
        loadList.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, menumask));
        loadList.addActionListener(this);
        loadList.setEnabled(false);
        fileMenu.add(loadList);
        loadExclude = new JMenuItem("Load sample exclude list");
        loadExclude.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, menumask));
        loadExclude.addActionListener(this);
        loadExclude.setEnabled(false);
        fileMenu.add(loadExclude);
        filterData = new JCheckBoxMenuItem("Filter samples");
        filterData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, menumask));
        filterData.addActionListener(this);
        filterData.setEnabled(false);
        fileMenu.add(filterData);
        saveAll = new JMenuItem("Save SNP Plots");
        saveAll.addActionListener(this);
        saveAll.setEnabled(false);
        fileMenu.add(saveAll);
        /*JMenuItem dumpImages = new JMenuItem("Dump PNGs of all SNPs in list");
        dumpImages.addActionListener(this);
        fileMenu.add(dumpImages);*/

        if (!(System.getProperty("os.name").toLowerCase().contains("mac"))){
            JMenuItem quitItem = new JMenuItem("Quit");
            quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, menumask));
            quitItem.addActionListener(this);
            fileMenu.add(quitItem);
        }

        mb.add(fileMenu);

        historyMenu = new JMenu("History");
        returnToListPosition = new JMenuItem("Return to current list position");
        snpGroup = new ButtonGroup();
        returnToListPosition.setEnabled(false);
        returnToListPosition.addActionListener(this);
        historyMenu.add(returnToListPosition);
        historyMenu.addSeparator();
        mb.add(historyMenu);

        JMenu logMenu = new JMenu("Log");
        JMenuItem showLogItem = new JMenuItem("Show Evoker log");
        showLogItem.addActionListener(this);
        logMenu.add(showLogItem);
        mb.add(logMenu);
               
        setJMenuBar(mb);

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

       /* JButton back = new JButton("Back");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(this);
        controlsPanel.add(back);

        JPanel listPanel = new JPanel();
        JButton lp = new JButton("Prev");
        lp.addActionListener(this);
        listPanel.add(lp);
        JButton ln = new JButton("Next");
        ln.addActionListener(this);
        listPanel.add(ln);
        controlsPanel.add(listPanel);*/

        controlsPanel.add(Box.createRigidArea(new Dimension(50,1)));

        scorePanel = new JPanel();
        scorePanel.add(new JLabel("Approve?"));

        yesButton = new JButton("Yes");
        scorePanel.registerKeyboardAction(this,"Yes",
                KeyStroke.getKeyStroke(KeyEvent.VK_Y,0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        yesButton.addActionListener(this);
        yesButton.setEnabled(false);
        scorePanel.add(yesButton);

        maybeButton = new JButton("Maybe");
        scorePanel.registerKeyboardAction(this,"Maybe",
                KeyStroke.getKeyStroke(KeyEvent.VK_M,0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        maybeButton.addActionListener(this);
        maybeButton.setEnabled(false);
        scorePanel.add(maybeButton);

        noButton = new JButton("No");
        scorePanel.registerKeyboardAction(this,"No",
                KeyStroke.getKeyStroke(KeyEvent.VK_N,0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        noButton.addActionListener(this);
        noButton.setEnabled(false);
        scorePanel.add(noButton);

        controlsPanel.add(scorePanel);

        controlsPanel.setMaximumSize(new Dimension(2000,(int)controlsPanel.getPreferredSize().getHeight()));
        controlsPanel.setMinimumSize(new Dimension(10,(int)controlsPanel.getPreferredSize().getHeight()));

        plotArea = new JPanel();
        plotArea.setPreferredSize(new Dimension(700,350));
        plotArea.setBorder(new LineBorder(Color.BLACK));
        plotArea.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(plotArea);
        contentPanel.add(controlsPanel);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
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
        try{
            String command = actionEvent.getActionCommand();
            if (command.equals("Go")){
                plotIntensitas(snpField.getText().trim());
            }else if (command.equals("No")){
                noButton.requestFocusInWindow();
                recordVerdict(-1);
            }else if (command.equals("Maybe")){
                maybeButton.requestFocusInWindow();
                recordVerdict(0);
            }else if (command.equals("Yes")){
                yesButton.requestFocusInWindow();
                recordVerdict(1);
            }else if (command.equals("Return to current list position")){
                plotIntensitas(currentSNPinList);
            }else if (command.equals("Random")){
                plotIntensitas(db.getRandomSNP());
            }else if (command.startsWith("PLOTSNP")){
                String[] bits = command.split("\\s");
                plotIntensitas(bits[1]);
            }else if (command.equals("Open directory")){
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                    try{
                        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        db = new DataDirectory(jfc.getSelectedFile().getAbsolutePath());
                        finishLoadingDataSource();
                    }finally{
                        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            }else if (command.equals("Connect to remote server")){
                dcd.pack();
                dcd.setVisible(true);
                DataClient dc = new DataClient(dcd);
                if (dc.getConnectionStatus()){
                    try{
                        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        db = new DataDirectory(dc);
                        finishLoadingDataSource();
                    }finally{
                        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            }else if (command.equals("Load marker list")){
                mld.pack();
            	mld.setVisible(true);
            	if(mld.success()){
            		try{
                        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        loadList(mld.getMarkerList());
                    	File outfile = checkOverwriteFile(new File(mld.getMarkerList()+".scores"));				
                        FileOutputStream fos = new FileOutputStream(outfile);
                        output = new PrintStream(fos);
                        if (mld.savePlots()) {
                    		openPDFs();
                    	}
                    }finally{
                        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
            	}
            	
            }else if (command.equals("Load sample exclude list")) {
            	jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            	if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            		// TODO: before setting as the new list check it is a valid not empty exclude file
            		db.setExcludeList(new QCFilterData(jfc.getSelectedFile().getAbsolutePath()));
            		db.setFilterState(true);
            		filterData.setEnabled(true);
            		filterData.setSelected(true);
            		Genoplot.ld.log("Loaded exclude file: " + jfc.getSelectedFile().getName());
            		if (currentSNP != null){
                		plotIntensitas(currentSNP);
                	}
                }
            }else if (command.equals("Filter samples")){
            	// turn filtering on/off
            	if (filterData.isSelected()) {
            		db.setFilterState(true);
            	} else {
            		db.setFilterState(false);
            	}
            	if (currentSNP != null){
            		plotIntensitas(currentSNP);
            	}
            }else if (command.equals("Dump PNGs of all SNPs in list")){
                dumpAll();
            }else if (command.equals("Save SNP Plots")){
            	File defaultFileName = new File(plottedSNP + ".png");
            	jfc.setSelectedFile(defaultFileName);
            	if(jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
            		File png;
            		if (!jfc.getSelectedFile().toString().endsWith(".png")){
            			png = new File(jfc.getSelectedFile().toString() + ".png");
            		} else{
            			png = jfc.getSelectedFile();
            		}
            		BufferedImage image = new BufferedImage(plotArea.getWidth(), plotArea.getHeight(), BufferedImage.TYPE_INT_RGB);
            		Graphics2D g2 = image.createGraphics();
                	plotArea.paint(g2);
                    g2.dispose();
                    try {
                      	ImageIO.write(image, "png", png);
                    }
                    catch(IOException ioe) {
                    	System.out.println(ioe.getMessage());
                    }
            	}                
            }else if (command.equals("Show Evoker log")){
                ld.setVisible(true);
            }else if (command.equals("Quit")){
                System.exit(0);
            }
        }catch (IOException ioe){
            JOptionPane.showMessageDialog(this,ioe.getMessage(),"File error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private File checkOverwriteFile(File file) {
 
    	if (file.exists()) {
        	int n = JOptionPane.showConfirmDialog(
        			null, 
        			"The file " + file.getName() + " already exists\n would you like to overwrite this file?",
        			"Overwrite file?",
        			JOptionPane.YES_NO_OPTION,
        			JOptionPane.QUESTION_MESSAGE );
        	// n 0 = yes 1 = no
        	if (n == 1) {		
        		if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
        			file = new File(jfc.getSelectedFile().getAbsolutePath());
                }
        	}
        }
    	return file;
	}
    
	private void dumpAll() throws IOException{
        for (String snp : snpList){
            //TODO: borken!
            /*for (int i = 0; i < collectionDropdown.getItemCount(); i++){
                collectionDropdown.setSelectedIndex(i);
                plotIntensitas(snp);
                //ppp.saveToFile(new File(snp+"-"+collectionDropdown.getSelectedItem()+".png"));
            } */
        }
    }

    private void recordVerdict(int v) throws DocumentException{
        if (currentSNPinList != null){
            output.println(currentSNPinList + "\t" + v);
            
            if (mld.savePlots()) {
            	if (mld.allPlots()) {
                	allPDF.writePanel2PDF(plotArea);
                }
                if (v == 1 && mld.yesPlots()) {
                    yesPDF.writePanel2PDF(plotArea);
                }
                if (v == 0 && mld.maybePlots()) {
                	maybePDF.writePanel2PDF(plotArea);
                }
                if (v == -1 && mld.noPlots()) {
                	noPDF.writePanel2PDF(plotArea);
                }
            }
            
            if (!snpList.isEmpty()){
                currentSNPinList = snpList.removeFirst();
                plotIntensitas(currentSNPinList);   
            }else{
                plotIntensitas("ENDLIST");
                currentSNPinList = null;
                output.close();
                yesButton.setEnabled(false);
                noButton.setEnabled(false);
                maybeButton.setEnabled(false);
                returnToListPosition.setEnabled(false);                
                if (mld.savePlots()) {
                	closeOpenPDFs();
                }
                
            }
        }
    }
    
    private void openPDFs() throws DocumentException, IOException {
		
    	if (mld.allPlots()) {
			allPDF = new PDFFile(checkOverwriteFile(new File(mld.getPdfDir() + "/all.pdf")));
    	}
    	if (mld.yesPlots()) {
    		yesPDF = new PDFFile(checkOverwriteFile(new File(mld.getPdfDir() + "/yes.pdf")));
    	}
    	if (mld.maybePlots()) {
    		maybePDF = new PDFFile(checkOverwriteFile(new File(mld.getPdfDir() + "/maybe.pdf")));
    	}
    	if (mld.noPlots()) {
    		noPDF = new PDFFile(checkOverwriteFile(new File(mld.getPdfDir() + "/no.pdf")));
    	}
	}
    
    private void closeOpenPDFs() {
    	if (mld.allPlots() && allPDF.isFileOpen()) {
        	allPDF.getDocument().close();
        }
        
    	if (mld.yesPlots() && yesPDF.isFileOpen()) {
        	yesPDF.getDocument().close();
        }
    	
    	if (mld.maybePlots() && maybePDF.isFileOpen()) {
        	maybePDF.getDocument().close();
        }
    	
    	if (mld.noPlots() && noPDF.isFileOpen()) {
        	noPDF.getDocument().close();
        }	
	}

	private void viewedSNP(String name){

        boolean alreadyHere = false;
        for (Component i : historyMenu.getMenuComponents()){
            if (i instanceof  JMenuItem){
                if (((JMenuItem)i).getText().equals(name)){
                    ((JRadioButtonMenuItem)i).setSelected(true);
                    alreadyHere = true;
                    break;
                }
            }
        }

        if (alreadyHere){
            if (currentSNPinList != null){
                if (snpGroup.getSelection().getActionCommand().equals("PLOTSNP "+currentSNPinList)){
                    yesButton.setEnabled(true);
                    noButton.setEnabled(true);
                    maybeButton.setEnabled(true);                    
                    scorePanel.setEnabled(true);
                    
                }else{
                    //we're viewing a SNP from the history, so we can't allow
                    //the user to take any action on a SNP list (if one exists) because
                    //we're not viewing the "active" SNP from the list
                	// disable the score panel to also stop key stroke events
                	yesButton.setEnabled(false);
                    noButton.setEnabled(false);
                    maybeButton.setEnabled(false);
                    scorePanel.setEnabled(false);
                    
                    //TODO: actually allow you to go back and change your mind?
                }
            }
        }else{
            //new guy
            JRadioButtonMenuItem snpItem = new JRadioButtonMenuItem(name);
            snpItem.setActionCommand("PLOTSNP "+name);
            snpItem.addActionListener(this);
            snpGroup.add(snpItem);
            snpItem.setSelected(true);
            historyMenu.add(snpItem,2);

            //only track last ten
            if (historyMenu.getMenuComponentCount() > 12){
                historyMenu.remove(12);
            }
        }

    }

    private void finishLoadingDataSource(){
        if (db != null){
            if (db.getCollections().size() == 3){
                this.setSize(new Dimension(1000,420));
            }else if (db.getCollections().size() == 4){
                this.setSize(new Dimension(700,750));
            }
            goButton.setEnabled(true);
            randomSNPButton.setEnabled(true);
            snpField.setEnabled(true);
            loadList.setEnabled(true);
            loadExclude.setEnabled(true);
            saveAll.setEnabled(true);
            while(historyMenu.getMenuComponentCount() > 2){
                historyMenu.remove(2);
            }
            if(db.qcList() != null){
            	// if a exclude file is loaded from the directory enable filtering
            	filterData.setEnabled(true);
            	filterData.setSelected(true);
            }

            this.setTitle("Evoke...["+db.getDisplayName()+"]");

            plotArea.removeAll();
            plotArea.repaint();
        }
    }

    private void plotIntensitas(String name){
    	plottedSNP = name;
    	plotArea.removeAll();
        plotArea.setLayout(new BoxLayout(plotArea,BoxLayout.Y_AXIS));
        if (name != null){
            if (!name.equals("ENDLIST")){
                currentSNP = name;
            	plotArea.add(new JLabel(name));
                fetchRecord(name);
                viewedSNP(name);
            }else{
                //I tried very hard to get the label right in the middle and failed because java layouts blow
                plotArea.add(Box.createVerticalGlue());
                JPanel p = new JPanel();
                p.add(new JLabel("End of list."));
                p.setBackground(Color.WHITE);
                plotArea.add(p);
                plotArea.add(Box.createVerticalGlue());
            }
        }

        //seems to need both of these to avoid floating old crud left behind
        plotArea.revalidate();
        plotArea.repaint();
    }

    private void loadList(String filename)throws IOException{
        snpList = new LinkedList<String>();
        BufferedReader listReader = new BufferedReader(new FileReader(filename));
        String currentLine;
        while ((currentLine = listReader.readLine()) != null){
            String[] bits = currentLine.split("\n");
            snpList.add(bits[0]);
        }
        listReader.close();

        try{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            db.listNotify((LinkedList)snpList.clone());
            currentSNPinList = snpList.removeFirst();
            plotIntensitas(currentSNPinList);
            returnToListPosition.setEnabled(true);
            yesButton.setEnabled(true);
            noButton.setEnabled(true);
            maybeButton.setEnabled(true);
            yesButton.requestFocusInWindow();
        }finally{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }


    }

    private void fetchRecord(String name){

        try{
            if (db.isRemote()){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
            JPanel plotHolder = new JPanel();
            plotHolder.setBackground(Color.WHITE);
            plotArea.add(plotHolder);


            Vector<String> v = db.getCollections();
            Vector<PlotPanel> plots = new Vector<PlotPanel>();
            double maxdim=-100000;
            double mindim=100000;
            for (String c : v){
                PlotPanel pp = new PlotPanel(c,db.getRecord(name, c));

                pp.refresh();
                if (pp.getMaxDim() > maxdim){
                    maxdim = pp.getMaxDim();
                }
                if (pp.getMinDim() < mindim){
                    mindim = pp.getMinDim();
                }
                plots.add(pp);
            }

            for (PlotPanel pp : plots){
                pp.setDimensions(mindim,maxdim);
                plotHolder.add(pp);
            }
        }catch (IOException ioe){
            JOptionPane.showMessageDialog(this,ioe.getMessage(),"File error",
                    JOptionPane.ERROR_MESSAGE);
        }finally{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}
