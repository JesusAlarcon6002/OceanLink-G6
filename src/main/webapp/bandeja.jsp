<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.oceanlink.daos.BandejaDao" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda BandejaServlet: avisos (copia de los correos del sistema) y mensajes entre usuarios
    String ctx = request.getContextPath();
    List<BandejaDao.Aviso> avisos = (List<BandejaDao.Aviso>) request.getAttribute("avisos");
    List<BandejaDao.Destinatario> destinatarios = (List<BandejaDao.Destinatario>) request.getAttribute("destinatarios");
    String error = (String) request.getAttribute("error");
    String fDestino = request.getAttribute("f_idDestino") == null ? "" : (String) request.getAttribute("f_idDestino");
    String fAsunto = request.getAttribute("f_asunto") == null ? "" : (String) request.getAttribute("f_asunto");
    String fMensaje = request.getAttribute("f_mensaje") == null ? "" : (String) request.getAttribute("f_mensaje");
    boolean enviado = request.getParameter("enviado") != null;
    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    int sinLeer = 0;
    if (avisos != null) for (BandejaDao.Aviso a : avisos) if (!a.leida) sinLeer++;
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Mensajes y avisos"/>
    <jsp:param name="encabezado" value="MENSAJES Y AVISOS"/>
    <jsp:param name="subtitulo" value="Lo mismo que te llega por correo, más los mensajes de otros usuarios"/>
    <jsp:param name="activo" value="mensajes"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <div class="panel-toolbar">
            <span style="color:#fff;font-size:16px;font-weight:700;">BANDEJA (<%=sinLeer%> sin leer)</span>
            <% if (sinLeer > 0) { %>
            <form method="post" action="<%=ctx%>/BandejaServlet" style="margin:0;">
              <input type="hidden" name="action" value="leerTodas">
              <button type="submit" class="btn-edit-white">Marcar todo como leído</button>
            </form>
            <% } %>
          </div>
          <div class="dark-table-wrap">
            <table>
              <thead><tr><th>FECHA</th><th>TIPO</th><th>DETALLE</th><th></th></tr></thead>
              <tbody>
                <% if (avisos == null || avisos.isEmpty()) { %>
                <tr class="fila-vacia"><td colspan="4">No tienes avisos ni mensajes todavía.</td></tr>
                <% } else for (BandejaDao.Aviso a : avisos) { %>
                <tr class="<%= a.leida ? "" : "aviso-no-leida" %>">
                  <td><%=fmt.format(a.fecha)%></td>
                  <td><span class="badge <%= "Mensaje".equals(a.tipo) ? "badge-warning" : "badge-success" %>"><%=Html.esc(a.tipo)%></span></td>
                  <td>
                    <%=Html.esc(a.asunto)%>
                    <% if ("Mensaje".equals(a.tipo) && a.remitente != null) { %><br><small>De: <%=Html.esc(a.remitente)%></small><% } %>
                    <div class="aviso-texto"><%=Html.esc(a.mensaje)%></div>
                  </td>
                  <td>
                    <% if (!a.leida) { %>
                    <form method="post" action="<%=ctx%>/BandejaServlet" style="margin:0;">
                      <input type="hidden" name="action" value="leer">
                      <input type="hidden" name="id" value="<%=a.id%>">
                      <button type="submit" class="btn-edit-white">Marcar leído</button>
                    </form>
                    <% } %>
                  </td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
        </div>

        <form class="form-panel-dark" method="post" action="<%=ctx%>/BandejaServlet" style="margin-top:16px;"
              data-confirmar="¿Enviar este mensaje? También le llegará por correo." data-confirmar-btn="Enviar" data-confirmar-titulo="Enviar mensaje">
          <input type="hidden" name="action" value="enviar">
          <div class="form-panel-header"><span style="color:#fff;font-size:16px;font-weight:700;">ESCRIBIR UN MENSAJE</span></div>
          <% if (enviado) { %><p class="msg msg-ok">Mensaje enviado.</p><% } %>
          <% if (error != null) { %><p class="msg msg-error"><%=Html.esc(error)%></p><% } %>
          <div class="form-body" style="display:flex;flex-direction:column;gap:14px;">
            <div class="form-field">
              <label for="idDestino">PARA *</label>
              <select name="idDestino" id="idDestino" required>
                <option value="">Seleccionar usuario...</option>
                <% if (destinatarios != null) for (BandejaDao.Destinatario d : destinatarios) { %>
                <option value="<%=d.id%>" <%= String.valueOf(d.id).equals(fDestino) ? "selected" : "" %>><%=Html.esc(d.nombre)%> · <%=Html.esc(d.rol)%></option>
                <% } %>
              </select>
            </div>
            <div class="form-field">
              <label for="asunto">ASUNTO *</label>
              <input type="text" id="asunto" name="asunto" maxlength="150" required value="<%=Html.esc(fAsunto)%>">
            </div>
            <div class="form-field">
              <label for="mensaje">MENSAJE * (máx. 2000 caracteres; se permiten 10 mensajes cada 10 minutos)</label>
              <textarea id="mensaje" name="mensaje" rows="5" maxlength="2000" required><%=Html.esc(fMensaje)%></textarea>
            </div>
          </div>
          <div class="form-actions">
            <button type="submit" class="btn-form-save">ENVIAR MENSAJE</button>
          </div>
        </form>

<jsp:include page="/WEB-INF/includes/fin.jsp"/>
