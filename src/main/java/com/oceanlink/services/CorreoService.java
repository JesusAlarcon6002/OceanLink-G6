package com.oceanlink.services;

import com.oceanlink.util.ConfigCorreo;
import com.oceanlink.util.Html;
import com.oceanlink.util.ValidadorCorreo;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Envío de correos REALES con Jakarta Mail por SMTP (Gmail, Outlook, el servidor de la universidad...).
 * Solo se encarga de enviar: qué se envía y a quién lo deciden NotificadorCorreos y VerificacionCorreo.
 *
 * Detalles importantes:
 *  - Cada destinatario recibe SU PROPIO correo: un cliente nunca ve las direcciones de los demás.
 *  - La conexión usa TLS (STARTTLS en el 587 o SSL en el 465) porque viaja la contraseña.
 *  - Cada intento (bueno o malo) queda en un registro en memoria que se ve en la pantalla "Registro de correos".
 */
public final class CorreoService {

    /** Una línea del registro de envíos. */
    public static final class RegistroEnvio {
        public final long hora;
        public final String para;
        public final String asunto;
        public final boolean ok;
        public final String detalle;

        RegistroEnvio(String para, String asunto, boolean ok, String detalle) {
            this.hora = System.currentTimeMillis();
            this.para = para;
            this.asunto = asunto;
            this.ok = ok;
            this.detalle = detalle;
        }
    }

    private static final int MAX_REGISTROS = 100;
    private static final ConcurrentLinkedDeque<RegistroEnvio> REGISTROS = new ConcurrentLinkedDeque<>();

    // Un solo hilo en segundo plano: así guardar una incidencia no espera al servidor SMTP
    private static final ExecutorService COLA = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "oceanlink-correo");
        t.setDaemon(true);
        return t;
    });

    private CorreoService() {
    }

    /** Últimos envíos, del más reciente al más antiguo. */
    public static List<RegistroEnvio> registros() {
        List<RegistroEnvio> lista = new ArrayList<>(REGISTROS);
        java.util.Collections.reverse(lista);
        return lista;
    }

    private static void registrar(String para, String asunto, boolean ok, String detalle) {
        REGISTROS.addLast(new RegistroEnvio(para, asunto, ok, detalle));
        while (REGISTROS.size() > MAX_REGISTROS) {
            REGISTROS.pollFirst();
        }
    }

    /**
     * Envía un correo HTML a cada destinatario (uno por uno) y espera el resultado.
     * @throws MessagingException si no se pudo entregar a NINGÚN destinatario (conexión, clave o dirección rechazada)
     * @throws IllegalStateException si falta la configuración SMTP
     */
    public static void enviar(Collection<String> destinatarios, String asunto, String html) throws MessagingException {
        if (!ConfigCorreo.estaConfigurado()) {
            throw new IllegalStateException("El servidor de correo no está configurado (faltan SMTP_HOST, SMTP_USER o SMTP_PASSWORD).");
        }
        String asuntoLimpio = asunto == null ? "" : asunto.replaceAll("[\\r\\n]+", " ").trim();

        // direcciones únicas y con formato válido
        Set<String> unicos = new LinkedHashSet<>();
        if (destinatarios != null) {
            for (String d : destinatarios) {
                if (d == null || d.trim().isEmpty()) continue;
                String dir = d.trim().toLowerCase(Locale.ROOT);
                if (ValidadorCorreo.sintaxisValida(dir)) {
                    unicos.add(dir);
                } else {
                    registrar(dir, asuntoLimpio, false, "Dirección con formato inválido: no se envió");
                }
            }
        }
        if (unicos.isEmpty()) {
            throw new IllegalArgumentException("No hay destinatarios con una dirección válida.");
        }

        Session sesion = Session.getInstance(propiedades());
        Transport transporte = sesion.getTransport("smtp");
        try {
            transporte.connect(ConfigCorreo.host(), ConfigCorreo.puerto(), ConfigCorreo.usuario(), ConfigCorreo.clave());
        } catch (MessagingException e) {
            String motivo = explicar(e);
            for (String para : unicos) {
                registrar(para, asuntoLimpio, false, motivo);
            }
            throw new MessagingException(motivo);
        }

        int enviados = 0;
        String ultimoError = null;
        try {
            for (String para : unicos) {
                try {
                    MimeMessage mensaje = armar(sesion, para, asuntoLimpio, html);
                    mensaje.saveChanges();
                    transporte.sendMessage(mensaje, mensaje.getAllRecipients());
                    enviados++;
                    registrar(para, asuntoLimpio, true, ConfigCorreo.redirigirA() != null
                            ? "Enviado (modo demostración, entregado a " + ConfigCorreo.redirigirA() + ")" : "Enviado");
                } catch (MessagingException | UnsupportedEncodingException e) {
                    ultimoError = explicar(e);
                    registrar(para, asuntoLimpio, false, ultimoError);
                }
            }
        } finally {
            try {
                transporte.close();
            } catch (MessagingException ignorado) {
                // nada que hacer si falla al cerrar
            }
        }
        if (enviados == 0) {
            throw new MessagingException(ultimoError == null ? "No se pudo enviar el correo." : ultimoError);
        }
    }

    /**
     * Envía en segundo plano. Nunca lanza excepción: si falla, queda en el registro y en la consola
     * para que el flujo principal (guardar incidencia, solicitud, etc.) no se vea afectado.
     */
    public static void enviarAsync(Collection<String> destinatarios, String asunto, String html) {
        if (destinatarios == null || destinatarios.isEmpty()) {
            return;
        }
        if (!ConfigCorreo.estaConfigurado()) {
            System.out.println("[Correo] SMTP no configurado: se omite \"" + asunto + "\"");
            registrar(String.join(", ", destinatarios), asunto, false, "No enviado: el servidor de correo no está configurado");
            return;
        }
        final List<String> copia = new ArrayList<>(destinatarios);
        COLA.submit(() -> {
            try {
                enviar(copia, asunto, html);
                System.out.println("[Correo] Enviado \"" + asunto + "\" a " + copia.size() + " destinatario(s)");
            } catch (Exception e) {
                System.err.println("[Correo] No se pudo enviar \"" + asunto + "\": " + e.getMessage());
            }
        });
    }

    // ------------------------------------------------------------------ interno

    private static Properties propiedades() {
        Properties props = new Properties();
        props.put("mail.smtp.host", ConfigCorreo.host());
        props.put("mail.smtp.port", String.valueOf(ConfigCorreo.puerto()));
        props.put("mail.smtp.auth", "true");
        if (ConfigCorreo.puerto() == 465) {
            props.put("mail.smtp.ssl.enable", "true");              // SSL directo (Gmail puerto 465)
        } else {
            props.put("mail.smtp.starttls.enable", "true");         // STARTTLS (puerto 587)
            props.put("mail.smtp.starttls.required", "true");       // sin cifrado no se manda la clave
        }
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.ssl.checkserveridentity", "true");
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "20000");
        props.put("mail.smtp.writetimeout", "20000");
        return props;
    }

    private static MimeMessage armar(Session sesion, String para, String asunto, String html)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage mensaje = new MimeMessage(sesion);
        mensaje.setFrom(new InternetAddress(ConfigCorreo.remitente(), ConfigCorreo.nombreRemitente(), "UTF-8"));

        String destino = para;
        String asuntoFinal = asunto;
        String cuerpo = html;
        String desvio = ConfigCorreo.redirigirA();
        if (desvio != null && ValidadorCorreo.sintaxisValida(desvio)) {
            // modo demostración: el correo llega a una sola bandeja y avisa a quién iba dirigido
            destino = desvio;
            asuntoFinal = "[DEMO para " + para + "] " + asunto;
            cuerpo = "<div style=\"font-family:Arial,sans-serif;background:#fef3c7;border:1px solid #f59e0b;color:#78350f;"
                    + "padding:8px 12px;margin-bottom:12px;border-radius:6px;font-size:12px\">"
                    + "<b>Modo demostración:</b> este correo iba dirigido a " + Html.esc(para) + ".</div>" + html;
        }
        mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(destino));
        mensaje.setSubject(asuntoFinal, "UTF-8");
        mensaje.setContent(cuerpo, "text/html; charset=UTF-8");
        return mensaje;
    }

    /** Mensaje entendible para el error más común (clave rechazada). */
    private static String explicar(Exception e) {
        if (e instanceof AuthenticationFailedException) {
            return "El servidor rechazó el usuario o la clave. En Gmail hay que usar una \"contraseña de aplicación\", no la contraseña normal.";
        }
        String m = e.getMessage();
        return m == null || m.isEmpty() ? e.getClass().getSimpleName() : m;
    }
}
