import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

public class DataConnectionDialog extends JDialog implements ActionListener {
    private JPasswordField pf;
    private char[] password;
    private String username;
    private String directory;
    private String host;
    private JTextField userField;
    private JTextField dirField;
    private JTextField hostField;

    public DataConnectionDialog(JFrame parent){
        super(parent,"Data Connection",true);

        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));

        JPanel hostPanel = new JPanel();
        hostPanel.add(new JLabel("Host: "));
        hostField = new JTextField(20);
        hostPanel.add(hostField);
        contents.add(hostPanel);

        JPanel dirPanel = new JPanel();
        dirPanel.add(new JLabel("Directory: "));
        dirField = new JTextField(20);
        dirPanel.add(dirField);
        contents.add(dirPanel);

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
            directory = dirField.getText();
            host = hostField.getText();
            this.dispose();
        }else if (e.getActionCommand().equals("Cancel")){
            this.dispose();
        }
    }

    public char[] getPassword(){
        return password;
    }

    public String getUsername(){
        return username;
    }

    public String getDirectory() {
        return directory;
    }

    public String getHost() {
        return host;
    }

    public void clearPassword(){
        for (int i = 0; i < password.length; i++){
            password[i] = 0;
        }
    }

}