/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica;

import cargaasignacionautomatica.Class.Calidad.Calidad;
import cargaasignacionautomatica.Class.Excel.FlexibleExcelReaderExample;
import cargaasignacionautomatica.Class.Carga.Carga;
import cargaasignacionautomatica.Class.DB.DB;
import cargaasignacionautomatica.Class.FTP.FTP;
import cargaasignacionautomatica.Class.Fonos.Fono;
import cargaasignacionautomatica.Class.Functions.Functions;
import cargaasignacionautomatica.Class.XML.XML;
import cargaasignacionautomatica.Class.ZIP.ZIP;
import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//import org.apache.poi.ss.usermodel.*;
import jxl.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;


/**
 *
 * @author jonthan
 */
public class CargaAsignacionAutomatica {
    public static String RutaMandanteCedente = "";
    public static String RutaProject = "";
    public static String idUsuario = "";
    public static String Mandante = "";
    public static String Cedente = "";
    public static String MarcaData = "";
    public static String FilesTXT = "";
    public static String TipoCarga = "";
    public static Boolean InicioPeriodo = false;
    public static String FechaInicioPeriodo = "";
    
    public static String DatabaseIP = "";
    public static String DatabaseName = "";
    public static String DatabasePort = "";
    public static String DatabaseUser = "";
    public static String DatabasePass = "";
    
    public static String FtpIP = "";
    public static String FtpPort = "";
    public static String FtpUser = "";
    public static String FtpPass = "";
    
    public static Connection Conn;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack" , "true");
        try{
            
            Functions functions = new Functions();
            Carga carga = new Carga();
            Fono ClassFono = new Fono();
            RutaMandanteCedente = "";
            RutaProject = "";
            int Cont = 1;
            for (Object arg : args) {
                System.out.println("--");
                System.out.println(arg.toString());
                System.out.println("--");
                switch(Cont){
                    case 1:
                        TipoCarga = arg.toString();
                        break;
                    case 2: //Ruta del Proyecto
                        RutaProject = arg.toString();
                        break;
                    case 3: //Ruta Mandante/Cedente
                        RutaMandanteCedente = arg.toString();
                        break;
                    case 4: //id del Usuario actual
                        idUsuario = arg.toString();
                        break;
                    case 5: //id del Usuario actual
                        MarcaData = arg.toString();
                        break;
                    case 6: //id del Usuario actual
                        FilesTXT = arg.toString();
                        break;
                    case 7: //Fecha inicio Periodo
                        String[] FechaInicioPeriodoArray = arg.toString().split("\\_");
                        if(FechaInicioPeriodoArray[0].equals("1")){
                            InicioPeriodo = true;
                            FechaInicioPeriodo = FechaInicioPeriodoArray[1];
                        }
                        break;
                }
                Cont++;
            }
            System.out.println(RutaMandanteCedente);
            String[] ArrayMandanteCedente = RutaMandanteCedente.split("\\/");
            System.out.println(ArrayMandanteCedente);
            Mandante = ArrayMandanteCedente[0];
            Cedente = ArrayMandanteCedente[1];

            String sDirectorio = RutaProject+"task/CargaAsignaciones/Asignaciones/"+RutaMandanteCedente;//Test
            //String sDirectorio = RutaProject+"Asignaciones/"+RutaMandanteCedente;
                XML xml = new XML();
                xml.LeerConfigXML();
                DB db = new DB();
                Conn = db.connect();
                cargaasignacionautomatica.CargaAsignacionAutomatica.Conn.setAutoCommit(false);
            try{
                switch(TipoCarga){
                    case "depurar_fonos":
                        ClassFono.PasarFonoCob_FonoHistorico();
                        ClassFono.PasarFonoHistorico_FonoCob();
                        ClassFono.DebugFonos();
                        break;
                    case "coloreo_fonos":
                        ClassFono.ColoreoFonos();
                        break;
                    default:
                        functions.ReadFolder(sDirectorio);
                        break;
                }
                 cargaasignacionautomatica.CargaAsignacionAutomatica.Conn.commit();
            }catch(IOException e){
                carga.changeCommentProcess("IOException: "+e.getMessage());
            }
            catch(InvalidFormatException e){
                carga.changeCommentProcess("InvalidFormatException: "+e.getMessage());
            }
        }catch(SQLException ex){
            Carga carga = new Carga();
            try {
                carga.getErrorMessage(ex.getMessage());
                cargaasignacionautomatica.CargaAsignacionAutomatica.Conn.rollback();
                Logger.getLogger(Carga.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex1) {
                Logger.getLogger(CargaAsignacionAutomatica.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }    
}
