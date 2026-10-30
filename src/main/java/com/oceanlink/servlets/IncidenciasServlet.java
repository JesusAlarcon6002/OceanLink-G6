package com.oceanlink.servlets;

import com.oceanlink.beans.Incidencias;
import com.oceanlink.daos.IncidenciasDao;
import com.oceanlink.daos.Landing_StationsDao;
import com.oceanlink.daos.SegmentosDao;
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

@WebServlet(name = "IncidenciasServlet", value = "/IncidenciasServlet")
public class IncidenciasServlet extends HttpServlet {

    // Valores permitidos (los mismos ENUM de la tabla incidencias; sin tildes, tal como están en la BD)
    private static final List<String> SEVERIDADES = Arrays.asList("Baja", "Media", "Alta", "Critica");
    private static final List<String> ESTADOS = Arrays.asList("Detectada", "En analisis",
            "Reparacion programada", "En reparacion", "Servicio restaurado", "Cerrada");
    // estados en los que la incidencia ya se resolvió: exigen fecha de restauración
    private static final List<String> ESTADOS_RESUELTOS = Arrays.asList("Servicio restaurado", "Cerrada");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action") == null ? "lista" : request.getParameter("action");
        IncidenciasDao dao = new IncidenciasDao();

        switch (action) {
            case "formCrear":
                Incidencias nueva = new Incidencias();
                nueva.setSeveridad("Media");
                nueva.setEstado("Detectada");
                nueva.setFechaDeteccion(LocalDateTime.now().withSecond(0).withNano(0));
                mostrarFormulario(request, response, nueva, "crear");
                break;
            case "editar":
                int id = leerId(request.getParameter("id"));
                Incidencias existente = id > 0 ? dao.obtenerIncidencias(id) : null;
                if (existente == null) {
                    response.sendRedirect(request.getContextPath() + "/IncidenciasServlet");
                } else {
                    mostrarFormulario(request, response, existente, "editar");
                }
                break;
            case "lista":
            default:
                request.setAttribute("listaIncidencias", dao.obtenerListaIncidencias());
                request.getRequestDispatcher("listaIncidencias.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action") == null ? "lista" : request.getParameter("action");
        IncidenciasDao dao = new IncidenciasDao();

        switch (action) {
            case "guardar":
                guardar(request, response, dao);
                break;
            case "borrar":
                int id = leerId(request.getParameter("id"));
                boolean borrado = id > 0 && dao.borrarIncidencias(id);
                response.sendRedirect(request.getContextPath() + "/IncidenciasServlet?" + (borrado ? "ok=borrado" : "err=borrar"));
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/IncidenciasServlet");
        }
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response, IncidenciasDao dao)
            throws ServletException, IOException {
        ArrayList<String> errores = new ArrayList<>();
        Incidencias inc = leerFormulario(request, errores);

        if (inc.getEtiqueta() != null && dao.existeEtiqueta(inc.getEtiqueta(), inc.getIdIncidencia())) {
            errores.add("Ya existe una incidencia con la etiqueta \"" + inc.getEtiqueta() + "\".");
        }

        boolean esNueva = inc.getIdIncidencia() == 0;

        if (errores.isEmpty()) {
            boolean ok = esNueva ? dao.crearIncidencias(inc) : dao.actualizarIncidencias(inc);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/IncidenciasServlet?ok=" + (esNueva ? "creado" : "actualizado"));
                return;
            }
            errores.add("No se pudo guardar en la base de datos. Revisa la consola del servidor.");
        }

        request.setAttribute("errores", errores);
        mostrarFormulario(request, response, inc, esNueva ? "crear" : "editar");
    }

    private Incidencias leerFormulario(HttpServletRequest request, ArrayList<String> errores) {
        Incidencias inc = new Incidencias();
        inc.setIdIncidencia(Math.max(leerId(request.getParameter("id")), 0));

        String etiqueta = texto(request.getParameter("etiqueta"));
        inc.setEtiqueta(etiqueta);
        if (etiqueta == null) {
            errores.add("La etiqueta es obligatoria (ej: INC-01).");
        } else if (etiqueta.length() > 45) {
            errores.add("La etiqueta no puede pasar de 45 caracteres.");
        }

        String titulo = texto(request.getParameter("titulo"));
        inc.setTitulo(titulo);
        if (titulo == null) {
            errores.add("El título es obligatorio.");
        } else if (titulo.length() > 150) {
            errores.add("El título no puede pasar de 150 caracteres.");
        }

        String descripcion = texto(request.getParameter("descripcion"));
        inc.setDescripcion(descripcion);
        if (descripcion == null) {
            errores.add("La descripción es obligatoria.");
        }

        String severidad = texto(request.getParameter("severidad"));
        inc.setSeveridad(severidad);
        if (severidad == null || !SEVERIDADES.contains(severidad)) {
            errores.add("Elige una severidad válida.");
        }

        String estado = texto(request.getParameter("estado"));
        inc.setEstado(estado);
        if (estado == null || !ESTADOS.contains(estado)) {
            errores.add("Elige un estado válido.");
        }

        inc.setSolucion(texto(request.getParameter("solucion")));
        inc.setObservaciones(texto(request.getParameter("observaciones")));

        LocalDateTime deteccion = parsearFecha(request.getParameter("fechaDeteccion"));
        inc.setFechaDeteccion(deteccion);
        if (deteccion == null) {
            errores.add("La fecha de detección es obligatoria y debe ser válida.");
        }

        String restStr = texto(request.getParameter("fechaRestauracion"));
        LocalDateTime restauracion = parsearFecha(restStr);
        if (restStr != null && restauracion == null) {
            errores.add("La fecha de restauración no es válida.");
        }
        inc.setFechaRestauracion(restauracion);
        if (estado != null && ESTADOS_RESUELTOS.contains(estado) && restauracion == null) {
            errores.add("Si el estado es \"Servicio restaurado\" o \"Cerrada\", indica la fecha de restauración.");
        }
        if (restauracion != null && deteccion != null && restauracion.isBefore(deteccion)) {
            errores.add("La fecha de restauración no puede ser anterior a la de detección.");
        }

        // Infraestructura afectada: "S:5" = segmento 5, "E:2" = estación 2, vacío = ninguna
        String infra = texto(request.getParameter("infra"));
        if (infra != null) {
            int id = infra.length() > 2 ? leerId(infra.substring(2)) : -1;
            if (infra.startsWith("S:") && id > 0) {
                inc.setTipoInfra("Segmento");
                inc.setIdInfra(id);
            } else if (infra.startsWith("E:") && id > 0) {
                inc.setTipoInfra("Estación");
                inc.setIdInfra(id);
            } else {
                errores.add("La infraestructura elegida no es válida.");
            }
        }
        return inc;
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   Incidencias inc, String modo) throws ServletException, IOException {
        request.setAttribute("incidencia", inc);
        request.setAttribute("modo", modo);
        request.setAttribute("listaSegmentos", new SegmentosDao().obtenerListaSegmentos());
        request.setAttribute("listaLanding_Stations", new Landing_StationsDao().obtenerListaLanding_Stations());
        request.getRequestDispatcher("formIncidencias.jsp").forward(request, response);
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

    /** Convierte el valor de un input datetime-local (2026-10-23T14:30); null si está vacío o mal escrito. */
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
