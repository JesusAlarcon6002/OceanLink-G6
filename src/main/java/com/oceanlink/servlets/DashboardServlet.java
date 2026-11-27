package com.oceanlink.servlets;

import com.oceanlink.daos.DashboardDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Dashboard general de la red (indicadores + gráficos). Pantalla: dashboard.jsp */
@WebServlet(name = "DashboardServlet", value = "/DashboardServlet")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("datos", new DashboardDao().obtenerDatos());
        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }
}
