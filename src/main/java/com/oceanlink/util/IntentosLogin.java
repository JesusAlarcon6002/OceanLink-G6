package com.oceanlink.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Freno contra fuerza bruta en el login (todo en memoria, se reinicia al reiniciar Tomcat).
 * Se cuentan los fallos por "correo + IP" (máx. 5) y por IP sola (máx. 20) en una ventana de 15 minutos;
 * al pasarse, ese par/IP queda bloqueado 5 minutos. Un ingreso correcto borra el contador del par correo+IP.
 *
 * Se cuenta también por correo+IP (y no solo por correo) para que alguien de afuera no pueda dejar
 * bloqueada a una cuenta real escribiendo su correo desde cualquier lado.
 */
public final class IntentosLogin {

    public static final int MAX_POR_CUENTA = 5;
    public static final int MAX_POR_IP = 20;
    static final long VENTANA_MS = 15 * 60_000L;
    static final long BLOQUEO_MS = 5 * 60_000L;

    private static final class Registro {
        int fallos;
        long primerFallo;
        long bloqueadoHasta;
    }

    private static final Map<String, Registro> REGISTROS = new HashMap<>();

    private IntentosLogin() {
    }

    public static String claveCuenta(String correo, String ip) {
        return "c|" + (correo == null ? "" : correo.trim().toLowerCase()) + "|" + ip;
    }

    public static String claveIp(String ip) {
        return "i|" + ip;
    }

    /** Segundos que faltan para poder reintentar con esa clave (0 si no está bloqueada). */
    public static synchronized long segundosBloqueado(String clave) {
        return segundosBloqueado(clave, System.currentTimeMillis());
    }

    static synchronized long segundosBloqueado(String clave, long ahora) {
        Registro r = REGISTROS.get(clave);
        if (r == null || r.bloqueadoHasta <= ahora) return 0;
        return (r.bloqueadoHasta - ahora + 999) / 1000;
    }

    public static synchronized void fallo(String clave, int maximo) {
        fallo(clave, maximo, System.currentTimeMillis());
    }

    static synchronized void fallo(String clave, int maximo, long ahora) {
        if (REGISTROS.size() > 5000) purgar(ahora);
        Registro r = REGISTROS.computeIfAbsent(clave, k -> new Registro());
        if (r.fallos == 0 || ahora - r.primerFallo > VENTANA_MS) {
            r.fallos = 0;
            r.primerFallo = ahora;
        }
        r.fallos++;
        if (r.fallos >= maximo) {
            r.bloqueadoHasta = ahora + BLOQUEO_MS;
            r.fallos = 0;
        }
    }

    public static synchronized void exito(String clave) {
        REGISTROS.remove(clave);
    }

    /** Solo para pruebas. */
    static synchronized void reiniciar() {
        REGISTROS.clear();
    }

    private static void purgar(long ahora) {
        REGISTROS.values().removeIf(r -> r.bloqueadoHasta <= ahora && ahora - r.primerFallo > VENTANA_MS);
    }

    /** "5 minutos", "1 minuto" o "40 segundos" para el mensaje al usuario. */
    public static String textoEspera(long segundos) {
        if (segundos >= 60) {
            long m = (segundos + 59) / 60;
            return m + (m == 1 ? " minuto" : " minutos");
        }
        return segundos + (segundos == 1 ? " segundo" : " segundos");
    }
}
