/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.Fonos;

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
public class Fono {
    Functions functions = new Functions();
    public void Insert(ArrayList Personas,String Cedente,String Mandante) throws SQLException{
        Functions functions = new Functions();
        ArrayList<String> Campos = new ArrayList<String>();
        ArrayList<String> CamposMerge = new ArrayList<String>();
        ArrayList<String> Values = new ArrayList<String>();
        ArrayList<String> CamposTmp = new ArrayList<String>();
        ArrayList<String> ValuesTmp = new ArrayList<String>();
        ArrayList<String> CamposCedente = new ArrayList<String>();
        ArrayList<String> CamposCedenteMerge= new ArrayList<String>();
        ArrayList<String> ValuesCedente = new ArrayList<String>();
        ArrayList<String> CamposCarga = new ArrayList<String>();
        
        ArrayList<String> ArrayValues = new ArrayList<String>();
        ArrayList<String> ArrayValuesTmp = new ArrayList<String>();
        
        int Cont = 0;
        int ContValues = 1;
        int ContValuesTmp = 1;
        for (Object Persona : Personas) {
            ArrayList<String> Value = new ArrayList<String>();
            ArrayList<String> ValueTmp = new ArrayList<String>();
            ArrayList<String> ValueCedente = new ArrayList<String>();
            String StringTmp = Persona.toString();
            String[] Fields = StringTmp.split("\\[]");
            Boolean CanInsert = true;
            for(String Field:Fields){
                String[] ArrayField = Field.split("\\|");
                String FieldValue = ArrayField[0];
                String FieldName = ArrayField[1];
                switch(FieldName){
                    case "formato_subtel":
                        FieldValue = FieldValue.replace("+56", "").trim();
                        if(FieldValue.indexOf('-') >= 0){
                            //FieldValue = FieldValue.replaceAll("[^0-9]", "");
                        }
                        if(FieldValue.indexOf(' ') >= 0){
                            //FieldValue = FieldValue.replaceAll("[0-9]*", "");
                        }
                        FieldValue = FieldValue.replaceAll("[^0-9]", "");
                        FieldValue = FieldValue.length() == 11 ? FieldValue.substring(2,FieldValue.length()) : FieldValue;
                        //CanInsert = Depurador("",FieldValue,Cedente,"",false,false) ? true : false;
                        CanInsert = true;
                    break;
                }
                if(Cont == 0){
                    Campos.add(FieldName);
                    CamposMerge.add("Source."+FieldName);
                    CamposTmp.add(FieldName);
                    CamposCedente.add(FieldName);
                    CamposCedenteMerge.add("Source."+FieldName);
                    CamposCarga.add(FieldName);
                }
                Value.add("'"+FieldValue+"'");
                ValueTmp.add("'"+FieldValue+"'");
                ValueCedente.add("'"+FieldValue+"'");
            }
            if(Cont == 0){
                Campos.add("fecha_carga");
                CamposMerge.add("Source.fecha_carga");
                CamposTmp.add("Id_Cedente");
                CamposCedente.add("Id_Cedente");
                CamposCedenteMerge.add("Source.Id_Cedente");
                CamposTmp.add("fecha_carga");
                CamposCedente.add("fecha_carga");
                CamposCedenteMerge.add("Source.fecha_carga");
            }
            if(CanInsert){
                ValueTmp.add("'"+Cedente+"'");
                ValueCedente.add("'"+Cedente+"'");
                ValueTmp.add("GETDATE()");
                ValueCedente.add("GETDATE()");
                Values.add("("+functions.implode(Value)+")");
                ValuesTmp.add("("+functions.implode(ValueTmp)+")");
                ValuesCedente.add("("+functions.implode(ValueCedente)+")");
                ContValues++;
                ContValuesTmp++;
                if(ContValues >= 1000){
                    ArrayValues.add(functions.implode(Values));
                    Values.clear();
                    ContValues = 1;
                }
                if(ContValuesTmp >= 1000){
                    ArrayValuesTmp.add(functions.implode(ValuesTmp));
                    ValuesTmp.clear();
                    ContValuesTmp = 1;
                }
            }
            Cont++;
        }
        if(Values.size()>0){
            ArrayValues.add(functions.implode(Values));
            Values.clear();
        }
        if(ValuesTmp.size()>0){
            ArrayValuesTmp.add(functions.implode(ValuesTmp));
            ValuesTmp.clear();
        }
        if(ArrayValues.size()>0){
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String Fecha = format.format(date);
            //String sql = "INSERT INTO fono_cob ("+functions.implode(Campos)+") SELECT "+functions.implode(Campos)+" FROM fono_cob_tmp ON DUPLICATE KEY UPDATE fono_cob.cedente = CONCAT(fono_cob.cedente , ',' ,'"+Cedente+"_"+Fecha+"'), Prioridad_Fono = VALUES(Prioridad_Fono)";
            String sql = "merge into fono_cob as Target\n" +
                        "using (select "+functions.implode(Campos)+" from fono_cob_tmp) as Source\n" +
                        "on Target.Rut=Source.Rut and Target.formato_subtel=Source.formato_subtel\n" +
                        "when matched then \n" +
                        "update set Target.cedente = CONCAT(Target.cedente , ',' ,'"+Cedente+"_"+Fecha+"'), Target.Prioridad_Fono = Source.Prioridad_Fono\n" +
                        "when not matched then\n" +
                        "insert ("+functions.implode(Campos)+") values ("+functions.implode(CamposMerge)+");";
            String sqlTmp = "INSERT IGNORE INTO fono_cob_tmp ("+functions.implode(CamposTmp)+") values "+functions.implode(ValuesTmp);
            //String sqlCedente = "INSERT INTO fono_cob_cedente ("+functions.implode(CamposCedente)+") SELECT "+functions.implode(CamposCedente)+" FROM fono_cob_tmp ON DUPLICATE KEY UPDATE fono_cob_cedente.cedente = CONCAT(fono_cob_cedente.cedente , ',' ,'"+Cedente+"'), Prioridad_Fono = VALUES(Prioridad_Fono)";
            String sqlCedente = "merge into fono_cob_cedente as Target\n" +
                        "using (select "+functions.implode(CamposCedente)+" from fono_cob_tmp) as Source\n" +
                        "on Target.Rut=Source.Rut and Target.formato_subtel=Source.formato_subtel\n" +
                        "when matched then \n" +
                        "update set Target.cedente = CONCAT(Target.cedente , ',' ,'"+Cedente+"_"+Fecha+"'), Target.Prioridad_Fono = Source.Prioridad_Fono\n" +
                        "when not matched then\n" +
                        "insert ("+functions.implode(CamposCedente)+") values ("+functions.implode(CamposCedenteMerge)+");";
            
            System.out.println(CamposCedente);
            
            PreCarga(ArrayValuesTmp,CamposTmp,sql,sqlTmp,sqlCedente,Cedente,Mandante);
            DB db = new DB();
            String SqlCamposCargas = "merge into campos_cargas_asignaciones as Target\n" +
                        "using (values (GETDATE(),'fono_cob','"+functions.implode(CamposCarga)+"','"+Cedente+"')) as Source (fecha,tabla,campos,Id_Cedente)\n" +
                        "on Target.tabla=Source.tabla and Target.Id_Cedente=Source.Id_Cedente\n" +
                        "when matched then \n" +
                        "update set Target.campos = '"+functions.implode(CamposCarga)+"'\n" +
                        "when not matched then\n" +
                        "insert (fecha,tabla,campos,Id_Cedente) values (Source.fecha,Source.tabla,Source.campos,Source.Id_Cedente);";
            db.query(SqlCamposCargas, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        }
    }
    public void PreCarga(ArrayList ArrayValues,ArrayList Campos,String SqlInsertValues,String SqlInsertValuesTmp, String SqlInsertValuesCedente, String Cedente,String Mandante) throws SQLException{
        Boolean ToReturn = false;
        String sql = "";
        DB db = new DB();
            sql = "DELETE FROM fono_cob_cedente WHERE Id_Cedente = "+Cedente;
            ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            
            /*
                *   Insert Values into fono_cob_tmp Table
            */
            for (Object Values : ArrayValues) {
                sql = "insert into fono_cob_tmp ("+functions.implode(Campos)+")  values "+Values.toString();
                ToReturn = db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            }
            //ToReturn = db.query(SqlInsertValuesTmp, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn); 
            System.out.println("fono_cob_tmp");
            /*
                *   Repair Fonos Tmp
            */
            //RepairFonos(Cedente,"fono_cob_tmp");
            
            /*
                *   Insert Values into fono_cob_Cedentes Table
            */
            ToReturn = db.query(SqlInsertValuesCedente, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            System.out.println("fono_cob_cedente");
            /*
                *   Insert Values into fono_cob Table
            */
            ToReturn = db.query(SqlInsertValues, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn); 
            System.out.println("fono_cob");
    }
    public static ArrayList<String> getColumnas() throws SQLException{
        ArrayList<String> Columns =  new ArrayList<String>();
        DB db = new DB();
        String sql = "select COLUMN_NAME as Field from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'fono_cob'";
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    String Field = rs.getString("Field");
                    switch(Field){
                        case "id_fono":
                        break;
                        default:
                            Columns.add(Field);
                        break;
                    }
                }
            }
        return Columns;
    }
    public void RepairFonos(String Cedente,String Tabla) throws SQLException{
        DB db = new DB();
        String sql = "SELECT * FROM "+Tabla+" where Id_Cedente='"+Cedente+"'";
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    String Codigo = rs.getString("codigo_area") == null ? "" : rs.getString("codigo_area");
                    String Fono = rs.getString("formato_subtel");
                    Boolean FonoDepurado = Depurador(Codigo,Fono,Cedente,Tabla,true,false);
                    if(!FonoDepurado){
                        deleteFono(Fono, Cedente, Tabla);
                    }
                }
            }
    }

    /**
     *
     * @param Codigo
     * @param Fono
     * @param Rut
     * @param ImportFonos
     * @return Valor booleano que representa si el Telefono es valido o no;
     *         Cuando el telefono es valido pero no contiene 9 caracteres
     *         el Telefono es actualizado.
     */
    public Boolean Depurador(String Codigo, String Fono, String Cedente, String Tabla,Boolean Update,Boolean Debug) throws SQLException{
        Boolean ToReturn = false;
        
        int CodigoLength = Codigo.length();
        int FonoLength = Fono.length();
        
        String fonoOld = Fono;
        
        switch(FonoLength){
            case 11:
                Fono = Fono.substring(2,FonoLength);
                ToReturn = true;
            break;
            case 9:
                ToReturn = true;
            break;
            case 8:
                int CodigoTmp = Integer.parseInt(Fono.substring(0,2));
                int CodigoArea = getCodigoArea(CodigoTmp);
                if((CodigoTmp == CodigoArea) && (Codigo.equals(""))){
                    if(!Codigo.equals("")){
                        if(CodigoArea == Integer.parseInt(Codigo)){
                            Fono = CodigoTmp+"2"+Fono.substring(2,10);
                        }else{
                            Fono = "9"+Fono;
                        }
                    }else{
                        Fono = "9"+Fono;
                    }
                }else{
                    Fono = Fono.replaceAll("[^0-9]", "");
                    int PrimerDigito = Integer.parseInt(Fono.substring(0,1));
                    if(PrimerDigito >= 4){
                        Fono = "9"+Fono;
                    }else{
                        Fono = "2"+Fono;
                    }
                }
                ToReturn = true;
            break;
            case 7:
                if(!Codigo.equals("")){
                    Fono = Codigo+Fono;
                    ToReturn = true;
                }
            break;
            case 6:
                if(!Codigo.equals("")){
                    switch(CodigoLength){
                        case 2:
                            Fono = Codigo+"2"+Fono;
                            ToReturn = true;
                        break;
                        case 1:
                            Fono = Codigo+"222"+Fono;
                            ToReturn = true;
                        break;
                    }
                }
            break;
            default:
            break;
        }
        if((ToReturn) && (!Fono.equals(fonoOld)) && (Update)){
            if(Debug){
                updateFonoDebug(Fono, fonoOld, Tabla);
            }else{
                updateFono(Fono, fonoOld, Tabla);
            }
            
        }
        return ToReturn;
    }
    /**
     *
     * @param Codigo
     * @return Codigo de Area
     */
    public int getCodigoArea(int Codigo) throws SQLException{
        int ToReturn = 0;
        DB db = new DB();
        String sql = "SELECT TOP(1) Codigo FROM Codigo_Area WHERE Codigo='"+Codigo+"'";
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    ToReturn = Integer.parseInt(rs.getString("Codigo"));
                }
            }
        return ToReturn;
    }
    /**
     *
     * @param Fono
     * @param Cedente
     * @param Tabla
     */
    public void updateFono(String Fono, String fonoOld, String Tabla) throws SQLException{
        DB db = new DB();
        String sql = "update "+Tabla+" set formato_subtel='"+Fono+"',numero_telefono='"+Fono+"',formato_dial='"+Fono+"' where formato_subtel='"+fonoOld+"'";
        db.query(sql,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        sql = "delete from "+Tabla+" where formato_subtel='"+fonoOld+"'";
        db.query(sql,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        db.disconnect();
    }
    public void updateFonoDebug(String Fono, String fonoOld, String Tabla){
        DB db = new DB();
        try{
            String sql = "update "+Tabla+" set formato_subtel='"+Fono+"',numero_telefono='"+Fono+"',formato_dial='"+Fono+"' where formato_subtel='"+fonoOld+"'";
            db.query(sql,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            sql = "delete from "+Tabla+" where formato_subtel='"+fonoOld+"'";
            db.query(sql,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            db.disconnect();
        }catch( SQLException ex){
            
        }
        
    }
    /**
     *
     * @param Fono
     * @param Cedente
     * @param Tabla
     */
    public void deleteFono(String Fono, String Cedente, String Tabla) throws SQLException{
        DB db = new DB();
        String sql = "delete from "+Tabla+" where formato_subtel='"+Fono+"' and Id_Cedente='"+Cedente+"'";
        db.query(sql,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        db.disconnect();
    }
    public void deleteFonoDebug(String Fono) throws SQLException{
        DB db = new DB();
        String sql = "delete from fono_cob where formato_subtel='"+Fono+"'";
        db.query(sql,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        db.disconnect();
    }
    public void deleteFonoCobTmp(String Cedente) throws SQLException{
        DB db = new DB();
        String sql = "delete from fono_cob_tmp where Id_Cedente='"+Cedente+"'";
        db.query(sql,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        db.disconnect();
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
    public void DebugFonos() throws SQLException{
        DB db = new DB();
        String sql = "SELECT * FROM fono_cob";
        ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        if(db.HaveData(rs)){
            while(rs.next()){
                String Codigo = rs.getString("codigo_area") == null ? "" : rs.getString("codigo_area");
                String Fono = rs.getString("formato_subtel");
                System.out.println(Fono);
                Boolean FonoDepurado = Depurador(Codigo,Fono,"","fono_cob",true,true);
                if(!FonoDepurado){
                    deleteFonoDebug(Fono);
                }
            }
        }
    }
    public void PasarFonoCob_FonoHistorico() throws SQLException{
        DB db = new DB();
        String sql = "INSERT INTO fono_cob_historico (Rut,codigo_pais,codigo_area,numero_telefono,formato_dial,formato_subtel,tipo_fono,score,vigente,fecha_carga,cedente,discado,id_categoria,color,color_ivr,cantidad_llamados,Nombre,Cargo,Observacion,Prioridad_Fono) SELECT fono_cob.Rut,fono_cob.codigo_pais,fono_cob.codigo_area,fono_cob.numero_telefono,fono_cob.formato_dial,fono_cob.formato_subtel,fono_cob.tipo_fono,fono_cob.score,fono_cob.vigente,fono_cob.fecha_carga,fono_cob.cedente,fono_cob.discado,fono_cob.id_categoria,fono_cob.color,fono_cob.color_ivr,fono_cob.cantidad_llamados,fono_cob.Nombre,fono_cob.Cargo,fono_cob.Observacion,fono_cob.Prioridad_Fono FROM dbo.fono_cob LEFT JOIN dbo.Persona ON Persona.Rut = dbo.fono_cob.Rut WHERE Persona.Rut IS NULL";
        db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        
        sql = "DELETE FROM dbo.fono_cob WHERE Rut IN (SELECT fono_cob.Rut FROM dbo.fono_cob LEFT JOIN dbo.Persona ON Persona.Rut = dbo.fono_cob.Rut WHERE Persona.Rut IS NULL)";
        db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
    }
    public void PasarFonoHistorico_FonoCob() throws SQLException{
        DB db = new DB();
        String sql = "INSERT INTO fono_cob (Rut,codigo_pais,codigo_area,numero_telefono,formato_dial,formato_subtel,tipo_fono,score,vigente,fecha_carga,cedente,discado,id_categoria,color,color_ivr,cantidad_llamados,Nombre,Cargo,Observacion,Prioridad_Fono) SELECT fono_cob_historico.Rut,fono_cob_historico.codigo_pais,fono_cob_historico.codigo_area,fono_cob_historico.numero_telefono,fono_cob_historico.formato_dial,fono_cob_historico.formato_subtel,fono_cob_historico.tipo_fono,fono_cob_historico.score,fono_cob_historico.vigente,fono_cob_historico.fecha_carga,fono_cob_historico.cedente,fono_cob_historico.discado,fono_cob_historico.id_categoria,fono_cob_historico.color,fono_cob_historico.color_ivr,fono_cob_historico.cantidad_llamados,fono_cob_historico.Nombre,fono_cob_historico.Cargo,fono_cob_historico.Observacion,fono_cob_historico.Prioridad_Fono FROM dbo.fono_cob_historico LEFT JOIN dbo.Persona ON Persona.Rut = dbo.fono_cob_historico.Rut WHERE Persona.Rut IS NOT NULL";
        db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
    }
    public void ColoreoFonos() throws SQLException{
        DB db = new DB();
        String sql = "SELECT color,tipo_contacto,cond1,logica,cond2,cant1,cant2,dias FROM SIS_Categoria_Fonos WHERE mundo = 1 AND color != 0 and tipo_contacto <> 'Default' ORDER BY prioridad DESC";
        ResultSet rsCat = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        int CantGestiones = 0;
        if(db.HaveData(rsCat)){
            sql = "SELECT fono_cob.Rut,fono_cob.formato_subtel FROM fono_cob inner join Persona on Persona.Rut = fono_cob.Rut WHERE LEN(fono_cob.formato_subtel) = 9 ORDER by fono_cob.Rut";
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                int Cont = 1;
                while(rs.next()){
                    System.out.println(Cont);
                    String Fono = rs.getString("formato_subtel");
                    String Rut = rs.getString("Rut");
                    System.out.println(Rut +" - "+ Fono);
                    while(rsCat.next()){
                        int Color = Integer.parseInt(rsCat.getString("color"));
                        int TipoContacto = Integer.parseInt(rsCat.getString("tipo_contacto"));
                        int Cond1 = Integer.parseInt(rsCat.getString("cond1"));
                        int Logica = Integer.parseInt(rsCat.getString("logica"));
                        int Cond2 = Integer.parseInt(rsCat.getString("cond2"));
                        int Cant1 = Integer.parseInt(rsCat.getString("cant1"));
                        int Cant2 = Integer.parseInt(rsCat.getString("cant2"));
                        int Dias = Integer.parseInt(rsCat.getString("dias"));
                        Boolean CanUpdate = false;
                        sql = "SELECT count(*) as CantGestiones FROM gestion_asignacion WHERE rut_cliente  = '"+Rut+"' AND fono_discado = '"+Fono+"' AND fecha_gestion >= DATEADD(DAY, -"+Dias+", GETDATE()) AND Id_TipoGestion = '"+TipoContacto+"' ";
                        ResultSet rsGestiones = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
                        while(rsGestiones.next()){
                            CantGestiones = Integer.parseInt(rsGestiones.getString("CantGestiones"));
                        }
                        switch(Logica){
                            case 1:
                                switch(Cond1){
                                    case 1:
                                        if(CantGestiones < Cant1){
                                            CanUpdate = true;
                                        }
                                        break;
                                    case 2:
                                        if(CantGestiones <= Cant1){
                                            CanUpdate = true;
                                        }
                                        break;
                                    case 3:
                                        if(CantGestiones == Cant1){
                                            CanUpdate = true;
                                        }
                                        break;
                                    case 4:
                                        if(CantGestiones > Cant1){
                                            CanUpdate = true;
                                        }
                                        break;
                                    case 5:
                                        if(CantGestiones >= Cant1){
                                            CanUpdate = true;
                                        }
                                        break;
                                }
                                if(CanUpdate){
                                    sql = "UPDATE fono_cob SET color = '"+Color+"' WHERE Rut = '"+Rut+"' AND formato_subtel = '"+Fono+"'";
                                    db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
                                    CanUpdate = false;
                                }
                                break;
                            case 2:
                                switch(Cond2){
                                    case 1:
                                        if(CantGestiones < Cant1 && CantGestiones < Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones <= Cant1 && CantGestiones < Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones == Cant1 && CantGestiones < Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones > Cant1 && CantGestiones < Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones >= Cant1 && CantGestiones < Cant2){
                                            CanUpdate = true;
                                        }
                                        break;
                                    case 2:
                                        if(CantGestiones < Cant1 && CantGestiones <= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones <= Cant1 && CantGestiones <= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones == Cant1 && CantGestiones <= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones > Cant1 && CantGestiones <= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones >= Cant1 && CantGestiones <= Cant2){
                                            CanUpdate = true;
                                        }
                                        break;
                                    case 3:
                                        if(CantGestiones < Cant1 && CantGestiones == Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones <= Cant1 && CantGestiones == Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones == Cant1 && CantGestiones == Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones > Cant1 && CantGestiones == Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones >= Cant1 && CantGestiones == Cant2){
                                            CanUpdate = true;
                                        }
                                        break;
                                    case 4:
                                        if(CantGestiones < Cant1 && CantGestiones > Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones <= Cant1 && CantGestiones > Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones == Cant1 && CantGestiones > Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones > Cant1 && CantGestiones > Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones >= Cant1 && CantGestiones > Cant2){
                                            CanUpdate = true;
                                        }
                                        break;
                                    case 5:
                                        if(CantGestiones < Cant1 && CantGestiones >= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones <= Cant1 && CantGestiones >= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones == Cant1 && CantGestiones >= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones > Cant1 && CantGestiones >= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones >= Cant1 && CantGestiones >= Cant2){
                                            CanUpdate = true;
                                        }
                                        break;
                                }
                                if(CanUpdate){
                                    sql = "UPDATE fono_cob SET color = '"+Color+"' WHERE Rut = '"+Rut+"' AND formato_subtel = '"+Fono+"'";
                                    db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
                                    CanUpdate = false;
                                }
                                break;
                            case 3:
                                switch(Cond2){
                                    case 1:
                                        if(CantGestiones < Cant1 || CantGestiones < Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones <= Cant1 || CantGestiones < Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones == Cant1 || CantGestiones < Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones > Cant1 || CantGestiones < Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones >= Cant1 || CantGestiones < Cant2){
                                            CanUpdate = true;
                                        }
                                        break;
                                    case 2:
                                        if(CantGestiones < Cant1 || CantGestiones <= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones <= Cant1 || CantGestiones <= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones == Cant1 || CantGestiones <= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones > Cant1 || CantGestiones <= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones >= Cant1 || CantGestiones <= Cant2){
                                            CanUpdate = true;
                                        }
                                        break;
                                    case 3:
                                        if(CantGestiones < Cant1 || CantGestiones == Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones <= Cant1 || CantGestiones == Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones == Cant1 || CantGestiones == Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones > Cant1 || CantGestiones == Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones >= Cant1 || CantGestiones == Cant2){
                                            CanUpdate = true;
                                        }
                                        break;
                                    case 4:
                                        if(CantGestiones < Cant1 || CantGestiones > Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones <= Cant1 || CantGestiones > Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones == Cant1 || CantGestiones > Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones > Cant1 || CantGestiones > Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones >= Cant1 || CantGestiones > Cant2){
                                            CanUpdate = true;
                                        }
                                        break;
                                    case 5:
                                        if(CantGestiones < Cant1 || CantGestiones >= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones <= Cant1 || CantGestiones >= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones == Cant1 || CantGestiones >= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones > Cant1 || CantGestiones >= Cant2){
                                            CanUpdate = true;
                                        }
                                        if(CantGestiones >= Cant1 || CantGestiones >= Cant2){
                                            CanUpdate = true;
                                        }
                                        break;
                                }
                                if(CanUpdate){
                                    sql = "UPDATE fono_cob SET color = '"+Color+"' WHERE Rut = '"+Rut+"' AND formato_subtel = '"+Fono+"'";
                                    db.query(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
                                    CanUpdate = false;
                                }
                                break;
                        }
                    }
                    rsCat.first();
                    Cont++;
                }
            }
        }
    }
}
