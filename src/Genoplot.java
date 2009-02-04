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

    private DataDirectory db;

    private JTextField snpField;
    private JButton goBut;
    private JPanel plotArea;

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
        JMenuItem openDirectory = new JMenuItem("Open directory");
        openDirectory.addActionListener(this);
        fileMenu.add(openDirectory);
        JMenuItem openRemote = new JMenuItem("Connect to remote server");
        openRemote.addActionListener(this);
        fileMenu.add(openRemote);
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

        snpField = new JTextField(10);
        JPanel snpPanel = new JPanel();
        snpPanel.add(new JLabel("SNP:"));
        snpPanel.add(snpField);
        goBut = new JButton("Go");
        goBut.addActionListener(this);
        goBut.setEnabled(false);
        snpPanel.add(goBut);
        JButton randomSNPButton = new JButton("Random");
        randomSNPButton.addActionListener(this);
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

        JPanel scorePanel = new JPanel();
        JLabel approvelabel = new JLabel("Approve?");
        scorePanel.add(approvelabel);
        JButton fb = new JButton("Yes");
        fb.addActionListener(this);
        scorePanel.add(fb);
        JButton nb = new JButton("Maybe");
        nb.addActionListener(this);
        scorePanel.add(nb);
        JButton ab = new JButton("No");
        ab.addActionListener(this);
        scorePanel.add(ab);
        controlsPanel.add(scorePanel);
        //JScrollPane scrollzor = new JScrollPane(controlsPanel);
        //scrollzor.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        controlsPanel.setMaximumSize(new Dimension(2000,(int)controlsPanel.getPreferredSize().getHeight()));
        controlsPanel.setMinimumSize(new Dimension(10,(int)controlsPanel.getPreferredSize().getHeight()));

        plotArea = new JPanel();
        plotArea.setBorder(new LineBorder(Color.BLACK));
        plotArea.setBackground(Color.WHITE);
        plotArea.setPreferredSize(new Dimension(1000,350));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(plotArea);
        contentPanel.add(controlsPanel);

        this.setContentPane(contentPanel);
        this.pack();
        this.setVisible(true);

    }

    public void actionPerformed(ActionEvent actionEvent) {
        try{
            String command = actionEvent.getActionCommand();
            if (command.equals("Go")){
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
            }else if (command.equals("No")){
                output.println(snpList.get(index)+"\t-1");
                if (index < snpList.size() - 1){
                    index++;
                    plotIntensitas(snpList.get(index));
                }else{
                    plotIntensitas(null);
                    output.close();
                }
            }else if (command.equals("Maybe")){
                output.println(snpList.get(index)+"\t0");
                if (index < snpList.size() - 1){
                    index++;
                    plotIntensitas(snpList.get(index));
                }else{
                    plotIntensitas(null);
                    output.close();
                }
            }else if (command.equals("Yes")){
                output.println(snpList.get(index)+"\t1");
                if (index < snpList.size() - 1){
                    index++;
                    plotIntensitas(snpList.get(index));
                }else{
                    plotIntensitas(null);
                    output.close();
                }
            }else if (command.equals("Random")){
                plotIntensitas(db.getRandomSNP());                
            }else if (command.equals("Back")){
                viewedSNPs.pop(); //the guy who was just plotted
                if (!viewedSNPs.isEmpty()){
                    String snp = viewedSNPs.pop();
                    plotIntensitas(snp);
                }
            }else if (command.equals("Open directory")){
                //JFileChooser jfc = new JFileChooser(System.getProperty("user.dir"));
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                    db = new DataDirectory(jfc.getSelectedFile().getAbsolutePath());
                    goBut.setEnabled(true);
                }

            }else if (command.equals("Connect to remote server")){
                DataClient dc = new DataClient(this);
                if (dc.getConnectionStatus()){
                    goBut.setEnabled(true);
                    db = new DataDirectory(dc);
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
        plotArea.removeAll();
        plotArea.setLayout(new BoxLayout(plotArea,BoxLayout.Y_AXIS));

        if (name != null){
            plotArea.add(new JLabel(name));
            fetchRecord(name);
            viewedSNPs.push(name);
        }else{
            //I tried very hard to get the label right in the middle and failed because java layouts blow
            plotArea.add(Box.createVerticalGlue());
            JPanel p = new JPanel();
            p.add(new JLabel("End of list."));
            p.setBackground(Color.WHITE);
            plotArea.add(p);
            plotArea.add(Box.createVerticalGlue());
        }

        //seems to need both of these to avoid floating old crud left behind
        plotArea.revalidate();
        plotArea.repaint();
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

        try{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            db.listNotify((Vector)snpList.clone());
            plotIntensitas(snpList.get(0));
        }finally{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        index = 0;
        //listfwd.setEnabled(true);
        //listbck.setEnabled(true);
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
        }finally{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}
