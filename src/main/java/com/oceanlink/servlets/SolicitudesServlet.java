package com.oceanlink.servlets;

import com.oceanlink.beans.Clientes;
import com.oceanlink.beans.Landing_Stations;
import com.oceanlink.beans.Solicitudes;
import com.oceanlink.daos.ClientesDao;
import com.oceanlink.daos.Landing_StationsDao;
import com.oceanlink.daos.SolicitudesDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebServlet(name = "SolicitudesServlet", value = "/SolicitudesServlet")
public class SolicitudesServlet extends HttpServlet {

    // Mismos valores del ENUM de la tabla solicitudes_capacidad (con tildes, tal como están en la BD)
    private static final List<String> ESTADOS = Arrays.asList("Registrada", "En evaluación", "Aprobada",
            "Rechazada", "Provisionada", "Activa");
    private static final double CAPACIDAD_MAXIMA = 99999999.99; // DECIMAL(10,2)

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action") == null ? "lista" : request.getParameter("action");
        SolicitudesDao dao = new SolicitudesDao();

        switch (action) {
            case "formCrear":
                Solicitudes nueva = new Solicitudes();
                nueva.setEstado("Registrada");
                nueva.setFechaSolicitud(LocalDateTime.now().withSecond(0).withNano(0));
                mostrarFormulario(request, response, nueva, "crear");
                break;
            case "editar":
                int id = leerId(request.getParameter("id"));
                Solicitudes existente = id > 0 ? dao.obtenerSolicitudes(id) : null;
                if (existente == null) {
                    response.sendRedirect(request.getContextPath() + "/SolicitudesServlet");
                } else {
                    mostrarFormulario(request, response, existente, "editar");
                }
                break;
            case "lista":
            default:
                request.setAttribute("listaSolicitudes", dao.obtenerListaSolicitudes());
                request.getRequestDispatcher("listaSolicitudes.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action") == null ? "lista" : request.getParameter("action");
        SolicitudesDao dao = new SolicitudesDao();

        switch (action) {
            case "guardar":
                guardar(request, response, dao);
                break;
            case "borrar":
                int id = leerId(request.getParameter("id"));
                String resultado;
                if (id <= 0) {
                    resultado = "err=borrar";
                } else if (dao.tieneReservas(id)) {
                    // borrarla dejaría reservas de capacidad huérfanas en los segmentos
                    resultado = "err=reservas";
                } else {
                    resultado = dao.borrarSolicitudes(id) ? "ok=borrado" : "err=borrar";
                }
                response.sendRedirect(request.getContextPath() + "/SolicitudesServlet?" + resultado);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/SolicitudesServlet");
        }
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response, SolicitudesDao dao)
            throws ServletException, IOException {
        ArrayList<String> errores = new ArrayList<>();
        Solicitudes sol = leerFormulario(request, errores);

        if (sol.getEtiqueta() != null && dao.existeEtiqueta(sol.getEtiqueta(), sol.getIdSolicitud())) {
            errores.add("Ya existe una solicitud con la etiqueta \"" + sol.getEtiqueta() + "\".");
        }

        boolean esNueva = sol.getIdSolicitud() == 0;

        // Si ya tiene capacidad reservada, los datos de la reserva y su estado se manejan desde "Capacidad"/Servicios
        if (!esNueva && errores.isEmpty() && dao.tieneReservas(sol.getIdSolicitud())) {
            Solicitudes original = dao.obtenerSolicitudes(sol.getIdSolicitud());
            if (original != null) {
                boolean cambioReserva = original.getEstacionOrigen().getId_landing_stations() != sol.getEstacionOrigen().getId_landing_stations()
                        || original.getEstacionDestino().getId_landing_stations() != sol.getEstacionDestino().getId_landing_stations()
                        || Math.abs(original.getCapacidadSolicitadaGbps() - sol.getCapacidadSolicitadaGbps()) > 1e-9;
                if (cambioReserva) {
                    errores.add("La solicitud tiene capacidad reservada: no se puede cambiar origen, destino ni capacidad. Libera la reserva primero (botón Capacidad).");
                }
                if (!original.getEstado().equals(sol.getEstado())) {
                    errores.add("La solicitud tiene capacidad reservada: su estado lo manejan la reserva y los servicios. Usa el botón Capacidad o la pantalla de Servicios.");
                }
            }
        }

        if (errores.isEmpty()) {
            boolean ok = esNueva ? dao.crearSolicitudes(sol) : dao.actualizarSolicitudes(sol);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/SolicitudesServlet?ok=" + (esNueva ? "creado" : "actualizado"));
                return;
            }
            errores.add("No se pudo guardar en la base de datos. Revisa la consola del servidor.");
        }

        request.setAttribute("errores", errores);
        mostrarFormulario(request, response, sol, esNueva ? "crear" : "editar");
    }

    private Solicitudes leerFormulario(HttpServletRequest request, ArrayList<String> errores) {
        Solicitudes sol = new Solicitudes();
        sol.setIdSolicitud(Math.max(leerId(request.getParameter("id")), 0));

        String etiqueta = texto(request.getParameter("etiqueta"));
        sol.setEtiqueta(etiqueta);
        if (etiqueta == null) {
            errores.add("La etiqueta es obligatoria (ej: SOL-001).");
        } else if (etiqueta.length() > 45) {
            errores.add("La etiqueta no puede pasar de 45 caracteres.");
        }

        // Cliente, origen y destino llegan como ID desde los select; guardamos beans solo con el ID
        int idCliente = leerId(request.getParameter("cliente"));
        Clientes cliente = new Clientes();
        cliente.setIdCliente(idCliente);
        sol.setCliente(cliente);
        if (idCliente <= 0) {
            errores.add("Elige un cliente.");
        }

        int idOrigen = leerId(request.getParameter("origen"));
        int idDestino = leerId(request.getParameter("destino"));
        Landing_Stations origen = new Landing_Stations();
        origen.setId_landing_stations(idOrigen);
        Landing_Stations destino = new Landing_Stations();
        destino.setId_landing_stations(idDestino);
        sol.setEstacionOrigen(origen);
        sol.setEstacionDestino(destino);
        if (idOrigen <= 0 || idDestino <= 0) {
            errores.add("Elige la estación de origen y la de destino.");
        } else if (idOrigen == idDestino) {
            errores.add("El origen y el destino deben ser estaciones distintas.");
        }

        String capStr = texto(request.getParameter("capacidad"));
        if (capStr == null) {
            errores.add("La capacidad solicitada es obligatoria.");
        } else {
            try {
                double cap = Double.parseDouble(capStr.replace(',', '.'));
                if (cap <= 0 || cap > CAPACIDAD_MAXIMA) {
                    errores.add("La capacidad debe ser mayor que 0 Gbps.");
                } else {
                    sol.setCapacidadSolicitadaGbps(cap);
                }
            } catch (NumberFormatException e) {
                errores.add("La capacidad debe ser un número (ej: 20 o 12.5).");
            }
        }

        LocalDateTime fecha = parsearFecha(request.getParameter("fechaSolicitud"));
        sol.setFechaSolicitud(fecha);
        if (fecha == null) {
            errores.add("La fecha de solicitud es obligatoria y debe ser válida.");
        }

        String estado = texto(request.getParameter("estado"));
        sol.setEstado(estado);
        if (estado == null || !ESTADOS.contains(estado)) {
            errores.add("Elige un estado válido.");
        }

        sol.setObservaciones(texto(request.getParameter("observaciones")));
        return sol;
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   Solicitudes sol, String modo) throws ServletException, IOException {
        request.setAttribute("solicitud", sol);
        request.setAttribute("modo", modo);
        request.setAttribute("listaClientes", new ClientesDao().getListaClientes());
        request.setAttribute("listaLanding_Stations", new Landing_StationsDao().obtenerListaLanding_Stations());
        request.getRequestDispatcher("formSolicitudes.jsp").forward(request, response);
    }

    // ---- utilidades ----

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

    private LocalDateTime parsearFecha(String valor) {
        valor = texto(valor);
        if (valor == null) return null;
        try {
            return LocalDateTime.parse(valor);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
