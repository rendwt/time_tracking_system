package com.tproject.dao.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JdbcConnection {

    private static String URL = "jdbc:mysql://192.168.1.11:3306/time_log_db";
    private static String USER = "test";
    private static String PASSWORD = "Adminadmin123!";
    private static final Logger LOGGER =
            Logger.getLogger(JdbcConnection.class.getName());
    private static JdbcConnection connection;

    private JdbcConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }


    public static JdbcConnection getInstance() {
        JdbcConnection localInstance = connection;
        if (localInstance == null) {
            synchronized (JdbcConnection.class) {
                localInstance = connection;
                if (localInstance == null) {
                    connection = localInstance = new JdbcConnection();
                }
            }
        }
        return localInstance;
    }

    public Connection getConnection() throws SQLException {
        Connection conn = null;
        conn = DriverManager.getConnection(URL, USER, PASSWORD);
        return conn;
    }
}