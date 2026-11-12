<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="com.oceanlink.beans.Landing_Stations" %>
<%@ page import="com.oceanlink.beans.Rutas" %>
<%@ page import="com.oceanlink.util.GrafoRutas" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Un solo formulario para CREAR y EDITAR. Lo manda RutasServlet.
    Rutas ruta = (Rutas) request.getAttribute("ruta");
    boolean editando = "editar".equals(request.getAttribute("modo"));
    GrafoRutas grafo = (GrafoRutas) request.getAttribute("grafo");
    ArrayList<Landing_Stations> listaStations = (ArrayList<Landing_Stations>) request.getAttribute("listaLanding_Stations");
    ArrayList<String> errores = (ArrayList<String>) request.getAttribute("errores");
    List<GrafoRutas.Candidato> candidatos = (List<GrafoRutas.Candidato>) request.getAttribute("candidatos");
    double capacidad = request.getAttribute("capacidad") == null ? 0 : (Double) request.getAttribute("capacidad");
    String ctx = request.getContextPath();
    DecimalFormat gbps = new DecimalFormat("#,##0.##");

    boolean bloqueado = editando && ruta.isTieneServicios();   // con servicios solo se edita etiqueta y estado
    int idOrigen = ruta.getIdOrigen();
    int idDestino = ruta.getIdDestino();
    String estadoRuta = ruta.getEstado() == null ? "Activa" : ruta.getEstado();
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Registrar Ruta"/>
    <jsp:param name="encabezado" value="REGISTRAR/EDITAR RUTA"/>
    <jsp:param name="subtitulo" value="Camino de segmentos entre dos estaciones"/>
    <jsp:param name="rol" value="capacity-planner"/>
    <jsp:param name="activo" value="rutas"/>
</jsp:include>

        <form class="form-panel-dark" method="post" action="<%=ctx%>/RutasServlet?action=guardar" id="form-ruta">
          <input type="hidden" name="id" value="<%= editando ? ruta.getIdRuta() : "" %>">

          <div class="form-panel-header">
            <span style="color:#fff;font-size:16px;font-weight:700;"><%= editando ? Html.esc(ruta.getEtiqueta()) : "NUEVA RUTA" %></span>
          </div>

          <% if (errores != null && !errores.isEmpty()) { %>
          <div class="msg msg-error">
            Revisa lo siguiente:
            <ul>
              <% for (String e : errores) { %><li><%=Html.esc(e)%></li><% } %>
            </ul>
          </div>
          <% } %>
          <% if (bloqueado) { %>
          <p class="msg msg-ok">Esta ruta ya tiene servicios: solo se puede cambiar su etiqueta y su estado. Para otro camino, registra una ruta nueva.</p>
          <% } %>

          <p class="form-section-label">INFORMACIÓN GENERAL</p>

          <div class="form-body">
            <div class="form-grid-2" style="grid-template-columns:1fr 1fr;flex:1 1 100%;">
              <div class="form-field">
                <label for="etiqueta">ETIQUETA *</label>
                <input type="text" id="etiqueta" name="etiqueta" maxlength="100" placeholder="RUTA-001"
                       value="<%=Html.esc(ruta.getEtiqueta())%>">
              </div>
              <div class="form-field">
                <label for="estado">ESTADO (INTERRUPTOR MANUAL)</label>
                <select id="estado" name="estado">
                  <option value="Activa" <%= "Activa".equals(estadoRuta) ? "selected" : "" %>>Activa</option>
                  <option value="Inactiva" <%= "Inactiva".equals(estadoRuta) ? "selected" : "" %>>Inactiva</option>
                </select>
              </div>
              <div class="form-field">
                <label for="origen">ORIGEN *</label>
                <select id="origen" name="origen" <%= bloqueado ? "disabled" : "" %>>
                  <option value="">Elige una estación...</option>
                  <% if (listaStations != null) for (Landing_Stations ls : listaStations) { %>
                  <option value="<%=ls.getId_landing_stations()%>" <%= ls.getId_landing_stations() == idOrigen ? "selected" : "" %>><%=Html.esc(ls.getEtiqueta_station())%></option>
                  <% } %>
                </select>
              </div>
              <div class="form-field">
                <label for="destino">DESTINO *</label>
                <select id="destino" name="destino" <%= bloqueado ? "disabled" : "" %>>
                  <option value="">Elige una estación...</option>
                  <% if (listaStations != null) for (Landing_Stations ls : listaStations) { %>
                  <option value="<%=ls.getId_landing_stations()%>" <%= ls.getId_landing_stations() == idDestino ? "selected" : "" %>><%=Html.esc(ls.getEtiqueta_station())%></option>
                  <% } %>
                </select>
              </div>
            </div>

            <% if (!bloqueado) { %>
            <div class="info-card-dark" style="flex:1 1 100%;max-width:none;box-sizing:border-box;">
              <h4>SUGERENCIA DE RUTAS</h4>
              <p style="color:#c9d4e3;font-size:13px;margin:0 0 10px;">Elige origen y destino, indica la capacidad que necesitas y pulsa SUGERIR: se listan los caminos posibles, la mejor opción primero. Los segmentos se pueden recorrer en ambos sentidos.</p>
              <div class="form-field" style="max-width:260px;">
                <label for="capacidad">CAPACIDAD NECESARIA (GBPS)</label>
                <input type="number" id="capacidad" name="capacidad" step="0.01" min="0" placeholder="20"
                       value="<%= capacidad > 0 ? String.valueOf(capacidad) : "" %>">
              </div>
              <button type="submit" class="btn-mini" style="margin-top:10px;" formaction="<%=ctx%>/RutasServlet?action=sugerir" formnovalidate>SUGERIR RUTAS</button>

              <% if (candidatos != null) { %>
                <% if (candidatos.isEmpty()) { %>
                <p class="msg msg-error" style="margin-top:12px;">No hay ningún camino disponible entre esas estaciones (revisa segmentos y estaciones caídas).</p>
                <% } else { %>
                <div class="dark-table-wrap" style="margin-top:12px;">
                  <table>
                    <thead><tr><th>#</th><th>CAMINO</th><th>SALTOS</th><th>GBPS LIBRES</th><th>OBSERVACIÓN</th><th></th></tr></thead>
                    <tbody>
                    <% int n = 0; for (GrafoRutas.Candidato c : candidatos) { n++;
                         String obs;
                         if (capacidad > 0 && !c.cumpleCapacidad) obs = "No alcanza la capacidad pedida";
                         else if (c.evaluacion.bloqueaNuevos) obs = Html.esc(c.evaluacion.motivo);
                         else if (c.evaluacion.advertencia) obs = Html.esc(c.evaluacion.motivo);
                         else obs = n == 1 ? "Recomendada" : "Sin observaciones";
                    %>
                    <tr>
                      <td><%=n%></td>
                      <td><%=Html.esc(c.etiquetasTexto())%></td>
                      <td><%=c.evaluacion.saltos%></td>
                      <td><%=gbps.format(c.evaluacion.cuello)%></td>
                      <td><%= n == 1 && c.cumpleCapacidad && !c.evaluacion.bloqueaNuevos ? "<strong>Recomendada</strong>" : obs %></td>
                      <td><button type="submit" class="btn-mini" name="usar" value="<%=c.idsTexto()%>"
                                  formaction="<%=ctx%>/RutasServlet?action=usar" formnovalidate>Usar</button></td>
                    </tr>
                    <% } %>
                    </tbody>
                  </table>
                </div>
                <% } %>
              <% } %>
            </div>
            <% } %>

            <div class="info-card-dark" style="flex:1 1 100%;max-width:none;box-sizing:border-box;">
              <h4>SEGMENTOS DE LA RUTA (EN ORDEN, DE ORIGEN A DESTINO)</h4>
              <% if (bloqueado) { %>
              <p style="color:#fff;margin:0;"><%=Html.esc(ruta.getSegmentosTexto())%></p>
              <% } else { %>
              <div id="lista-segmentos">
                <% for (GrafoRutas.Tramo t : ruta.getSegmentos()) { %>
                <div class="fila-seg">
                  <select name="segmento">
                    <% for (GrafoRutas.Tramo o : grafo.todosLosTramos()) { %>
                    <option value="<%=o.id%>" <%= o.id == t.id ? "selected" : "" %>><%=Html.esc(o.etiqueta)%> (<%=Html.esc(grafo.etiquetaEstacion(o.idOrigen))%> ↔ <%=Html.esc(grafo.etiquetaEstacion(o.idDestino))%>) · <%=Html.esc(o.estado)%></option>
                    <% } %>
                  </select>
                  <button type="button" class="btn-mini" data-mover="-1" title="Subir">↑</button>
                  <button type="button" class="btn-mini" data-mover="1" title="Bajar">↓</button>
                  <button type="button" class="btn-mini" data-quitar title="Quitar">✕</button>
                </div>
                <% } %>
              </div>
              <button type="button" class="btn-mini" id="agregar-segmento" style="margin-top:8px;">+ AGREGAR SEGMENTO</button>
              <template id="plantilla-segmento">
                <div class="fila-seg">
                  <select name="segmento">
                    <% for (GrafoRutas.Tramo o : grafo.todosLosTramos()) { %>
                    <option value="<%=o.id%>"><%=Html.esc(o.etiqueta)%> (<%=Html.esc(grafo.etiquetaEstacion(o.idOrigen))%> ↔ <%=Html.esc(grafo.etiquetaEstacion(o.idDestino))%>) · <%=Html.esc(o.estado)%></option>
                    <% } %>
                  </select>
                  <button type="button" class="btn-mini" data-mover="-1" title="Subir">↑</button>
                  <button type="button" class="btn-mini" data-mover="1" title="Bajar">↓</button>
                  <button type="button" class="btn-mini" data-quitar title="Quitar">✕</button>
                </div>
              </template>
              <% } %>

              <% if (ruta.getEvaluacion() != null && !ruta.getSegmentos().isEmpty()) {
                   GrafoRutas.Evaluacion ev = ruta.getEvaluacion(); %>
              <p style="color:#c9d4e3;font-size:13px;margin:12px 0 0;">
                Capacidad libre del tramo más limitado: <strong><%=gbps.format(ev.cuello)%> Gbps</strong>
                · Saltos: <%=ev.saltos%>
                <% if (ev.motivo != null) { %> · <strong><%=Html.esc(ev.motivo)%></strong><% } %>
              </p>
              <% } %>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-form-save">GUARDAR</button>
            <a href="<%=ctx%>/RutasServlet" class="btn-form-cancel" style="text-decoration:none;display:inline-flex;align-items:center;">CANCELAR</a>
          </div>
        </form>

<jsp:include page="/WEB-INF/includes/acciones-clientes.jsp"/>
<script>
  (function () {
    var lista = document.getElementById('lista-segmentos');
    if (!lista) return;
    document.getElementById('agregar-segmento').addEventListener('click', function () {
      var plantilla = document.getElementById('plantilla-segmento');
      lista.appendChild(plantilla.content.cloneNode(true));
    });
    lista.addEventListener('click', function (e) {
      var fila = e.target.closest('.fila-seg');
      if (!fila) return;
      if (e.target.hasAttribute('data-quitar')) {
        fila.remove();
      } else if (e.target.hasAttribute('data-mover')) {
        var dir = parseInt(e.target.getAttribute('data-mover'), 10);
        if (dir < 0 && fila.previousElementSibling) {
          lista.insertBefore(fila, fila.previousElementSibling);
        } else if (dir > 0 && fila.nextElementSibling) {
          lista.insertBefore(fila.nextElementSibling, fila);
        }
      }
    });
    // Si el origen/destino están bloqueados no se mandan; en ese caso el servlet usa los guardados.
    document.getElementById('form-ruta').addEventListener('submit', function (e) {
      var o = document.getElementById('origen'), d = document.getElementById('destino');
      var boton = e.submitter;
      if (boton && boton.hasAttribute('formnovalidate')) return;
      if (o && d && !o.disabled && o.value && o.value === d.value) {
        e.preventDefault();
        alert('El origen y el destino deben ser estaciones distintas.');
      }
    });
  })();
</script>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
