package com.gradebook.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        Properties props = new Properties();
        try (InputStream in = TestDatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
                System.out.println("URL: " + props.getProperty("db.url"));
                System.out.println("User: " + props.getProperty("db.user"));
            } else {
                System.out.println("URL: (db.properties introuvable)");
                System.out.println("User: (db.properties introuvable)");
            }
        } catch (IOException e) {
            System.out.println("URL: (erreur lecture db.properties)");
            System.out.println("User: (erreur lecture db.properties)");
        }

        try {
            DatabaseConnection.getInstance().getConnection();
            System.out.println("Connexion réussie à la base de données GradeBook ✓");
        } catch (Exception e) {
            System.out.println("Échec de la connexion ✗");
            System.out.println("Message : " + e.getMessage());
            System.out.println("Cause : " + e.getCause());
            e.printStackTrace();
        }
    }
}
