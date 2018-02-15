/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ArchivosDeTexto;

import cargaasignacionautomatica.Class.Carga.Carga;
import cargaasignacionautomatica.Class.Excel.FlexibleExcelReaderExample;
import cargaasignacionautomatica.Class.Functions.Functions;
import cargaasignacionautomatica.Class.Templates.Template;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 *
 * @author CobrandingMariana
 */
public class ArchivoDeTexto {
    public ArrayList[] ReadCSV(String FilePath) throws SQLException{
        Template template = new Template();
        Functions functions = new Functions();
        ArrayList[] ToReturn = new ArrayList[7];
        ArrayList Deudas = new ArrayList();
        ArrayList Personas = new ArrayList();
        ArrayList Mails = new ArrayList();
        ArrayList Fonos = new ArrayList();
        ArrayList Direcciones = new ArrayList();
        ArrayList Pagos = new ArrayList();
        ArrayList Gestiones = new ArrayList();
                
        String[] ArrayPath = FilePath.split("\\/");
        int FileNamePos = ArrayPath.length - 1;
        String Cedente = ArrayPath[FileNamePos - 1];
        String Mandante = ArrayPath[FileNamePos - 2];
        String FileName = ArrayPath[FileNamePos];
        
        List<HashMap<String, String>> Template = template.getTemplate(Cedente);
        if(Template.size() > 0){
            Carga carga = new Carga();
            carga.changeCommentProcess("Plantilla Localizada");
        }
        for (int i=0;i<Template.size();i++){
            HashMap<String, String> templatehm = Template.get(i);
            String TemplateID = templatehm.get("TemplateID");
            String Separador_Cabecero = templatehm.get("Separador_Cabecero");
            boolean haveHeader = templatehm.get("haveHeader").equals("1") ? true : false;
            
            List<HashMap<String, String>> Sheets = template.getSheetsTemplate(TemplateID);
            for (int sheet=0;sheet<Sheets.size();sheet++){
                HashMap<String, String> sheethm = Sheets.get(sheet);
                String Sheet = sheethm.get("Sheet");
                String SheetTemplate = sheethm.get("idSheet");
                List<HashMap<String, String>> Tablas = template.getTablesTemplate(TemplateID,SheetTemplate);
                for (int j=0;j<Tablas.size();j++){
                    HashMap<String, String> tablashm = Tablas.get(j);
                    String Tabla = tablashm.get("Tabla");
                    List<HashMap<String, String>> Columns = template.getColumnsTemplate(TemplateID,Tabla,SheetTemplate);
                    BufferedReader br = null;
                    try{
                        br =new BufferedReader(new FileReader(FilePath));
                        String line = br.readLine();
                        int Cont = 0;
                        int LineaEmpieza = haveHeader ? 0 : -1;
                        while (null!=line) {
                            if(Cont > LineaEmpieza){
                                String [] fields = functions.split(line, Separador_Cabecero);
                                //String [] fields = line.split(Separador_Cabecero);
                                String RowValues = "";
                                if(Columns.size() > 0){
                                    for (int k=0;k<Columns.size();k++)
                                    {
                                        HashMap<String, String> columnshm = Columns.get(k);
                                        //int Columna = Integer.parseInt(columnshm.get("Columna"));
                                        String Columnas = columnshm.get("Columna");
                                        String Funcion = columnshm.get("Funcion");
                                        String Campo = columnshm.get("Campo");
                                        String Parametros = columnshm.get("Parametros");
                                        String PatronFecha = columnshm.get("PatronFecha");
                                        String Prioridad = columnshm.get("Prioridad_Fono");
                                        String Value = "";
                                        ArrayList<String> ValuesArray = new ArrayList<String>();
                                        String [] ColumnasArray = Columnas.split(",");
                                        String [] PrioridadArray = Prioridad.split(",");
                                        int ContPrioridad = 0;
                                        for(String Columna:ColumnasArray){
                                            Value = fields[Integer.parseInt(Columna)];
                                            //if(functions.isDateColumn(Tabla, Campo)){
                                            if(!PatronFecha.equals("")){
                                                if(!functions.isDateValid(Value)){
                                                    if((Value.indexOf(':')>=0) && (Value.length() == 8) ){
                                                        Value = functions.StringToTime(Value,PatronFecha);
                                                    }else{
                                                        if((Value.length() > 8) && ((Value.indexOf(':')>=0))){
                                                            Value = functions.StringToDateTime(Value,PatronFecha);
                                                        }else{
                                                            Value = functions.StringToDate(Value,PatronFecha);
                                                        }
                                                    }
                                                    //Value = functions.StringToDate(Value,PatronFecha);
                                                }
                                            }
                                            Value = Value.replace("'", "");
                                            if(!Value.equals("")){
                                                Value = Value.replace("|","");
                                                switch(Tabla){
                                                    case "fono_cob":
                                                        switch(Campo){
                                                            case "formato_subtel":
                                                                Value = Value.replaceAll("[^0-9]", "");
                                                                Value = PrioridadArray[ContPrioridad]+"@"+Value;
                                                                break;
                                                        }
                                                    break;
                                                }
                                                ValuesArray.add(Value);
                                            }
                                            ContPrioridad++;
                                        }
                                        //System.out.println(functions.implode(ValuesArray," "));
                                        Value = functions.implode(ValuesArray," ");
                                        RowValues += Value+"|"+Campo+"[]";
                                    }
                                    RowValues = RowValues.substring(0,RowValues.length() - 2);
                                    //System.out.println(Tabla+"{"+RowValues+"}");
                                    switch(Tabla){
                                        case "Persona":
                                            Personas.add(RowValues);
                                        break;
                                        case "Deuda":
                                            Deudas.add(RowValues);
                                        break;
                                        case "Mail":
                                            String[] MailsTmp = null;
                                            String[] FieldsMails = RowValues.split("\\[]");
                                            for(String Fono:FieldsMails){
                                                String[] FieldsArray = Fono.split("\\|");
                                                String FieldValue = FieldsArray[0];
                                                String FieldName = FieldsArray[1];
                                                switch(FieldName){
                                                    case "correo_electronico":
                                                        MailsTmp = FieldValue.split("\\ ");
                                                        break;
                                                }
                                            }
                                            if(MailsTmp.length > 0){
                                                for(String MailNew:MailsTmp){
                                                    if(!MailNew.equals("")){
                                                        String Tmp = "";
                                                        for(String Field:FieldsMails){
                                                            String[] FieldArray = Field.split("\\|");
                                                            String FieldValue = FieldArray[0];
                                                            String FieldName = FieldArray[1];
                                                            switch(FieldName){
                                                                case "correo_electronico":
                                                                    Tmp += MailNew+"|"+FieldName+"[]";
                                                                    break;
                                                                default:
                                                                    Tmp += Field+"[]";
                                                                    break;
                                                            }
                                                        }
                                                        Tmp = Tmp.substring(0,Tmp.length() - 2);
                                                        Mails.add(Tmp);
                                                    }
                                                }
                                            }
                                            //Mails.add(RowValues);
                                        break;
                                        case "fono_cob":
                                            String[] FonosTmp = null;
                                            String[] Fields = RowValues.split("\\[]");
                                            for(String Fono:Fields){
                                                String[] FieldsArray = Fono.split("\\|");
                                                String FieldValue = FieldsArray[0];
                                                String FieldName = FieldsArray[1];
                                                switch(FieldName){
                                                    case "formato_subtel":
                                                        FonosTmp = FieldValue.split("\\ ");
                                                        break;
                                                }
                                            }
                                            if(FonosTmp.length > 0){
                                                for(String FonoNew:FonosTmp){
                                                    if(!FonoNew.equals("")){
                                                        String Tmp = "";
                                                        for(String Field:Fields){
                                                            String[] FieldArray = Field.split("\\|");
                                                            String FieldValue = FieldArray[0];
                                                            String FieldName = FieldArray[1];
                                                            switch(FieldName){
                                                                case "formato_subtel":
                                                                    String[] FonoArray = FonoNew.split("\\@");
                                                                    String Prioridad = FonoArray[0];
                                                                    String FonoValue = FonoArray[1];
                                                                    
                                                                    Tmp += FonoValue+"|"+FieldName+"[]";
                                                                    Tmp += Prioridad+"|Prioridad_Fono[]";
                                                                    break;
                                                                default:
                                                                    Tmp += Field+"[]";
                                                                    break;
                                                            }
                                                        }
                                                        Tmp = Tmp.substring(0,Tmp.length() - 2);
                                                        Fonos.add(Tmp);
                                                    }
                                                }
                                            }
                                            //Fonos.add(RowValues);
                                        break;
                                        case "Direcciones":
                                            Direcciones.add(RowValues);
                                        break;
                                        case "pagos_deudas":
                                            Pagos.add(RowValues);
                                        break;
                                        case "gestion_ult_trimestre":
                                            Gestiones.add(RowValues);
                                        break;
                                    }
                                }
                            }
                            line = br.readLine();
                            Cont++;
                        }
                    }catch (Exception e) {
                        System.out.println(e.getStackTrace());
                    }finally {
                        if (null!=br) {
                            try {
                                br.close();
                            } catch (IOException ex) {
                                Logger.getLogger(ArchivoDeTexto.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
        }
        ToReturn[0] = Personas;
        ToReturn[1] = Deudas;
        ToReturn[2] = Mails;
        ToReturn[3] = Direcciones;
        ToReturn[4] = Fonos;
        ToReturn[5] = Pagos;
        ToReturn[6] = Gestiones;
        return ToReturn;
    }
    public ArrayList[] ReadTXT(String FilePath) throws SQLException{
        Template template = new Template();
        Functions functions = new Functions();
        ArrayList[] ToReturn = new ArrayList[7];
        ArrayList Deudas = new ArrayList();
        ArrayList Personas = new ArrayList();
        ArrayList Mails = new ArrayList();
        ArrayList Fonos = new ArrayList();
        ArrayList Direcciones = new ArrayList();
        ArrayList Pagos = new ArrayList();
        ArrayList Gestiones = new ArrayList();
                
        String[] ArrayPath = FilePath.split("\\/");
        int FileNamePos = ArrayPath.length - 1;
        String Cedente = ArrayPath[FileNamePos - 1];
        String Mandante = ArrayPath[FileNamePos - 2];
        String FileName = ArrayPath[FileNamePos];
        
        List<HashMap<String, String>> Template = template.getTemplate(Cedente);
        if(Template.size() > 0){
            Carga carga = new Carga();
            carga.changeCommentProcess("Plantilla Localizada");
        }
        for (int i=0;i<Template.size();i++){
            HashMap<String, String> templatehm = Template.get(i);
            String TemplateID = templatehm.get("TemplateID");
            String Separador_Cabecero = templatehm.get("Separador_Cabecero");
            boolean haveHeader = templatehm.get("haveHeader").equals("1") ? true : false;
            String SheetTemplate = "";//sheethm.get("idSheet");
            String [] FilesTXTArray = cargaasignacionautomatica.CargaAsignacionAutomatica.FilesTXT.split("\\&");
            for(String File:FilesTXTArray){
                String [] FileArray = File.split("\\|");
                if(FileName.equalsIgnoreCase(FileArray[0]+".txt")){
                    SheetTemplate = FileArray[1];
                }
            }
                List<HashMap<String, String>> Tablas = template.getTablesTemplate(TemplateID,SheetTemplate);
                for (int j=0;j<Tablas.size();j++){
                    HashMap<String, String> tablashm = Tablas.get(j);
                    String Tabla = tablashm.get("Tabla");
                    if(Tabla.equals("fono_cob")){
                        System.err.println("");
                    }
                    List<HashMap<String, String>> Columns = template.getColumnsTemplateTXT(TemplateID,Tabla,SheetTemplate);
                    BufferedReader br = null;
                    try{
                        br =new BufferedReader(new FileReader(FilePath));
                        String line = br.readLine();
                        int Cont = 0;
                        int LineaEmpieza = haveHeader ? 0 : -1;
                        while (null!=line) {
                            if(Cont > LineaEmpieza){
                                
                                //String [] fields = line.split(Separador_Cabecero);
                                String RowValues = "";
                                if(Columns.size() > 0){
                                    for (int k=0;k<Columns.size();k++)
                                    {
                                        HashMap<String, String> columnshm = Columns.get(k);
                                        //int Columna = Integer.parseInt(columnshm.get("Columna"));
                                        String Funcion = columnshm.get("Funcion");
                                        String Campo = columnshm.get("Campo");
                                        String Parametros = columnshm.get("Parametros");
                                        String PatronFecha = columnshm.get("PatronFecha");
                                        /*int posicionInicio = Integer.parseInt(columnshm.get("posicionInicio"));
                                        int cantCaracteres = Integer.parseInt(columnshm.get("cantCaracteres"));*/
                                        String posicionInicioString = columnshm.get("posicionInicio");
                                        String cantCaracteresString = columnshm.get("cantCaracteres");
                                        String Prioridad = columnshm.get("Prioridad_Fono");
                                        String [] posicionInicioArray = posicionInicioString.split(",");
                                        String [] cantCaracteresArray = cantCaracteresString.split(",");
                                        String [] PrioridadArray = Prioridad.split(",");
                                        String Value = "";
                                        int cont = 0;
                                        ArrayList<String> ValuesArray = new ArrayList<String>();
                                        for(String PosicionInicio:posicionInicioArray){
                                            int posicionInicio = Integer.parseInt(posicionInicioArray[cont]);
                                            int cantCaracteres = Integer.parseInt(cantCaracteresArray[cont]);
                                            Value = line.substring(posicionInicio - 1,(posicionInicio + cantCaracteres) - 1);
                                            Value = Value.replaceFirst ("^0*", "");
                                            Value = Value.trim();
                                            if(!Value.equals("")){
                                                if(functions.isNumeric(Value)){
                                                    Value = Long.parseLong(Value)+"";
                                                }
                                            }
                                            if(!PatronFecha.equals("")){
                                                if(!functions.isDateValid(Value)){
                                                    Value = functions.StringToDate(Value,PatronFecha);
                                                }
                                            }
                                            Value = Value.replace("|","");
                                            switch(Tabla){
                                                case "fono_cob":
                                                    switch(Campo){
                                                        case "formato_subtel":
                                                            Value = Value.replaceAll("[^0-9]", "");
                                                            if(!Value.equals("")){
                                                                Value = PrioridadArray[cont]+"@"+Value;
                                                            }
                                                            break;
                                                    }
                                                break;
                                            }
                                            cont++;
                                            ValuesArray.add(Value);
                                        }
                                        Value = functions.implode(ValuesArray," ");
                                        Value = Value.replace("'", "");
                                        Value = Value.replace("|","");
                                        RowValues += Value+"|"+Campo+"[]";
                                    }
                                    RowValues = RowValues.substring(0,RowValues.length() - 2);
                                    //System.out.println(Tabla+"{"+RowValues+"}");
                                    switch(Tabla){
                                        case "Persona":
                                            Personas.add(RowValues);
                                        break;
                                        case "Deuda":
                                            Deudas.add(RowValues);
                                        break;
                                        case "Mail":
                                            Mails.add(RowValues);
                                        break;
                                        case "fono_cob":
                                            String[] FonosTmp = null;
                                            String[] Fields = RowValues.split("\\[]");
                                            for(String Fono:Fields){
                                                String[] FieldsArray = Fono.split("\\|");
                                                String FieldValue = FieldsArray[0];
                                                String FieldName = FieldsArray[1];
                                                switch(FieldName){
                                                    case "formato_subtel":
                                                        FonosTmp = FieldValue.split("\\ ");
                                                        break;
                                                }
                                            }
                                            if(FonosTmp.length > 0){
                                                for(String FonoNew:FonosTmp){
                                                    if(!FonoNew.equals("")){
                                                        String Tmp = "";
                                                        for(String Field:Fields){
                                                            String[] FieldArray = Field.split("\\|");
                                                            String FieldValue = FieldArray[0];
                                                            String FieldName = FieldArray[1];
                                                            switch(FieldName){
                                                                case "formato_subtel":
                                                                    //Tmp += FonoNew+"|"+FieldName+"[]";
                                                                    String[] FonoArray = FonoNew.split("\\@");
                                                                    String Prioridad = FonoArray[0];
                                                                    String FonoValue = FonoArray[1];
                                                                    
                                                                    Tmp += FonoValue+"|"+FieldName+"[]";
                                                                    Tmp += Prioridad+"|Prioridad_Fono[]";
                                                                    break;
                                                                default:
                                                                    Tmp += Field+"[]";
                                                                    break;
                                                            }
                                                        }
                                                        Tmp = Tmp.substring(0,Tmp.length() - 2);
                                                        Fonos.add(Tmp);
                                                    }
                                                }
                                            }
                                            //Fonos.add(RowValues);
                                        break;
                                        case "Direcciones":
                                            Direcciones.add(RowValues);
                                        break;
                                        case "pagos_deudas":
                                            Pagos.add(RowValues);
                                        break;
                                        case "gestion_ult_trimestre":
                                            Gestiones.add(RowValues);
                                        break;
                                    }
                                }
                            }
                            line = br.readLine();
                            Cont++;
                        }
                    }catch (Exception e) {
                        System.out.println(e.getMessage());
                    }finally {
                        if (null!=br) {
                            try {
                                br.close();
                            } catch (IOException ex) {
                                Logger.getLogger(ArchivoDeTexto.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
        }
        ToReturn[0] = Personas;
        ToReturn[1] = Deudas;
        ToReturn[2] = Mails;
        ToReturn[3] = Direcciones;
        ToReturn[4] = Fonos;
        ToReturn[5] = Pagos;
        ToReturn[6] = Gestiones;
        return ToReturn;
    }
}
