package com.oceanlink.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Pantalla principal de cada rol (el filtro ya garantiza que hay sesión). */
@WebServlet(name = "InicioServlet", value = "/InicioServlet")
public class InicioServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/vistas/inicioRol.jsp").forward(request, response);
    }
}
