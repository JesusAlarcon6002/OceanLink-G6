package com.oceanlink.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Configuración del servidor SMTP. Las credenciales NO van en el código:
 *   1) primero se buscan las variables de entorno SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASSWORD, SMTP_FROM
 *      (se definen en la configuración de ejecución de IntelliJ / Smart Tomcat);
 *   2) si no están, se lee el archivo local src/main/resources/correo.properties (no se sube a Git),
 *      con las mismas claves en minúscula: smtp.host, smtp.port, smtp.user, smtp.password, smtp.from.
 * Si no hay configuración, el sistema sigue funcionando y simplemente no envía correos.
 */
public final class ConfigCorreo {

    private static final Properties ARCHIVO = cargarArchivo();

    private ConfigCorreo() {
    }

    private static Properties cargarArchivo() {
        Properties p = new Properties();
        try (InputStream in = ConfigCorreo.class.getClassLoader().getResourceAsStream("correo.properties")) {
            if (in != null) {
                p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    private static String leer(String variable, String clave, String porDefecto) {
        String v = System.getenv(variable);
        if (v == null || v.trim().isEmpty()) {
            v = ARCHIVO.getProperty(clave);
        }
        return (v == null || v.trim().isEmpty()) ? porDefecto : v.trim();
    }

    public static String host() {
        return leer("SMTP_HOST", "smtp.host", null);
    }

    public static int puerto() {
        try {
            return Integer.parseInt(leer("SMTP_PORT", "smtp.port", "2525"));
        } catch (NumberFormatException e) {
            return 2525;
        }
    }

    public static String usuario() {
        return leer("SMTP_USER", "smtp.user", null);
    }

    public static String clave() {
        return leer("SMTP_PASSWORD", "smtp.password", null);
    }

    public static String remitente() {
        return leer("SMTP_FROM", "smtp.from", "notificaciones@oceanlink.example");
    }

    /** true si hay al menos servidor, usuario y contraseña. */
    public static boolean estaConfigurado() {
        return host() != null && usuario() != null && clave() != null;
    }
}
