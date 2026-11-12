package com.oceanlink.servlets;

import com.oceanlink.beans.Landing_Stations;
import com.oceanlink.beans.Rutas;
import com.oceanlink.beans.Solicitudes;
import com.oceanlink.daos.Landing_StationsDao;
import com.oceanlink.daos.RutasDao;
import com.oceanlink.daos.SolicitudesDao;
import com.oceanlink.util.GrafoRutas;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebServlet(name = "RutasServlet", value = "/RutasServlet")
public class RutasServlet extends HttpServlet {

    private static final List<String> ESTADOS = Arrays.asList("Activa", "Inactiva");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action") == null ? "lista" : request.getParameter("action");
        RutasDao dao = new RutasDao();

        switch (action) {
            case "formCrear": {
                Rutas nueva = new Rutas();
                // Si venimos desde una solicitud, precargamos origen, destino y capacidad
                int idSol = leerId(request.getParameter("solicitud"));
                double cap = 0;
                if (idSol > 0) {
                    Solicitudes sol = new SolicitudesDao().obtenerSolicitudes(idSol);
                    if (sol != null) {
                        nueva.setEstacionOrigen(estacion(sol.getEstacionOrigen().getId_landing_stations()));
                        nueva.setEstacionDestino(estacion(sol.getEstacionDestino().getId_landing_stations()));
                        cap = sol.getCapacidadSolicitadaGbps();
                    }
                }
                mostrarFormulario(request, response, nueva, "crear", dao.cargarGrafo(), cap, null);
                break;
            }
            case "editar": {
                int id = leerId(request.getParameter("id"));
                Rutas existente = id > 0 ? dao.obtenerRutas(id) : null;
                if (existente == null) {
                    response.sendRedirect(request.getContextPath() + "/RutasServlet");
                } else {
                    mostrarFormulario(request, response, existente, "editar", dao.cargarGrafo(), 0, null);
                }
                break;
            }
            case "lista":
            default:
                request.setAttribute("listaRutas", dao.obtenerListaRutas());
                request.getRequestDispatcher("listaRutas.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action") == null ? "lista" : request.getParameter("action");
        RutasDao dao = new RutasDao();

        switch (action) {
            case "guardar":
                guardar(request, response, dao);
                break;
            case "sugerir":
            case "usar":
                sugerirOUsar(request, response, dao, "usar".equals(action));
                break;
            case "borrar": {
                int id = leerId(request.getParameter("id"));
                Rutas r = id > 0 ? dao.obtenerRutas(id) : null;
                String resultado;
                if (r == null) {
                    resultado = "err=borrar";
                } else if (r.isTieneServicios()) {
                    resultado = "err=servicios";
                } else {
                    resultado = dao.borrarRutas(id) ? "ok=borrado" : "err=borrar";
                }
                response.sendRedirect(request.getContextPath() + "/RutasServlet?" + resultado);
                break;
            }
            default:
                response.sendRedirect(request.getContextPath() + "/RutasServlet");
        }
    }

    // ---------------------------------------------------------------- guardar

    private void guardar(HttpServletRequest request, HttpServletResponse response, RutasDao dao)
            throws ServletException, IOException {
        ArrayList<String> errores = new ArrayList<>();
        GrafoRutas grafo = dao.cargarGrafo();
        int id = Math.max(leerId(request.getParameter("id")), 0);
        boolean esNueva = id == 0;

        Rutas original = esNueva ? null : dao.obtenerRutas(id);
        if (!esNueva && original == null) {
            response.sendRedirect(request.getContextPath() + "/RutasServlet");
            return;
        }
        // Con servicios, el camino y los extremos ya no se pueden tocar: solo etiqueta y estado
        boolean soloBasico = original != null && original.isTieneServicios();

        Rutas r = leerFormulario(request, grafo, errores, soloBasico ? original : null);
        r.setIdRuta(id);
        r.setTieneServicios(soloBasico);

        if (r.getEtiqueta() != null && dao.existeEtiqueta(r.getEtiqueta(), id)) {
            errores.add("Ya existe una ruta con la etiqueta \"" + r.getEtiqueta() + "\".");
        }

        if (errores.isEmpty()) {
            boolean ok = esNueva ? dao.crearRutas(r) : dao.actualizarRutas(r, !soloBasico);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/RutasServlet?ok=" + (esNueva ? "creado" : "actualizado"));
                return;
            }
            errores.add("No se pudo guardar en la base de datos. Revisa la consola del servidor.");
        }

        request.setAttribute("errores", errores);
        mostrarFormulario(request, response, r, esNueva ? "crear" : "editar", grafo, leerCapacidad(request), null);
    }

    private Rutas leerFormulario(HttpServletRequest request, GrafoRutas grafo, ArrayList<String> errores, Rutas fija) {
        Rutas r = new Rutas();

        String etiqueta = texto(request.getParameter("etiqueta"));
        r.setEtiqueta(etiqueta);
        if (etiqueta == null) {
            errores.add("La etiqueta es obligatoria (ej: RUTA-001).");
        } else if (etiqueta.length() > 100) {
            errores.add("La etiqueta no puede pasar de 100 caracteres.");
        }

        String estado = texto(request.getParameter("estado"));
        r.setEstado(estado);
        if (estado == null || !ESTADOS.contains(estado)) {
            errores.add("Elige un estado válido (Activa o Inactiva).");
        }

        if (fija != null) {
            // la ruta ya tiene servicios: se conserva el camino original
            r.setEstacionOrigen(fija.getEstacionOrigen());
            r.setEstacionDestino(fija.getEstacionDestino());
            r.setSegmentos(fija.getSegmentos());
            r.setEvaluacion(fija.getEvaluacion());
            return r;
        }

        int idOrigen = leerId(request.getParameter("origen"));
        int idDestino = leerId(request.getParameter("destino"));
        r.setEstacionOrigen(estacion(idOrigen));
        r.setEstacionDestino(estacion(idDestino));

        List<Integer> ids = leerSegmentos(request);
        for (int idSeg : ids) {
            GrafoRutas.Tramo t = grafo.tramo(idSeg);
            if (t != null) r.getSegmentos().add(t);
        }
        r.setEvaluacion(grafo.evaluar(r.getSegmentos()));

        if (idOrigen <= 0 || idDestino <= 0) {
            errores.add("Elige la estación de origen y la de destino.");
        } else if (idOrigen == idDestino) {
            errores.add("El origen y el destino deben ser estaciones distintas.");
        } else if (ids.isEmpty()) {
            errores.add("Agrega al menos un segmento a la ruta.");
        } else {
            errores.addAll(grafo.validarContinuidad(idOrigen, idDestino, ids));
        }
        return r;
    }

    // ---------------------------------------------------------------- sugerir / usar

    /** Recalcula las rutas sugeridas (sugerir) o carga una de ellas en el formulario (usar). Nada se guarda. */
    private void sugerirOUsar(HttpServletRequest request, HttpServletResponse response, RutasDao dao, boolean usar)
            throws ServletException, IOException {
        GrafoRutas grafo = dao.cargarGrafo();
        int id = Math.max(leerId(request.getParameter("id")), 0);
        Rutas r = new Rutas();
        r.setIdRuta(id);
        r.setEtiqueta(texto(request.getParameter("etiqueta")));
        String estado = texto(request.getParameter("estado"));
        r.setEstado(ESTADOS.contains(estado) ? estado : "Activa");
        int idOrigen = leerId(request.getParameter("origen"));
        int idDestino = leerId(request.getParameter("destino"));
        r.setEstacionOrigen(estacion(idOrigen));
        r.setEstacionDestino(estacion(idDestino));

        List<Integer> ids = usar ? parsearIds(request.getParameter("usar")) : leerSegmentos(request);
        for (int idSeg : ids) {
            GrafoRutas.Tramo t = grafo.tramo(idSeg);
            if (t != null) r.getSegmentos().add(t);
        }
        r.setEvaluacion(grafo.evaluar(r.getSegmentos()));

        double cap = leerCapacidad(request);
        List<GrafoRutas.Candidato> candidatos = null;
        ArrayList<String> errores = new ArrayList<>();
        if (idOrigen <= 0 || idDestino <= 0 || idOrigen == idDestino) {
            errores.add("Para sugerir rutas elige un origen y un destino distintos.");
        } else {
            candidatos = grafo.sugerir(idOrigen, idDestino, cap);
        }
        if (!errores.isEmpty()) request.setAttribute("errores", errores);
        mostrarFormulario(request, response, r, id > 0 ? "editar" : "crear", grafo, cap, candidatos);
    }

    // ---------------------------------------------------------------- vista

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response, Rutas ruta, String modo,
                                   GrafoRutas grafo, double capacidad, List<GrafoRutas.Candidato> candidatos)
            throws ServletException, IOException {
        request.setAttribute("ruta", ruta);
        request.setAttribute("modo", modo);
        request.setAttribute("grafo", grafo);
        request.setAttribute("capacidad", capacidad);
        request.setAttribute("candidatos", candidatos);
        request.setAttribute("listaLanding_Stations", new Landing_StationsDao().obtenerListaLanding_Stations());
        request.getRequestDispatcher("formRutas.jsp").forward(request, response);
    }

    // ---------------------------------------------------------------- utilidades

    private Landing_Stations estacion(int id) {
        Landing_Stations ls = new Landing_Stations();
        ls.setId_landing_stations(id);
        return ls;
    }

    private List<Integer> leerSegmentos(HttpServletRequest request) {
        List<Integer> ids = new ArrayList<>();
        String[] valores = request.getParameterValues("segmento");
        if (valores != null) {
            for (String v : valores) {
                int n = leerId(v);
                if (n > 0) ids.add(n);
            }
        }
        return ids;
    }

    private List<Integer> parsearIds(String csv) {
        List<Integer> ids = new ArrayList<>();
        if (csv != null) {
            for (String p : csv.split(",")) {
                int n = leerId(p);
                if (n > 0) ids.add(n);
            }
        }
        return ids;
    }

    private double leerCapacidad(HttpServletRequest request) {
        String s = texto(request.getParameter("capacidad"));
        if (s == null) return 0;
        try {
            double v = Double.parseDouble(s.replace(',', '.'));
            return v > 0 ? v : 0;
        } catch (NumberFormatException e) {
            return 0;
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
