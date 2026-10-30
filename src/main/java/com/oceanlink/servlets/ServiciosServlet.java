package com.oceanlink.servlets;

import com.oceanlink.beans.Clientes;
import com.oceanlink.beans.Rutas;
import com.oceanlink.beans.Servicios;
import com.oceanlink.beans.Solicitudes;
import com.oceanlink.daos.ReservasDao;
import com.oceanlink.daos.ServiciosDao;
import com.oceanlink.daos.SolicitudesDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebServlet(name = "ServiciosServlet", value = "/ServiciosServlet")
public class ServiciosServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action") == null ? "lista" : request.getParameter("action");
        ServiciosDao dao = new ServiciosDao();

        switch (action) {
            case "formCrear": {
                int idSol = leerId(request.getParameter("solicitud"));
                if (idSol <= 0) {
                    mostrarSelectorSolicitud(request, response);
                    break;
                }
                Solicitudes sol = new SolicitudesDao().obtenerSolicitudes(idSol);
                if (sol == null) {
                    response.sendRedirect(request.getContextPath() + "/ServiciosServlet");
                    break;
                }
                Servicios nuevo = new Servicios();
                nuevo.setIdSolicitud(idSol);
                nuevo.setEtiqueta(sol.getEtiqueta().replace("SOL", "SRV"));
                nuevo.setFechaActivacion(LocalDate.now());
                mostrarFormulario(request, response, nuevo, "crear", sol);
                break;
            }
            case "editar": {
                int id = leerId(request.getParameter("id"));
                Servicios existente = id > 0 ? dao.obtenerServicios(id) : null;
                if (existente == null) {
                    response.sendRedirect(request.getContextPath() + "/ServiciosServlet");
                } else {
                    mostrarFormulario(request, response, existente, "editar", null);
                }
                break;
            }
            case "lista":
            default:
                request.setAttribute("listaServicios", dao.obtenerListaServicios());
                request.getRequestDispatcher("listaServicios.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action") == null ? "lista" : request.getParameter("action");
        ServiciosDao dao = new ServiciosDao();

        switch (action) {
            case "guardar":
                guardar(request, response, dao);
                break;
            case "borrar": {
                int id = leerId(request.getParameter("id"));
                String error = id > 0 ? dao.borrarServicios(id) : "Servicio inválido.";
                if (error == null) {
                    response.sendRedirect(request.getContextPath() + "/ServiciosServlet?ok=borrado");
                } else {
                    response.sendRedirect(request.getContextPath() + "/ServiciosServlet?err=" + URLEncoder.encode(error, StandardCharsets.UTF_8));
                }
                break;
            }
            default:
                response.sendRedirect(request.getContextPath() + "/ServiciosServlet");
        }
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response, ServiciosDao dao)
            throws ServletException, IOException {
        ArrayList<String> errores = new ArrayList<>();
        int id = Math.max(leerId(request.getParameter("id")), 0);
        boolean esNuevo = id == 0;
        int idSolicitud = leerId(request.getParameter("solicitud"));

        Servicios s = new Servicios();
        s.setIdServicio(id);
        Servicios original = null;
        Solicitudes sol = null;
        if (esNuevo) {
            sol = idSolicitud > 0 ? new SolicitudesDao().obtenerSolicitudes(idSolicitud) : null;
            if (sol == null) {
                response.sendRedirect(request.getContextPath() + "/ServiciosServlet");
                return;
            }
            s.setIdSolicitud(idSolicitud);
            s.setIdRuta(leerId(request.getParameter("ruta")));
            if (s.getIdRuta() <= 0) errores.add("Elige la ruta del servicio.");
        } else {
            original = dao.obtenerServicios(id);
            if (original == null) {
                response.sendRedirect(request.getContextPath() + "/ServiciosServlet");
                return;
            }
            // lo que no se puede editar se conserva
            s.setCliente(original.getCliente());
            s.setIdRuta(original.getIdRuta());
            s.setEtiquetaRuta(original.getEtiquetaRuta());
            s.setIdSolicitud(original.getIdSolicitud());
            s.setEtiquetaSolicitud(original.getEtiquetaSolicitud());
            s.setCapacidadContratadaGbps(original.getCapacidadContratadaGbps());
        }

        String etiqueta = texto(request.getParameter("etiqueta"));
        s.setEtiqueta(etiqueta);
        if (etiqueta == null) {
            errores.add("La etiqueta es obligatoria (ej: SRV-001).");
        } else if (etiqueta.length() > 100) {
            errores.add("La etiqueta no puede pasar de 100 caracteres.");
        } else if (dao.existeEtiqueta(etiqueta, id)) {
            errores.add("Ya existe un servicio con la etiqueta \"" + etiqueta + "\".");
        }

        LocalDate activacion = parsearFecha(request.getParameter("fechaActivacion"), "activación", errores);
        LocalDate expiracion = parsearFecha(request.getParameter("fechaExpiracion"), "expiración", errores);
        s.setFechaActivacion(activacion);
        s.setFechaExpiracion(expiracion);
        if (activacion != null && expiracion != null && expiracion.isBefore(activacion)) {
            errores.add("La fecha de expiración no puede ser anterior a la de activación.");
        }

        String estado = texto(request.getParameter("estado"));
        s.setEstado(estado);
        List<String> permitidos = esNuevo ? Arrays.asList("Pendiente", "Activo") : estadosPermitidos(original.getEstado());
        if (estado == null || !permitidos.contains(estado)) {
            errores.add("Estado no permitido para este servicio.");
        }

        if (errores.isEmpty()) {
            String error = esNuevo ? dao.crearServicios(s, idSolicitud, estado) : dao.actualizarServicios(s);
            if (error == null) {
                response.sendRedirect(request.getContextPath() + "/ServiciosServlet?ok=" + (esNuevo ? "creado" : "actualizado"));
                return;
            }
            errores.add(error);
        }
        request.setAttribute("errores", errores);
        if (!esNuevo) s.setEstado(estado);
        mostrarFormulario(request, response, s, esNuevo ? "crear" : "editar", sol);
    }

    /** Estados a los que puede pasar un servicio desde su estado actual (incluye el mismo). */
    public static List<String> estadosPermitidos(String actual) {
        if ("Pendiente".equals(actual)) return Arrays.asList("Pendiente", "Activo", "Finalizado");
        if ("Activo".equals(actual)) return Arrays.asList("Activo", "Suspendido", "Finalizado");
        if ("Suspendido".equals(actual)) return Arrays.asList("Suspendido", "Activo", "Finalizado");
        return Arrays.asList("Finalizado");
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response, Servicios servicio,
                                   String modo, Solicitudes sol) throws ServletException, IOException {
        request.setAttribute("servicio", servicio);
        request.setAttribute("modo", modo);
        if ("crear".equals(modo)) {
            request.setAttribute("solicitud", sol);
            List<Rutas> compatibles = new ServiciosDao().rutasCompatibles(sol.getIdSolicitud(),
                    sol.getEstacionOrigen().getId_landing_stations(), sol.getEstacionDestino().getId_landing_stations());
            request.setAttribute("rutasCompatibles", compatibles);
            List<ReservasDao.Reserva> reservas = new ReservasDao().obtenerReservas(sol.getIdSolicitud());
            request.setAttribute("capacidadReservada", reservas.isEmpty() ? 0.0 : reservas.get(0).capacidad);
            request.setAttribute("estadosDisponibles", Arrays.asList("Pendiente", "Activo"));
        } else {
            request.setAttribute("estadosDisponibles", estadosPermitidos(
                    new ServiciosDao().obtenerServicios(servicio.getIdServicio()).getEstado()));
        }
        request.getRequestDispatcher("formServicios.jsp").forward(request, response);
    }

    /** Sin solicitud elegida: lista las solicitudes que sí pueden convertirse en servicio. */
    private void mostrarSelectorSolicitud(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ReservasDao reservasDao = new ReservasDao();
        List<Solicitudes> elegibles = new ArrayList<>();
        for (Solicitudes s : new SolicitudesDao().obtenerListaSolicitudes()) {
            if ("Aprobada".equals(s.getEstado()) && !reservasDao.obtenerReservas(s.getIdSolicitud()).isEmpty()
                    && !reservasDao.tieneServicio(s.getIdSolicitud())) {
                elegibles.add(s);
            }
        }
        request.setAttribute("solicitudesElegibles", elegibles);
        request.setAttribute("modo", "elegir");
        request.getRequestDispatcher("formServicios.jsp").forward(request, response);
    }

    private LocalDate parsearFecha(String valor, String nombre, List<String> errores) {
        valor = texto(valor);
        if (valor == null) return null;
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            errores.add("La fecha de " + nombre + " no es válida.");
            return null;
        }
    }

    private String texto(String valor) {
        if (valor == null) return null;
        valor = valor.trim();
        return valor.isEmpty() ? null : valor;
    }

    private int leerId(String valor) {
        try {
            return Integer.parseInt(valor == null ? "" : valor.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
