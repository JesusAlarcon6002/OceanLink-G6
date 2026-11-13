package com.oceanlink.servlets;

import com.oceanlink.beans.Reporte;
import com.oceanlink.daos.ClientesDao;
import com.oceanlink.daos.ReportesDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Reportes con filtros. Parámetros: tipo = utilizacion | incidencias | servicios | mantenimientos,
 * los filtros de cada tipo, y formato=csv para descargar. Pantalla: reportes.jsp
 */
@WebServlet(name = "ReportesServlet", value = "/ReportesServlet")
public class ReportesServlet extends HttpServlet {

    private static final List<String> TIPOS = Arrays.asList("utilizacion", "incidencias", "servicios", "mantenimientos");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String tipo = request.getParameter("tipo");
        if (tipo == null || !TIPOS.contains(tipo)) {
            tipo = "utilizacion";
        }

        ReportesDao dao = new ReportesDao();
        Reporte reporte;
        switch (tipo) {
            case "incidencias":
                reporte = dao.incidencias(request.getParameter("estado"), request.getParameter("severidad"),
                        request.getParameter("desde"), request.getParameter("hasta"));
                break;
            case "servicios":
                reporte = dao.servicios(leerId(request.getParameter("cliente")), request.getParameter("estado"));
                break;
            case "mantenimientos":
                reporte = dao.mantenimientos(request.getParameter("estado"), request.getParameter("tipoMant"),
                        request.getParameter("desde"), request.getParameter("hasta"));
                break;
            case "utilizacion":
            default:
                reporte = dao.utilizacion(request.getParameter("estado"), request.getParameter("nivel"));
                break;
        }

        if ("csv".equals(request.getParameter("formato"))) {
            byte[] datos = ("﻿" + reporte.aCsv()).getBytes(StandardCharsets.UTF_8); // BOM para que Excel lea las tildes
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"reporte-" + tipo + ".csv\"");
            response.setContentLength(datos.length);
            response.getOutputStream().write(datos);
            return;
        }

        request.setAttribute("tipo", tipo);
        request.setAttribute("reporte", reporte);
        request.setAttribute("listaClientes", new ClientesDao().getListaClientes());
        request.getRequestDispatcher("reportes.jsp").forward(request, response);
    }

    private int leerId(String valor) {
        try {
            return Integer.parseInt(valor == null ? "" : valor.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
