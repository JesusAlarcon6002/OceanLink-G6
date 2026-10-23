<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Landing_Stations" %>
<%@ page import="com.oceanlink.beans.Mantenimientos" %>
<%@ page import="com.oceanlink.beans.Segmentos" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Un solo formulario para CREAR y para EDITAR (lo decide el atributo "modo").
    // Lo manda MantenimientosServlet con request.setAttribute
    Mantenimientos m = (Mantenimientos) request.getAttribute("mantenimiento");
    String modo = (String) request.getAttribute("modo");
    boolean editando = "editar".equals(modo);
    ArrayList<Segmentos> listaSegmentos = (ArrayList<Segmentos>) request.getAttribute("listaSegmentos");
    ArrayList<Landing_Stations> listaStations = (ArrayList<Landing_Stations>) request.getAttribute("listaLanding_Stations");
    ArrayList<String> errores = (ArrayList<String>) request.getAttribute("errores");
    String ctx = request.getContextPath();
    String hoy = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    String infraActual = m.getInfraValor();
    double duracion = m.getDuracionEstimadaHoras();
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Registrar Mantenimiento"/>
    <jsp:param name="encabezado" value="REGISTRAR/EDITAR MANTENIMIENTOS"/>
    <jsp:param name="subtitulo" value="Gestiona los mantenimientos"/>
    <jsp:param name="rol" value="maintenance-coordinator"/>
    <jsp:param name="activo" value="historial-mant"/>
</jsp:include>

        <form class="form-panel-dark" method="post" action="<%=ctx%>/MantenimientosServlet?action=guardar">
          <input type="hidden" name="id" value="<%= editando ? m.getIdMantenimiento() : "" %>">

          <div class="form-panel-header">
            <span style="color:#fff;font-size:16px;font-weight:700;"><%= editando ? Html.esc(m.getEtiqueta()) : "NUEVO MANTENIMIENTO" %></span>
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
                <label for="etiqueta">ETIQUETA</label>
                <input type="text" id="etiqueta" name="etiqueta" maxlength="45" required
                       placeholder="MAN-01" value="<%=Html.esc(m.getEtiqueta())%>">
              </div>
              <div class="form-field">
                <label for="tipo">TIPO</label>
                <select id="tipo" name="tipo">
                  <option value="Preventivo" <%= "Preventivo".equals(m.getTipo()) ? "selected" : "" %>>Preventivo</option>
                  <option value="Correctivo" <%= "Correctivo".equals(m.getTipo()) ? "selected" : "" %>>Correctivo</option>
                </select>
              </div>
              <div class="form-field">
                <label for="infra">INFRAESTRUCTURA</label>
                <select id="infra" name="infra">
                  <option value="">(Sin infraestructura asociada)</option>
                  <optgroup label="Segmentos">
                    <% if (listaSegmentos != null) for (Segmentos sg : listaSegmentos) {
                           String valor = "S:" + sg.getIdSegmento(); %>
                    <option value="<%=valor%>" <%= valor.equals(infraActual) ? "selected" : "" %>><%=Html.esc(sg.getEtiquetaSegmento())%></option>
                    <% } %>
                  </optgroup>
                  <optgroup label="Landing Stations">
                    <% if (listaStations != null) for (Landing_Stations ls : listaStations) {
                           String valor = "E:" + ls.getId_landing_stations(); %>
                    <option value="<%=valor%>" <%= valor.equals(infraActual) ? "selected" : "" %>><%=Html.esc(ls.getEtiqueta_station())%></option>
                    <% } %>
                  </optgroup>
                </select>
              </div>
              <div class="form-field">
                <label for="fechaProgramada">FECHA PROGRAMADA</label>
                <input type="datetime-local" id="fechaProgramada" name="fechaProgramada" required
                       value="<%=m.getFechaProgramadaInput()%>">
              </div>
              <div class="form-field">
                <label for="duracion">DURACIÓN ESTIMADA (HORAS)</label>
                <input type="number" id="duracion" name="duracion" step="0.01" min="0.01" max="999.99" required
                       placeholder="4" value="<%= duracion > 0 ? String.valueOf(duracion) : "" %>">
              </div>
              <div class="form-field">
                <label for="estado">ESTADO DEL MANTENIMIENTO</label>
                <select id="estado" name="estado">
                  <option value="Programado" <%= "Programado".equals(m.getEstado()) ? "selected" : "" %>>Programado</option>
                  <option value="En curso" <%= "En curso".equals(m.getEstado()) ? "selected" : "" %>>En curso</option>
                  <option value="Finalizado" <%= "Finalizado".equals(m.getEstado()) ? "selected" : "" %>>Finalizado</option>
                </select>
              </div>
              <div class="form-field">
                <label for="fechaFinalizacion">FECHA DE FINALIZACIÓN (si ya terminó)</label>
                <input type="datetime-local" id="fechaFinalizacion" name="fechaFinalizacion"
                       value="<%=m.getFechaFinalizacionInput()%>">
              </div>
            </div>

            <div class="info-card-dark">
              <h4>DESCRIPCIÓN</h4>
              <textarea name="descripcion" rows="4" required
                        placeholder="Detalle del trabajo a realizar..."><%=Html.esc(m.getDescripcion())%></textarea>
              <h4 style="margin-top:14px;">ACTIVIDADES</h4>
              <textarea name="actividades" rows="3"
                        placeholder="Actividades realizadas o planificadas..."><%=Html.esc(m.getActividades())%></textarea>
              <h4 style="margin-top:14px;">RESULTADO</h4>
              <textarea name="resultado" rows="3"
                        placeholder="Resultado del mantenimiento (al finalizar)..."><%=Html.esc(m.getResultado())%></textarea>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-form-save">GUARDAR</button>
            <a href="<%=ctx%>/MantenimientosServlet" class="btn-form-cancel" style="text-decoration:none;display:inline-flex;align-items:center;">CANCELAR</a>
          </div>
        </form>

<jsp:include page="/WEB-INF/includes/fin.jsp"/>
