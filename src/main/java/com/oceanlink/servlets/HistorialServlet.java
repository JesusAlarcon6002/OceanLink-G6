package com.oceanlink.servlets;

import com.oceanlink.daos.HistorialDao;
import com.oceanlink.daos.SegmentosDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Historial de utilización (solo lectura). Filtros opcionales por GET:
 * segmento (id), desde y hasta (AAAA-MM-DD).
 */
@WebServlet(name = "HistorialServlet", value = "/HistorialServlet")
public class HistorialServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        int idSegmento = 0;
        try {
            String p = request.getParameter("segmento");
            if (p != null && !p.isEmpty()) idSegmento = Math.max(0, Integer.parseInt(p));
        } catch (NumberFormatException ignorar) {
            // id inválido: se muestran todos
        }
        LocalDate desde = fecha(request.getParameter("desde"));
        LocalDate hasta = fecha(request.getParameter("hasta"));
        String errorFiltro = null;
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            errorFiltro = "La fecha \"desde\" no puede ser posterior a \"hasta\".";
            desde = null;
            hasta = null;
        }
        request.setAttribute("errorFiltro", errorFiltro);
        request.setAttribute("fSegmento", idSegmento);
        request.setAttribute("fDesde", desde == null ? "" : desde.toString());
        request.setAttribute("fHasta", hasta == null ? "" : hasta.toString());
        request.setAttribute("listaSegmentos", new SegmentosDao().obtenerListaSegmentos());
        request.setAttribute("listaHistorial", new HistorialDao().obtenerListaHistorial(idSegmento, desde, hasta));
        request.getRequestDispatcher("listaHistorial.jsp").forward(request, response);
    }

    private static LocalDate fecha(String texto) {
        if (texto == null || texto.isEmpty()) return null;
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
