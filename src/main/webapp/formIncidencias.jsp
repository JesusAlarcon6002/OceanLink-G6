<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Incidencias" %>
<%@ page import="com.oceanlink.beans.Landing_Stations" %>
<%@ page import="com.oceanlink.beans.Segmentos" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Un solo formulario para CREAR y EDITAR (lo decide el atributo "modo"). Lo manda IncidenciasServlet.
    Incidencias inc = (Incidencias) request.getAttribute("incidencia");
    boolean editando = "editar".equals(request.getAttribute("modo"));
    ArrayList<Segmentos> listaSegmentos = (ArrayList<Segmentos>) request.getAttribute("listaSegmentos");
    ArrayList<Landing_Stations> listaStations = (ArrayList<Landing_Stations>) request.getAttribute("listaLanding_Stations");
    ArrayList<String> errores = (ArrayList<String>) request.getAttribute("errores");
    String ctx = request.getContextPath();
    String hoy = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    String infraActual = inc.getInfraValor();

    // {valor en la BD, texto que ve el usuario}
    String[][] severidades = { {"Baja","Baja"}, {"Media","Media"}, {"Alta","Alta"}, {"Critica","Crítica"} };
    String[][] estados = { {"Detectada","Detectada"}, {"En analisis","En análisis"}, {"Reparacion programada","Reparación programada"},
                           {"En reparacion","En reparación"}, {"Servicio restaurado","Servicio restaurado"}, {"Cerrada","Cerrada"} };
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Registrar Incidencia"/>
    <jsp:param name="encabezado" value="REGISTRAR/EDITAR INCIDENCIA"/>
    <jsp:param name="subtitulo" value="Gestiona las incidencias de la red"/>
    <jsp:param name="rol" value="network-operator"/>
    <jsp:param name="activo" value="incidencias"/>
</jsp:include>

        <form class="form-panel-dark" method="post" action="<%=ctx%>/IncidenciasServlet?action=guardar">
          <input type="hidden" name="id" value="<%= editando ? inc.getIdIncidencia() : "" %>">

          <div class="form-panel-header">
            <span style="color:#fff;font-size:16px;font-weight:700;"><%= editando ? Html.esc(inc.getEtiqueta()) : "NUEVA INCIDENCIA" %></span>
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
                       placeholder="INC-01" value="<%=Html.esc(inc.getEtiqueta())%>">
              </div>
              <div class="form-field">
                <label for="titulo">TÍTULO *</label>
                <input type="text" id="titulo" name="titulo" maxlength="150" required
                       placeholder="Caída de señal en el tramo..." value="<%=Html.esc(inc.getTitulo())%>">
              </div>
              <div class="form-field">
                <label for="infra">INFRAESTRUCTURA AFECTADA</label>
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
                <label for="severidad">SEVERIDAD</label>
                <select id="severidad" name="severidad">
                  <% for (String[] s : severidades) { %>
                  <option value="<%=s[0]%>" <%= s[0].equals(inc.getSeveridad()) ? "selected" : "" %>><%=s[1]%></option>
                  <% } %>
                </select>
              </div>
              <div class="form-field">
                <label for="estado">ESTADO DE LA INCIDENCIA</label>
                <select id="estado" name="estado">
                  <% for (String[] s : estados) { %>
                  <option value="<%=s[0]%>" <%= s[0].equals(inc.getEstado()) ? "selected" : "" %>><%=s[1]%></option>
                  <% } %>
                </select>
              </div>
              <div class="form-field">
                <label for="fechaDeteccion">FECHA DE DETECCIÓN *</label>
                <input type="datetime-local" id="fechaDeteccion" name="fechaDeteccion" required
                       value="<%=inc.getFechaDeteccionInput()%>">
              </div>
              <div class="form-field">
                <label for="fechaRestauracion">FECHA DE RESTAURACIÓN (si ya se resolvió)</label>
                <input type="datetime-local" id="fechaRestauracion" name="fechaRestauracion"
                       value="<%=inc.getFechaRestauracionInput()%>">
              </div>
            </div>

            <div class="info-card-dark">
              <h4>DESCRIPCIÓN *</h4>
              <textarea name="descripcion" rows="4" required
                        placeholder="Describe la incidencia detectada..."><%=Html.esc(inc.getDescripcion())%></textarea>
              <h4 style="margin-top:14px;">SOLUCIÓN</h4>
              <textarea name="solucion" rows="3"
                        placeholder="Qué se hizo para resolverla..."><%=Html.esc(inc.getSolucion())%></textarea>
              <h4 style="margin-top:14px;">OBSERVACIONES</h4>
              <textarea name="observaciones" rows="3"
                        placeholder="Notas del operador..."><%=Html.esc(inc.getObservaciones())%></textarea>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-form-save">GUARDAR</button>
            <a href="<%=ctx%>/IncidenciasServlet" class="btn-form-cancel" style="text-decoration:none;display:inline-flex;align-items:center;">CANCELAR</a>
          </div>
        </form>

<jsp:include page="/WEB-INF/includes/fin.jsp"/>
