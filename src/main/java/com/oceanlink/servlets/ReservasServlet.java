package com.oceanlink.servlets;

import com.oceanlink.beans.Rutas;
import com.oceanlink.beans.Solicitudes;
import com.oceanlink.daos.ReservasDao;
import com.oceanlink.daos.RutasDao;
import com.oceanlink.daos.SolicitudesDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Reservar / liberar capacidad de una solicitud. Pantalla: reservarCapacidad.jsp */
@WebServlet(name = "ReservasServlet", value = "/ReservasServlet")
public class ReservasServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        int idSolicitud = leerId(request.getParameter("solicitud"));
        Solicitudes sol = idSolicitud > 0 ? new SolicitudesDao().obtenerSolicitudes(idSolicitud) : null;
        if (sol == null) {
            response.sendRedirect(request.getContextPath() + "/SolicitudesServlet");
            return;
        }
        ReservasDao reservasDao = new ReservasDao();
        request.setAttribute("solicitud", sol);
        request.setAttribute("reservas", reservasDao.obtenerReservas(idSolicitud));
        request.setAttribute("tieneServicio", reservasDao.tieneServicio(idSolicitud));
        request.setAttribute("rutasPosibles", new RutasDao().obtenerRutasEntre(
                sol.getEstacionOrigen().getId_landing_stations(), sol.getEstacionDestino().getId_landing_stations()));
        request.getRequestDispatcher("reservarCapacidad.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action") == null ? "" : request.getParameter("action");
        int idSolicitud = leerId(request.getParameter("solicitud"));
        String base = request.getContextPath() + "/ReservasServlet?solicitud=" + idSolicitud;
        if (idSolicitud <= 0) {
            response.sendRedirect(request.getContextPath() + "/SolicitudesServlet");
            return;
        }
        ReservasDao dao = new ReservasDao();
        String error;
        String ok;
        switch (action) {
            case "reservar":
                error = dao.reservar(idSolicitud, leerId(request.getParameter("ruta")));
                ok = "reservado";
                break;
            case "liberar":
                error = dao.liberar(idSolicitud);
                ok = "liberado";
                break;
            default:
                response.sendRedirect(base);
                return;
        }
        if (error == null) {
            response.sendRedirect(base + "&ok=" + ok);
        } else {
            response.sendRedirect(base + "&err=" + URLEncoder.encode(error, StandardCharsets.UTF_8));
        }
    }

    private int leerId(String valor) {
        try {
            return Integer.parseInt(valor == null ? "" : valor.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
