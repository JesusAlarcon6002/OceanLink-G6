package com.oceanlink.servlets;

import com.oceanlink.daos.HistorialDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Historial de utilización (solo lectura). */
@WebServlet(name = "HistorialServlet", value = "/HistorialServlet")
public class HistorialServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        request.setAttribute("listaHistorial", new HistorialDao().obtenerListaHistorial());
        request.getRequestDispatcher("listaHistorial.jsp").forward(request, response);
    }
}
