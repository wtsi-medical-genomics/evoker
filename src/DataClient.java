import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;

import javax.swing.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;


public class DataClient{


    static PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
    private SshClient ssh;
    private SftpClient ftp;
    private String dir;


    public DataClient(JFrame parent) throws IOException{
        Logger.getLogger("com.sshtools").setLevel(Level.WARNING);

        // Create a password authentication instance
        DataConnectionDialog dcd = new DataConnectionDialog(parent);
        dcd.pack();
        dcd.setVisible(true);
        if (dcd.getUsername() != null){
            pwd.setUsername(dcd.getUsername());
            pwd.setPassword(new String(dcd.getPassword()));
            ssh = new SshClient();
            ssh.connect(dcd.getHost(),new IgnoreHostKeyVerification());
            dir = dcd.getDirectory();
            dcd.clearPassword();

            // Try the authentication
            int result = ssh.authenticate(pwd);
            // Evaluate the result
            if (result == AuthenticationProtocolState.COMPLETE) {
                System.out.println("Success!");
                ftp = ssh.openSftpClient();
                ftp.cd(dir);
            }else{
                //TODO: handle connection error.
                 throw new IOException("Authentication failed.");
            }
        }
    }

    public void getSNPFiles(String snp, String chrom, String collection, int index, int numinds) throws IOException{

        SessionChannelClient session = ssh.openSessionChannel();
        //session.requestPseudoTerminal("vt102",80,24,0,0,"");
        session.startShell();

        /** Writing to the session OutputStream */
        OutputStream out = session.getOutputStream();
        String cmd = "cd "+ dir + "\n./fandango.pl "+ snp + " " +
                chrom + " " + collection + " " + index + " " + numinds +"\n";
         out.write(cmd.getBytes());
        System.out.println("Begin interaction.");

        /**
         * Reading from the session InputStream
         */
        InputStream in = session.getInputStream();
        byte buffer[] = new byte[255];
        int read;
        while((read = in.read(buffer)) > 0) {
            String outstr = new String(buffer, 0, read);
            if (outstr.contains(snp)){
                break;
            }
        }
        session.close();
        System.out.println("executed.");

        ftp.get(collection+"."+snp+".bed");
        ftp.get(collection+"."+snp+".bnt");
        System.out.println("fetched.");

    }

}
