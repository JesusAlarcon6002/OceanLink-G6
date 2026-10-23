package com.oceanlink.daos;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Único lugar donde se abre la conexión a MySQL.
 * La URL, el usuario y la contraseña se leen de src/main/resources/db.properties,
 * un archivo LOCAL que cada integrante crea en su PC (no se sube a Git).
 * Plantilla: src/main/resources/db.properties.example
 */
public class ConexionBD {

    private static final Properties CONFIG = cargarConfiguracion();

    private static Properties cargarConfiguracion() {
        Properties p = new Properties();
        try (InputStream in = ConexionBD.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                System.err.println("[ConexionBD] No se encontró db.properties. "
                        + "Copia db.properties.example como db.properties y pon tu contraseña de MySQL.");
            } else {
                p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = CONFIG.getProperty("db.url", "jdbc:mysql://localhost:3306/proyecto_siete");
        String user = CONFIG.getProperty("db.user", "root");
        String pass = CONFIG.getProperty("db.password", "");
        return DriverManager.getConnection(url, user, pass);
    }
}
