/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.ZIP;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author COBRANDINGJONATHAN
 */
public class ZIP {
    public void UnZip(){
        try {
            // Create a ZipInputStream to read the zip file
            BufferedOutputStream dest = null;
            File file = new File("test1.zip");
            if (!file.exists()){
                System.out.println("File doesn't exist");
                System.exit(1);
            }
            FileInputStream fis = new FileInputStream("test1.zip");
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream( fis ));
            String destination = "./Records";
            // Loop over all of the entries in the zip file
            int count;
            byte data[] = new byte[ 8192 ];
            ZipEntry entry;
            while( ( entry = zis.getNextEntry() ) != null ){
                if( !entry.isDirectory() ){
                //prepareFileDirectories( destination, entryName );
                String destFN = destination + File.separator + entry.getName();
                // Write the file to the file system
                FileOutputStream fos = new FileOutputStream( destFN );
                dest = new BufferedOutputStream( fos, 8192 );
                while( (count = zis.read( data, 0, 8192 ) ) != -1 ){
                    dest.write( data, 0, count );
                }
                dest.flush();
                dest.close();
                }
            }
            zis.close();
        }
            catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}
