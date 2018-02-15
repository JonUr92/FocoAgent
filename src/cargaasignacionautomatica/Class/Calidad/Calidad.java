/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.Calidad;

import cargaasignacionautomatica.Class.DB.DB;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author COBRANDINGJONATHAN
 */
public class Calidad {
    public void InsertRecordsToDataBase() throws SQLException {
        DB db = new DB();
        Hashtable<String, String> Cedentes = getCedenteArray();
        //System.out.println(Cedentes.get("001"));
        File Folder = new File(cargaasignacionautomatica.CargaAsignacionAutomatica.RutaProject+"Records/Tmp/");
        if (Folder.exists()){
            File[] Files = Folder.listFiles();
            for (int x=0;x<Files.length;x++){
             
                String FileName = Files[x].getName();
//                String FileNameWithoutExtension = FileName.substring(0,FileName.lastIndexOf("-all"));
                
                String SqlSelect = "SELECT * FROM gestion_ult_trimestre WHERE nombre_grabacion = '"+FileName+"'";
                ResultSet rs = db.select(SqlSelect, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
                
                if(db.HaveData(rs)){
                    while(rs.next()){
                        

//                        String[] FileNameArray = FileNameWithoutExtension.split("\\_"); // 0=>Time; 1=>Phone; 2=>List; 3=>User
//                        String[] TimeArrayFileName = FileNameArray[0].split("\\-");// 0=>Date; 1=>Time
                        
                        String Extension = FileName.substring(FileName.lastIndexOf(".") + 1);
//                        String DateTime = rs.getString("fechahora");
//                        String[] DateTimeArray = DateTime.split("\\s+");
//                        String Date = DateTimeArray[0];
//                        String Time = DateTimeArray[1];
                        String Date = rs.getString("fecha_gestion");
                        String Time = rs.getString("hora_gestion");
                        Date = Date.replace("-", "");
                        Time = Time.replace(":", "");
                        String Phone = rs.getString("fono_discado");
                        String List = rs.getString("cedente");
                        List = List.length() == 1 ? "00"+List : List.length() == 2 ? "0"+List : List;
                        String User = rs.getString("nombre_ejecutivo");
                        
                        String FileNameGestionTable = Date + "-" + Time  + "_" + Phone + "_" + List + "_" + User;
                        String FileNameGrabacionTable = FileNameGestionTable + "-all." + Extension;

                        File rutaOriginalFichero = new File(cargaasignacionautomatica.CargaAsignacionAutomatica.RutaProject+"Records/Tmp/"+FileName);
                        File rutaDestinoFichero = new File(cargaasignacionautomatica.CargaAsignacionAutomatica.RutaProject+"Records/"+List+"/"+Date+"/"+User+"/"+FileNameGrabacionTable);
                        File rutaDestino = new File(cargaasignacionautomatica.CargaAsignacionAutomatica.RutaProject+"Records/"+List+"/"+Date+"/"+User+"/");

                        String SqlInsert = "insert IGNORE into grabacion_2 (Nombre_Grabacion, Fecha, Cartera, Usuario, Telefono) values('"+FileNameGrabacionTable+"','"+Date+"','"+Cedentes.get(List)+"','"+User+"','"+Phone+"')";
                        boolean Insert = db.query(SqlInsert, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
                        if(Insert){
                            rutaDestino.mkdirs();
                            String SqlUpdate = "UPDATE gestion_ult_trimestre set nombre_grabacion='"+FileNameGestionTable+"' where nombre_grabacion='"+FileName+"'";
                            boolean Update = db.query(SqlUpdate, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
                            db.disconnect();
                            if(!rutaDestinoFichero.exists()){
                                boolean estatus = rutaOriginalFichero.renameTo(rutaDestinoFichero);
                                if(estatus){
                                    //System.out.println("cargaasignacionautomatica.Class.Calidad.Calidad.InsertRecordsToDataBase(1)");
                                }else{
                                    //System.out.println("cargaasignacionautomatica.Class.Calidad.Calidad.InsertRecordsToDataBase(2)");
                                }
                            }else{
                                rutaOriginalFichero.delete();
                            }
                        }
                    }
                }
            }
        }
    }
//    public void InsertRecordsToDataBase() throws SQLException {
//        DB db = new DB();
//        Hashtable<String, String> Cedentes = getCedenteArray();
//        //System.out.println(Cedentes.get("001"));
//        File f = new File(cargaasignacionautomatica.CargaAsignacionAutomatica.RutaProject+"Records/Tmp/");
//        if (f.exists()){
//            File[] ficheros = f.listFiles();
//            for (int x=0;x<ficheros.length;x++){
//                String FileName = ficheros[x].getName();
//                String FileNameWithoutExtension = FileName.substring(0,FileName.lastIndexOf("-all"));
//                String[] FileNameArray = FileNameWithoutExtension.split("\\_"); // 0=>Time; 1=>Phone; 2=>List; 3=>User
//                String[] TimeArrayFileName = FileNameArray[0].split("\\-");// 0=>Date; 1=>Time
//                
//                File rutaOriginalFichero = new File(cargaasignacionautomatica.CargaAsignacionAutomatica.RutaProject+"Records/Tmp/"+FileName);
//                File rutaDestinoFichero = new File(cargaasignacionautomatica.CargaAsignacionAutomatica.RutaProject+"Records/"+FileNameArray[2]+"/"+TimeArrayFileName[0]+"/"+FileNameArray[3]+"/"+FileName);
//                File FileTo = new File(cargaasignacionautomatica.CargaAsignacionAutomatica.RutaProject+"Records/"+FileNameArray[2]+"/"+TimeArrayFileName[0]+"/"+FileNameArray[3]+"/");
//                if(Cedentes.get(FileNameArray[2]) != null){
//                    String SqlInsert = "insert IGNORE into grabacion_2 (Nombre_Grabacion, Fecha, Cartera, Usuario, Telefono) values('"+FileName+"','"+TimeArrayFileName[0]+"','"+Cedentes.get(FileNameArray[2])+"','"+FileNameArray[3]+"','"+FileNameArray[1]+"')";
//                    boolean Insert = db.query(SqlInsert, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
//                    if(Insert){
//                        FileTo.mkdirs();
//                        if(!rutaDestinoFichero.exists()){
//                            boolean estatus = rutaOriginalFichero.renameTo(rutaDestinoFichero);
//                            if(estatus){
//                                //System.out.println("cargaasignacionautomatica.Class.Calidad.Calidad.InsertRecordsToDataBase(1)");
//                            }else{
//                                //System.out.println("cargaasignacionautomatica.Class.Calidad.Calidad.InsertRecordsToDataBase(2)");
//                            }
//                        }else{
//                            rutaOriginalFichero.delete();
//                        }
//                    }
//                }
//            }
//        }
//    }
    public  Hashtable<String, String> getCedenteArray(){
        Hashtable<String, String> Cedentes = new Hashtable<String, String>();
        DB db = new DB();
        String sql = "select Cedente.Nombre_Cedente as NombreCedente, mandante_cedente.Lista_Vicidial as Campanas from mandante_cedente inner join Cedente on Cedente.Id_Cedente = mandante_cedente.Id_Cedente";
        try {
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    String NombreCedente = rs.getString("NombreCedente");
                    String Campanas = rs.getString("Campanas");
                    String[] CampanasArray = Campanas.split("\\,");
                    for(String Campana : CampanasArray){
                        Campana = Campana.length() == 1 ? "00"+Campana : Campana.length() == 2 ? "0"+Campana : Campana;
                        Cedentes.put(Campana, NombreCedente);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.disconnect();
        }
        return Cedentes;
    }
}
