package com.bolivartech.utils.db.poolconnections;

import com.bolivartech.utils.exception.UtilsException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <p>
 * Copyright 2016 BolivarTech INC.</p>
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * <p>
 * This Class is the BolivarTech's Class to manage the loading of JDBC drivers
 * and the creation of connections.</p>
 *
 * <p>
 * Database is a utility class that employs the use of a static variable and two
 * static methods that allow you to call the methods without instantiating the
 * class.</p>
 *
 * <p>
 * The following is a example of a properties file for MySQL:</p>
 *
 * <ul style="list-style-type:none">
 * <li>database.driver=com.mysql.jdbc.Driver</li>
 * <li>database.url=jdbc:mysql://server/datavase</li>
 * <li>database.username=scotty</li>
 * <li>database.password=BeanUp</li>
 * </ul>
 *
 * <p>
 * The following is a example of a properties file for Oracle:</p>
 *
 * <ul style="list-style-type:none">
 * <li>database.driver=oracle.jdbc.driver.OracleDriver</li>
 * <li>database.url=jdbc:oracle:thin:@server:port:database</li>
 * <li>database.username=scotty</li>
 * <li>database.password=BeanUp</li>
 * </ul>
 *
 * <p>
 * The pool name is used as the properties filename, so each pool can have its
 * own, distinct set of connection properties. All connections in a given pool
 * share the same set of properties.</p>
 *
 * <ul>
 * <li>Class ID: "DBDSCCL"</li>
 * <li>Loc: 000-005</li>
 * </ul>
 *
 * @author Julian Bolivar
 * @since 2016 - Date: April 16, 2016.
 * @version 1.0.0
 *
 * <p>
 * Change Logs:</p>
 * <ul>
 * <li>v1.0.0 (2016-04-16): Version Inicial.</li>
 * </ul>
 */
public class Database {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "DBDSCCL";

    // Error de Excecion en la ejecucion del SQL
    public static final int ERROR_SQLEXCECUTION = -1;
    // Error de recurso no encontrado
    public static final int ERROR_MISSINGRESOURCE = -2;
    // Error de recurso no encontrado
    public static final int ERROR_CLASSNOTFOUND = -3;
    // Error de acceso ilegal
    public static final int ERROR_ILLEGALACCESSEXCEPTION = -4;
    // Error de instanciacion
    public static final int ERROR_INSTANTIATIONEXCEPTION = -5;
    // Error de NULL Pointer
    public static final int ERROR_NULLPOINTER = -6;

    // URL to the database
    private static String dbUrl = null;

    /**
     * Return the URL to the database
     *
     * @return URL to the database
     */
    public static String getDbUrl() {
        return (dbUrl);
    }

    /**
     * This method takes a String argument named databaseDescriptor, which
     * identifies a properties file on the local filesystem. This properties
     * file must be generated before invoking the getConnection( ) method, and
     * in it you should place the connection properties that you want each new
     * connection to have.
     *
     * @param databaseDescriptor properties file on the local filesystem with
     * the database connections properties
     * @return Connetion to the database or NULL
     * @throws UtilsException
     */
    public static final Connection getConnection(String databaseDescriptor) throws UtilsException {
        Connection conn = null;
        String driver = null;
        String url = null;
        String username = null;
        String password = null;
        ResourceBundle resb;

        try {
            resb = ResourceBundle.getBundle(databaseDescriptor);
            driver = resb.getString("database.driver");
            url = resb.getString("database.url");
            dbUrl = url;
            //dbIp = ds.getConnection().getMetaData().getURL();
            username = resb.getString("database.username");
            password = resb.getString("database.password");
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(url, username, password);
        } catch (NullPointerException ex) {
            conn = null;
            throw new UtilsException("ERROR Null Pointer Exception: " + ex.getMessage(), Database.ERROR_NULLPOINTER, Database.CLASSID + "000");
        } catch (MissingResourceException ex) {
            conn = null;
            throw new UtilsException("ERROR Missing Resource: " + ex.getMessage(), Database.ERROR_MISSINGRESOURCE, Database.CLASSID + "001");
        } catch (ClassNotFoundException ex) {
            conn = null;
            throw new UtilsException("ERROR Class not found: " + ex.getMessage(), Database.ERROR_CLASSNOTFOUND, Database.CLASSID + "002");
        } catch (SQLException ex) {
            conn = null;
            throw new UtilsException("ERROR SQL Exception: "+ex.getMessage(), Database.ERROR_SQLEXCECUTION, Database.CLASSID + "003");
        } catch (InstantiationException ex) {
            conn = null;
            throw new UtilsException("ERROR Instantiation: "+ex.getMessage(), Database.ERROR_INSTANTIATIONEXCEPTION, Database.CLASSID + "004");
        } catch (IllegalAccessException ex) {
            conn = null;
            throw new UtilsException("ERROR Illegal Access: "+ex.getMessage(), Database.ERROR_ILLEGALACCESSEXCEPTION, Database.CLASSID + "005");
        }
        return conn;
    }
}
