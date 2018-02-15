/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cargaasignacionautomatica.Class.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 *
 * @author jonthan
 */
public class DB {
    private static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DATABASE_URL = "jdbc:mysql://"+cargaasignacionautomatica.CargaAsignacionAutomatica.DatabaseIP+":"+cargaasignacionautomatica.CargaAsignacionAutomatica.DatabasePort+"/"+cargaasignacionautomatica.CargaAsignacionAutomatica.DatabaseName;
    //private static final String DATABASE_URL = "jdbc:mysql://192.168.1.8:3306/foco";
    private static final String USERNAME = cargaasignacionautomatica.CargaAsignacionAutomatica.DatabaseUser;
    //private static final String PASSWORD = "s9q7l5.,777";
    private static final String PASSWORD = cargaasignacionautomatica.CargaAsignacionAutomatica.DatabasePass;
    private static final String MAX_POOL = "250";
    
    // init connection object
    public Connection connection;
    // init properties object
    private Properties properties;
    
    
    String connectionUrl = "jdbc:sqlserver://"+cargaasignacionautomatica.CargaAsignacionAutomatica.DatabaseIP+":"+cargaasignacionautomatica.CargaAsignacionAutomatica.DatabasePort+";databaseName="+cargaasignacionautomatica.CargaAsignacionAutomatica.DatabaseName+";user="+USERNAME+";password="+PASSWORD+";";
    
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    
    

    // create properties
    private Properties getProperties() {
        System.out.println(DATABASE_URL);
        if (properties == null) {
            properties = new Properties();
            properties.setProperty("user", USERNAME);
            properties.setProperty("password", PASSWORD);
            properties.setProperty("MaxPooledStatements", MAX_POOL);
        }
        return properties;
    }

    // connect database
    public Connection connect() {
        if (con == null) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                con = DriverManager.getConnection(connectionUrl);
                /*Class.forName(DATABASE_DRIVER);
                connection = DriverManager.getConnection(DATABASE_URL, getProperties());*/
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            catch(SQLException ex){
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        }
        return con;
    }

    // disconnect database
    public void disconnect() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public ResultSet select(String Query,Connection Connection){
        ResultSet rs = null;
        try {
            Statement stmt = Connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(Query);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //this.disconnect();
        }
        return rs;
    }
    public Boolean query(String Query,Connection Connection) throws SQLException{
        Boolean ToReturn = false;
            Statement stmt = Connection.createStatement();
            int Result = stmt.executeUpdate(Query);
            ToReturn = Result == 1 ? true : false;
        return ToReturn;
    }
    public Boolean HaveData(ResultSet rs){
        Boolean ToReturn = false;
        try{
            if(rs.next() == false){
                ToReturn = false;
            }else{
                ToReturn = true;
            }
            //rs.absolute(0);
            //rs.beforeFirst();
            rs.previous();
        }catch(Exception ex){
        }
        return ToReturn;
    }
}
