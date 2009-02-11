import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;

import javax.swing.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;


public class DataClient{


    static PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
    private SshClient ssh;
    private SftpClient ftp;
    private String remoteDir;
    private String displayName;
    private List files;

    public String getLocalDir() {
        return localDir;
    }

    public String getDisplayName(){
        return displayName;
    }

    private String localDir;


    public DataClient(DataConnectionDialog dcd) throws IOException{
        Logger.getLogger("com.sshtools").setLevel(Level.WARNING);


        // Create a password authentication instance
        if (dcd.getUsername() != null){
            localDir = dcd.getLocalDirectory();

            //clean up locally if asked.
            if (dcd.getEmpty()){
                File ld = new File(localDir);
            }

            pwd.setUsername(dcd.getUsername());
            pwd.setPassword(new String(dcd.getPassword()));
            ssh = new SshClient();
            ssh.connect(dcd.getHost(),new IgnoreHostKeyVerification());
            remoteDir = dcd.getRemoteDirectory();
            dcd.clearPassword();

            // Try the authentication
            int result = ssh.authenticate(pwd);
            // Evaluate the result
            if (result == AuthenticationProtocolState.COMPLETE) {
                Genoplot.ld.log("Successful connection to " + dcd.getHost());
                ftp = ssh.openSftpClient();
                ftp.cd(remoteDir);
                ftp.lcd(localDir);
                try{
                    String perms = ftp.stat("evoker-helper.pl").getPermissionsString();
                    if (!perms.endsWith("x")){
                        throw new IOException("must be fully executable");
                    }
                }catch (IOException ioe){
                    ssh.disconnect();
                    throw new IOException("Problem with evoker-helper.pl on remote server:\n"+ioe.getMessage());
                }
                displayName = dcd.getHost()+":"+remoteDir;
            }else{
                 throw new IOException("Authentication to host "+dcd.getHost()+" failed.");
            }
        }
    }

    public String[] getFilesInRemoteDir() throws IOException{
        if (files != null){
            String[] out = new String[files.size()];
            for (int i = 0; i < files.size(); i++){
                out[i] = ((SftpFile)files.get(i)).getFilename();
            }
            return out;
        }else{
            throw new IOException("Cannot ls files in remote directory");
        }
    }

    public boolean getConnectionStatus(){
        return (ssh != null);
    }

    public void getSNPFiles(String snp, String chrom, String collection, int index, int numinds) throws IOException{
        String filestem = collection+"."+snp;
        if (!(new File(localDir+File.separator+filestem+".bed").exists() &&
                new File(localDir+File.separator+filestem+".bnt").exists())){
            long prev = System.currentTimeMillis();

            SessionChannelClient session = ssh.openSessionChannel();
            session.startShell();

            //Fire off the script on the remote server to get the requested slice of data
            OutputStream out = session.getOutputStream();
            String cmd = "cd "+ remoteDir + "\n./evoker-helper.pl "+ snp + " " +
                    chrom + " " + collection + " " + index + " " + numinds +"\n";
            out.write(cmd.getBytes());


            //monitor the remote server for news that the script has been finished
            //this is pretty slow -- is there a better way?
            InputStream in = session.getInputStream();
            byte buffer[] = new byte[1024];
            int read;
            while((read = in.read(buffer)) > 0) {
                String outstr = new String(buffer, 0, read);
                if (outstr.contains(snp)){
                    break;
                }
            }

            session.close();

            
            ftp.get(filestem+".bed");
            ftp.get(filestem+".bnt");
            ftp.rm(filestem+".bed");
            ftp.rm(filestem+".bnt");

            double time = ((double)(System.currentTimeMillis() - prev))/1000;
            Genoplot.ld.log(snp +" for "+ collection +" was fetched in "+ time + "s.");
        }else{
            Genoplot.ld.log(snp +" for "+ collection +" was cached.");
        }

    }

    public File prepMetaFiles() throws IOException{
        files = ftp.ls();

        Iterator i = files.iterator();
        while (i.hasNext()){
            String filename = ((SftpFile)i.next()).getFilename();
            if (filename.endsWith(".fam")){
                if (!new File(localDir+File.separator+filename).exists()){
                    ftp.get(filename);
                }
            }
        }

        i = files.iterator();
        while (i.hasNext()){
            String filename = ((SftpFile)i.next()).getFilename();
            if (filename.endsWith(".bim")){
                if (!new File(localDir+File.separator+filename).exists()){
                    ftp.get(filename);
                }
            }
        }

        return new File(localDir);
    }

}
