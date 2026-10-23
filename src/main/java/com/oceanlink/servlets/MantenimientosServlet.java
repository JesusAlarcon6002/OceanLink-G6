package com.oceanlink.servlets;

import com.oceanlink.beans.Mantenimientos;
import com.oceanlink.daos.Landing_StationsDao;
import com.oceanlink.daos.MantenimientosDao;
import com.oceanlink.daos.SegmentosDao;
import jakarta.servlet.RequestDispatcher;
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

@WebServlet(name = "MantenimientosServlet", value = "/MantenimientosServlet")
public class MantenimientosServlet extends HttpServlet {

    // Valores permitidos (los mismos ENUM de la tabla mantenimientos)
    private static final List<String> TIPOS = Arrays.asList("Preventivo", "Correctivo");
    private static final List<String> ESTADOS = Arrays.asList("Programado", "En curso", "Finalizado");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action") == null ? "lista" : request.getParameter("action");
        MantenimientosDao mantenimientosDao = new MantenimientosDao();
        RequestDispatcher view;

        switch (action) {
            case "formCrear":
                Mantenimientos nuevo = new Mantenimientos();
                nuevo.setTipo("Preventivo");
                nuevo.setEstado("Programado");
                mostrarFormulario(request, response, nuevo, "crear");
                break;
            case "editar":
                int id = leerId(request.getParameter("id"));
                Mantenimientos existente = id > 0 ? mantenimientosDao.obtenerMantenimientos(id) : null;
                if (existente == null) {
                    response.sendRedirect(request.getContextPath() + "/MantenimientosServlet");
                } else {
                    mostrarFormulario(request, response, existente, "editar");
                }
                break;
            case "lista":
            default:
                request.setAttribute("listaMantenimientos", mantenimientosDao.obtenerListaMantenimientos());
                view = request.getRequestDispatcher("listaMantenimientos.jsp");
                view.forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action") == null ? "lista" : request.getParameter("action");
        MantenimientosDao mantenimientosDao = new MantenimientosDao();

        switch (action) {
            case "guardar":
                guardar(request, response, mantenimientosDao);
                break;
            case "borrar":
                int id = leerId(request.getParameter("id"));
                boolean borrado = id > 0 && mantenimientosDao.borrarMantenimientos(id);
                // Post/Redirect/Get: redirigimos para que F5 no repita la acción
                response.sendRedirect(request.getContextPath() + "/MantenimientosServlet?" + (borrado ? "ok=borrado" : "err=borrar"));
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/MantenimientosServlet");
        }
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response, MantenimientosDao dao)
            throws ServletException, IOException {

        ArrayList<String> errores = new ArrayList<>();
        Mantenimientos m = leerFormulario(request, errores);

        // la etiqueta es UNIQUE en la BD: avisamos antes de que falle el INSERT/UPDATE
        if (m.getEtiqueta() != null && !m.getEtiqueta().isEmpty()
                && dao.existeEtiqueta(m.getEtiqueta(), m.getIdMantenimiento())) {
            errores.add("Ya existe un mantenimiento con la etiqueta \"" + m.getEtiqueta() + "\".");
        }

        boolean esNuevo = m.getIdMantenimiento() == 0;

        if (errores.isEmpty()) {
            boolean ok = esNuevo ? dao.crearMantenimientos(m) : dao.actualizarMantenimientos(m);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/MantenimientosServlet?ok=" + (esNuevo ? "creado" : "actualizado"));
                return;
            }
            errores.add("No se pudo guardar en la base de datos. Revisa la consola del servidor.");
        }

        // hubo errores: volvemos al formulario con lo que el usuario escribió
        request.setAttribute("errores", errores);
        mostrarFormulario(request, response, m, esNuevo ? "crear" : "editar");
    }

    /** Lee y valida los campos del formulario. Los problemas se agregan a "errores". */
    private Mantenimientos leerFormulario(HttpServletRequest request, ArrayList<String> errores) {
        Mantenimientos m = new Mantenimientos();

        m.setIdMantenimiento(Math.max(leerId(request.getParameter("id")), 0));

        String etiqueta = texto(request.getParameter("etiqueta"));
        m.setEtiqueta(etiqueta);
        if (etiqueta == null) {
            errores.add("La etiqueta es obligatoria (ej: MAN-01).");
        } else if (etiqueta.length() > 45) {
            errores.add("La etiqueta no puede pasar de 45 caracteres.");
        }

        String tipo = texto(request.getParameter("tipo"));
        m.setTipo(tipo);
        if (tipo == null || !TIPOS.contains(tipo)) {
            errores.add("Elige un tipo válido (Preventivo o Correctivo).");
        }

        String estado = texto(request.getParameter("estado"));
        m.setEstado(estado);
        if (estado == null || !ESTADOS.contains(estado)) {
            errores.add("Elige un estado válido.");
        }

        String descripcion = texto(request.getParameter("descripcion"));
        m.setDescripcion(descripcion);
        if (descripcion == null) {
            errores.add("La descripción es obligatoria.");
        }
        m.setActividades(texto(request.getParameter("actividades")));
        m.setResultado(texto(request.getParameter("resultado")));

        // Fecha programada
        LocalDateTime programada = parsearFecha(request.getParameter("fechaProgramada"));
        m.setFechaProgramada(programada);
        if (programada == null) {
            errores.add("La fecha programada es obligatoria y debe ser válida.");
        }

        // Duración en horas (la columna es DECIMAL(5,2): máximo 999.99)
        String duracionStr = texto(request.getParameter("duracion"));
        if (duracionStr == null) {
            errores.add("La duración estimada es obligatoria.");
        } else {
            try {
                double duracion = Double.parseDouble(duracionStr.replace(',', '.'));
                if (duracion <= 0 || duracion > 999.99) {
                    errores.add("La duración debe estar entre 0.01 y 999.99 horas.");
                } else {
                    m.setDuracionEstimadaHoras(duracion);
                }
            } catch (NumberFormatException e) {
                errores.add("La duración debe ser un número (ej: 4 o 2.5).");
            }
        }

        // Fecha de finalización: solo obligatoria si el estado es Finalizado
        String finStr = texto(request.getParameter("fechaFinalizacion"));
        LocalDateTime fin = parsearFecha(finStr);
        if (finStr != null && fin == null) {
            errores.add("La fecha de finalización no es válida.");
        }
        m.setFechaFinalizacion(fin);
        if ("Finalizado".equals(estado) && fin == null) {
            errores.add("Si el estado es Finalizado, indica la fecha de finalización.");
        }
        if (fin != null && programada != null && fin.isBefore(programada)) {
            errores.add("La fecha de finalización no puede ser anterior a la programada.");
        }

        // Infraestructura: "S:5" = segmento 5, "E:2" = estación 2, vacío = ninguna
        String infra = texto(request.getParameter("infra"));
        if (infra != null) {
            int id = infra.length() > 2 ? leerId(infra.substring(2)) : -1;
            if (infra.startsWith("S:") && id > 0) {
                m.setTipoInfra("Segmento");
                m.setIdInfra(id);
            } else if (infra.startsWith("E:") && id > 0) {
                m.setTipoInfra("Estación");
                m.setIdInfra(id);
            } else {
                errores.add("La infraestructura elegida no es válida.");
            }
        }
        return m;
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   Mantenimientos m, String modo) throws ServletException, IOException {
        request.setAttribute("mantenimiento", m);
        request.setAttribute("modo", modo);
        request.setAttribute("listaSegmentos", new SegmentosDao().obtenerListaSegmentos());
        request.setAttribute("listaLanding_Stations", new Landing_StationsDao().obtenerListaLanding_Stations());
        request.getRequestDispatcher("formMantenimientos.jsp").forward(request, response);
    }

    // ---- utilidades ----

    /** Devuelve el texto sin espacios sobrantes, o null si viene vacío. */
    private String texto(String valor) {
        if (valor == null) {
            return null;
        }
        valor = valor.trim();
        return valor.isEmpty() ? null : valor;
    }

    /** Convierte "123" en 123; si no es un número válido devuelve -1. */
    private int leerId(String valor) {
        try {
            return Integer.parseInt(valor == null ? "" : valor.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Convierte el valor de un input datetime-local (2026-10-23T14:30); null si está vacío o mal escrito. */
    private LocalDateTime parsearFecha(String valor) {
        valor = texto(valor);
        if (valor == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(valor);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
