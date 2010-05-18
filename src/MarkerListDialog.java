import javax.swing.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MarkerListDialog extends JDialog implements ActionListener {
	
	private boolean success = false;
    private String markerList;
    private String pdfDir;
    private boolean savePlots;
    private boolean all;
    private boolean yes;
    private boolean maybe;
    private boolean no;
    
    private JTextField markerlistField;
    private JTextField pdfDirField;
    private JButton pdfBrowseButton;
    private JLabel pdfDirLabel;
    private JRadioButton savePlotsButton;
    private JCheckBox allPlotsButton;
    private JCheckBox yesPlotsButton;
    private JCheckBox maybePlotsButton;
    private JCheckBox noPlotsButton;
    
    private JFileChooser jfc;

    public MarkerListDialog(JFrame parent){
        super(parent,"Load Marker List",true);
        
        jfc = new JFileChooser("user.dir");
        
        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));

        JPanel markerlistPanel = new JPanel();
        markerlistPanel.add(new JLabel("Marker list: "));
        markerlistField = new JTextField(20);
        markerlistPanel.add(markerlistField);
        JButton markerBrowseButton = new JButton("Browse");
        markerBrowseButton.addActionListener(this);
        markerlistPanel.add(markerBrowseButton);
        contents.add(markerlistPanel);
        
        contents.add(new JPanel());
        
        JPanel savePlotsPanel = new JPanel();
        savePlotsPanel.setLayout(new BoxLayout(savePlotsPanel,BoxLayout.PAGE_AXIS));
        savePlotsButton = new JRadioButton("Save viewed plots to PDF");
        savePlotsButton.setSelected(false);
        savePlotsButton.addActionListener(this);
        savePlotsPanel.add(savePlotsButton);
        allPlotsButton = new JCheckBox("Save all plots");
        allPlotsButton.setSelected(false);
        allPlotsButton.setEnabled(false);
        savePlotsPanel.add(allPlotsButton);
        yesPlotsButton = new JCheckBox("Save all Yes plots");
        yesPlotsButton.setSelected(false);
        yesPlotsButton.setEnabled(false);
        savePlotsPanel.add(yesPlotsButton);
        maybePlotsButton = new JCheckBox("Save all Maybe plots");
        maybePlotsButton.setSelected(false);
        maybePlotsButton.setEnabled(false);
        savePlotsPanel.add(maybePlotsButton);
        noPlotsButton = new JCheckBox("Save all No plots");
        noPlotsButton.setSelected(false);
        noPlotsButton.setEnabled(false);
        savePlotsPanel.add(noPlotsButton);
        
        contents.add(savePlotsPanel);
        
        JPanel pdfDirPanel = new JPanel();
        pdfDirLabel = new JLabel("Destination directory for PDFs: ");
        pdfDirLabel.setEnabled(false);
        pdfDirPanel.add(pdfDirLabel);
        pdfDirField = new JTextField(20);
        pdfDirField.setEnabled(false);
        pdfDirPanel.add(pdfDirField);
        pdfBrowseButton = new JButton("Save to");
        pdfBrowseButton.addActionListener(this);
        pdfBrowseButton.setEnabled(false);
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
        	markerList = markerlistField.getText();
            pdfDir     = pdfDirField.getText();
            success    = true;
            this.dispose();
        }else if (e.getActionCommand().equals("Save viewed plots to PDF")) {
        	if(savePlotsButton.isSelected()) {
        		pdfDirLabel.setEnabled(true);
        		pdfDirField.setEnabled(true);
                pdfBrowseButton.setEnabled(true);
                allPlotsButton.setEnabled(true);
                yesPlotsButton.setEnabled(true);
                maybePlotsButton.setEnabled(true);
                noPlotsButton.setEnabled(true);
        	}else {
        		pdfDirLabel.setEnabled(false);
        		pdfDirField.setEnabled(false);
                pdfBrowseButton.setEnabled(false);
                allPlotsButton.setEnabled(false);
                yesPlotsButton.setEnabled(false);
                maybePlotsButton.setEnabled(false);
                noPlotsButton.setEnabled(false);
        	}
        }else if (e.getActionCommand().equals("Browse")){
        	jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            	markerlistField.setText(jfc.getSelectedFile().getAbsolutePath());
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
    
    public String getMarkerList() {
    	return markerList;
    }
    
    public String getPdfDir(){
        return pdfDir;
    }

    public boolean savePlots() {
        return savePlotsButton.isSelected();
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