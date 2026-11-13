<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="com.oceanlink.daos.DashboardDao" %>
<%@ page import="com.oceanlink.daos.DashboardDao.Datos" %>
<%@ page import="com.oceanlink.daos.DashboardDao.SegmentoKpi" %>
<%@ page import="com.oceanlink.daos.DashboardDao.ProximoMantenimiento" %>
<%@ page import="com.oceanlink.util.Html" %>
<%@ page import="com.oceanlink.util.Json" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%
    // Lo manda DashboardServlet
    Datos d = (Datos) request.getAttribute("datos");
    if (d == null) d = new Datos();
    String ctx = request.getContextPath();
    DecimalFormat n1 = new DecimalFormat("#,##0.#");
    DecimalFormat n0 = new DecimalFormat("#,##0");

    // datos de los gráficos
    List<String> segEtiquetas = new ArrayList<>();
    List<Double> segPct = new ArrayList<>();
    List<String> segColores = new ArrayList<>();
    for (SegmentoKpi s : d.segmentos) {
        segEtiquetas.add(s.etiqueta);
        segPct.add(s.porcentaje());
        String niv = s.nivel();
        segColores.add("Crítica".equals(niv) ? "#f87171" : "Alta".equals(niv) ? "#fbbf24" : "#34d399");
    }
    List<Integer> estadoValores = new ArrayList<>(d.segmentosPorEstado.values());
    List<Integer> sevValores = new ArrayList<>(d.incidenciasActivasPorSeveridad.values());
    List<Double> cliValores = new ArrayList<>();
    List<String> cliNombres = new ArrayList<>();
    int tope = 0;
    for (java.util.Map.Entry<String, Double> e : d.contratadoPorCliente.entrySet()) {
        if (tope++ >= 8) break;
        cliNombres.add(e.getKey());
        cliValores.add(e.getValue());
    }
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Dashboard"/>
    <jsp:param name="encabezado" value="DASHBOARD DE LA RED"/>
    <jsp:param name="subtitulo" value="Indicadores generales de operación"/>
    <jsp:param name="rol" value="supervisor"/>
    <jsp:param name="activo" value="dashboard"/>
</jsp:include>

        <div class="dash-col">
        <div class="kpi-grid">
          <div class="kpi-card"><p class="kpi-num"><%=n1.format(d.porcentajeRed())%> %</p><p class="kpi-lbl">Utilización de la red</p>
            <p class="kpi-sub"><%=n1.format(d.capacidadUtilizada + d.capacidadReservada)%> de <%=n1.format(d.capacidadTotal)%> Gbps</p></div>
          <div class="kpi-card"><p class="kpi-num"><%=n1.format(d.capacidadDisponible())%> <small>Gbps</small></p><p class="kpi-lbl">Capacidad disponible</p>
            <p class="kpi-sub">Reservada: <%=n1.format(d.capacidadReservada)%> Gbps</p></div>
          <div class="kpi-card"><p class="kpi-num"><%=d.segmentosOperativos%> / <%=d.totalSegmentos%></p><p class="kpi-lbl">Segmentos operativos</p>
            <p class="kpi-sub"><%=d.segmentosNoOperativos%> no operativo(s)</p></div>
          <div class="kpi-card <%= d.incidenciasActivas > 0 ? "kpi-alerta" : "" %>"><p class="kpi-num"><%=d.incidenciasActivas%></p><p class="kpi-lbl">Incidencias activas</p>
            <p class="kpi-sub">Críticas: <%=d.incidenciasActivasPorSeveridad.get("Critica")%> · Altas: <%=d.incidenciasActivasPorSeveridad.get("Alta")%></p></div>
          <div class="kpi-card <%= d.serviciosAfectados > 0 ? "kpi-alerta" : "" %>"><p class="kpi-num"><%=d.serviciosAfectados%></p><p class="kpi-lbl">Servicios afectados</p>
            <p class="kpi-sub"><%=d.clientesAfectados%> cliente(s) · <%=d.serviciosActivos%> servicios activos</p></div>
          <div class="kpi-card"><p class="kpi-num"><%=d.mantenimientosProgramados%></p><p class="kpi-lbl">Mantenimientos programados</p>
            <p class="kpi-sub"><%=d.segmentosConAltaUtilizacion()%> segmento(s) con utilización alta o crítica</p></div>
        </div>

        <div class="chart-grid">
          <div class="form-panel-dark chart-panel">
            <div class="infra-header"><h2>UTILIZACIÓN POR SEGMENTO (%)</h2></div>
            <div class="chart-box"><canvas id="grafUtilizacion"></canvas></div>
            <p class="chart-nota">Verde &lt; 80 % Normal · Ámbar 80–95 % Alta · Rojo &gt; 95 % Crítica</p>
          </div>
          <div class="form-panel-dark chart-panel">
            <div class="infra-header"><h2>ESTADO DE LOS SEGMENTOS</h2></div>
            <div class="chart-box"><canvas id="grafEstados"></canvas></div>
          </div>
          <div class="form-panel-dark chart-panel">
            <div class="infra-header"><h2>CAPACIDAD CONTRATADA POR CLIENTE (GBPS)</h2></div>
            <div class="chart-box"><canvas id="grafClientes"></canvas></div>
          </div>
          <div class="form-panel-dark chart-panel">
            <div class="infra-header"><h2>INCIDENCIAS ACTIVAS POR SEVERIDAD</h2></div>
            <div class="chart-box"><canvas id="grafIncidencias"></canvas></div>
          </div>
        </div>

        <div class="chart-grid">
          <div class="form-panel-dark" style="min-height:auto;">
            <div class="infra-header"><h2>SEGMENTOS CON MAYOR UTILIZACIÓN</h2></div>
            <div class="dark-table-wrap">
              <table>
                <thead><tr><th>SEGMENTO</th><th>ESTADO</th><th>DISPONIBLE (GBPS)</th><th>UTILIZACIÓN</th><th>NIVEL</th></tr></thead>
                <tbody>
                  <% if (d.segmentos.isEmpty()) { %>
                  <tr class="fila-vacia"><td colspan="5">No hay segmentos registrados.</td></tr>
                  <% }
                     int mostrados = 0;
                     for (SegmentoKpi s : d.segmentos) {
                         if (mostrados++ >= 6) break;
                         String niv = s.nivel();
                         String clase = "Crítica".equals(niv) ? "badge-danger" : "Alta".equals(niv) ? "badge-warning" : "badge-success";
                  %>
                  <tr>
                    <td><%=Html.esc(s.etiqueta)%></td>
                    <td><%=Html.esc(s.estado)%></td>
                    <td><%=n1.format(s.disponible())%></td>
                    <td><%=n1.format(s.porcentaje())%> %</td>
                    <td><span class="badge <%=clase%>"><%=niv%></span></td>
                  </tr>
                  <% } %>
                </tbody>
              </table>
            </div>
          </div>
          <div class="form-panel-dark" style="min-height:auto;">
            <div class="infra-header"><h2>PRÓXIMOS MANTENIMIENTOS</h2></div>
            <div class="dark-table-wrap">
              <table>
                <thead><tr><th>ETIQUETA</th><th>TIPO</th><th>FECHA PROGRAMADA</th></tr></thead>
                <tbody>
                  <% if (d.proximosMantenimientos.isEmpty()) { %>
                  <tr class="fila-vacia"><td colspan="3">No hay mantenimientos programados.</td></tr>
                  <% }
                     for (ProximoMantenimiento m : d.proximosMantenimientos) { %>
                  <tr><td><%=Html.esc(m.etiqueta)%></td><td><%=Html.esc(m.tipo)%></td><td><%=Html.esc(m.fecha.replace('T', ' '))%></td></tr>
                  <% } %>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        </div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.min.js"></script>
<script>
(function () {
  if (typeof Chart === "undefined") { return; }   // sin internet no hay gráficos, pero las tablas siguen
  Chart.defaults.color = "#c9d4e3";
  Chart.defaults.borderColor = "rgba(148,163,184,0.18)";
  var base = { responsive: true, maintainAspectRatio: false };

  new Chart(document.getElementById("grafUtilizacion"), {
    type: "bar",
    data: { labels: <%=Json.listaTextos(segEtiquetas)%>,
            datasets: [{ label: "Utilización (%)", data: <%=Json.listaNumeros(segPct)%>,
                         backgroundColor: <%=Json.listaTextos(segColores)%> }] },
    options: Object.assign({}, base, { plugins: { legend: { display: false } },
             scales: { y: { beginAtZero: true, suggestedMax: 100, title: { display: true, text: "%" } } } })
  });
  new Chart(document.getElementById("grafEstados"), {
    type: "doughnut",
    data: { labels: <%=Json.listaTextos(d.segmentosPorEstado.keySet())%>,
            datasets: [{ data: <%=Json.listaNumeros(estadoValores)%>,
                         backgroundColor: ["#34d399", "#fbbf24", "#60a5fa", "#f87171"], borderWidth: 0 }] },
    options: Object.assign({}, base, { plugins: { legend: { position: "bottom" } } })
  });
  new Chart(document.getElementById("grafClientes"), {
    type: "bar",
    data: { labels: <%=Json.listaTextos(cliNombres)%>,
            datasets: [{ label: "Gbps contratados", data: <%=Json.listaNumeros(cliValores)%>, backgroundColor: "#3b82f6" }] },
    options: Object.assign({}, base, { indexAxis: "y", plugins: { legend: { display: false } },
             scales: { x: { beginAtZero: true } } })
  });
  new Chart(document.getElementById("grafIncidencias"), {
    type: "doughnut",
    data: { labels: <%=Json.listaTextos(d.incidenciasActivasPorSeveridad.keySet())%>,
            datasets: [{ data: <%=Json.listaNumeros(sevValores)%>,
                         backgroundColor: ["#f87171", "#fb923c", "#fbbf24", "#60a5fa"], borderWidth: 0 }] },
    options: Object.assign({}, base, { plugins: { legend: { position: "bottom" } } })
  });
})();
</script>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
