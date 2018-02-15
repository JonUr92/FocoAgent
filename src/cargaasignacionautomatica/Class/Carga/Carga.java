/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.Carga;

import cargaasignacionautomatica.CargaAsignacionAutomatica;
import cargaasignacionautomatica.Class.Calidad.Calidad;
import cargaasignacionautomatica.Class.DB.DB;
import cargaasignacionautomatica.Class.Deudas.Deuda;
import cargaasignacionautomatica.Class.Direcciones.Direccion;
import cargaasignacionautomatica.Class.FTP.FTP;
import cargaasignacionautomatica.Class.Fonos.Fono;
import cargaasignacionautomatica.Class.Gestiones.Gestion;
import cargaasignacionautomatica.Class.Mails.Mail;
import cargaasignacionautomatica.Class.Pagos.Pago;
import cargaasignacionautomatica.Class.Personas.Persona;
import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jonthan
 */
public class Carga {
    
    public void InsertCarga(ArrayList[] DataTables,String Cedente,String Mandante) throws SQLException, MySQLSyntaxErrorException{
        int Cont = 0;
        for (ArrayList Table : DataTables) {
            switch(Cont){
                case 0:
                    System.out.println("Persona");
                    Persona persona = new Persona();
                    changeCommentProcess("Registrando Data de Personas");
                    persona.Insert(Table,Cedente,Mandante);
                    break;
                case 1:
                    System.out.println("Deuda");
                    Deuda deuda = new Deuda();
                    changeCommentProcess("Registrando Data de Deudas");
                    deuda.Insert(Table,Cedente,Mandante);
                    break;
                case 2:
                    System.out.println("Mail");
                    Mail mail = new Mail();
                    changeCommentProcess("Registrando Data de Correos");
                    mail.Insert(Table,Cedente,Mandante);
                    break;
                case 3:
                    System.out.println("Direcciones");
                    Direccion direccion = new Direccion();
                    changeCommentProcess("Registrando Data de Direcciones");
                    direccion.Insert(Table,Cedente,Mandante);
                    break;
                case 4:
                    System.out.println("Fono");
                    Fono fono = new Fono();
                    changeCommentProcess("Registrando Data de Telefono");
                    fono.Insert(Table,Cedente,Mandante);
                    break;
                case 5:
                    System.out.println("Pago");
                    Pago pago = new Pago();
                    changeCommentProcess("Registrando Data de Pagos");
                    pago.Insert(Table,Cedente,Mandante);
                    break;
                case 6:
                    System.out.println("Gestiones");
                    Gestion gestion = new Gestion();
                    changeCommentProcess("Registrando Data de Gestiones");
                    gestion.Insert(Table,Cedente,Mandante);
                    break;
            }
            Cont++;
        }
        switch(CargaAsignacionAutomatica.TipoCarga){
            case "carga":
                changeCommentProcess("Registrando Data de Carga");
                InsertCargaHistorica(Cedente);
                break;
            case "cargagestiones":
                /*FTP ftp = new FTP();
                Calidad calidad = new Calidad();
                ftp.download();
                calidad.InsertRecordsToDataBase();*/
                break;
        }
        endProcess();
        changeCommentProcess("Archivo Procesado Satisfactoriamente");
    }
    public void TruncateTables() throws SQLException{
        DB db = new DB();
        String Truncates = "";
        Truncates = "truncate table Persona;";
        db.query(Truncates,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        
        Truncates = "truncate table Persona_Historico;";
        db.query(Truncates,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        
        Truncates = "truncate table Persona_Periodo;";
        db.query(Truncates,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        
        Truncates = "truncate table Deuda;";
        db.query(Truncates,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        
        Truncates = "truncate table Deuda_Historico;";
        db.query(Truncates,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        
        Truncates = "truncate table Direcciones;";
        db.query(Truncates,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        
        Truncates = "truncate table Direcciones_cedentes;";
        db.query(Truncates,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        
        Truncates = "truncate table fono_cob;";
        db.query(Truncates,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        
        Truncates = "truncate table fono_cob_cedente;";
        db.query(Truncates,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        
        Truncates = "truncate table Mail;";
        db.query(Truncates,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        
        Truncates = "truncate table Mail_cedente;";
        db.query(Truncates,cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        
        db.disconnect();
    }
    public void InsertCargaHistorica(String Cedente) throws SQLException{
        DB db = new DB();
        Persona persona = new Persona();
        Deuda deuda = new Deuda();
        Fono fono = new Fono();
        int CantidadPersonas = persona.getCantidadPersonas(Cedente);
        double DeudaTotal = deuda.getSumaTotalDeuda(Cedente);
        String SqlHistoricoCarga = "INSERT INTO Historico_Carga (Id_Cedente,fecha,Cant_Ruts,Deuda_Total) values ('"+Cedente+"',GETDATE(),'"+CantidadPersonas+"','"+DeudaTotal+"')";
        db.query(SqlHistoricoCarga, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        if(cargaasignacionautomatica.CargaAsignacionAutomatica.InicioPeriodo){
            String SqlInicioPeriodo = "update Cedente set inicio_periodo='"+cargaasignacionautomatica.CargaAsignacionAutomatica.FechaInicioPeriodo+"' where Id_Cedente='"+Cedente+"'";
            db.query(SqlInicioPeriodo, cargaasignacionautomatica.CargaAsignacionAutomatica.Conn);
        }
        fono.deleteFonoCobTmp(Cedente);
        db.disconnect();
    }
    public void startProcess(String FileName) throws SQLException{
        DB db = new DB();
        String SqlHistoricoCarga = "INSERT INTO java_process_live (id_process,Id_Usuario,Id_Mandante,Id_Cedente,status,date,fileName,comment) values ('1','"+cargaasignacionautomatica.CargaAsignacionAutomatica.idUsuario+"','"+cargaasignacionautomatica.CargaAsignacionAutomatica.Mandante+"','"+cargaasignacionautomatica.CargaAsignacionAutomatica.Cedente+"','Procesando',GETDATE(),'"+FileName+"','Leyendo Archivo')";
        db.query(SqlHistoricoCarga, db.connect());
        db.disconnect();
    }
    public void changeCommentProcess(String Comment) throws SQLException{
        DB db = new DB();
        String SqlHistoricoCarga = "UPDATE java_process_live set comment='"+Comment+"' where Id_Mandante='"+cargaasignacionautomatica.CargaAsignacionAutomatica.Mandante+"' and Id_Cedente='"+cargaasignacionautomatica.CargaAsignacionAutomatica.Cedente+"'";
        db.query(SqlHistoricoCarga, db.connect());
        db.disconnect();
    }
    public void endProcess() throws SQLException{
        DB db = new DB();
        String SqlHistoricoCarga = "UPDATE java_process_live set status='Finalizado' where Id_Mandante='"+cargaasignacionautomatica.CargaAsignacionAutomatica.Mandante+"' and Id_Cedente='"+cargaasignacionautomatica.CargaAsignacionAutomatica.Cedente+"'";
        db.query(SqlHistoricoCarga, db.connect());
        db.disconnect();
    }
    public void getErrorMessage(String Error) throws SQLException{
        Error = Error.replace("'","|");
        DB db = new DB();
        String SqlHistoricoCarga = "UPDATE java_process_live set errorMessage='"+Error+"' where Id_Mandante='"+cargaasignacionautomatica.CargaAsignacionAutomatica.Mandante+"' and Id_Cedente='"+cargaasignacionautomatica.CargaAsignacionAutomatica.Cedente+"'";
        db.query(SqlHistoricoCarga, db.connect());
        db.disconnect();
    }
    public void InsertMarca(ArrayList DataTables,String Cedente,String Mandante) throws SQLException{
        String[] ArrayMarca = cargaasignacionautomatica.CargaAsignacionAutomatica.MarcaData.split("\\|");
        String[] CamposMarca = ArrayMarca[2].split("\\,");
        switch(ArrayMarca[0]){
            case "Persona":
                System.out.println("Persona");
                Persona persona = new Persona();
                changeCommentProcess("Registrando Data de Personas");
                persona.InsertMarca(DataTables,Cedente,Mandante);
                break;
            case "Deuda":
                System.out.println("Deuda");
                Deuda deuda = new Deuda();
                changeCommentProcess("Registrando Data de Deudas");
                deuda.InsertMarca(DataTables,Cedente,Mandante);
                break;
            case "fono_cob":
                System.out.println("Mail");
                Mail mail = new Mail();
                changeCommentProcess("Registrando Data de Correos");
                mail.InsertMarca(DataTables,Cedente,Mandante);
                break;
            case "Mail":
                System.out.println("Direcciones");
                Direccion direccion = new Direccion();
                changeCommentProcess("Registrando Data de Direcciones");
                direccion.InsertMarca(DataTables,Cedente,Mandante);
                break;
            case "Direcciones":
                System.out.println("Fono");
                Fono fono = new Fono();
                changeCommentProcess("Registrando Data de Telefono");
                fono.InsertMarca(DataTables,Cedente,Mandante);
                break;
        }
        endProcess();
    }
}
