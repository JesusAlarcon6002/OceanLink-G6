package com.oceanlink.services;

import com.oceanlink.util.ConfigCorreo;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Envío de correos con Jakarta Mail por SMTP (en desarrollo se usa el sandbox de Mailtrap/Ethereal,
 * que captura los mensajes sin entregarlos). Solo se encarga de enviar: qué se envía y a quién lo decide
 * NotificadorCorreos. Los datos del servidor salen de ConfigCorreo (variables de entorno).
 */
public final class CorreoService {

    // Un solo hilo en segundo plano: así guardar una incidencia no espera al servidor SMTP
    private static final ExecutorService COLA = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "oceanlink-correo");
        t.setDaemon(true);
        return t;
    });

    private CorreoService() {
    }

    /**
     * Envía un correo HTML y espera el resultado (se usa en la pantalla de prueba).
     * @throws MessagingException si el servidor rechaza el mensaje o no hay conexión
     * @throws IllegalStateException si falta la configuración SMTP
     */
    public static void enviar(Collection<String> destinatarios, String asunto, String html) throws MessagingException {
        if (!ConfigCorreo.estaConfigurado()) {
            throw new IllegalStateException("El servidor SMTP no está configurado (faltan SMTP_HOST, SMTP_USER o SMTP_PASSWORD).");
        }
        if (destinatarios == null || destinatarios.isEmpty()) {
            throw new IllegalArgumentException("No hay destinatarios.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", ConfigCorreo.host());
        props.put("mail.smtp.port", String.valueOf(ConfigCorreo.puerto()));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        final String usuario = ConfigCorreo.usuario();
        final String clave = ConfigCorreo.clave();
        Session sesion = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, clave);
            }
        });

        MimeMessage mensaje = new MimeMessage(sesion);
        mensaje.setFrom(new InternetAddress(ConfigCorreo.remitente()));
        for (String d : destinatarios) {
            try {
                mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(d, true));
            } catch (AddressException e) {
                System.err.println("[Correo] Dirección inválida, se omite: " + d);
            }
        }
        if (mensaje.getAllRecipients() == null) {
            throw new IllegalArgumentException("Ninguna dirección de destino es válida.");
        }
        mensaje.setSubject(asunto, "UTF-8");
        mensaje.setContent(html, "text/html; charset=UTF-8");
        Transport.send(mensaje);
    }

    /**
     * Envía en segundo plano. Nunca lanza excepción: si falla, lo deja en la consola del servidor
     * para que el flujo principal (guardar incidencia, solicitud, etc.) no se vea afectado.
     */
    public static void enviarAsync(Collection<String> destinatarios, String asunto, String html) {
        if (destinatarios == null || destinatarios.isEmpty()) {
            return;
        }
        if (!ConfigCorreo.estaConfigurado()) {
            System.out.println("[Correo] SMTP no configurado: se omite \"" + asunto + "\"");
            return;
        }
        COLA.submit(() -> {
            try {
                enviar(destinatarios, asunto, html);
                System.out.println("[Correo] Enviado \"" + asunto + "\" a " + destinatarios.size() + " destinatario(s)");
            } catch (Exception e) {
                System.err.println("[Correo] No se pudo enviar \"" + asunto + "\": " + e.getMessage());
            }
        });
    }
}
