/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.Deudas;

import cargaasignacionautomatica.Class.Carga.Carga;
import cargaasignacionautomatica.Class.Cedente.Cedente;
import cargaasignacionautomatica.Class.DB.DB;
import cargaasignacionautomatica.Class.Functions.Functions;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author jonthan
 */
public class Deuda {
    Functions functions = new Functions();
    public void Insert(ArrayList Personas,String Cedente,String Mandante) throws SQLException{
        Functions functions = new Functions();
        ArrayList<String> Campos = new ArrayList<String>();
        ArrayList<String> CamposCarga = new ArrayList<String>();
        ArrayList<String> Values = new ArrayList<String>();
        
        ArrayList<String> ArrayValues = new ArrayList<String>();
        Hashtable<String, String> ColumnsType = functions.getColumnsType("Deuda");
        
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
                String Type = ColumnsType.get(FieldName);
                switch(Type){
                    case "int":
                    case "float":
                    case "double":
                    case "decimal":
                        if(FieldValue.equals("")){
                            Value.add("'0'");
                        }else{
                            Value.add("'"+FieldValue+"'");
                        }
                        break;
                    default:
                        Value.add("'"+FieldValue+"'");
                        break;
                }
            }
            if(Cont == 0){
                Campos.add("Id_Cedente");
            }
            Value.add(Cedente);
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
            String sql = "INSERT INTO Deuda ("+functions.implode(Campos)+") values "+functions.implode(Values);
            String sqlTmp = "INSERT INTO Deuda_tmp ("+functions.implode(Campos)+") values "+functions.implode(Values);
            //System.out.println(sql);
            PreCarga(ArrayValues,Campos,Cedente,Mandante);
            
            DB db = new DB();
            //String SqlCamposCargas = "INSERT INTO campos_cargas_asignaciones (fecha,tabla,campos,Id_Cedente) values(GETDATE(),'Deuda','"+functions.implode(CamposCarga)+"','"+Cedente+"') ON DUPLICATE KEY UPDATE campos = '"+functions.implode(CamposCarga)+"'";
            String SqlCamposCargas = "merge into campos_cargas_asignaciones as Target\n" +
                        "using (values (GETDATE(),'Deuda','"+functions.implode(CamposCarga)+"','"+Cedente+"')) as Source (fecha,tabla,campos,Id_Cedente)\n" +
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
        
        ArrayList<String> ColumnasDeudas = getColumnas();
            sql = "INSERT INTO Deuda_Historico ("+functions.implode(ColumnasDeudas)+",fecha_descarga) SELECT "+functions.implode(ColumnasDeudas)+",GETDATE() FROM Deuda WHERE Id_Cedente =  "+Cedente;
            ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);

            for (Object Values : ArrayValues) {
                sql = "insert into Deuda_tmp ("+functions.implode(Campos)+")  values "+Values.toString();
                ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            }
            //ToReturn = db.query(SqlInsertValuesTmp, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            
            
            
            /*
                *   
            */
            
            Cedente cedente = new Cedente();
            List<HashMap<String, String>> CedenteData = cedente.getCedente(Cedente);
            String TipoCedente = "";
            String Nombre = "";
            String TipoRefresco = "";
            String InicioPeriodo = "";
            String FinPeriodo = "";
            for (int i=0;i<CedenteData.size();i++){
                HashMap<String, String> cedentehm = CedenteData.get(i);
                TipoCedente = cedentehm.get("TipoCedente");
                Nombre = cedentehm.get("Nombre");
                TipoRefresco = cedentehm.get("TipoRefresco");
                InicioPeriodo = cedentehm.get("InicioPeriodo");
            }
            
            switch(TipoRefresco){
                case "1":
                    String CampoClave = "";
                    switch(TipoCedente){
                        case "1": /* FACTURA */
                            CampoClave = "Numero_Factura";
                            break;
                        case "2": /* MASIVO */
                            CampoClave = "Numero_Operacion";
                            break;
                    }
                    if(!CampoClave.equals("")){
                        List<HashMap<String, String>> Tramos = cedente.getTramos(Cedente);
                        String SqlPagosErrados = "SELECT pagos_deudas.*, pagos_deudas."+CampoClave+" as NOperacion,CASE WHEN pagos_deudas.fec_compromiso != '0000-00-00' THEN pagos_deudas.fec_compromiso END as FechaCompromiso FROM Deuda_tmp inner join pagos_deudas on pagos_deudas.Rut = Deuda_tmp.Rut and pagos_deudas."+CampoClave+" = Deuda_tmp."+CampoClave+" WHERE Deuda_tmp.Id_Cedente='"+cargaasignacionautomatica.CargaAsignacionAutomatica.Cedente+"'";
                        try {
                            ResultSet rs = db.select(SqlPagosErrados, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
                            if(db.HaveData(rs)){
                                ArrayList<String> Values = new ArrayList<String>();
                                ArrayList<String> Ids = new ArrayList<String>();
                                while(rs.next()){
                                    String Rut = rs.getString("Rut");
                                    String NOperacion = rs.getString("NOperacion");
                                    String Deuda = rs.getString("Monto");
                                    String FechaPago = rs.getString("Fecha_Pago");
                                    String ID = rs.getString("id");
                                    String FechaVencimiento = rs.getString("Fecha_vencimiento");
                                    String DiasVencimiento = rs.getString("Dias_vencimiento");
                                    String Tramo = rs.getString("Tramo");
                                    String Gestion = rs.getString("Mejor_gestion");
                                    String Ejecutivo = rs.getString("Ejecutivo_mejor_gestion");
                                    String Fecha = rs.getString("Fecha_mejor_gestion");
                                    String FechaCompromiso = rs.getString("FechaCompromiso") == null ? "0000-00-00" : rs.getString("FechaCompromiso");
                                    String idGestion = rs.getString("Id_TipoGestion");
                                    
                                    String ValuesTmp = "(GETDATE(),'"+Rut+"','"+Deuda+"','"+NOperacion+"','"+FechaPago+"','"+FechaVencimiento+"','"+DiasVencimiento+"','"+Tramo+"','"+Gestion+"','"+Ejecutivo+"','"+Fecha+"','"+FechaCompromiso+"','"+idGestion+"','"+cargaasignacionautomatica.CargaAsignacionAutomatica.Mandante+"','"+cargaasignacionautomatica.CargaAsignacionAutomatica.Cedente+"')";
                                    Values.add(ValuesTmp);
                                    Ids.add(ID);
                                }
                                String Insert = "INSERT INTO pagos_deudas_errados (Fecha_Carga,Rut,Monto,"+CampoClave+",Fecha_Pago,Fecha_vencimiento,Dias_vencimiento,Tramo,Mejor_gestion,Ejecutivo_mejor_gestion,Fecha_mejor_gestion,Fec_compromiso,Id_TipoGestion,Mandante,Id_Cedente) values "+functions.implode(Values);
                                ToReturn = db.query(Insert, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
                                if(ToReturn){
                                    String Delete = "DELETE FROM pagos_deudas where id in ("+functions.implode(Ids)+")";
                                    ToReturn = db.query(Delete, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            db.disconnect();
                        }


                        String SqlDeudasPagadas = "select Deuda.Rut,Deuda.Deuda,Deuda.Fecha_Vencimiento,datediff(GETDATE(),Deuda.Fecha_Vencimiento) as DiasVecimiento,Deuda."+CampoClave+" as NOperacion from Deuda left join Deuda_tmp on Deuda_tmp.Rut = Deuda.Rut and Deuda_tmp."+CampoClave+" = Deuda."+CampoClave+" where Deuda.Id_Cedente='"+Cedente+"' and Deuda_tmp.Rut is null";
                        try {
                            ResultSet rs = db.select(SqlDeudasPagadas, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
                            if(db.HaveData(rs)){
                                ArrayList<String> Values = new ArrayList<String>();
                                while(rs.next()){
                                    String Rut = rs.getString("Rut");
                                    String NOperacion = rs.getString("NOperacion");
                                    String Deuda = rs.getString("Deuda");
                                    String FechaVencimiento = rs.getString("Fecha_Vencimiento");
                                    int DiasVencimiento = Integer.parseInt(rs.getString("DiasVecimiento"));
                                    String Tramo = getTramoDiasVencimiento(Tramos,DiasVencimiento);
                                    
                                    String Gestion = "";
                                    String Ejecutivo = "";
                                    String Fecha = "";
                                    String FechaCompromiso = "";
                                    String idGestion = "";
                                    
                                    String SqlMejorGestion = "select Mejor_Gestion_Cedente.Rut as Rut,Tipo_Contacto.Nombre as Gestion,Tipo_Contacto.Id_TipoContacto as idGestion,Mejor_Gestion_Cedente.fecha_gestion as Fecha,Mejor_Gestion_Cedente.nombre_ejecutivo as Ejecutivo,CASE WHEN Mejor_Gestion_Cedente.fec_compromiso != '0000-00-00' THEN Mejor_Gestion_Cedente.fec_compromiso END as FechaCompromiso from Mejor_Gestion_Cedente inner join Tipo_Contacto on Tipo_Contacto.Id_TipoContacto = Mejor_Gestion_Cedente.Id_TipoGestion where Mejor_Gestion_Cedente.Rut='"+Rut+"' and Mejor_Gestion_Cedente.Id_Cedente='"+cargaasignacionautomatica.CargaAsignacionAutomatica.Cedente+"'";
                                    ResultSet rsMG = db.select(SqlMejorGestion, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
                                    if(db.HaveData(rsMG)){
                                        while(rsMG.next()){
                                            Gestion = rsMG.getString("Gestion");
                                            Ejecutivo = rsMG.getString("Ejecutivo");
                                            Fecha = rsMG.getString("Fecha");
                                            FechaCompromiso = rsMG.getString("FechaCompromiso") == null ? "0000-00-00" : rsMG.getString("FechaCompromiso");
                                            idGestion = rsMG.getString("idGestion");
                                        }
                                    }
                                    String ValuesTmp = "('"+Rut+"','"+Deuda+"','"+NOperacion+"',GETDATE(),'"+FechaVencimiento+"','"+DiasVencimiento+"','"+Tramo+"','"+Gestion+"','"+Ejecutivo+"','"+Fecha+"','"+FechaCompromiso+"','"+idGestion+"','"+cargaasignacionautomatica.CargaAsignacionAutomatica.Mandante+"','"+cargaasignacionautomatica.CargaAsignacionAutomatica.Cedente+"')";
                                    Values.add(ValuesTmp);
                                }
                                String Insert = "INSERT INTO pagos_deudas (Rut,Monto,"+CampoClave+",Fecha_Pago,Fecha_vencimiento,Dias_vencimiento,Tramo,Mejor_gestion,Ejecutivo_mejor_gestion,Fecha_mejor_gestion,Fec_compromiso,Id_TipoGestion,Mandante,Id_Cedente) values "+functions.implode(Values);
                                ToReturn = db.query(Insert, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            db.disconnect();
                        }
                    }
                    break;
            }

            /*
                *   Delete Values from Deuda Table
            */
            
            sql = "DELETE FROM Deuda WHERE Id_Cedente =  "+Cedente;
            ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            
            /*
                *   Delete Values from Deuda_tmp Table
            */
            
            sql = "DELETE FROM Deuda_tmp WHERE Id_Cedente =  "+Cedente;
            ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            
            
            /*
                *   Insert Values into Deuda Table
            */
            for (Object Values : ArrayValues) {
                sql = "insert into Deuda ("+functions.implode(Campos)+")  values "+Values.toString();
                ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            }
            
            sql = "DELETE from Agendamiento where Rut in (select Deuda.Rut from Deuda left join Agendamiento on Agendamiento.Rut = Deuda.Rut and Agendamiento.Id_Cedente = Deuda.Id_Cedente where Agendamiento.id is null and Deuda.Id_Cedente='"+Cedente+"' group by Deuda.Rut)";
            ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            
            sql = "DELETE from Agendamiento_Compromiso where Rut in (select Deuda.Rut from Deuda left join Agendamiento on Agendamiento.Rut = Deuda.Rut and Agendamiento.Id_Cedente = Deuda.Id_Cedente where Agendamiento.id is null and Deuda.Id_Cedente='"+Cedente+"' group by Deuda.Rut)";
            ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            
    }
    public static ArrayList<String> getColumnas(){
        ArrayList<String> Columns =  new ArrayList<String>();
        DB db = new DB();
        String sql = "select COLUMN_NAME as Field from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'Deuda'";
        try {
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    String Field = rs.getString("Field");
                    switch(Field){
                        case "Id_deuda":
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
    public double getSumaTotalDeuda(String Cedente){
        double ToReturn = 0;
        DB db = new DB();
        String sql = "select SUM(Deuda) as SumaMontoMora from Deuda where Id_Cedente='"+Cedente+"'";
        try {
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    ToReturn = rs.getDouble("SumaMontoMora");
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
            String SqlUpdate = "UPDATE "+ArrayMarca[0]+" SET "+Updates+" WHERE Id_Cedente='"+Cedente+"' and "+Relacion;
            UpdateList.add(SqlUpdate);
        }
        for (Object Update : UpdateList) {
            DB db = new DB();
            db.query(Update.toString(), cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        }
    }
    public String getTramoDiasVencimiento(List<HashMap<String, String>> Tramos, int DiasVencimiento){
        String ToReturn = "";
        for (int i=0;i<Tramos.size();i++){
            HashMap<String, String> tramohm = Tramos.get(i);
            String Descripcion = tramohm.get("Descripcion");
            int Desde = Integer.parseInt(tramohm.get("Desde"));
            int Hasta = Integer.parseInt(tramohm.get("Hasta"));
            int Operacion = Integer.parseInt(tramohm.get("Operacion"));
            switch(Operacion){
                case 0:
                    if((DiasVencimiento >= Desde) && (DiasVencimiento <= Hasta)){
                        ToReturn = Descripcion;
                    }
                    break;
                case 1:
                    if(DiasVencimiento <= Desde){
                        ToReturn = Descripcion;
                    }
                    break;
                case 2:
                    if(DiasVencimiento >= Desde){
                        ToReturn = Descripcion;
                    }
                    break;
            }
            if(!ToReturn.equals("")){
               break; 
            }
        }
        return ToReturn;
    }
}
