<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Clientes" %>
<%@ page import="com.oceanlink.beans.Landing_Stations" %>
<%@ page import="com.oceanlink.beans.Solicitudes" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Un solo formulario para CREAR y EDITAR (lo decide el atributo "modo"). Lo manda SolicitudesServlet.
    Solicitudes sol = (Solicitudes) request.getAttribute("solicitud");
    boolean editando = "editar".equals(request.getAttribute("modo"));
    ArrayList<Clientes> listaClientes = (ArrayList<Clientes>) request.getAttribute("listaClientes");
    ArrayList<Landing_Stations> listaStations = (ArrayList<Landing_Stations>) request.getAttribute("listaLanding_Stations");
    ArrayList<String> errores = (ArrayList<String>) request.getAttribute("errores");
    String ctx = request.getContextPath();
    String hoy = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

    // al crear, sol.getCliente() es null; al editar o tras un error ya viene con su ID
    int idCliente = sol.getCliente() == null ? 0 : sol.getCliente().getIdCliente();
    int idOrigen = sol.getEstacionOrigen() == null ? 0 : sol.getEstacionOrigen().getId_landing_stations();
    int idDestino = sol.getEstacionDestino() == null ? 0 : sol.getEstacionDestino().getId_landing_stations();
    double cap = sol.getCapacidadSolicitadaGbps();
    String[] estados = { "Registrada", "En evaluación", "Aprobada", "Rechazada", "Provisionada", "Activa" };
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Registrar Solicitud"/>
    <jsp:param name="encabezado" value="REGISTRAR/EDITAR SOLICITUD"/>
    <jsp:param name="subtitulo" value="Solicitud de capacidad de un cliente"/>
    <jsp:param name="rol" value="capacity-planner"/>
    <jsp:param name="activo" value="solicitudes"/>
</jsp:include>

        <form class="form-panel-dark" method="post" action="<%=ctx%>/SolicitudesServlet?action=guardar">
          <input type="hidden" name="id" value="<%= editando ? sol.getIdSolicitud() : "" %>">

          <div class="form-panel-header">
            <span style="color:#fff;font-size:16px;font-weight:700;"><%= editando ? Html.esc(sol.getEtiqueta()) : "NUEVA SOLICITUD" %></span>
            <span class="date-label">Fecha: <%=hoy%></span>
          </div>

          <% if (errores != null && !errores.isEmpty()) { %>
          <div class="msg msg-error">
            No se pudo guardar. Corrige lo siguiente:
            <ul>
              <% for (String e : errores) { %><li><%=Html.esc(e)%></li><% } %>
            </ul>
          </div>
          <% } %>

          <p class="form-section-label">INFORMACIÓN GENERAL</p>

          <div class="form-body">
            <div class="form-grid-2" style="grid-template-columns:1fr 1fr;">
              <div class="form-field">
                <label for="etiqueta">ETIQUETA *</label>
                <input type="text" id="etiqueta" name="etiqueta" maxlength="45" required
                       placeholder="SOL-001" value="<%=Html.esc(sol.getEtiqueta())%>">
              </div>
              <div class="form-field">
                <label for="cliente">CLIENTE *</label>
                <select id="cliente" name="cliente" required>
                  <option value="">Elige un cliente...</option>
                  <% if (listaClientes != null) for (Clientes c : listaClientes) { %>
                  <option value="<%=c.getIdCliente()%>" <%= c.getIdCliente() == idCliente ? "selected" : "" %>><%=Html.esc(c.getNombre())%><%= "Inactivo".equals(c.getEstado()) ? " (Inactivo)" : "" %></option>
                  <% } %>
                </select>
              </div>
              <div class="form-field">
                <label for="origen">ORIGEN *</label>
                <select id="origen" name="origen" required>
                  <option value="">Elige una estación...</option>
                  <% if (listaStations != null) for (Landing_Stations ls : listaStations) { %>
                  <option value="<%=ls.getId_landing_stations()%>" <%= ls.getId_landing_stations() == idOrigen ? "selected" : "" %>><%=Html.esc(ls.getEtiqueta_station())%></option>
                  <% } %>
                </select>
              </div>
              <div class="form-field">
                <label for="destino">DESTINO *</label>
                <select id="destino" name="destino" required>
                  <option value="">Elige una estación...</option>
                  <% if (listaStations != null) for (Landing_Stations ls : listaStations) { %>
                  <option value="<%=ls.getId_landing_stations()%>" <%= ls.getId_landing_stations() == idDestino ? "selected" : "" %>><%=Html.esc(ls.getEtiqueta_station())%></option>
                  <% } %>
                </select>
              </div>
              <div class="form-field">
                <label for="capacidad">CAPACIDAD SOLICITADA (GBPS) *</label>
                <input type="number" id="capacidad" name="capacidad" step="0.01" min="0.01" required
                       placeholder="20" value="<%= cap > 0 ? String.valueOf(cap) : "" %>">
              </div>
              <div class="form-field">
                <label for="fechaSolicitud">FECHA DE SOLICITUD *</label>
                <input type="datetime-local" id="fechaSolicitud" name="fechaSolicitud" required
                       value="<%=sol.getFechaSolicitudInput()%>">
              </div>
              <div class="form-field">
                <label for="estado">ESTADO DE LA SOLICITUD</label>
                <select id="estado" name="estado">
                  <% for (String e : estados) { %>
                  <option value="<%=e%>" <%= e.equals(sol.getEstado()) ? "selected" : "" %>><%=e%></option>
                  <% } %>
                </select>
              </div>
            </div>

            <div class="info-card-dark">
              <h4>OBSERVACIONES</h4>
              <textarea name="observaciones" rows="8"
                        placeholder="Notas de la solicitud o de su evaluación..."><%=Html.esc(sol.getObservaciones())%></textarea>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-form-save">GUARDAR</button>
            <a href="<%=ctx%>/SolicitudesServlet" class="btn-form-cancel" style="text-decoration:none;display:inline-flex;align-items:center;">CANCELAR</a>
          </div>
        </form>

<jsp:include page="/WEB-INF/includes/acciones-clientes.jsp"/>
<script>
  // Origen y destino no pueden ser la misma estación
  document.querySelector('form.form-panel-dark').addEventListener('submit', function (e) {
    if (document.getElementById('origen').value === document.getElementById('destino').value) {
      e.preventDefault();
      alert('El origen y el destino deben ser estaciones distintas.');
    }
  });
</script>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
