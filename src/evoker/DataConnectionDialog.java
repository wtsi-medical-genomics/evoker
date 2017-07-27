package evoker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import evoker.Types.FileFormat;

public class DataConnectionDialog extends JDialog implements ActionListener {

    private FileFormat fileFormat;
    private JPasswordField pf;
    private char[] password;
    private String username;
    private String remoteDir;
    private String localDir;
    private String remoteTempDir;
    private String host;
    private int port;
	private String fam;
	private JTextField userField;
    private JTextField remdirField;
	private JTextField remoteTempDirField;
	private JTextField famField;
    private JTextField locdirField;
    private JTextField hostField;
    private JTextField portField;
	private JPanel famPanel;
	private JPanel remoteTempDirPanel;
	private JCheckBox emptyIt;
    private JRadioButton defaultFormatButton;
    private JRadioButton oxfordFormatButton;
    private JRadioButton ukBioBankFormatButton;
    private Boolean cancelled;

    public DataConnectionDialog(JFrame parent){
        super(parent,"Data Connection",true);
		fam = "";
        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));
		cancelled = true;
        JPanel hostPanel = new JPanel();
        hostPanel.add(new JLabel("Host: "));
        hostField = new JTextField(20);
        hostPanel.add(hostField);

        hostPanel.add(new JLabel("Port: "));
        portField = new JTextField("22", 3);
        hostPanel.add(portField);
        contents.add(hostPanel);

        JPanel remdirPanel = new JPanel();
        remdirPanel.add(new JLabel("Remote directory: "));
		remdirField = new JTextField(30);
        remdirPanel.add(remdirField);
        contents.add(remdirPanel);

        JPanel localdirPanel = new JPanel();
        localdirPanel.add(new JLabel("Local directory: "));
        locdirField = new JTextField(20);
        localdirPanel.add(locdirField);
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(this);
        localdirPanel.add(browseButton);
        contents.add(localdirPanel);

        JPanel bottomPanel = new JPanel();

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel,BoxLayout.Y_AXIS));

        JPanel userPanel = new JPanel();
        userPanel.add(new JLabel("Username: "));
        userField = new JTextField(10);
        userPanel.add(userField);
        loginPanel.add(userPanel);

        JPanel passPanel = new JPanel();
        passPanel.add(new JLabel("Password: "));
        pf = new JPasswordField(8);
        passPanel.add(pf);
        loginPanel.add(passPanel);

        bottomPanel.add(loginPanel);

        ButtonGroup bg = new ButtonGroup();
        JPanel formatPanel = new JPanel();
        formatPanel.setLayout(new BoxLayout(formatPanel,BoxLayout.Y_AXIS));

        defaultFormatButton = new JRadioButton("Default format");
		defaultFormatButton .addActionListener(this);
        formatPanel.add(defaultFormatButton);
        defaultFormatButton.setSelected(true);
        bg.add(defaultFormatButton);

        oxfordFormatButton = new JRadioButton("Oxford format");
		oxfordFormatButton.addActionListener(this);
        formatPanel.add(oxfordFormatButton);
        bg.add(oxfordFormatButton);

        ukBioBankFormatButton = new JRadioButton("UK BioBank v2 format");
		ukBioBankFormatButton.addActionListener(this);
        formatPanel.add(ukBioBankFormatButton);
        bg.add(ukBioBankFormatButton);
        bottomPanel.add(formatPanel);
        contents.add(bottomPanel);

		famPanel = new JPanel();
		famPanel.add(new JLabel("Remote FAM file: "));
		famField = new JTextField(30);
		famPanel.add(famField);
		famPanel.setVisible(false);

		remoteTempDirPanel = new JPanel();
		remoteTempDirPanel.add(new JLabel("Remote temp directory: "));
		remoteTempDirField = new JTextField(30);
		remoteTempDirPanel.add(remoteTempDirField);
		remoteTempDirPanel.setVisible(false);


		contents.add(famPanel);
		contents.add(remoteTempDirPanel);

        //TODO: should this be reactivated?
        emptyIt = new JCheckBox("Clear local cache?");
        //contents.add(emptyIt);

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


		this.setPreferredSize(new Dimension(550,350));
		this.setMinimumSize(new Dimension(550,350));

    }

    public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
        if (command.equals("UK BioBank v2 format")) {
			famPanel.setVisible(true);
			remoteTempDirPanel.setVisible(true);
		} else if (command.equals("Default format") || command.equals("Oxford format")) {
			famPanel.setVisible(false);
			remoteTempDirPanel.setVisible(false);
		} else if (command.equals("OK")){
            password = pf.getPassword();
            username = userField.getText();
            remoteDir = remdirField.getText();
            localDir = locdirField.getText();
            host = hostField.getText();
            port = Integer.parseInt(portField.getText());
            cancelled = false;
            if (defaultFormatButton.isSelected()) {
            	fileFormat = FileFormat.DEFAULT;
            } else if (oxfordFormatButton.isSelected()) {
            	fileFormat = FileFormat.OXFORD;
            } else if (ukBioBankFormatButton.isSelected()) {
            	fileFormat = FileFormat.UKBIOBANK;
				fam = famField.getText();
				remoteTempDir = remoteTempDirField.getText();

            }
            this.dispose();
        }else if (command.equals("Browse")){
            JFileChooser jfc = new JFileChooser("user.dir");
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                locdirField.setText(jfc.getSelectedFile().getAbsolutePath());
            }
        }else if (command.equals("Cancel")){
        	cancelled = true;
            this.dispose();
        }
    }

    public boolean getEmpty(){
        return emptyIt.isSelected();
    }

    public char[] getPassword(){
        return password;
    }

    public String getUsername(){
        return username;
    }

    public String getRemoteDirectory() {
        return remoteDir;
    }

    public String getHost() {
        return host;
    }
    
    public int getPort() {
    	return port;
    }

    public FileFormat getFileFormat(){
        return fileFormat;
    }

    public String getFam() { return fam; }

    public String getRemoteTempDir(){ return remoteTempDir; }

    public boolean isOxformat() {
        return getFileFormat() == FileFormat.OXFORD;
    }

    public boolean isCancelled() { return cancelled; }

    public void clearPassword(){
        for (int i = 0; i < password.length; i++){
            password[i] = 0;
            pf.setText("");
        }
    }

    public String getLocalDirectory() {
        return localDir;
    }
}