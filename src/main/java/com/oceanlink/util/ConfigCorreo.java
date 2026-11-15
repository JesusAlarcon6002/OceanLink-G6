package com.oceanlink.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Configuración del servidor SMTP (correo REAL: Gmail, Outlook, el SMTP de la universidad, etc.). Las credenciales NO van en el código:
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
            return Integer.parseInt(leer("SMTP_PORT", "smtp.port", "587"));
        } catch (NumberFormatException e) {
            return 587;
        }
    }

    public static String usuario() {
        return leer("SMTP_USER", "smtp.user", null);
    }

    public static String clave() {
        return leer("SMTP_PASSWORD", "smtp.password", null);
    }

    /**
     * Dirección que aparece como remitente. Los proveedores reales (Gmail, Outlook) solo aceptan la cuenta
     * con la que uno inicia sesión, así que si no se define SMTP_FROM se usa el mismo SMTP_USER.
     */
    public static String remitente() {
        String from = leer("SMTP_FROM", "smtp.from", null);
        return from != null ? from : usuario();
    }

    /** Nombre que ve el destinatario junto al remitente. */
    public static String nombreRemitente() {
        return leer("SMTP_FROM_NAME", "smtp.from.name", "OceanLink");
    }

    /**
     * MODO DEMOSTRACIÓN (opcional). Si se define CORREO_REDIRIGIR_A (o correo.redirigir en el archivo),
     * TODOS los correos se entregan a esa dirección en vez de a sus destinatarios reales; el mensaje avisa
     * a quién iba dirigido. Sirve para mostrar el sistema sin escribirle a personas reales con datos de prueba.
     * Si no se define, los correos van a sus destinatarios reales.
     */
    public static String redirigirA() {
        return leer("CORREO_REDIRIGIR_A", "correo.redirigir", null);
    }

    /** true si hay al menos servidor, usuario y contraseña (clave de aplicación en Gmail). */
    public static boolean estaConfigurado() {
        return host() != null && usuario() != null && clave() != null;
    }

    /**
     * Dominios de correo aceptados para USUARIOS (personal). Por defecto pucp.edu.pe, pucp.pe y gmail.com.
     * Se cambia con CORREO_DOMINIOS_USUARIOS (o correo.dominios.usuarios), separados por coma;
     * con el valor "*" se acepta cualquier dominio real.
     */
    public static java.util.Set<String> dominiosUsuarios() {
        String v = leer("CORREO_DOMINIOS_USUARIOS", "correo.dominios.usuarios", "pucp.edu.pe,pucp.pe,gmail.com");
        java.util.Set<String> r = new java.util.LinkedHashSet<>();
        for (String d : v.split(",")) {
            d = d.trim().toLowerCase(java.util.Locale.ROOT);
            if (d.equals("*")) return java.util.Collections.emptySet();   // sin restricción
            if (!d.isEmpty()) r.add(d);
        }
        return r;
    }
}
