package com.oceanlink.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Configuración se unió al Perfil (datos, contraseña y apariencia): esta ruta vieja solo redirige. */
@WebServlet(name = "ConfiguracionServlet", value = "/ConfiguracionServlet")
public class ConfiguracionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/PerfilServlet");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/PerfilServlet");
    }
}
