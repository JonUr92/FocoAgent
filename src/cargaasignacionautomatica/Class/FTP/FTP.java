/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.FTP;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 *
 * @author COBRANDINGJONATHAN
 */
public class FTP {
    public void download() {
        String serverAddress = cargaasignacionautomatica.CargaAsignacionAutomatica.FtpIP; // ftp server address 
        int port = Integer.parseInt(cargaasignacionautomatica.CargaAsignacionAutomatica.FtpPort); // ftp uses default port Number 21
        String username = cargaasignacionautomatica.CargaAsignacionAutomatica.FtpUser;// username of ftp server
        String password = cargaasignacionautomatica.CargaAsignacionAutomatica.FtpPass; // password of ftp server
  
        FTPClient ftpClient = new FTPClient();
        try {
  
            ftpClient.connect(serverAddress, port);
            showServerReply(ftpClient);
            ftpClient.login(username,password);
            showServerReply(ftpClient);
            ftpClient.setFileTransferMode(FTPClient.BLOCK_TRANSFER_MODE);
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            
            String remoteFilePath = "ftp/Records";
            boolean success = ftpClient.changeWorkingDirectory(remoteFilePath);
            String[] Files = ftpClient.listNames();
            for (String File : Files) {
                int PosDot = File.lastIndexOf(".");
                if(PosDot >= 0){
                    String Extension = File.substring(PosDot + 1,File.length());
                    String FileName = File.substring(0,PosDot);
                    File localfile = new File(cargaasignacionautomatica.CargaAsignacionAutomatica.RutaProject+"Records/Tmp/"+File);
                    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localfile));
                    success = ftpClient.retrieveFile(File, outputStream);
                    System.out.println(ftpClient.deleteFile(File));
                    outputStream.close();
                }
            }
        } catch (IOException ex) {
            System.out.println("Error occurs in downloading files from ftp Server : " + ex.getMessage());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    boolean checkFileExists(String filePath, FTPClient ftpClient) throws IOException {
        InputStream inputStream = ftpClient.retrieveFileStream(filePath);
        int returnCode = ftpClient.getReplyCode();
        if (inputStream == null || returnCode == 550) {
            return false;
        }
        return true;
    }
    private static void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
            }
        }
    }
    private static void printFileDetails(FTPFile[] files) {
        DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (FTPFile file : files) {
            String details = file.getName();
            if (file.isDirectory()) {
                details = "[" + details + "]";
            }
            details += "\t\t" + file.getSize();
            details += "\t\t" + dateFormater.format(file.getTimestamp().getTime());
 
            System.out.println(details);
        }
    }
}
