package evoker;

import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;

import javax.swing.*;

import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Iterator;

import evoker.Types.*;

public class DataClient{

    static PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
    private SshClient ssh;
    private SftpClient ftp;
    private String remoteDir;
    private String displayName;
    private List files;
    private String oxPlatform;
    private Genoplot gp;
    private FileFormat fileFormat;
    private String famPath;
    private String remoteTempDir;
    ProgressMonitor pm;

    public String getLocalDir() {
        return localDir;
    }

    public String getDisplayName(){
        return displayName;
    }

    private String localDir;


    public DataClient(DataConnectionDialog dcd, Genoplot genoplot) throws IOException{
        Logger.getLogger("com.sshtools").setLevel(Level.WARNING);
        this.gp = genoplot;
        // Create a password authentication instance
        if (dcd.getUsername() != null){
            localDir = dcd.getLocalDirectory();
			fileFormat = dcd.getFileFormat();

			if (fileFormat == FileFormat.UKBIOBANK) {
				famPath = dcd.getFam();
				remoteTempDir = dcd.getRemoteTempDir();
			}

            pwd.setUsername(dcd.getUsername());
            pwd.setPassword(new String(dcd.getPassword()));
            ssh = new SshClient();
            try{
                ssh.connect(dcd.getHost(), dcd.getPort(), new IgnoreHostKeyVerification());
                remoteDir = dcd.getRemoteDirectory();
            }finally{
                dcd.clearPassword();
            }

            // Try the authentication
            int result = ssh.authenticate(pwd);
            // Evaluate the result
            if (result == AuthenticationProtocolState.COMPLETE) {
                Genoplot.ld.log("Successful connection to " + dcd.getHost());
                ftp = ssh.openSftpClient();
                ftp.cd(remoteDir);
                ftp.lcd(localDir);
				// TODO Verify that evoker-helper.pl is there and working.

//				SessionChannelClient session = ssh.openSessionChannel();
//				session.startShell();
//				InputStream in = session.getInputStream();
//				OutputStream out = session.getOutputStream();
//				out.write("evoker-helper.pl --version".getBytes());
//				int c;
//				while ((c=in.read()) != -1)
//				{
//					System.out.print((char) c);
//				}
//				session.close();
//
//
//
//                try{
//                    String perms = ftp.stat("evoker-helper.pl").getPermissionsString();
//                    if (!perms.endsWith("x")){
//                        throw new IOException("must be fully executable");
//                    }
//                }catch (IOException ioe){
//                    ssh.disconnect();
//                    throw new IOException("Problem with evoker-helper.pl on remote server:\n"+ioe.getMessage());
//                }
                
                // check if the localdir contains files already
                File[] localFiles = new File(localDir).listFiles();
                if (localFiles.length > 0){
                	Genoplot.ld.log("Files in the local directory");
                	// ask the user if they wants it emptied
                	int n = JOptionPane.showConfirmDialog(
                			genoplot.getContentPane(), 
                			"The local directory selected is not empty.\n Would you like to clear all files in this directory?",
                			"Clear the local directory?",
                			JOptionPane.YES_NO_OPTION,
                			JOptionPane.QUESTION_MESSAGE );
                	// n 0 = yes 1 = no
                	if (n == 0) {
                		for (File localFile : localFiles){
                			try {
                				localFile.delete();	
                			}catch (SecurityException se){
                				JOptionPane.showMessageDialog(null,se.getMessage(), "File delete error", JOptionPane.ERROR_MESSAGE);
                			}
                		}
                	}
                }
                displayName = dcd.getHost()+":"+remoteDir;
            }else{
                 throw new IOException("Authentication to host '"+dcd.getHost()+"' failed.");
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

    public void getSNPFiles(String snp, String chrom, String collection, int index, int numinds, int totNumSNPs) throws IOException{
        String filestem = collection+"."+snp;
        if (!(new File(localDir+File.separator+filestem+".bed").exists() &&
                new File(localDir+File.separator+filestem+".bnt").exists())){
            long prev = System.currentTimeMillis();

            SessionChannelClient session = ssh.openSessionChannel();
            session.startShell();
            
            // variable to pass to the evoker-helper.pl script
            int oxStatus;
            if (fileFormat == FileFormat.OXFORD){
                oxStatus = 1;
            } else {
            	oxStatus = 0;
            }

            int ukbiobank_v2;
            String outpath;
            if (fileFormat == FileFormat.UKBIOBANK) {
				ukbiobank_v2 = 1;
				outpath = remoteTempDir;
				filestem = remoteTempDir + File.separator + filestem;
			} else {
				ukbiobank_v2 = 0;
				outpath = "0";
			}


            //Fire off the script on the remote server to get the requested slice of data
            OutputStream out = session.getOutputStream();
			//String cmd = "cd "+ remoteDir + "\nperl evoker-helper.pl "+ snp + " " + chrom + " " +
            String cmd = "cd "+ remoteDir + "\nevoker-helper.pl "+ snp + " " + chrom + " " +
					collection + " " + index + " " + numinds + " " + totNumSNPs + " " + oxStatus + " " +
					this.getOxPlatform() + " " + ukbiobank_v2 + " " + outpath + "\n";
            out.write(cmd.getBytes());


            //monitor the remote server for news that the script has been finished
            //this is pretty slow -- is there a better way?
            InputStream in = session.getInputStream();
            byte buffer[] = new byte[1024];
            int read;
            long start = System.currentTimeMillis();
                       
            while((System.currentTimeMillis() - start)/1000 < 120) {
            	try{
                	read = in.read(buffer);
                	String outstr = new String(buffer, 0, read);
                    if (outstr.contains(snp)){
                        break;
                    } else if (outstr.contains("write_error")) {
                    	throw new IOException("user does not have write privileges");
                    }
                }catch (IOException ioe){
                    ssh.disconnect();
                    throw new IOException("Problem with remote directory permissions:\n"+ioe.getMessage());
                }
                
            }

            if ((System.currentTimeMillis() - start)/1000 >= 120) {
            	// if nothing is output from evoker-helper.pl in 2 minutes then die
            	throw new IOException("evoker-helper.pl is not responsive check the script will run");
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


    public boolean isOxFormat() {
        return fileFormat == FileFormat.OXFORD;
    }

	public File prepMetaFiles() throws IOException {
		files = ftp.ls();

		String famending = ".fam";
		String bimending = ".bim";
		if (fileFormat == FileFormat.OXFORD) {
			famending = ".sample";
			bimending = ".snp";
		}

		Iterator i = files.iterator();

		if (fileFormat == FileFormat.UKBIOBANK) {
			// UKB provides a separate fam files
			File f = new File(famPath);
			String famFile = f.getName();

			if (!new File(localDir + File.separator + famFile).exists()) {
				try {
					ftp.get(famPath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else {
			while (i.hasNext()) {
				String filename = ((SftpFile) i.next()).getFilename();
				if (filename.endsWith(famending)) {
					if (!new File(localDir + File.separator + filename).exists()) {
						try {
							ftp.get(filename);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			}
		}

		gp.pm.setMaximum(files.size() * 2);
		int loopCount = 0;
		i = files.iterator();
		while (i.hasNext()) {
			gp.pm.setProgress(++loopCount);
			if (gp.pm.isCanceled()) {
				return null;
			}
			String filename = ((SftpFile) i.next()).getFilename();
			if (filename.endsWith(bimending)) {
				if (!new File(localDir + File.separator + filename).exists()) {
					try {
						ftp.get(filename);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}

		i = files.iterator();
		while (i.hasNext()) {
			gp.pm.setProgress(++loopCount);
			if (gp.pm.isCanceled()) {
				return null;
			}
			String filename = ((SftpFile) i.next()).getFilename();
			if (filename.endsWith(".qc")) {
				if (!new File(localDir + File.separator + filename).exists()) {
					try {
						ftp.get(filename);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}

		return new File(localDir);
	}

	public void setOxPlatform(String oxPlatform) {
		this.oxPlatform = oxPlatform;
	}

	public String getOxPlatform() {
		return oxPlatform;
	}
	
	public SftpClient getFTP() {
		return ftp;
	}

}
