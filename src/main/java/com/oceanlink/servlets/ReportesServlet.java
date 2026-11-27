package com.oceanlink.servlets;

import com.oceanlink.beans.Clientes;
import com.oceanlink.beans.Reporte;
import com.oceanlink.beans.UsuarioSesion;
import com.oceanlink.util.Rol;
import com.oceanlink.daos.ClientesDao;
import com.oceanlink.daos.ReportesDao;
import com.oceanlink.util.ExportadorPdf;
import com.oceanlink.util.ExportadorXlsx;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Reportes con filtros. Parámetros: tipo = utilizacion | incidencias | servicios | mantenimientos,
 * los filtros de cada tipo, y formato=csv | xlsx | pdf para descargar. Pantalla: reportes.jsp
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
        // El Network Operator solo puede ver y descargar reportes de incidencias
        UsuarioSesion u = (UsuarioSesion) request.getSession().getAttribute("usuario");
        boolean soloIncidencias = u != null && u.getRol() == Rol.NETWORK_OPERATOR;
        if (soloIncidencias) {
            tipo = "incidencias";
        }
        request.setAttribute("soloIncidencias", soloIncidencias);

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

        String formato = request.getParameter("formato");
        if ("csv".equals(formato) || "xlsx".equals(formato) || "pdf".equals(formato)) {
            String filtros = describirFiltros(request, tipo);
            String fecha = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            byte[] datos;
            switch (formato) {
                case "xlsx":
                    datos = ExportadorXlsx.generar(reporte, filtros);
                    response.setContentType(ExportadorXlsx.CONTENT_TYPE);
                    break;
                case "pdf":
                    datos = ExportadorPdf.generar(reporte, filtros);
                    response.setContentType(ExportadorPdf.CONTENT_TYPE);
                    break;
                default:
                    datos = ("\uFEFF" + reporte.aCsv()).getBytes(StandardCharsets.UTF_8); // BOM para que Excel lea las tildes
                    response.setContentType("text/csv; charset=UTF-8");
            }
            response.setHeader("Content-Disposition", "attachment; filename=\"reporte-" + tipo + "-" + fecha + "." + formato + "\"");
            response.setContentLength(datos.length);
            response.getOutputStream().write(datos);
            return;
        }

        request.setAttribute("tipo", tipo);
        request.setAttribute("reporte", reporte);
        request.setAttribute("listaClientes", new ClientesDao().getListaClientes());
        request.getRequestDispatcher("reportes.jsp").forward(request, response);
    }

    /** Texto con los filtros aplicados (para el encabezado del PDF y del Excel). Vacío si no hay filtros. */
    private String describirFiltros(HttpServletRequest request, String tipo) {
        String[][] posibles = {{"estado", "Estado"}, {"nivel", "Nivel de utilización"}, {"severidad", "Severidad"},
                {"tipoMant", "Tipo"}, {"desde", "Desde"}, {"hasta", "Hasta"}};
        StringBuilder sb = new StringBuilder();
        for (String[] p : posibles) {
            String v = request.getParameter(p[0]);
            if (v != null && !v.trim().isEmpty()) {
                if (sb.length() > 0) sb.append("  |  ");
                sb.append(p[1]).append(": ").append(v.trim());
            }
        }
        int idCliente = leerId(request.getParameter("cliente"));
        if ("servicios".equals(tipo) && idCliente > 0) {
            for (Clientes c : new ClientesDao().getListaClientes()) {
                if (c.getIdCliente() == idCliente) {
                    if (sb.length() > 0) sb.append("  |  ");
                    sb.append("Cliente: ").append(c.getNombre());
                }
            }
        }
        return sb.toString();
    }

    private int leerId(String valor) {
        try {
            return Integer.parseInt(valor == null ? "" : valor.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
