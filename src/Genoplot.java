import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.*;
import java.util.Vector;
import java.util.Stack;
import java.util.StringTokenizer;

public class Genoplot extends JFrame implements ActionListener {

    private BinaryFloatData bid;
    private BedfileData bed;
    private MarkerData md;
    private SampleData sd;
    private DataDirectory db;

    private JTextField snpField;
    private JButton goBut;
    private JButton randomSNPButton;
    private JPanel plotArea;

    private boolean dbMode;
    private PrintStream output;
    private Stack<String> viewedSNPs;
    private Vector<String> snpList;
    private int index;
    JFileChooser jfc;

    public static void main(String[] args){

        new Genoplot();

    }

    Genoplot(){
        super("Evoke...");

        jfc = new JFileChooser("user.dir");

        viewedSNPs = new Stack<String>();

        JMenuBar mb = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem openFile = new JMenuItem("Open file");
        openFile.addActionListener(this);
        fileMenu.add(openFile);
        JMenuItem openDirectory = new JMenuItem("Open directory");
        openDirectory.addActionListener(this);
        fileMenu.add(openDirectory);
        JMenuItem loadList = new JMenuItem("Load marker list");
        loadList.addActionListener(this);
        fileMenu.add(loadList);
        JMenuItem dumpImages = new JMenuItem("Dump PNGs of all SNPs in list");
        dumpImages.addActionListener(this);
        fileMenu.add(dumpImages);
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(this);
        fileMenu.add(quitItem);
        mb.add(fileMenu);


        setJMenuBar(mb);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel,BoxLayout.Y_AXIS));

        snpField = new JTextField(10);
        JPanel snpPanel = new JPanel();
        snpPanel.add(new JLabel("SNP:"));
        snpPanel.add(snpField);
        controlsPanel.add(snpPanel);

        JButton back = new JButton("Back");
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
        controlsPanel.add(listPanel);

        JPanel scorePanel = new JPanel();
        JButton fb = new JButton("-");
        fb.addActionListener(this);
        scorePanel.add(fb);
        JButton nb = new JButton("?");
        nb.addActionListener(this);
        scorePanel.add(nb);
        JButton ab = new JButton("+");
        ab.addActionListener(this);
        scorePanel.add(ab);
        controlsPanel.add(scorePanel);

        goBut = new JButton("Plot intensity");
        goBut.setAlignmentX(Component.CENTER_ALIGNMENT);
        goBut.addActionListener(this);
        goBut.setEnabled(false);
        controlsPanel.add(goBut);

        randomSNPButton = new JButton("I'm feeling lucky");
        randomSNPButton.addActionListener(this);
        randomSNPButton.setEnabled(false);
        randomSNPButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlsPanel.add(randomSNPButton);

        plotArea = new JPanel();
        plotArea.setBorder(new LineBorder(Color.BLACK));
        plotArea.setPreferredSize(new Dimension(920,320));

        JPanel content = new JPanel();
        content.add(controlsPanel);
        content.add(plotArea);

        this.setContentPane(content);
        this.pack();
        this.setVisible(true);
        try{
            DataClient dc = new DataClient(this);
        }catch (Exception e){

        }
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try{
            String command = actionEvent.getActionCommand();
            if (command.equals("Plot intensity")){
                plotIntensitas(snpField.getText());
            }else if (command.equals("Next")){
                if (index < snpList.size() - 1){
                    index++;
                    plotIntensitas(snpList.get(index));
                }
            }else if (command.equals("Prev")){
                if (index > 0){
                    index--;
                    plotIntensitas(snpList.get(index));
                }
            }else if (command.equals("-")){
                output.println(snpList.get(index)+"\t-1");
                if (index < snpList.size() - 1){
                    index++;
                    plotIntensitas(snpList.get(index));
                }else{
                    plotIntensitas("END");
                    output.close();
                }
            }else if (command.equals("?")){
                output.println(snpList.get(index)+"\t0");
                if (index < snpList.size() - 1){
                    index++;
                    plotIntensitas(snpList.get(index));
                }else{
                    plotIntensitas("END");
                    output.close();
                }
            }else if (command.equals("+")){
                output.println(snpList.get(index)+"\t1");
                if (index < snpList.size() - 1){
                    index++;
                    plotIntensitas(snpList.get(index));
                }else{
                    plotIntensitas("END");
                    output.close();
                }
            }else if (command.equals("I'm feeling lucky")){
                String snp;
                if (dbMode){
                    snp = db.getRandomSNP();
                }else{
                    snp = md.getSNPs().get((int)(Math.random()*md.getSNPs().size()));
                }
                plotIntensitas(snp);
            }else if (command.equals("Back")){
                viewedSNPs.pop(); //the guy who was just plotted
                if (!viewedSNPs.isEmpty()){
                    String snp = viewedSNPs.pop();
                    plotIntensitas(snp);
                }
            }else if (command.equals("Open file")){
                //JFileChooser jfc = new JFileChooser(System.getProperty("user.dir"));
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                    if (loadData(jfc.getSelectedFile().getAbsolutePath())){
                        goBut.setEnabled(true);
                        randomSNPButton.setEnabled(true);
                        //collectionDropdown.setEnabled(false);
                        dbMode = false;
                    }
                }
            }else if (command.equals("Open directory")){
                //JFileChooser jfc = new JFileChooser(System.getProperty("user.dir"));
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                    db = new DataDirectory(jfc.getSelectedFile().getAbsolutePath());
                    goBut.setEnabled(true);
                    randomSNPButton.setEnabled(true);
                    dbMode = true;
                }
            }else if (command.equals("Load marker list")){
                //JFileChooser jfc = new JFileChooser(System.getProperty("user.dir"));
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                    loadList(jfc.getSelectedFile().getAbsolutePath());
                }
                FileOutputStream fos = new FileOutputStream(jfc.getSelectedFile().getAbsolutePath()+".scores");
                output = new PrintStream(fos);
            }else if (command.equals("Dump PNGs of all SNPs in list")){
                dumpAll();
            }else if (command.equals("Quit")){
                System.exit(0);
            }
        }catch (IOException ioe){
            JOptionPane.showMessageDialog(this,ioe.getMessage(),"File error",
                    JOptionPane.ERROR_MESSAGE);
        }
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

    private void plotIntensitas(String name){
        //dc.writeToServer(name);
        if (dbMode){
            fetchRecord(name,null);
        }else{
            fetchRecord(name);
        }
        viewedSNPs.push(name);
    }

    private void loadList(String filename)throws IOException{
        snpList = new Vector<String>();
        BufferedReader listReader = new BufferedReader(new FileReader(filename));
        String currentLine;
        StringTokenizer st;
        while ((currentLine = listReader.readLine()) != null){
            st = new StringTokenizer(currentLine);
            snpList.add(st.nextToken());
        }
        listReader.close();
        index = 0;
        plotIntensitas(snpList.get(0));
        //listfwd.setEnabled(true);
        //listbck.setEnabled(true);
    }

    private boolean loadData(String filename){
        //see if the filename selected ends with one of our favorite endings
        if (filename.endsWith(".bnt") || filename.endsWith(".bed") ||filename.endsWith(".bpr")
                || filename.endsWith(".bim") || filename.endsWith(".fam")){
            String stem = filename.substring(0,filename.length()-4);
            try{
                md = new MarkerData(stem+".bim");
                sd = new SampleData(stem+".fam");
                bid = new BinaryFloatData(stem + ".bnt",sd,md,2);
                bed = new BedfileData(stem+".bed",sd,md);
                return true;
            }catch (IOException ioe){
                JOptionPane.showMessageDialog(this,ioe.getMessage(),"File error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }else{
            JOptionPane.showMessageDialog(this, filename + "\ndoes not have correct extension.",
                    "File error",JOptionPane.ERROR_MESSAGE);
        }

        return false;
    }

    private void fetchRecord(String name){
        PlotData pd = new PlotData(null,bid.getRecord(name,0),bed.getRecord(name,0),sd);
        PlotPanel ppp = new PlotPanel(name,"a","b",pd);

        /*probCallFrame.setContentPane(ppp);
        probCallFrame.pack();
        probCallFrame.setVisible(true);*/
    }
    
    private void fetchRecord(String name, String collection){
        plotArea.removeAll();
        plotArea.setLayout(new BoxLayout(plotArea,BoxLayout.Y_AXIS));
        plotArea.add(new JLabel(name));

        JPanel plotHolder = new JPanel();
        plotArea.add(plotHolder);


        Vector<String> v = db.getCollections();
        Vector<PlotPanel> plots = new Vector<PlotPanel>();
        double maxdim=0;
        for (String c : v){
            PlotPanel pp = new PlotPanel(c,"a","b",
                    db.getRecord(name, c));

            pp.refresh();
            if (pp.getMaxDim() > maxdim){
                maxdim = pp.getMaxDim();
            }
            plots.add(pp);
        }

        for (PlotPanel pp : plots){
            pp.setMaxDim(maxdim);
            plotHolder.add(pp);
        }
        plotArea.revalidate();
    }
}
