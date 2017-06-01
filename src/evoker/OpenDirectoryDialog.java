package evoker;

import javax.swing.*;

import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;


import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import evoker.Types.FileFormat;


public class OpenDirectoryDialog extends JDialog implements ActionListener {

//    public static enum FileFormat {
//        DEFAULT, OXFORD, UKBIOBANK
//    }

    private FileFormat fileFormat;
    private String directory;
    private JTextField directoryField;
    private JRadioButton defaultFormatButton;
    private JRadioButton oxfordFormatButton;
    private JRadioButton ukBioBankFormatButton;
    private ButtonGroup bg;
    private JFileChooser jfc;
    private boolean success;

    public OpenDirectoryDialog(JFrame parent){
        super(parent,"Open Directory",true);

        jfc = new JFileChooser("user.dir");

        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));

        JPanel directoryPanel = new JPanel();
        directoryPanel.add(new JLabel("Directory: "));
        directoryField = new JTextField(30);
        directoryPanel.add(directoryField );
        JButton directoryBrowseButton = new JButton("Browse");
        directoryBrowseButton.addActionListener(this);
        directoryPanel.add(directoryBrowseButton);
        contents.add(directoryPanel);

        contents.add(new JPanel());

        bg = new ButtonGroup();
        JPanel formatPanel = new JPanel();
        formatPanel.setLayout(new BoxLayout(formatPanel,BoxLayout.Y_AXIS));

        defaultFormatButton = new JRadioButton("Default format");
        formatPanel.add(defaultFormatButton);
        defaultFormatButton.setSelected(true);
        bg.add(defaultFormatButton);

        oxfordFormatButton = new JRadioButton("Oxford format");
        formatPanel.add(oxfordFormatButton);
        bg.add(oxfordFormatButton);

        ukBioBankFormatButton = new JRadioButton("UK BioBank format");
        formatPanel.add(ukBioBankFormatButton);
        bg.add(ukBioBankFormatButton);

        contents.add(formatPanel);

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
            directory = directoryField.getText();
            if (defaultFormatButton.isSelected()) { fileFormat = FileFormat.DEFAULT; }
            else if (oxfordFormatButton.isSelected()) { fileFormat = FileFormat.OXFORD; }
            else if (ukBioBankFormatButton.isSelected()) { fileFormat = FileFormat.UKBIOBANK; }
            setSuccess(true);
            this.dispose();
        }else if (e.getActionCommand().equals("Browse")){
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                directoryField.setText(jfc.getSelectedFile().getAbsolutePath());
            }
        }else if (e.getActionCommand().equals("Cancel")){
            setSuccess(false);
            this.dispose();
        }
    }

    public boolean success() {
        return success;
    }

    public String getDirectory() {
        return directory;
    }

    public FileFormat getFileFormat(){
        return fileFormat;
    }
//
//    public boolean allPlots() {
//        return allPlotsButton.isSelected();
//    }
//
//    public boolean yesPlots() {
//        return yesPlotsButton.isSelected();
//    }
//
//    public boolean maybePlots() {
//        return maybePlotsButton.isSelected();
//    }
//
//    public boolean noPlots() {
//        return noPlotsButton.isSelected();
//    }
//
    public void setSuccess(boolean b) {
        success = b;

    }
}