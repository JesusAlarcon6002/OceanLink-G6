package com.oceanlink.servlets;

import com.oceanlink.daos.CuentasDao;
import com.oceanlink.services.CorreoService;
import com.oceanlink.services.NotificadorCorreos;
import com.oceanlink.util.ConfigCorreo;
import com.oceanlink.util.Html;
import com.oceanlink.util.LimiteTasa;
import com.oceanlink.util.Seguridad;
import com.oceanlink.util.ValidadorCorreo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Collections;

/**
 * "Olvidé mi contraseña" (pública, sin sesión de usuario). Dos pasos:
 *   1. POST accion=pedir   correo=...                         -> si el correo es de un usuario activo se le manda un código de 6 dígitos
 *   2. POST accion=cambiar codigo=... clave=... clave2=...    -> con el código correcto se guarda la clave nueva
 * Seguridad: la respuesta del paso 1 es SIEMPRE la misma (no revela si el correo existe); el código vence a los 10 min,
 * 5 intentos, 30 s entre reenvíos, 5 pedidos cada 15 min por IP y 3 por correo (para no llenar de correos a alguien).
 * El código nunca va a la campana ni al registro. Sin servidor de correo configurado la función queda desactivada.
 */
@WebServlet(name = "RecuperarServlet", value = "/RecuperarServlet")
public class RecuperarServlet extends HttpServlet {

    private static final String ATRIBUTO = "recuperarClave";
    private static final long VIGENCIA_MS = 10 * 60_000L;
    private static final long ESPERA_MS = 30_000L;
    private static final long VENTANA_MS = 15 * 60_000L;
    private static final int MAX_INTENTOS = 5;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private static final class Pendiente implements Serializable {
        String correo;
        String codigo;      // null si el correo no existe (el flujo se ve igual, pero nunca coincide)
        String nombre;
        long vence, enviado;
        int intentos;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        mostrar(request, response, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        if ("pedir".equals(accion)) {
            pedir(request, response);
        } else if ("cambiar".equals(accion)) {
            cambiar(request, response);
        } else {
            mostrar(request, response, null, null);
        }
    }

    private void pedir(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String correo = request.getParameter("correo") == null ? "" : request.getParameter("correo").trim();
        if (!ConfigCorreo.estaConfigurado()) {
            mostrar(request, response, "Por ahora no se pueden enviar códigos por correo. Pídele a un Network Admin que te cambie la contraseña.", null);
            return;
        }
        if (!ValidadorCorreo.sintaxisValida(correo)) {
            mostrar(request, response, "Escribe un correo válido.", null);
            return;
        }
        HttpSession sesion = request.getSession();
        long ahora = System.currentTimeMillis();
        Pendiente previo = (Pendiente) sesion.getAttribute(ATRIBUTO);
        if (previo != null && previo.correo.equalsIgnoreCase(correo) && ahora - previo.enviado < ESPERA_MS) {
            long falta = (ESPERA_MS - (ahora - previo.enviado) + 999) / 1000;
            mostrar(request, response, "Espera " + falta + " segundos antes de pedir otro código.", null);
            return;
        }
        if (!LimiteTasa.permitir("rec-ip|" + request.getRemoteAddr(), 5, VENTANA_MS)
                || !LimiteTasa.permitir("rec-c|" + correo.toLowerCase(), 3, VENTANA_MS)) {
            mostrar(request, response, "Pediste demasiados códigos. Espera unos minutos e inténtalo de nuevo.", null);
            return;
        }
        String nombre = new CuentasDao().nombreUsuarioActivo(correo);
        Pendiente p = new Pendiente();
        p.correo = correo;
        p.nombre = nombre;
        p.enviado = ahora;
        p.vence = ahora + VIGENCIA_MS;
        if (nombre != null) {
            p.codigo = String.format("%06d", ALEATORIO.nextInt(1_000_000));
            String html = NotificadorCorreos.plantilla("Recuperar contraseña",
                    "<p style=\"margin:0 0 12px\">Usa este código para elegir una contraseña nueva en OceanLink:</p>"
                            + "<p style=\"font-size:32px;letter-spacing:8px;font-weight:bold;margin:8px 0 16px;color:#1e3a5f\">" + Html.esc(p.codigo) + "</p>"
                            + "<p style=\"margin:0;font-size:13px;color:#475569\">Vence en 10 minutos. Si no lo pediste tú, ignora este mensaje: tu contraseña sigue igual.</p>");
            CorreoService.enviarSinCampana(Collections.singletonList(correo), "[OceanLink] Código para recuperar tu contraseña", html);
        }
        sesion.setAttribute(ATRIBUTO, p);
        mostrar(request, response, null, "Si el correo pertenece a una cuenta activa, te enviamos un código de 6 dígitos (revisa también spam).");
    }

    private void cambiar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sesion = request.getSession();
        Pendiente p = (Pendiente) sesion.getAttribute(ATRIBUTO);
        if (p == null) {
            mostrar(request, response, "Primero pide el código con tu correo.", null);
            return;
        }
        long ahora = System.currentTimeMillis();
        if (ahora > p.vence) {
            sesion.removeAttribute(ATRIBUTO);
            mostrar(request, response, "El código venció. Pide uno nuevo.", null);
            return;
        }
        String codigo = request.getParameter("codigo") == null ? "" : request.getParameter("codigo").trim();
        String clave = request.getParameter("clave");
        String clave2 = request.getParameter("clave2");
        boolean coincide = p.codigo != null
                && MessageDigest.isEqual(codigo.getBytes(StandardCharsets.UTF_8), p.codigo.getBytes(StandardCharsets.UTF_8));
        if (!coincide) {
            p.intentos++;
            if (p.intentos >= MAX_INTENTOS) {
                sesion.removeAttribute(ATRIBUTO);
                mostrar(request, response, "Demasiados intentos fallidos. Pide un código nuevo.", null);
            } else {
                sesion.setAttribute(ATRIBUTO, p);
                mostrar(request, response, "Código incorrecto. Te quedan " + (MAX_INTENTOS - p.intentos) + " intento(s).", null);
            }
            return;
        }
        String errorClave = clave == null ? "Escribe la contraseña nueva." : Seguridad.validarClave(clave);
        if (errorClave == null && !clave.equals(clave2)) errorClave = "Las contraseñas no coinciden.";
        if (errorClave != null) {
            mostrar(request, response, errorClave, null);
            return;
        }
        if (!new CuentasDao().cambiarClavePorCorreo(p.correo, clave)) {
            mostrar(request, response, "No se pudo cambiar la contraseña. Inténtalo de nuevo.", null);
            return;
        }
        sesion.removeAttribute(ATRIBUTO);
        NotificadorCorreos.contrasenaCambiada(p.nombre, p.correo);
        response.sendRedirect(request.getContextPath() + "/LoginServlet?ok=clave");
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response, String error, String aviso)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession();
        Pendiente p = (Pendiente) sesion.getAttribute(ATRIBUTO);
        boolean paso2 = p != null && System.currentTimeMillis() <= p.vence;
        request.setAttribute("error", error);
        request.setAttribute("aviso", aviso);
        request.setAttribute("paso2", paso2);
        request.setAttribute("correo", paso2 ? p.correo : (request.getParameter("correo") == null ? "" : request.getParameter("correo")));
        request.setAttribute("smtp", ConfigCorreo.estaConfigurado());
        request.getRequestDispatcher("/WEB-INF/vistas/recuperarClave.jsp").forward(request, response);
    }
}
