/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.Pagos;

import cargaasignacionautomatica.Class.Direcciones.*;
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
public class Pago {
    Functions functions = new Functions();
    public void Insert(ArrayList Personas,String Cedente,String Mandante) throws SQLException{
        Functions functions = new Functions();
        ArrayList<String> Campos = new ArrayList<String>();
        ArrayList<String> Values = new ArrayList<String>();
        ArrayList<String> CamposCedente = new ArrayList<String>();
        ArrayList<String> ValuesCedente = new ArrayList<String>();
        ArrayList<String> CamposCarga = new ArrayList<String>();
        
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
                    CamposCarga.add(FieldName);
                }
                Value.add("'"+FieldValue+"'");
            }
            if(Cont == 0){
                Campos.add("Id_Cedente");
                Campos.add("Cedente");
            }
            Value.add("'"+Cedente+"'");
            Value.add("'"+Cedente+"'");
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
            Date date = new Date();
            String sql = "INSERT IGNORE INTO pagos_deudas ("+functions.implode(Campos)+") values "+functions.implode(Values);
            //System.out.println(sql);
            PreCarga(ArrayValues,Campos,Cedente,Mandante);
            
            DB db = new DB();
            String SqlCamposCargas = "merge into campos_cargas_asignaciones as Target\n" +
                        "using (values (GETDATE(),'pagos_deudas','"+functions.implode(CamposCarga)+"','"+Cedente+"')) as Source (fecha,tabla,campos,Id_Cedente)\n" +
                        "on Target.tabla=Source.tabla and Target.Id_Cedente=Source.Id_Cedente\n" +
                        "when matched then \n" +
                        "update set Target.campos = '"+functions.implode(CamposCarga)+"'\n" +
                        "when not matched then\n" +
                        "insert (fecha,tabla,campos,Id_Cedente) values (Source.fecha,Source.tabla,Source.campos,Source.Id_Cedente);";
            db.query(SqlCamposCargas, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        }
    }
    public void PreCarga(ArrayList ArrayValues, ArrayList Campos, String Cedente,String Mandante) throws SQLException{
        Boolean ToReturn = false;
        String sql = "";
        DB db = new DB();

            /*
                *   Insert Values into Direcciones Table
            */
            for (Object Values : ArrayValues) {
                sql = "insert into pagos_deudas ("+functions.implode(Campos)+")  values "+Values.toString();
                ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            }
    }
}
