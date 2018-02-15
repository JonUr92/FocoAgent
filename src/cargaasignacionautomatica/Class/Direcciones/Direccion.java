/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.Direcciones;

import cargaasignacionautomatica.Class.DB.DB;
import cargaasignacionautomatica.Class.Functions.Functions;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author jonthan
 */
public class Direccion {
    Functions functions = new Functions();
    public void Insert(ArrayList Personas,String Cedente,String Mandante) throws SQLException{
        Functions functions = new Functions();
        ArrayList<String> Campos = new ArrayList<String>();
        ArrayList<String> Values = new ArrayList<String>();
        ArrayList<String> CamposCedente = new ArrayList<String>();
        ArrayList<String> ValuesCedente = new ArrayList<String>();
        ArrayList<String> CamposCarga = new ArrayList<String>();
        
        ArrayList<String> ArrayValues = new ArrayList<String>();
        ArrayList<String> ArrayValuesCedentes = new ArrayList<String>();
        
        int Cont = 0;
        int ContValues = 1;
        int ContValuesCedentes = 1;
        for (Object Persona : Personas) {
            ArrayList<String> Value = new ArrayList<String>();
            ArrayList<String> ValueCedente = new ArrayList<String>();
            String StringTmp = Persona.toString();
            String[] Fields = StringTmp.split("\\[]");
            for(String Field:Fields){
                String[] ArrayField = Field.split("\\|");
                String FieldValue = ArrayField[0];
                String FieldName = ArrayField[1];
                if(Cont == 0){
                    Campos.add(FieldName);
                    CamposCedente.add(FieldName);
                    CamposCarga.add(FieldName);
                }
                Value.add("'"+FieldValue+"'");
                ValueCedente.add("'"+FieldValue+"'");
            }
            if(Cont == 0){
                CamposCedente.add("Id_Cedente");
            }
            ValueCedente.add("'"+Cedente+"'");
            Values.add("("+functions.implode(Value)+")");
            ValuesCedente.add("("+functions.implode(ValueCedente)+")");
            Cont++;
            ContValues++;
            ContValuesCedentes++;
            if(ContValues >= 1000){
                ArrayValues.add(functions.implode(Values));
                Values.clear();
                ContValues = 1;
            }
            if(ContValuesCedentes >= 1000){
                ArrayValuesCedentes.add(functions.implode(ValuesCedente));
                ValuesCedente.clear();
                ContValuesCedentes = 1;
            }
        }
        if(Values.size()>0){
            ArrayValues.add(functions.implode(Values));
            Values.clear();
        }
        if(ValuesCedente.size()>0){
            ArrayValuesCedentes.add(functions.implode(ValuesCedente));
            ValuesCedente.clear();
        }
        if(ArrayValues.size()>0){
            PreCarga(ArrayValues,Campos,ArrayValuesCedentes,CamposCedente,Cedente,Mandante);
            
            DB db = new DB();
            String SqlCamposCargas = "merge into campos_cargas_asignaciones as Target\n" +
                        "using (values (GETDATE(),'Direcciones','"+functions.implode(CamposCarga)+"','"+Cedente+"')) as Source (fecha,tabla,campos,Id_Cedente)\n" +
                        "on Target.tabla=Source.tabla and Target.Id_Cedente=Source.Id_Cedente\n" +
                        "when matched then \n" +
                        "update set Target.campos = '"+functions.implode(CamposCarga)+"'\n" +
                        "when not matched then\n" +
                        "insert (fecha,tabla,campos,Id_Cedente) values (Source.fecha,Source.tabla,Source.campos,Source.Id_Cedente);";
            db.query(SqlCamposCargas, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        }
    }
    public void PreCarga(ArrayList ArrayValues,ArrayList Campos,ArrayList ArrayValuesCedentes,ArrayList CamposCedentes, String Cedente,String Mandante) throws SQLException{
        Boolean ToReturn = false;
        String sql = "";
        DB db = new DB();
        
            sql = "DELETE FROM Direcciones_cedente WHERE Id_Cedente = "+Cedente;
            ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);

            /*
                *   Insert Values into Direcciones_Cedente Table
            */
            for (Object Values : ArrayValuesCedentes) {
                sql = "insert into Direcciones_cedente ("+functions.implode(CamposCedentes)+")  values "+Values.toString();
                ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            }
            /*
                *   Insert Values into Direcciones Table
            */
            for (Object Values : ArrayValues) {
                sql = "insert into Direcciones ("+functions.implode(Campos)+")  values "+Values.toString();
                ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            }
    }
    public static ArrayList<String> getColumnas(){
        ArrayList<String> Columns =  new ArrayList<String>();
        DB db = new DB();
        String sql = "select COLUMN_NAME as Field from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'Direcciones'";
        try {
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    String Field = rs.getString("Field");
                    switch(Field){
                        case "Id_Direccion":
                        break;
                        default:
                            Columns.add(Field);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.disconnect();
        }
        return Columns;
    }
    public void InsertMarca(ArrayList Rows,String Cedente,String Mandante) throws SQLException{
        String[] ArrayMarca = cargaasignacionautomatica.CargaAsignacionAutomatica.MarcaData.split("\\|");
        String[] CamposMarca = ArrayMarca[2].split("\\,");
        ArrayList<String> UpdateList = new ArrayList<String>();
        for (Object Row : Rows) {
            String[] Fields = Row.toString().split("\\[]");
            int Cont = 0;
            String Relacion = "";
            String Updates = "";
            for (Object Field : Fields) {
                String[] ArrayField = Field.toString().split("\\|");
                String Campo = ArrayField[1];
                String Value = ArrayField[0];
                if(Cont == 0){
                    Relacion = Campo+"='"+Value+"'";
                }else{
                    Updates += Campo+"='"+Value+"',";
                }
                Cont++;
            }
            Updates = Updates.substring(0,Updates.length() - 1);
            String SqlUpdate = "UPDATE "+ArrayMarca[0]+" SET "+Updates+" WHERE "+Relacion;
            UpdateList.add(SqlUpdate);
        }
        for (Object Update : UpdateList) {
            DB db = new DB();
            db.query(Update.toString(), cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        }
        System.out.println(UpdateList);
    }
}
