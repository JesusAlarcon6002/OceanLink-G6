package com.oceanlink.servlets;

import com.oceanlink.services.CorreoService;
import com.oceanlink.util.ConfigCorreo;
import com.oceanlink.util.Html;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

/** Pantalla para comprobar que el correo funciona: envía un mensaje de prueba. Solo Network Admin. */
@WebServlet(name = "CorreoServlet", value = "/CorreoServlet")
public class CorreoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        mostrar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String destino = request.getParameter("destino") == null ? "" : request.getParameter("destino").trim();
        request.setAttribute("destino", destino);

        if (destino.isEmpty() || !destino.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            request.setAttribute("error", "Escribe un correo de destino válido.");
        } else if (!ConfigCorreo.estaConfigurado()) {
            request.setAttribute("error", "El servidor SMTP no está configurado. Revisa las variables SMTP_* (ver guía).");
        } else {
            try {
                String html = "<div style=\"font-family:Arial,sans-serif\"><h2>Correo de prueba de OceanLink</h2>"
                        + "<p>Si ves este mensaje, la integración SMTP funciona correctamente.</p>"
                        + "<p style=\"color:#64748b;font-size:12px\">Enviado a " + Html.esc(destino) + "</p></div>";
                CorreoService.enviar(Collections.singletonList(destino), "[OceanLink] Correo de prueba", html);
                request.setAttribute("ok", "Correo enviado a " + destino + ". Revisa tu bandeja de pruebas (Mailtrap).");
            } catch (Exception e) {
                request.setAttribute("error", "No se pudo enviar: " + e.getMessage());
            }
        }
        mostrar(request, response);
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("configurado", ConfigCorreo.estaConfigurado());
        request.setAttribute("smtpHost", ConfigCorreo.host());
        request.setAttribute("smtpPuerto", ConfigCorreo.puerto());
        request.setAttribute("smtpRemitente", ConfigCorreo.remitente());
        request.getRequestDispatcher("correoPrueba.jsp").forward(request, response);
    }
}
