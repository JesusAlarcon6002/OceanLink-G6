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
 * DAO base (clase abstracta): único lugar donde se abre la conexión a MySQL.
 * Todos los demás DAO heredan de esta clase (extends DaoBase) y usan getConnection().
 * No se puede instanciar con new: solo sirve para que la hereden los DAO hijos.
 * La URL, el usuario y la contraseña se leen de src/main/resources/db.properties,
 * un archivo LOCAL que cada integrante crea en su PC (no se sube a Git).
 * Plantilla: src/main/resources/db.properties.example
 */
public abstract class DaoBase {

    private static final Properties CONFIG = cargarConfiguracion();

    private static Properties cargarConfiguracion() {
        Properties p = new Properties();
        try (InputStream in = DaoBase.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                System.err.println("[DaoBase] No se encontró db.properties. "
                        + "Copia db.properties.example como db.properties y pon tu contraseña de MySQL.");
            } else {
                p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    protected Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = CONFIG.getProperty("db.url", "jdbc:mysql://localhost:3306/proyecto_siete");
        String user = CONFIG.getProperty("db.user", "root");
        String pass = CONFIG.getProperty("db.password", "");
        return DriverManager.getConnection(url, user, pass);
    }
}
