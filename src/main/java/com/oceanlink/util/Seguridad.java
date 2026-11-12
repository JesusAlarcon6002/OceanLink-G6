package com.oceanlink.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Contraseñas: se guardan con PBKDF2-HMAC-SHA256 + sal aleatoria (viene en el JDK, sin librerías).
 * Formato guardado en usuarios.password_hash:   pbkdf2$65000$<sal en hex>$<hash en hex>   (unos 110 caracteres)
 *
 * Compatibilidad: si en la BD hay una contraseña vieja en texto plano (sin el prefijo "pbkdf2$"),
 * verificar() la sigue aceptando y LoginDao la convierte a hash en el primer inicio de sesión correcto.
 */
public final class Seguridad {

    private static final String PREFIJO = "pbkdf2";
    private static final int ITERACIONES = 65000;
    private static final int BITS_HASH = 256;
    private static final int BYTES_SAL = 16;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Seguridad() {
    }

    /** Devuelve la contraseña lista para guardar en la BD. Vacía o nula devuelve "" (el DAO decide qué hacer). */
    public static String hash(String clave) {
        if (clave == null || clave.isEmpty()) return "";
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = derivar(clave, sal, ITERACIONES);
        return PREFIJO + "$" + ITERACIONES + "$" + aHex(sal) + "$" + aHex(hash);
    }

    /** true si la clave escrita coincide con lo guardado (hash nuevo o texto plano antiguo). */
    public static boolean verificar(String clave, String guardado) {
        if (clave == null || guardado == null || guardado.isEmpty()) return false;
        if (!esHash(guardado)) {
            // contraseña antigua en texto plano: comparación de tiempo constante
            return MessageDigest.isEqual(clave.getBytes(StandardCharsets.UTF_8), guardado.getBytes(StandardCharsets.UTF_8));
        }
        String[] partes = guardado.split("\\$");
        if (partes.length != 4) return false;
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = deHex(partes[2]);
            byte[] esperado = deHex(partes[3]);
            byte[] calculado = derivar(clave, sal, iteraciones);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** true si lo guardado ya tiene el formato de hash (y no es texto plano). */
    public static boolean esHash(String guardado) {
        return guardado != null && guardado.startsWith(PREFIJO + "$");
    }

    private static byte[] derivar(String clave, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, BITS_HASH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }

    private static String aHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static byte[] deHex(String hex) {
        if (hex.length() % 2 != 0) throw new IllegalArgumentException("hex inválido");
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}
