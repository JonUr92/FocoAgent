/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.Personas;

import cargaasignacionautomatica.Class.DB.DB;
import cargaasignacionautomatica.Class.Functions.Functions;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import static javax.management.Query.lt;

/**
 *
 * @author jonthan
 */
public class Persona {
    private static Functions functions = new Functions();
    public void Insert(ArrayList Personas,String Cedente,String Mandante) throws SQLException{
        Functions functions = new Functions();
        ArrayList<String> Campos = new ArrayList<String>();
        ArrayList<String> CamposMerge = new ArrayList<String>();
        ArrayList<String> CamposCarga = new ArrayList<String>();
        ArrayList<String> Values = new ArrayList<String>();
        ArrayList<String> Ruts = new ArrayList<String>();
        
        ArrayList<String> ArrayValues = new ArrayList<String>();
        ArrayList<String> ArrayRuts = new ArrayList<String>();
        int Cont = 0;
        int ContValues = 1;
        int ContRuts = 1;
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
                    CamposMerge.add("Source."+FieldName);
                    CamposCarga.add(FieldName);
                }
                if(FieldName.equalsIgnoreCase("Rut")){
                    ContRuts++;
                    Ruts.add("'"+FieldValue+"'");
                    if(ContRuts >= 1000){
                        ArrayRuts.add(functions.implode(Ruts));
                        Ruts.clear();
                        ContRuts = 1;
                    }
                }
                Value.add("'"+FieldValue+"'");
            }
            if(Cont == 0){
                Campos.add("Id_Cedente");
                Campos.add("Mandante");
                CamposMerge.add("Source.Id_Cedente");
                CamposMerge.add("Source.Mandante");
            }
            Value.add("'"+Cedente+"'");
            Value.add("'"+Mandante+"'");
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
            ArrayRuts.add(functions.implode(Ruts));
            Ruts.clear();
        }
        if(ArrayValues.size()>0){
            String sqlUpdate = "";
            String sqlUpdatePeriodo = "";
            for (String ValueRuts : ArrayRuts) {
                sqlUpdate += "update Persona set Id_Cedente=CONCAT(REPLACE(Id_Cedente,',"+Cedente+"',''),',','"+Cedente+"'), Mandante = CONCAT(REPLACE(Mandante,',"+Mandante+"',''),',','"+Mandante+"') where Rut in ("+ValueRuts+");";
                sqlUpdatePeriodo += "update Persona_Periodo set Id_Cedente=CONCAT(REPLACE(Id_Cedente,',"+Cedente+"',''),',','"+Cedente+"'), Mandante = CONCAT(REPLACE(Mandante,',"+Mandante+"',''),',','"+Mandante+"') where Rut in ("+ValueRuts+");";
            }
            PreCarga(ArrayValues,Campos,sqlUpdate,sqlUpdatePeriodo,Cedente,Mandante);
            DB db = new DB();
            String SqlCamposCargas = "merge into campos_cargas_asignaciones as Target\n" +
                        "using (values (GETDATE(),'Persona','"+functions.implode(CamposCarga)+"','"+Cedente+"')) as Source (fecha,tabla,campos,Id_Cedente)\n" +
                        "on Target.tabla=Source.tabla and Target.Id_Cedente=Source.Id_Cedente\n" +
                        "when matched then \n" +
                        "update set Target.campos = '"+functions.implode(CamposCarga)+"'\n" +
                        "when not matched then\n" +
                        "insert (fecha,tabla,campos,Id_Cedente) values (Source.fecha,Source.tabla,Source.campos,Source.Id_Cedente);";
            db.query(SqlCamposCargas, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        }
    }
    public void PreCarga(ArrayList ArrayValues,ArrayList Campos, String sqlUpdateValues, String sqlUpdatePeriodoValues, String Cedente,String Mandante) throws SQLException, MySQLSyntaxErrorException{
        Boolean ToReturn = false;
        String sql = "";
        DB db = new DB();
        
        ArrayList<String> ColumnasPersonas = getColumnas();
            sql = "INSERT INTO Persona_Historico ("+functions.implode(ColumnasPersonas)+") SELECT "+functions.implode(ColumnasPersonas)+" FROM Persona WHERE '"+Cedente+"' in (select * from STRING_SPLIT(Id_Cedente,',')) ON DUPLICATE KEY UPDATE Persona_Historico.Id_Cedente = CONCAT(REPLACE(Persona_Historico.Id_Cedente,',"+Cedente+"',''),',','"+Cedente+"'),Persona_Historico.Mandante = CONCAT(REPLACE(Persona_Historico.Mandante,',"+Mandante+"',''),',','"+Mandante+"')";
            //ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);

            sql = "UPDATE Persona SET Id_Cedente = REPLACE(REPLACE(Id_Cedente,',"+Cedente+"',''),'"+Cedente+",','') WHERE '"+Cedente+"' in (select * from STRING_SPLIT(Id_Cedente,','))";
            ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);

            sql = "DELETE FROM Persona WHERE Id_Cedente = "+Cedente;
            ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);

            sql = "UPDATE Persona_Periodo SET Id_Cedente = REPLACE(Id_Cedente,',"+Cedente+"','') WHERE '"+Cedente+"' in (select * from STRING_SPLIT(Id_Cedente,','))";
            ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);

            sql = "DELETE FROM Persona_Periodo WHERE Id_Cedente = "+Cedente;
            ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn); 
            
            /*
                *   Insert Values into Persona_Periodo Table
            */
            ToReturn = db.query(sqlUpdatePeriodoValues, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            ToReturn = db.query(sqlUpdateValues, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            for (Object Values : ArrayValues) {
                sql = "insert into Persona_Periodo ("+functions.implode(Campos)+")  values "+Values.toString();
                ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
                
                sql = "insert into Persona ("+functions.implode(Campos)+")  values "+Values.toString();
                ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            }
    }
    public static ArrayList<String> getColumnas(){
        ArrayList<String> Columns =  new ArrayList<String>();
        DB db = new DB();
        String sql = "select COLUMN_NAME as Field from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'Persona'";
        try {
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    String Field = rs.getString("Field");
                    switch(Field){
                        case "id_persona":
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
    public int getCantidadPersonas(String Cedente){
        int ToReturn = 0;
        DB db = new DB();
        String sql = "select count(*) as CantPersonas from Persona where '"+Cedente+"' in (select * from STRING_SPLIT(Id_Cedente,','))";
        try {
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    ToReturn = rs.getInt("CantPersonas");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.disconnect();
        }
        return ToReturn;
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
            String SqlUpdate = "UPDATE "+ArrayMarca[0]+" SET "+Updates+" WHERE '"+Cedente+"' in (select * from STRING_SPLIT(Id_Cedente,',')) and "+Relacion;
            UpdateList.add(SqlUpdate);
        }
        for (Object Update : UpdateList) {
            DB db = new DB();
            db.query(Update.toString(), cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        }
        System.out.println(UpdateList);
    }
}
