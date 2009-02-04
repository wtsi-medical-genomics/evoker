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
    private JTextField userField;
    private JTextField remdirField;
    private JTextField locdirField;
    private JTextField hostField;
    private JCheckBox emptyIt;

    public DataConnectionDialog(JFrame parent){
        super(parent,"Data Connection",true);

        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));

        JPanel hostPanel = new JPanel();
        hostPanel.add(new JLabel("Host: "));
        hostField = new JTextField(20);
        hostPanel.add(hostField);
        contents.add(hostPanel);

        JPanel remdirPanel = new JPanel();
        remdirPanel.add(new JLabel("Remote directory: "));
        remdirField = new JTextField(30);
        remdirPanel.add(remdirField);
        contents.add(remdirPanel);

        JPanel localdirPanel = new JPanel();
        localdirPanel.add(new JLabel("Local directory: "));
        locdirField = new JTextField(30);
        localdirPanel.add(locdirField);
        contents.add(localdirPanel);

        JPanel userPanel = new JPanel();
        userPanel.add(new JLabel("Username: "));
        userField = new JTextField(10);
        userPanel.add(userField);
        contents.add(userPanel);

        JPanel passPanel = new JPanel();
        passPanel.add(new JLabel("Password: "));
        pf = new JPasswordField(8);
        passPanel.add(pf);
        contents.add(passPanel);

        emptyIt = new JCheckBox("Clear local cache?");
        //contents.add(emptyIt);

        JPanel butPan = new JPanel();
        JButton okbut = new JButton("OK");
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
            this.dispose();
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

    public void clearPassword(){
        for (int i = 0; i < password.length; i++){
            password[i] = 0;
        }
    }

    public String getLocalDirectory() {
        return localDir;
    }
}