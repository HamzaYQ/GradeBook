package com.gradebook.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static volatile DatabaseConnection instance;
    private static volatile String lastErrorMessage;

    private Connection connection;
    private String url;
    private String user;
    private String password;
    private String driver;

    private DatabaseConnection() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new RuntimeException("db.properties introuvable dans le classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Erreur de lecture du fichier db.properties", e);
        }

        url = props.getProperty("db.url");
        user = props.getProperty("db.user");
        password = props.getProperty("db.password");
        driver = props.getProperty("db.driver");

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver MySQL introuvable: " + driver, e);
        }

        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion JDBC vers " + url, e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de reconnexion JDBC vers " + url, e);
        }
    }

    public void closeConnection() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la fermeture de la connexion", e);
        }
    }

    public static boolean testConnection() {
        try {
            DatabaseConnection db = getInstance();
            Connection conn = db.getConnection();
            if (conn == null) {
                lastErrorMessage = "Connexion JDBC nulle";
                return false;
            }
            lastErrorMessage = null;
            return true;
        } catch (RuntimeException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }
}
