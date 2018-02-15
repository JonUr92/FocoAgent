/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.Templates;

import cargaasignacionautomatica.Class.DB.DB;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author jonthan
 */
public class Template {
    public List<HashMap<String, String>> getTemplate(String Cedente){
        List<HashMap<String, String>> Templates;
        Templates = new ArrayList<HashMap<String, String>>();
        DB db = new DB();
        String sql = "SELECT * FROM Template_Carga where Id_Cedente='"+Cedente+"'";
        try {
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    String TipoArchivo = rs.getString("Tipo_Archivo");
                    String SeparadorCabecero = rs.getString("Separador_Cabecero");
                    String TemplateID = rs.getString("id");
                    String HaveHeader = rs.getString("haveHeader");
                    HashMap hm = new HashMap();
                    hm.put("TemplateID", TemplateID);
                    hm.put("Tipo_Archivo", TipoArchivo);
                    hm.put("Separador_Cabecero", SeparadorCabecero);
                    hm.put("haveHeader", HaveHeader);
                    Templates.add(hm);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.disconnect();
        }
        return Templates;
    }
    public List<HashMap<String, String>> getColumnsTemplate(String Template,String Tabla,String SheetTemplate){
        List<HashMap<String, String>> Columns;
        Columns = new ArrayList<HashMap<String, String>>();
        DB db = new DB();
        String sql = "SELECT " +
                    "	id_template," +
                    "	SUBSTRING((select STUFF(ColTmp.Columna,1,0,',') from Columnas_Template_Carga ColTmp where ColTmp.id_template='"+Template+"' and ColTmp.id_sheet='"+SheetTemplate+"' and ColTmp.Tabla='"+Tabla+"' and ColTmp.Campo=Columnas_Template_Carga.Campo group by ColTmp.Columna FOR XML PATH('')),2,1000000) as Columna, " +
                    "	posicionInicio," +
                    "	cantCaracteres," +
                    "	id_sheet," +
                    "	Funcion," +
                    "	Parametros," +
                    "	Tabla," +
                    "	Campo," +
                    "	PatronFecha," +
                    "	SUBSTRING((select STUFF(ColTmp.Prioridad_Fono,1,0,',') from Columnas_Template_Carga ColTmp where ColTmp.id_template='"+Template+"' and ColTmp.id_sheet='"+SheetTemplate+"' and ColTmp.Tabla='"+Tabla+"' and ColTmp.Campo=Columnas_Template_Carga.Campo group by ColTmp.Prioridad_Fono FOR XML PATH('')),2,1000000) as Prioridad_Fono " +
                    "FROM " +
                    "	Columnas_Template_Carga " +
                    "where " +
                    "	id_template='"+Template+"' and" +
                    "	Tabla='"+Tabla+"' and" +
                    "	id_sheet='"+SheetTemplate+"' " +
                    "GROUP BY " +
                    "	id_template," +
                    "	posicionInicio," +
                    "	cantCaracteres," +
                    "	id_sheet," +
                    "	Funcion," +
                    "	Parametros," +
                    "	Tabla," +
                    "	Campo," +
                    "	PatronFecha " +
                    "order by " +
                    "	Tabla";
        //String sql = "SELECT id_template,GROUP_CONCAT(Columna) as Columna,posicionInicio,cantCaracteres,id_sheet,Funcion,Parametros,Tabla,Campo,PatronFecha,GROUP_CONCAT(Prioridad_Fono) as Prioridad_Fono FROM Columnas_Template_Carga where id_template='"+Template+"' and Tabla='"+Tabla+"' and id_sheet='"+SheetTemplate+"' GROUP BY Campo order by Tabla,Columna";
        try {
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    String Columna = rs.getString("Columna");
                    String Funcion = rs.getString("Funcion");
                    String Campo = rs.getString("Campo");
                    String Parametros = rs.getString("Parametros");
                    String PatronFecha = rs.getString("PatronFecha");
                    String posicionInicio = rs.getString("posicionInicio");
                    String cantCaracteres = rs.getString("cantCaracteres");
                    String Prioridad = rs.getString("Prioridad_Fono");
                    
                    HashMap hm = new HashMap();
                    hm.put("Columna", Columna);
                    hm.put("Funcion", Funcion);
                    hm.put("Campo", Campo);
                    hm.put("Parametros", Parametros);
                    hm.put("PatronFecha", PatronFecha);
                    hm.put("posicionInicio", posicionInicio);
                    hm.put("cantCaracteres", cantCaracteres);
                    hm.put("Prioridad_Fono", Prioridad);
                    Columns.add(hm);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.disconnect();
        }
        return Columns;
    }
    public List<HashMap<String, String>> getColumnsTemplateTXT(String Template,String Tabla,String SheetTemplate){
        List<HashMap<String, String>> Columns;
        Columns = new ArrayList<HashMap<String, String>>();
        DB db = new DB();
        //String sql = "SELECT id_template,Columna as Columna,GROUP_CONCAT(posicionInicio) as posicionInicio,GROUP_CONCAT(cantCaracteres) as cantCaracteres,id_sheet,Funcion,Parametros,Tabla,Campo,PatronFecha,GROUP_CONCAT(Prioridad_Fono) as Prioridad_Fono FROM Columnas_Template_Carga where id_template='"+Template+"' and Tabla='"+Tabla+"' and id_sheet='"+SheetTemplate+"' GROUP BY Campo order by Tabla,Columna";
        String sql = "SELECT\n" +
                    "	id_template,\n" +
                    "	Columna as Columna,\n" +
                    "	SUBSTRING((select STUFF(ColTmp.posicionInicio,1,0,',') from Columnas_Template_Carga ColTmp where ColTmp.id_template='"+Template+"' and ColTmp.Tabla='"+Tabla+"' and ColTmp.Campo=Columnas_Template_Carga.Campo FOR XML PATH('')),2,1000000) as posicionInicio,\n" +
                    "	SUBSTRING((select STUFF(ColTmp.cantCaracteres,1,0,',') from Columnas_Template_Carga ColTmp where ColTmp.id_template='"+Template+"' and ColTmp.Tabla='"+Tabla+"' and ColTmp.Campo=Columnas_Template_Carga.Campo FOR XML PATH('')),2,1000000) as cantCaracteres,\n" +
                    "	id_sheet,\n" +
                    "	Funcion,\n" +
                    "	Parametros,\n" +
                    "	Tabla,\n" +
                    "	Campo,\n" +
                    "	PatronFecha,\n" +
                    "	SUBSTRING((select STUFF(ColTmp.Prioridad_Fono,1,0,',') from Columnas_Template_Carga ColTmp where ColTmp.id_template='"+Template+"' and ColTmp.Tabla='"+Tabla+"' and ColTmp.Campo=Columnas_Template_Carga.Campo FOR XML PATH('')),2,1000000) as Prioridad_Fono\n" +
                    "FROM\n" +
                    "	Columnas_Template_Carga\n" +
                    "where\n" +
                    "	id_template='"+Template+"' and\n" +
                    "	Tabla='"+Tabla+"' and\n" +
                    "	id_sheet='"+SheetTemplate+"' \n" +
                    "GROUP BY \n" +
                    "	id_template,\n" +
                    "	Columna,\n" +
                    "	id_sheet,\n" +
                    "	Funcion,\n" +
                    "	Parametros,\n" +
                    "	Tabla,\n" +
                    "	Campo,\n" +
                    "	PatronFecha\n" +
                    "order by \n" +
                    "	Tabla";
        try {
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    String Columna = rs.getString("Columna");
                    String Funcion = rs.getString("Funcion");
                    String Campo = rs.getString("Campo");
                    String Parametros = rs.getString("Parametros");
                    String PatronFecha = rs.getString("PatronFecha");
                    String posicionInicio = rs.getString("posicionInicio");
                    String cantCaracteres = rs.getString("cantCaracteres");
                    String Prioridad = rs.getString("Prioridad_Fono");
                    
                    HashMap hm = new HashMap();
                    hm.put("Columna", Columna);
                    hm.put("Funcion", Funcion);
                    hm.put("Campo", Campo);
                    hm.put("Parametros", Parametros);
                    hm.put("PatronFecha", PatronFecha);
                    hm.put("posicionInicio", posicionInicio);
                    hm.put("cantCaracteres", cantCaracteres);
                    hm.put("Prioridad_Fono", Prioridad);
                    Columns.add(hm);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.disconnect();
        }
        return Columns;
    }
    public List<HashMap<String, String>> getSheetsTemplate(String Template){
        List<HashMap<String, String>> Sheets;
        Sheets = new ArrayList<HashMap<String, String>>();
        DB db = new DB();
        String sql = "SELECT id,Sheet FROM Sheet_Template_Carga where id_template='"+Template+"' and TipoCarga='"+cargaasignacionautomatica.CargaAsignacionAutomatica.TipoCarga+"' order by Sheet";
        try {
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    String idSheet = rs.getString("id");
                    String Sheet = rs.getString("Sheet");
                    HashMap hm = new HashMap();
                    hm.put("idSheet", idSheet);
                    hm.put("Sheet", Sheet);
                    Sheets.add(hm);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.disconnect();
        }
        return Sheets;
    }
    public List<HashMap<String, String>> getTablesTemplate(String Template,String SheetTemplate){
        List<HashMap<String, String>> Columns;
        Columns = new ArrayList<HashMap<String, String>>();
        DB db = new DB();
        String sql = "SELECT Tabla FROM Columnas_Template_Carga where id_template='"+Template+"' and id_sheet='"+SheetTemplate+"' group by Tabla order by Tabla";
        try {
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    String Tabla = rs.getString("Tabla");
                    HashMap hm = new HashMap();
                    hm.put("Tabla", Tabla);
                    Columns.add(hm);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.disconnect();
        }
        return Columns;
    }
}
