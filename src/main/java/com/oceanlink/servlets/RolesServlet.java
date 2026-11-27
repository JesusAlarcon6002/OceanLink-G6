package com.oceanlink.servlets;

import com.oceanlink.daos.RolesDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Roles y permisos (solo Network Admin, solo consulta). Muestra cada rol, cuántos usuarios activos tiene y a qué módulos
 * accede (acceso completo o solo lectura), tomado de la matriz de util.Permisos. El rol de cada usuario se cambia
 * desde Usuarios > Editar.
 */
@WebServlet(name = "RolesServlet", value = "/RolesServlet")
public class RolesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        request.setAttribute("conteoRoles", new RolesDao().contarUsuariosActivosPorRol());
        request.getRequestDispatcher("listaRoles.jsp").forward(request, response);
    }
}
