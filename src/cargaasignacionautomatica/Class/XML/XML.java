/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.XML;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author COBRANDINGJONATHAN
 */
public class XML {
    public void LeerConfigXML(){
        //Se crea un SAXBuilder para poder parsear el archivo
        SAXBuilder builder = new SAXBuilder();
        System.out.println(cargaasignacionautomatica.CargaAsignacionAutomatica.RutaProject+"task/CargaAsignaciones/config.xml");
        //File xmlFile = new File("config.xml" );
        File xmlFile = new File( cargaasignacionautomatica.CargaAsignacionAutomatica.RutaProject+"task/CargaAsignaciones/config.xml" );
        try{
            //Se crea el documento a traves del archivo
            Document document = (Document) builder.build( xmlFile );

            //Se obtiene la raiz 'tables'
            Element rootNode = document.getRootElement();

            //Se obtiene la lista de hijos de la raiz 'tables'
            List list = rootNode.getChildren();
            for(int i=0; i<list.size(); i++){
                Element Node = (Element) list.get(i);
                switch(Node.getName()){
                    case "Database":
                        cargaasignacionautomatica.CargaAsignacionAutomatica.DatabaseIP = Node.getChild("ip").getTextTrim();
                        cargaasignacionautomatica.CargaAsignacionAutomatica.DatabasePort = Node.getChild("port").getTextTrim();
                        cargaasignacionautomatica.CargaAsignacionAutomatica.DatabaseName = Node.getChild("name").getTextTrim();
                        cargaasignacionautomatica.CargaAsignacionAutomatica.DatabaseUser = Node.getChild("user").getTextTrim();
                        cargaasignacionautomatica.CargaAsignacionAutomatica.DatabasePass = Node.getChild("password").getTextTrim();
                        break;
                    case "Ftp":
                        cargaasignacionautomatica.CargaAsignacionAutomatica.FtpIP = Node.getChild("ip").getTextTrim();
                        cargaasignacionautomatica.CargaAsignacionAutomatica.FtpPort = Node.getChild("port").getTextTrim();
                        cargaasignacionautomatica.CargaAsignacionAutomatica.FtpUser = Node.getChild("user").getTextTrim();
                        cargaasignacionautomatica.CargaAsignacionAutomatica.FtpPass = Node.getChild("password").getTextTrim();
                        break;
                }
            }
        }catch ( IOException io ) {
            System.out.println( io.getMessage() );
        }catch ( JDOMException jdomex ) {
            System.out.println( jdomex.getMessage() );
        }
    }
}
