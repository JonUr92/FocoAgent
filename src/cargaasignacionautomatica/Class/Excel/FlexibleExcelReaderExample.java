/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.Excel;
import cargaasignacionautomatica.Class.Carga.Carga;
import cargaasignacionautomatica.Class.Functions.Functions;
import cargaasignacionautomatica.Class.Templates.Template;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.commons.collections4.*;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 * A Excel Reader for Foco-Estrategico Software
 * @author Ing. Jonathan Urbina
 *
 */
public class FlexibleExcelReaderExample {
    
    public ArrayList[] readBooksFromExcelFile(String excelFilePath, String TemplateID) throws IOException, InvalidFormatException {
        Functions functions = new Functions();
        Template template = new Template();
        ArrayList[] TablasArray = new ArrayList[7];
        ArrayList Deudas = new ArrayList();
        ArrayList Personas = new ArrayList();
        ArrayList Mails = new ArrayList();
        ArrayList Fonos = new ArrayList();
        ArrayList Direcciones = new ArrayList();
        ArrayList Pagos = new ArrayList();
        ArrayList Gestiones = new ArrayList();
        try{
            FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
            try(Workbook workbook = getWorkbook(inputStream, excelFilePath);){
                int i = 0;
                List<HashMap<String, String>> Sheets = template.getSheetsTemplate(TemplateID);
                for (int sheet=0;sheet<Sheets.size();sheet++){
                    HashMap<String, String> sheethm = Sheets.get(sheet);
                    String Sheet = sheethm.get("Sheet");
                    String SheetTemplate = sheethm.get("idSheet");
                    List<HashMap<String, String>> Tablas = template.getTablesTemplate(TemplateID,SheetTemplate);
                    for (int j=0;j<Tablas.size();j++){
                        HashMap<String, String> tablashm = Tablas.get(j);
                        String Tabla = tablashm.get("Tabla");
                        if(Tabla.equals("Deuda")){
                            System.out.println();
                        }
                        Sheet firstSheet = workbook.getSheetAt(Integer.parseInt(Sheet));
                        Iterator<Row> iterator = firstSheet.iterator();
                        List<HashMap<String, String>> Columns = template.getColumnsTemplate(TemplateID,Tabla,SheetTemplate);
                        int Cont = 0;
                        while (iterator.hasNext()) {
                            Cont++;
                            Row nextRow = iterator.next();
                            Iterator<Cell> cellIterator = nextRow.cellIterator();
                            if(nextRow.getRowNum() > 0){
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
                                        try{
                                            if(cellIterator.hasNext()){
                                                /*Cell cell = nextRow.getCell(Columna);
                                                String Value = "";
                                                if(cell != null){
                                                    Value = getCellValue(cell).toString();
                                                }
                                                if(!Funcion.equals("")){
                                                    Value = functions.Funciones(Value, Funcion, Parametros);
                                                }
                                                Value = Value.replace("'", "");*/
                                                
                                                String Value = "";
                                                ArrayList<String> ValuesArray = new ArrayList<String>();
                                                String [] ColumnasArray = Columnas.split(",");
                                                String [] PrioridadArray = Prioridad.split(",");
                                                int ContPrioridad = 0;
                                                for(String Columna:ColumnasArray){
                                                    Cell cell = nextRow.getCell(Integer.parseInt(Columna));
                                                    if(cell == null){
                                                        Value = "";
                                                    }else{
                                                        Value = getCellValue(cell,PatronFecha).toString();
                                                    }
                                       
                                                    if(!Funcion.equals("")){
                                                        Value = functions.Funciones(Value, Funcion, Parametros);
                                                    }
                                                    Value = Value.replace("'", "");

                                                    if(!Value.equals("")){
                                                        Value = Value.replace("|","");
                                                        switch(Tabla){
                                                            case "fono_cob":
                                                                switch(Campo){
                                                                    case "formato_subtel":
                                                                        Value = Value.replaceAll("[^0-9]", "");
                                                                        if(!Value.equals("")){
                                                                            Value = PrioridadArray[ContPrioridad]+"@"+Value;
                                                                        }
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
                                                //System.out.print(Value+"[]");
                                            }
                                            //System.out.println(template.Funciones(Value,Funcion,Parametros));
                                        }catch(Exception ex){
                                            ex.printStackTrace();
                                        }
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
                            i++;

                        }
                    }
                }
                /*
                    System.out.println(Personas);
                    System.out.println(Deudas);
                    System.out.println(Mails);
                    System.out.println(Fonos);
                    System.out.println(Direcciones);
                */
                TablasArray[0] = Personas;
                TablasArray[1] = Deudas;
                TablasArray[2] = Mails;
                TablasArray[3] = Direcciones;
                TablasArray[4] = Fonos;
                TablasArray[5] = Pagos;
                TablasArray[6] = Gestiones;
                //System.out.println(TablasArray);
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
        return TablasArray;
    }

    private Object getCellValue(Cell cell, String PatronFecha) {
        
        Functions functions = new Functions();
        
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();

            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            case 3:
                return cell.getStringCellValue();

            case Cell.CELL_TYPE_NUMERIC:
                    //return cell.getNumericCellValue();
                String Value = "";
                
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date dateCellValue = cell.getDateCellValue();

                    if (dateCellValue != null) {
                        Value =  new SimpleDateFormat(PatronFecha).format(dateCellValue);
                    }
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
                        }
                    }
                    return Value;
                } else {
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    return cell.getStringCellValue();
                    //return cell.getNumericCellValue();
                }

            case Cell.CELL_TYPE_FORMULA:
                //return cell.getRichStringCellValue();
                String value = "";
                switch (cell.getCachedFormulaResultType()){
                    case Cell.CELL_TYPE_STRING:
                        value = cell.getStringCellValue();
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        DecimalFormat format = new DecimalFormat("0.##");
                        value = format.format(cell.getNumericCellValue()) + "";
                        break;
                        default:
                }
                return value;
        }
        return "";
    }
    private Workbook getWorkbook(FileInputStream inputStream, String excelFilePath) throws IOException, InvalidFormatException {
        Workbook workbook = null;
        if (excelFilePath.endsWith("xlsx")) {
            File file = new File(excelFilePath);
            OPCPackage opcPackage = opcPackage = OPCPackage.open(file);
            Runtime rs =  Runtime.getRuntime();
            rs.gc();
            workbook = new XSSFWorkbook(opcPackage);
            rs.gc();
        } else if (excelFilePath.endsWith("xls")) {
                workbook = new HSSFWorkbook(inputStream);
        } else {
                throw new IllegalArgumentException("The specified file is not Excel file");
        }
        return workbook;
    }
    public ArrayList[] ReadExcel(String FilePath) throws IOException, InvalidFormatException, SQLException{
        Template template = new Template();
        ArrayList[] ToReturn = null;
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
        for (int j=0;j<Template.size();j++){
            HashMap<String, String> tablashm = Template.get(j);
            String TemplateID = tablashm.get("TemplateID");
            FlexibleExcelReaderExample reader = new FlexibleExcelReaderExample();
            ToReturn = reader.readBooksFromExcelFile(FilePath,TemplateID);
        }
        return ToReturn;
    }
    public ArrayList ReadMarca(String excelFilePath) throws IOException, InvalidFormatException {
        ArrayList[] TablasArray = new ArrayList[5];
        ArrayList Data = new ArrayList();
        String[] ArrayMarca = cargaasignacionautomatica.CargaAsignacionAutomatica.MarcaData.split("\\|");
        String[] CamposMarca = ArrayMarca[2].split("\\,");
        try{
            FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
            try(Workbook workbook = getWorkbook(inputStream, excelFilePath);){
                int i = 0;
                Sheet firstSheet = workbook.getSheetAt(0);
                Iterator<Row> iterator = firstSheet.iterator();
                while (iterator.hasNext()) {
                    Row nextRow = iterator.next();
                    Iterator<Cell> cellIterator = nextRow.cellIterator();
                    if(nextRow.getRowNum() > 0){
                        String RowValues = "";
                        try{
                            int Cont = 0;
                            if(cellIterator.hasNext()){
                                Cell cell;
                                cell = nextRow.getCell(0);
                                String Value = "";
                                if(cell != null){
                                    Value = getCellValue(cell,"").toString();
                                }
                                Value = Value.replace("'", "");
                                RowValues += Value+"|"+ArrayMarca[1]+"[]";
                                Cont++;
                                for (Object Campo : CamposMarca) {
                                    cell = nextRow.getCell(Cont);
                                    Value = "";
                                    if(cell != null){
                                        Value = Value.replace("|","");
                                        Value = getCellValue(cell,"").toString();
                                    }
                                    Value = Value.replace("'", "");
                                    RowValues += Value+"|"+Campo+"[]";
                                    Cont++;
                                }
                                
                                //System.out.print(Value+"[]");
                            }
                            //System.out.println(template.Funciones(Value,Funcion,Parametros));
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
                        RowValues = RowValues.substring(0,RowValues.length() - 2);
                        Data.add(RowValues);
                        //System.out.println(Tabla+"{"+RowValues+"}");
                    }
                    i++;
                }
                /*
                    System.out.println(Personas);
                    System.out.println(Deudas);
                    System.out.println(Mails);
                    System.out.println(Fonos);
                    System.out.println(Direcciones);
                */
                System.out.println(Data);
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
        return Data;
    }
}