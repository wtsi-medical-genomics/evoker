import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.forwarding.ForwardingIOChannel;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;

import javax.swing.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;


public class DataClient{

    static PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
    static final int PORT = 2718;
    private String host;
    private String dir;
    private PrintWriter out;
    private BufferedInputStream in;


    public DataClient(JFrame parent) throws IOException{
        Logger.getLogger("com.sshtools").setLevel(Level.WARNING);

        // Create a password authentication instance
        DataConnectionDialog dcd = new DataConnectionDialog(parent);
        dcd.pack();
        dcd.setVisible(true);
        if (dcd.getUsername() != null){
            pwd.setUsername(dcd.getUsername());
            pwd.setPassword(new String(dcd.getPassword()));
            host = dcd.getHost();
            dir = dcd.getDirectory();
            dcd.clearPassword();
            connectToServer();
        }
    }

    private void connectToServer() throws IOException{
            SshClient ssh = new SshClient();
            ssh.connect(host, new IgnoreHostKeyVerification());

            // Try the authentication
            int result = ssh.authenticate(pwd);
            // Evaluate the result
            if (result == AuthenticationProtocolState.COMPLETE) {
                ForwardingIOChannel channel = new ForwardingIOChannel
                        (ForwardingIOChannel.LOCAL_FORWARDING_CHANNEL,"genozitplots","127.0.0.1",PORT,host,PORT);

                if (ssh.openChannel(channel)){
                    out = new PrintWriter(channel.getOutputStream(), true);
                    in = new BufferedInputStream(channel.getInputStream(),512000);
                }else{
                    throw new IOException("Could not open channel to host.");
                }
            }else{
                throw new IOException("Authentication failed.");
            }
    }

    public void writeToServer(String message){
        /*out.println("LOADDIR " + dir);
byte c;
System.out.println("System.currentTimeMillis() = " + System.currentTimeMillis());
while ((c = (byte)in.read()) != -1){
//System.out.println("c = " + c);
}
System.out.println("System.currentTimeMillis() = " + System.currentTimeMillis());

out.println("FETCH " + "rs818582");
while ((c = (byte)in.read()) != -1){
System.out.println("c = " + c);
}

in.close();
out.close();                                                                     */
        out.println(message);
        try{

            byte[] buf = new byte[4];
            in.read(buf,0,4);
            int x = ((buf[0] & 0xFF) << 24)
                    | ((buf[1] & 0xFF) << 16)
                    | ((buf[2] & 0xFF) << 8)
                    | (buf[3] & 0xFF);

            System.out.println(x);

        }catch (IOException ioe){
            System.out.println("ioe.getMessage() = " + ioe.getMessage());
        }
    }

}
