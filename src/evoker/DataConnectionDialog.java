package evoker;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class DataConnectionDialog extends JDialog implements ActionListener {
    private JPasswordField pf;
    private char[] password;
    private String username;
    private String remoteDir;
    private String localDir;
    private String host;
    private int port;
    private JTextField userField;
    private JTextField remdirField;
    private JTextField locdirField;
    private JTextField hostField;
    private JTextField portField;
    private JCheckBox emptyIt;
    private JRadioButton defaultFormatButton;
    private JRadioButton oxfordFormatButton;

    public DataConnectionDialog(JFrame parent){
        super(parent,"Data Connection",true);

        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));

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
        formatPanel.add(defaultFormatButton);
        defaultFormatButton.setSelected(true);
        bg.add(defaultFormatButton);
        oxfordFormatButton = new JRadioButton("Oxford format");
        formatPanel.add(oxfordFormatButton);
        bg.add(oxfordFormatButton);

        bottomPanel.add(formatPanel);
        contents.add(bottomPanel);

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
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("OK")){
            password = pf.getPassword();
            username = userField.getText();
            remoteDir = remdirField.getText();
            localDir = locdirField.getText();
            host = hostField.getText();
            port = Integer.parseInt(portField.getText());
            this.dispose();
        }else if (e.getActionCommand().equals("Browse")){
            JFileChooser jfc = new JFileChooser("user.dir");
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);            
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                locdirField.setText(jfc.getSelectedFile().getAbsolutePath());
            }
        }else if (e.getActionCommand().equals("Cancel")){
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

    public boolean isOxformat() {
        return oxfordFormatButton.isSelected();
    }

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