<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="com.oceanlink.beans.Reporte" %>
<%@ page import="com.oceanlink.beans.Clientes" %>
<%@ page import="com.oceanlink.util.Html" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%!
    private static String sel(String actual, String valor) {
        return valor.equals(actual) ? " selected" : "";
    }
    private static String p(HttpServletRequest r, String nombre) {
        String v = r.getParameter(nombre);
        return v == null ? "" : v;
    }
    private static String opciones(String actual, String... valores) {
        StringBuilder sb = new StringBuilder("<option value=\"\">Todos</option>");
        for (String v : valores) {
            sb.append("<option value=\"").append(Html.esc(v)).append("\"").append(sel(actual, v)).append(">").append(Html.esc(v)).append("</option>");
        }
        return sb.toString();
    }
%>
<%
    // Lo manda ReportesServlet
    Reporte reporte = (Reporte) request.getAttribute("reporte");
    String tipo = (String) request.getAttribute("tipo");
    ArrayList<Clientes> listaClientes = (ArrayList<Clientes>) request.getAttribute("listaClientes");
    if (listaClientes == null) listaClientes = new ArrayList<>();
    String ctx = request.getContextPath();

    // enlace de descarga: los mismos filtros + formato=csv
    String qs = request.getQueryString() == null ? "tipo=" + tipo : request.getQueryString().replaceAll("&?formato=[^&]*", "");
    String urlCsv = ctx + "/ReportesServlet?" + qs + "&formato=csv";
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Reportes"/>
    <jsp:param name="encabezado" value="REPORTES"/>
    <jsp:param name="subtitulo" value="Consulta con filtros y descarga en CSV"/>
    <jsp:param name="rol" value="supervisor"/>
    <jsp:param name="activo" value="reportes"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <div class="infra-header"><h2><%=Html.esc(reporte.getTitulo()).toUpperCase()%></h2></div>

          <div class="rep-tabs">
            <a class="rep-tab <%= "utilizacion".equals(tipo) ? "activa" : "" %>" href="<%=ctx%>/ReportesServlet?tipo=utilizacion">Utilización</a>
            <a class="rep-tab <%= "incidencias".equals(tipo) ? "activa" : "" %>" href="<%=ctx%>/ReportesServlet?tipo=incidencias">Incidencias</a>
            <a class="rep-tab <%= "servicios".equals(tipo) ? "activa" : "" %>" href="<%=ctx%>/ReportesServlet?tipo=servicios">Servicios por cliente</a>
            <a class="rep-tab <%= "mantenimientos".equals(tipo) ? "activa" : "" %>" href="<%=ctx%>/ReportesServlet?tipo=mantenimientos">Mantenimientos</a>
          </div>

          <form class="rep-filtros" method="get" action="<%=ctx%>/ReportesServlet">
            <input type="hidden" name="tipo" value="<%=Html.esc(tipo)%>">
            <% if ("utilizacion".equals(tipo)) { %>
            <label>Estado del segmento
              <select name="estado"><%=opciones(p(request, "estado"), "Operativo", "Degradado", "En mantenimiento", "Fuera de servicio")%></select></label>
            <label>Nivel de utilización
              <select name="nivel"><%=opciones(p(request, "nivel"), "Normal", "Alta", "Crítica")%></select></label>
            <% } else if ("incidencias".equals(tipo)) { %>
            <label>Estado
              <select name="estado"><%=opciones(p(request, "estado"), "Detectada", "En analisis", "Reparacion programada", "En reparacion", "Servicio restaurado", "Cerrada")%></select></label>
            <label>Severidad
              <select name="severidad"><%=opciones(p(request, "severidad"), "Baja", "Media", "Alta", "Critica")%></select></label>
            <label>Detectada desde <input type="date" name="desde" value="<%=Html.esc(p(request, "desde"))%>"></label>
            <label>Hasta <input type="date" name="hasta" value="<%=Html.esc(p(request, "hasta"))%>"></label>
            <% } else if ("servicios".equals(tipo)) { %>
            <label>Cliente
              <select name="cliente">
                <option value="">Todos</option>
                <% for (Clientes c : listaClientes) { %>
                <option value="<%=c.getIdCliente()%>"<%= String.valueOf(c.getIdCliente()).equals(p(request, "cliente")) ? " selected" : "" %>><%=Html.esc(c.getNombre())%></option>
                <% } %>
              </select></label>
            <label>Estado del servicio
              <select name="estado"><%=opciones(p(request, "estado"), "Pendiente", "Activo", "Suspendido", "Finalizado")%></select></label>
            <% } else { %>
            <label>Estado
              <select name="estado"><%=opciones(p(request, "estado"), "Programado", "En curso", "Finalizado")%></select></label>
            <label>Tipo
              <select name="tipoMant"><%=opciones(p(request, "tipoMant"), "Preventivo", "Correctivo")%></select></label>
            <label>Programado desde <input type="date" name="desde" value="<%=Html.esc(p(request, "desde"))%>"></label>
            <label>Hasta <input type="date" name="hasta" value="<%=Html.esc(p(request, "hasta"))%>"></label>
            <% } %>
            <button type="submit" class="btn-mini">FILTRAR</button>
            <a class="btn-mini" style="text-decoration:none;" href="<%=ctx%>/ReportesServlet?tipo=<%=Html.esc(tipo)%>">LIMPIAR</a>
            <a class="btn-mini" style="text-decoration:none;" href="<%=Html.esc(urlCsv)%>">DESCARGAR CSV</a>
          </form>

          <div class="dark-table-wrap">
            <table>
              <thead><tr>
                <% for (String col : reporte.getColumnas()) { %><th><%=Html.esc(col).toUpperCase()%></th><% } %>
              </tr></thead>
              <tbody>
                <% if (reporte.getFilas().isEmpty()) { %>
                <tr class="fila-vacia"><td colspan="<%=reporte.getColumnas().size()%>">No hay resultados con esos filtros.</td></tr>
                <% }
                   for (List<String> fila : reporte.getFilas()) { %>
                <tr>
                  <% for (String celda : fila) { %><td><%=Html.esc(celda)%></td><% } %>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
          <p class="table-footer-note"><%=reporte.getFilas().size()%> resultado(s)</p>
        </div>

<jsp:include page="/WEB-INF/includes/fin.jsp"/>
