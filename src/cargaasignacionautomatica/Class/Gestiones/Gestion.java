/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.Gestiones;

import cargaasignacionautomatica.Class.DB.DB;
import cargaasignacionautomatica.Class.Functions.Functions;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author COBRANDINGJONATHAN
 */
public class Gestion {
    Functions functions = new Functions();
    public void Insert(ArrayList Personas,String Cedente,String Mandante) throws SQLException{
        Functions functions = new Functions();
        ArrayList<String> Campos = new ArrayList<String>();
        ArrayList<String> Values = new ArrayList<String>();
        
        ArrayList<String> ArrayValues = new ArrayList<String>();
        int Cont = 0;
        int ContValues = 1;
        for (Object Persona : Personas) {
            ArrayList<String> Value = new ArrayList<String>();
            String StringTmp = Persona.toString();
            String[] Fields = StringTmp.split("\\[]");
            for(String Field:Fields){
                String[] ArrayField = Field.split("\\|");
                String FieldValue = ArrayField[0];
                String FieldName = ArrayField[1];
                if(Cont == 0){
                    Campos.add(FieldName);
                }
                Value.add("'"+FieldValue+"'");
            }
            Values.add("("+functions.implode(Value)+")");
            Cont++;
            ContValues++;
            if(ContValues >= 1000){
                ArrayValues.add(functions.implode(Values));
                Values.clear();
                ContValues = 1;
            }
        }
        if(Values.size()>0){
            ArrayValues.add(functions.implode(Values));
            Values.clear();
        }
        if(ArrayValues.size()>0){
            PreCarga(ArrayValues,Campos);
        }
    }
    public void PreCarga(ArrayList ArrayValues, ArrayList Campos) throws SQLException{
        Boolean ToReturn = false;
        DB db = new DB();

            /*
                *   Insert Values into Direcciones Table
            */
            String sql = "";
            for (Object Values : ArrayValues) {
                sql = "insert into gestion_ult_trimestre ("+functions.implode(Campos)+")  values "+Values.toString();
                ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            } 
    }
}
