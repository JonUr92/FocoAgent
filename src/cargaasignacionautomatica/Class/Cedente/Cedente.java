/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.Cedente;

import cargaasignacionautomatica.Class.Carga.Carga;
import cargaasignacionautomatica.Class.DB.DB;
import cargaasignacionautomatica.Class.Functions.Functions;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author COBRANDINGJONATHAN
 */
public class Cedente {
    Functions functions = new Functions();
    
    public List<HashMap<String, String>> getCedente(String Cedente){
        List<HashMap<String, String>> Cedentes;
        Cedentes = new ArrayList<HashMap<String, String>>();
        DB db = new DB();
        String sql = "select * from Cedente where Id_Cedente='"+Cedente+"'";
        try {
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    String TipoCedente = rs.getString("tipo");
                    String Nombre = rs.getString("Nombre_Cedente");
                    String TipoRefresco = rs.getString("tipo_refresco");
                    String InicioPeriodo = rs.getString("inicio_periodo");
                    HashMap hm = new HashMap();
                    hm.put("TipoCedente", TipoCedente);
                    hm.put("Nombre", Nombre);
                    hm.put("TipoRefresco", TipoRefresco); /* 0:DEFAULT ; 1:DIARIO ; 2:MENSUAL*/
                    hm.put("InicioPeriodo", InicioPeriodo);
                    Cedentes.add(hm);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.disconnect();
        }
        return Cedentes;
    }
    public List<HashMap<String, String>> getTramos(String Cedente){
        List<HashMap<String, String>> Tramos;
        Tramos = new ArrayList<HashMap<String, String>>();
        DB db = new DB();
        String sql = "select * from tramos_cedentes where Id_Cedente='"+Cedente+"'";
        try {
            ResultSet rs = db.select(sql, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
            if(db.HaveData(rs)){
                while(rs.next()){
                    String Descripcion = rs.getString("Descripcion");
                    String Desde = rs.getString("desde");
                    String Hasta = rs.getString("hasta");
                    String Operacion = rs.getString("operacion");
                    HashMap hm = new HashMap();
                    hm.put("Descripcion", Descripcion);
                    hm.put("Desde", Desde);
                    hm.put("Hasta", Hasta);
                    hm.put("Operacion", Operacion); /* 0:NINGUNA ; 1:MENOR O IGUAL ; 2:MAYOR O IGUAL*/
                    Tramos.add(hm);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.disconnect();
        }
        return Tramos;
    }
}
