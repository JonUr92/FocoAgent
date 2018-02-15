/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.Functions;

import ArchivosDeTexto.ArchivoDeTexto;
import cargaasignacionautomatica.CargaAsignacionAutomatica;
import static cargaasignacionautomatica.CargaAsignacionAutomatica.Cedente;
import static cargaasignacionautomatica.CargaAsignacionAutomatica.Mandante;
import cargaasignacionautomatica.Class.Calidad.Calidad;
import cargaasignacionautomatica.Class.Carga.Carga;
import cargaasignacionautomatica.Class.DB.DB;
import cargaasignacionautomatica.Class.Excel.FlexibleExcelReaderExample;
import cargaasignacionautomatica.Class.FTP.FTP;
import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 *
 * @author jonthan
 */
public class Functions {
    public String implode(ArrayList<String> inputArray){
        String AsImplodedString;
        if (inputArray.isEmpty()) {
            AsImplodedString = "";
        } else {
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i <= inputArray.size() - 1 ; i++){
                sb.append(inputArray.get(i));
                if(i<(inputArray.size()-1)) sb.append(",");
            }
            AsImplodedString = sb.toString();
        }
        return AsImplodedString;
    }
    public String implode(ArrayList<String> inputArray, String Char){
        String AsImplodedString;
        if (inputArray.isEmpty()) {
            AsImplodedString = "";
        } else {
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i <= inputArray.size() - 1 ; i++){
                sb.append(inputArray.get(i));
                if(i<(inputArray.size()-1)) sb.append(Char);
            }
            AsImplodedString = sb.toString();
        }
        return AsImplodedString;
    }
    public String[] split(String Value,String Chars){
        ArrayList<String> Splited = new ArrayList<String>();
        String ValueTmp = Value + Chars;
        do{
            String Val = ValueTmp.substring(0,ValueTmp.indexOf(Chars));
            ValueTmp = ValueTmp.substring(ValueTmp.indexOf(Chars) + Chars.length(),ValueTmp.length());
            Splited.add(Val);
        }while(ValueTmp.indexOf(Chars) >= 0);
        String[] ToReturn = new String[Splited.size()];
        int i = 0;
        for (Object val : Splited) {
            ToReturn[i] = val.toString();
            i++;
        }
        return ToReturn;
    }
    public String Funciones(String Value, String Codigo,String Parametros){
        String ToReturn = "";
        switch(Codigo){
            case "DIVIDIR":
                String[] ArrayParametros = Parametros.split(",");
                String[] ArrayValueTmp = Value.split(ArrayParametros[0]);
                ToReturn = ArrayValueTmp[Integer.parseInt(ArrayParametros[1])];
            break;
            default:
                ToReturn = Value;
            break;
        }
        return ToReturn;
    }
    public void ReadFolder(String Folder) throws IOException, InvalidFormatException, SQLException{
        File f = new File(Folder);
        if(f.exists()){
            System.out.println("Existe");
            File[] ficheros = f.listFiles();
            for (int x=0;x<ficheros.length;x++){
                if(ficheros[x].isDirectory()){
                    ReadFolder(Folder+"/"+ficheros[x].getName());
                }else{
                    if(ficheros[x].isFile()){
                        String FilePath = Folder+"/"+ficheros[x].getName();
                        ProcessFile(FilePath);
                    }
                }
            }
        }else{
            System.out.println("No existe");
        }
    }
    public void ProcessFile(String FilePath) throws IOException, InvalidFormatException, SQLException, MySQLSyntaxErrorException{
        ArchivoDeTexto aTexto = new ArchivoDeTexto();
        FlexibleExcelReaderExample excel = new FlexibleExcelReaderExample();
        ArrayList[] DataTables = null;
        ArrayList MarcaData = null;
        String Extension = FilePath.substring(FilePath.lastIndexOf(".") + 1,FilePath.length());
        
        String[] ArrayPath = FilePath.split("\\/");
        int FileNamePos = ArrayPath.length - 1;
        String Cedente = ArrayPath[FileNamePos - 1];
        String Mandante = ArrayPath[FileNamePos - 2];
        String FileName = ArrayPath[FileNamePos];
        
        Carga carga = new Carga();
        carga.startProcess(FileName);
        
        switch(Extension.toLowerCase()){
            case "xlsx":
            case "xls":
                switch(CargaAsignacionAutomatica.TipoCarga){
                    case "carga":
                    case "pagos":
                    case "cargagestiones":
                        DataTables = excel.ReadExcel(FilePath);
                        break;
                    case "marca":
                        MarcaData = excel.ReadMarca(FilePath);
                        break;
                }
            break;
            case "csv":
                switch(CargaAsignacionAutomatica.TipoCarga){
                    case "carga":
                    case "pagos":
                    case "cargagestiones":
                        DataTables = aTexto.ReadCSV(FilePath);
                        break;
                    case "marca":
                        break;
                }
            break;
            case "txt":
                switch(CargaAsignacionAutomatica.TipoCarga){
                    case "carga":
                    case "pagos":
                    case "cargagestiones":
                        DataTables = aTexto.ReadTXT(FilePath);
                        break;
                    case "marca":
                        break;
                }
            break;
            
        }
        switch(CargaAsignacionAutomatica.TipoCarga){
            case "carga":
            case "pagos":
            case "cargagestiones":
                if(DataTables != null){
                    carga.InsertCarga(DataTables,Cedente,Mandante);
                }
                break;
            case "marca":
                if(MarcaData != null){
                    carga.InsertMarca(MarcaData,Cedente,Mandante);
                }
                break;
        }
    }
    public boolean isDateColumn(String Table, String Column) throws SQLException{
        boolean ToReturn = false;
        DB db = new DB();
        String SqlColumn = "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '"+Table+"' AND COLUMN_NAME = '"+Column+"'  and TABLE_SCHEMA='foco'";
        ResultSet rs = db.select(SqlColumn, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        if(db.HaveData(rs)){
            while(rs.next()){
                String TipoColumna = rs.getString("DATA_TYPE");
                switch(TipoColumna){
                    case "date":
                    case "datetime":
                        ToReturn = true;
                        break;
                }
            }
        }
        return ToReturn;
    }
    public static boolean isDateValid(String date){
        try {
            DateFormat df = new SimpleDateFormat("yyyyMMdd");
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    public static String StringToDate(String Value, String PatronFecha){
        String ToReturn = "";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(PatronFecha);
            //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date;
            date = formatter.parse(Value);
            ToReturn = new SimpleDateFormat("yyyyMMdd").format(date);
        } catch (ParseException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ToReturn;
    }
    public static String StringToDateTime(String Value, String PatronFecha){
        String ToReturn = "";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(PatronFecha);
            //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date;
            date = formatter.parse(Value);
            ToReturn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        } catch (ParseException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ToReturn;
    }
    public static String StringToTime(String Value, String PatronFecha){
        String ToReturn = "";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(PatronFecha);
            //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date;
            date = formatter.parse(Value);
            ToReturn = new SimpleDateFormat("HH:mm:ss").format(date);
        } catch (ParseException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ToReturn;
    }
    public static boolean isNumeric(String str){
        return str.matches("[+-]?\\d*(\\.\\d+)?");
    }
    public String ColumnType(String Table, String Column) throws SQLException{
        String ToReturn = "";
        DB db = new DB();
        String SqlColumn = "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '"+Table+"' AND COLUMN_NAME = '"+Column+"'  and TABLE_SCHEMA='foco'";
        ResultSet rs = db.select(SqlColumn, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        if(db.HaveData(rs)){
            while(rs.next()){
                String TipoColumna = rs.getString("DATA_TYPE");
                ToReturn = TipoColumna;
            }
        }
        return ToReturn;
    }
    public Hashtable<String, String> getColumnsType(String Table) throws SQLException{
        Hashtable<String, String> Campos = new Hashtable<String, String>();
        DB db = new DB();
        String SqlColumn = "SELECT DATA_TYPE as Tipo, COLUMN_NAME as Columna FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '"+Table+"' and TABLE_CATALOG='foco'";
        ResultSet rs = db.select(SqlColumn, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        if(db.HaveData(rs)){
            while(rs.next()){
                String Type = rs.getString("Tipo");
                String Column = rs.getString("Columna");
                Campos.put(Column, Type);
            }
        }
        return Campos;
    }
}
