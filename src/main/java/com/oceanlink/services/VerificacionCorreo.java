package com.oceanlink.services;

import com.oceanlink.util.ConfigCorreo;
import com.oceanlink.util.Html;
import com.oceanlink.util.ValidadorCorreo;
import jakarta.servlet.http.HttpSession;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Comprueba que un correo es REAL y que la persona tiene acceso a su bandeja: se le envía un código de
 * 6 dígitos y hay que escribirlo antes de guardar. No usa la base de datos: el estado vive en la sesión
 * del usuario que está registrando (por eso funciona sin cambios en las tablas).
 *
 * Reglas de seguridad: el código vence a los 10 minutos, se permiten 5 intentos, hay que esperar 30 segundos
 * entre reenvíos y como máximo 5 envíos cada 15 minutos por sesión (para que nadie use el sistema para
 * mandar correos a mansalva).
 */
public final class VerificacionCorreo {

    /** Resultado de una operación, con mensaje para mostrar en pantalla. */
    public static final class Resultado {
        public final boolean ok;
        public final String mensaje;
        /** true si no hay servidor de correo: solo se validó el dominio y no hace falta código. */
        public final boolean sinCodigo;

        Resultado(boolean ok, String mensaje, boolean sinCodigo) {
            this.ok = ok;
            this.mensaje = mensaje;
            this.sinCodigo = sinCodigo;
        }
    }

    private static final String ATRIBUTO = "verificacionCorreo";
    private static final long VIGENCIA_CODIGO_MS = 10 * 60 * 1000L;
    private static final long VIGENCIA_VERIFICADO_MS = 30 * 60 * 1000L;
    private static final long ESPERA_REENVIO_MS = 30 * 1000L;
    private static final long VENTANA_MS = 15 * 60 * 1000L;
    private static final int MAX_ENVIOS_VENTANA = 5;
    private static final int MAX_INTENTOS = 5;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private static final class Estado implements java.io.Serializable {
        final Map<String, Pendiente> pendientes = new HashMap<>();
        final Map<String, Long> verificados = new HashMap<>();   // correo -> hora en que se verificó
        final List<Long> envios = new ArrayList<>();
    }

    private static final class Pendiente implements java.io.Serializable {
        String codigo;
        long vence;
        long enviado;
        int intentos;
    }

    private VerificacionCorreo() {
    }

    /** ¿Hay que pedir código? Solo si el servidor de correo está configurado (si no, no hay cómo enviarlo). */
    public static boolean exigeCodigo() {
        return ConfigCorreo.estaConfigurado();
    }

    private static String clave(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    private static Estado estado(HttpSession sesion) {
        synchronized (sesion) {
            Estado e = (Estado) sesion.getAttribute(ATRIBUTO);
            if (e == null) {
                e = new Estado();
                sesion.setAttribute(ATRIBUTO, e);
            }
            return e;
        }
    }

    // ------------------------------------------------------------------ enviar código

    /** Valida el correo (formato y dominio) y, si hay servidor de correo, le manda el código. */
    public static Resultado enviarCodigo(HttpSession sesion, String correo) {
        return enviarCodigo(sesion, correo, null);
    }

    /** @param dominios si no es null/vacío, el correo debe pertenecer a uno de esos dominios */
    public static Resultado enviarCodigo(HttpSession sesion, String correo, java.util.Set<String> dominios) {
        ValidadorCorreo.Resultado v = ValidadorCorreo.validar(correo, dominios);
        if (!v.valido) {
            return new Resultado(false, v.mensaje, false);
        }
        if (!exigeCodigo()) {
            return new Resultado(true, "Dominio válido. (El servidor de correo no está configurado, así que no se envía código.)", true);
        }
        String k = clave(correo);
        long ahora = System.currentTimeMillis();
        Estado est = estado(sesion);
        String codigo;
        synchronized (est) {
            est.envios.removeIf(t -> ahora - t > VENTANA_MS);
            if (est.envios.size() >= MAX_ENVIOS_VENTANA) {
                return new Resultado(false, "Pediste demasiados códigos. Espera unos minutos e inténtalo de nuevo.", false);
            }
            Pendiente previo = est.pendientes.get(k);
            if (previo != null && ahora - previo.enviado < ESPERA_REENVIO_MS) {
                long falta = (ESPERA_REENVIO_MS - (ahora - previo.enviado) + 999) / 1000;
                return new Resultado(false, "Espera " + falta + " segundos antes de pedir otro código.", false);
            }
            codigo = String.format("%06d", ALEATORIO.nextInt(1_000_000));
            Pendiente p = new Pendiente();
            p.codigo = codigo;
            p.enviado = ahora;
            p.vence = ahora + VIGENCIA_CODIGO_MS;
            est.pendientes.put(k, p);
            est.verificados.remove(k);
            est.envios.add(ahora);
        }

        String html = "<div style=\"font-family:Arial,Helvetica,sans-serif;max-width:480px;margin:0 auto;color:#0f172a\">"
                + "<div style=\"background:#1e3a5f;color:#fff;padding:16px 20px;border-radius:8px 8px 0 0\">"
                + "<div style=\"font-size:12px;letter-spacing:2px;color:#93c5fd\">OCEAN LINK</div>"
                + "<div style=\"font-size:18px;font-weight:bold;margin-top:4px\">Código de verificación</div></div>"
                + "<div style=\"border:1px solid #cbd5e1;border-top:none;padding:20px;border-radius:0 0 8px 8px\">"
                + "<p style=\"margin:0 0 12px\">Usa este código para confirmar tu correo en OceanLink:</p>"
                + "<p style=\"font-size:32px;letter-spacing:8px;font-weight:bold;margin:8px 0 16px;color:#1e3a5f\">" + Html.esc(codigo) + "</p>"
                + "<p style=\"margin:0;font-size:13px;color:#475569\">Vence en 10 minutos. Si no lo pediste, ignora este mensaje.</p>"
                + "</div></div>";
        try {
            CorreoService.enviar(java.util.Collections.singletonList(correo.trim()), "[OceanLink] Tu código de verificación: " + codigo, html);
        } catch (Exception e) {
            synchronized (est) {
                est.pendientes.remove(k);
            }
            return new Resultado(false, "No se pudo enviar el código: " + e.getMessage(), false);
        }
        String aviso = ConfigCorreo.redirigirA() != null
                ? "Código enviado (modo demostración: llegó a " + ConfigCorreo.redirigirA() + ")."
                : "Te enviamos un código a " + correo.trim() + ". Revisa tu bandeja (y spam).";
        return new Resultado(true, aviso, false);
    }

    // ------------------------------------------------------------------ confirmar código

    public static Resultado confirmar(HttpSession sesion, String correo, String codigoIngresado) {
        String k = clave(correo);
        Estado est = estado(sesion);
        long ahora = System.currentTimeMillis();
        synchronized (est) {
            Pendiente p = est.pendientes.get(k);
            if (p == null) {
                return new Resultado(false, "Primero pide el código de verificación.", false);
            }
            if (ahora > p.vence) {
                est.pendientes.remove(k);
                return new Resultado(false, "El código venció. Pide uno nuevo.", false);
            }
            String ingresado = codigoIngresado == null ? "" : codigoIngresado.trim();
            boolean coincide = MessageDigest.isEqual(ingresado.getBytes(StandardCharsets.UTF_8), p.codigo.getBytes(StandardCharsets.UTF_8));
            if (!coincide) {
                p.intentos++;
                if (p.intentos >= MAX_INTENTOS) {
                    est.pendientes.remove(k);
                    return new Resultado(false, "Demasiados intentos fallidos. Pide un código nuevo.", false);
                }
                return new Resultado(false, "Código incorrecto. Te quedan " + (MAX_INTENTOS - p.intentos) + " intento(s).", false);
            }
            est.pendientes.remove(k);
            est.verificados.put(k, ahora);
            return new Resultado(true, "Correo verificado.", false);
        }
    }

    // ------------------------------------------------------------------ consulta al guardar

    /** ¿Este correo ya se verificó en esta sesión (y no hace mucho)? */
    public static boolean estaVerificado(HttpSession sesion, String correo) {
        if (sesion == null) return false;
        Estado est = estado(sesion);
        synchronized (est) {
            Long hora = est.verificados.get(clave(correo));
            return hora != null && System.currentTimeMillis() - hora < VIGENCIA_VERIFICADO_MS;
        }
    }

    /** Se llama después de guardar: la verificación se usa una sola vez. */
    public static void olvidar(HttpSession sesion, String correo) {
        if (sesion == null) return;
        Estado est = estado(sesion);
        synchronized (est) {
            est.verificados.remove(clave(correo));
            est.pendientes.remove(clave(correo));
        }
    }

    /**
     * Regla completa para guardar un correo nuevo o modificado. Devuelve null si está todo bien o el mensaje de error.
     * Formato y dominio siempre; el código de verificación solo si el servidor de correo está configurado.
     */
    public static String errorAlGuardar(HttpSession sesion, String correo) {
        return errorAlGuardar(sesion, correo, null);
    }

    public static String errorAlGuardar(HttpSession sesion, String correo, java.util.Set<String> dominios) {
        ValidadorCorreo.Resultado v = ValidadorCorreo.validar(correo, dominios);
        if (!v.valido) {
            return v.mensaje;
        }
        if (exigeCodigo() && !estaVerificado(sesion, correo)) {
            return "Verifica el correo antes de guardar: pulsa \"ENVIAR CÓDIGO\" y escribe el código que llegó a " + correo.trim() + ".";
        }
        return null;
    }
}
