package com.oceanlink.servlets;

import com.oceanlink.services.CorreoService;
import com.oceanlink.util.ConfigCorreo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** "Registro de correos": estado del servidor de correo y los últimos envíos (reales). Solo Network Admin. */
@WebServlet(name = "CorreoServlet", value = "/CorreoServlet")
public class CorreoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("configurado", ConfigCorreo.estaConfigurado());
        request.setAttribute("smtpHost", ConfigCorreo.host());
        request.setAttribute("smtpPuerto", ConfigCorreo.puerto());
        request.setAttribute("smtpRemitente", ConfigCorreo.remitente());
        request.setAttribute("redirigirA", ConfigCorreo.redirigirA());
        request.setAttribute("registros", CorreoService.registros());
        request.setAttribute("vencRevisados", request.getParameter("venc"));
        request.getRequestDispatcher("registroCorreos.jsp").forward(request, response);
    }

    /** "Revisar vencimientos ahora": ejecuta la misma revisión diaria de servicios por vencer. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int n = "vencimientos".equals(request.getParameter("accion")) ? com.oceanlink.listeners.VencimientosListener.revisar(false) : -1;
        response.sendRedirect(request.getContextPath() + "/CorreoServlet" + (n >= 0 ? "?venc=" + n : ""));
    }
}
