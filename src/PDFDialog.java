import javax.swing.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class PDFDialog extends JDialog implements ActionListener {
	
	private boolean success = false;
    private String scoresFile;
    private String pdfDir;
    private JTextField scoresFileField;
    private JTextField pdfDirField;
    private JButton pdfBrowseButton;
    private JLabel pdfDirLabel;
    private JCheckBox allPlotsButton;
    private JCheckBox yesPlotsButton;
    private JCheckBox maybePlotsButton;
    private JCheckBox noPlotsButton;
    
    private JFileChooser jfc;

    public PDFDialog(JFrame parent){
        super(parent,"Generate PDF from Scores",true);
        
        jfc = new JFileChooser("user.dir");
        
        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));

        JPanel scoresFilePanel = new JPanel();
        scoresFilePanel.add(new JLabel("Scores file: "));
        scoresFileField = new JTextField(20);
        scoresFilePanel.add(scoresFileField);
        JButton scoresBrowseButton = new JButton("Browse");
        scoresBrowseButton.addActionListener(this);
        scoresFilePanel.add(scoresBrowseButton);
        contents.add(scoresFilePanel);
        
        contents.add(new JPanel());
        
        JPanel savePlotsPanel = new JPanel();
        savePlotsPanel.setLayout(new BoxLayout(savePlotsPanel,BoxLayout.PAGE_AXIS));
        allPlotsButton = new JCheckBox("Save all plots");
        allPlotsButton.setSelected(false);
        savePlotsPanel.add(allPlotsButton);
        yesPlotsButton = new JCheckBox("Save all Yes plots");
        yesPlotsButton.setSelected(false);
        savePlotsPanel.add(yesPlotsButton);
        maybePlotsButton = new JCheckBox("Save all Maybe plots");
        maybePlotsButton.setSelected(false);
        savePlotsPanel.add(maybePlotsButton);
        noPlotsButton = new JCheckBox("Save all No plots");
        noPlotsButton.setSelected(false);
        savePlotsPanel.add(noPlotsButton);
        
        contents.add(savePlotsPanel);
        
        JPanel pdfDirPanel = new JPanel();
        pdfDirLabel = new JLabel("Destination directory for PDFs: ");
        pdfDirPanel.add(pdfDirLabel);
        pdfDirField = new JTextField(20);
        pdfDirPanel.add(pdfDirField);
        pdfBrowseButton = new JButton("Save to");
        pdfBrowseButton.addActionListener(this);
        pdfDirPanel.add(pdfBrowseButton);
        contents.add(pdfDirPanel);
        
        contents.add(new JPanel());
        
        JPanel butPan = new JPanel();
        JButton okbut = new JButton("OK");
        getRootPane().setDefaultButton(okbut);
        okbut.addActionListener(this);
        butPan.add(okbut);
        JButton cancelbut = new JButton("Cancel");
        cancelbut.addActionListener(this);
        butPan.add(cancelbut);
        contents.add(butPan);

        this.setContentPane(contents);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("OK")){
        	scoresFile = scoresFileField.getText();
            pdfDir     = pdfDirField.getText();
            success    = true;
            this.dispose();
        }else if (e.getActionCommand().equals("Browse")){
        	jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            	scoresFileField.setText(jfc.getSelectedFile().getAbsolutePath());
            }
        }else if (e.getActionCommand().equals("Save to")) {
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    		if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
    			pdfDirField.setText(jfc.getSelectedFile().getAbsolutePath());
            }
        }else if (e.getActionCommand().equals("Cancel")){
            this.dispose();
        }
    }

    public boolean success() {
    	return success;
    }
    
    public String getscoresFile() {
    	return scoresFile;
    }
    
    public String getPdfDir(){
        return pdfDir;
    }

    public boolean allPlots() {
        return allPlotsButton.isSelected();
    }
    
    public boolean yesPlots() {
        return yesPlotsButton.isSelected();
    }
    
    public boolean maybePlots() {
        return maybePlotsButton.isSelected();
    }
    
    public boolean noPlots() {
        return noPlotsButton.isSelected();
    }
}