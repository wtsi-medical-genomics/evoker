package evoker;

import javax.swing.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SettingsDialog extends JDialog implements ActionListener {

    private Genoplot gp;
	private JTextField heightField;
    private JTextField widthField;
    
    public SettingsDialog(Genoplot parent){
    	super(parent,"Plot settings",true);
    	
    	gp = parent;
    	
    	JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));

        JPanel heightPanel = new JPanel();
        heightPanel.add(new JLabel("Height: "));
        heightField = new JTextField(5);
        heightPanel.add(heightField);
        contents.add(heightPanel);
        
        JPanel widthPanel = new JPanel();
        widthPanel.add(new JLabel("Width: "));
        widthField = new JTextField(5);
        widthPanel.add(widthField);
        contents.add(widthPanel);
                
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
        	int plotHeight = 0;
        	int plotWidth = 0;
        	
        	try {
        		plotHeight = Integer.parseInt(heightField.getText());
            	plotWidth = Integer.parseInt(widthField.getText());
        	} catch (NumberFormatException nfe){
        		JOptionPane.showMessageDialog(this, "Dimentions must be numerical values");
        	} 
        	
        	if(plotHeight > 0 && plotWidth > 0) {
        		gp.setPlotHeight(plotHeight);
        		gp.setPlotWidth(plotWidth);
        		gp.setPlotAreaSize();
        		gp.refreshPlot();
        		this.dispose();
        	} else {
        		JOptionPane.showMessageDialog(this, "Dimentions must be greater than 0");
        	}
        	
        } else if (e.getActionCommand().equals("Cancel")){
        	this.dispose();
        }
    }
}