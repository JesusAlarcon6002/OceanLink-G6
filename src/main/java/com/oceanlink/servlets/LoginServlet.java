package com.oceanlink.servlets;

import com.oceanlink.beans.UsuarioSesion;
import com.oceanlink.daos.LoginDao;
import com.oceanlink.filters.AuthFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Pantalla de login (GET) y validación de credenciales (POST). */
@WebServlet(name = "LoginServlet", value = "/LoginServlet")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession s = request.getSession(false);
        if (s != null && s.getAttribute(AuthFilter.ATRIBUTO_USUARIO) != null) {
            response.sendRedirect(request.getContextPath() + "/InicioServlet");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/vistas/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String correo = request.getParameter("correo");
        String clave = request.getParameter("clave");

        UsuarioSesion usuario = autenticar(correo, clave);
        if (usuario == null) {
            // Mensaje genérico: no dice si falló el correo o la contraseña
            request.setAttribute("error", "Correo o contraseña incorrectos.");
            request.setAttribute("correo", correo == null ? "" : correo.trim());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            request.getRequestDispatcher("/WEB-INF/vistas/login.jsp").forward(request, response);
            return;
        }

        // Se descarta cualquier sesión previa y se crea una nueva (evita fijación de sesión)
        HttpSession vieja = request.getSession(false);
        if (vieja != null) vieja.invalidate();
        HttpSession sesion = request.getSession(true);
        sesion.setMaxInactiveInterval(30 * 60); // 30 minutos sin actividad
        sesion.setAttribute(AuthFilter.ATRIBUTO_USUARIO, usuario);
        response.sendRedirect(request.getContextPath() + "/InicioServlet");
    }

    /** Separado para poder probar el flujo sin base de datos. */
    protected UsuarioSesion autenticar(String correo, String clave) {
        return new LoginDao().autenticar(correo, clave);
    }
}
