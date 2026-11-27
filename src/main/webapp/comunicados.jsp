<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ page import="com.oceanlink.beans.Clientes" %>
<%@ page import="com.oceanlink.beans.Roles" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda ComunicadosServlet
    String ctx = request.getContextPath();
    ArrayList<String> errores = (ArrayList<String>) request.getAttribute("errores");
    List<Clientes> clientes = (List<Clientes>) request.getAttribute("clientes");
    List<Roles> roles = (List<Roles>) request.getAttribute("roles");
    boolean smtp = Boolean.TRUE.equals(request.getAttribute("smtp"));
    String ok = request.getParameter("ok");
    String destino = request.getAttribute("f_destino") == null ? "clientes" : (String) request.getAttribute("f_destino");
    String asunto = request.getAttribute("f_asunto") == null ? "" : (String) request.getAttribute("f_asunto");
    String mensaje = request.getAttribute("f_mensaje") == null ? "" : (String) request.getAttribute("f_mensaje");
    String idClienteSel = request.getAttribute("f_idCliente") == null ? "" : (String) request.getAttribute("f_idCliente");
    List<String> rolesSel = request.getAttribute("f_roles") == null ? new ArrayList<String>() : Arrays.asList((String[]) request.getAttribute("f_roles"));
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Enviar comunicado"/>
    <jsp:param name="encabezado" value="ENVIAR COMUNICADO"/>
    <jsp:param name="subtitulo" value="Escribe un correo para clientes o usuarios de la empresa"/>
    <jsp:param name="rol" value="admin"/>
    <jsp:param name="activo" value="comunicados"/>
</jsp:include>

        <form class="form-panel-dark" method="post" action="<%=ctx%>/ComunicadosServlet" id="form-comunicado"
              data-confirmar="¿Enviar este comunicado? Los correos no se pueden retirar una vez enviados."
              data-confirmar-btn="Enviar" data-confirmar-titulo="Enviar comunicado">
          <div class="form-panel-header"><span style="color:#fff;font-size:16px;font-weight:700;">NUEVO COMUNICADO</span></div>

          <% if (ok != null) { %><p class="msg msg-ok">Comunicado enviado a <%=Html.esc(ok)%> destinatario(s). Revisa el resultado en "Registro de correos".</p><% } %>
          <% if (!smtp) { %><p class="msg msg-error">El servidor de correo no está configurado: no se podrán enviar comunicados (ver "Registro de correos").</p><% } %>
          <% if (errores != null && !errores.isEmpty()) { %>
          <div class="msg msg-error">
            No se pudo enviar. Corrige lo siguiente:
            <ul><% for (String e : errores) { %><li><%=Html.esc(e)%></li><% } %></ul>
          </div>
          <% } %>

          <p class="form-section-label">DESTINATARIOS</p>
          <div class="form-body">
            <div class="form-field">
              <label><input type="radio" name="destino" value="clientes" <%= "clientes".equals(destino) ? "checked" : "" %>> Todos los clientes activos</label>
              <label><input type="radio" name="destino" value="cliente" <%= "cliente".equals(destino) ? "checked" : "" %>> Un cliente</label>
              <select name="idCliente" id="idCliente">
                <option value="">Seleccionar cliente...</option>
                <% if (clientes != null) for (Clientes c : clientes) { if (!"Activo".equals(c.getEstado())) continue; %>
                <option value="<%=c.getIdCliente()%>" <%= String.valueOf(c.getIdCliente()).equals(idClienteSel) ? "selected" : "" %>><%=Html.esc(c.getNombre())%></option>
                <% } %>
              </select>
              <label><input type="radio" name="destino" value="usuarios" <%= "usuarios".equals(destino) ? "checked" : "" %>> Todos los usuarios activos de la empresa</label>
              <label><input type="radio" name="destino" value="roles" <%= "roles".equals(destino) ? "checked" : "" %>> Usuarios activos de estos roles:</label>
              <div style="display:flex;flex-wrap:wrap;gap:14px;margin-left:22px;">
                <% if (roles != null) for (Roles r : roles) { %>
                <label><input type="checkbox" name="roles" value="<%=Html.esc(r.getNombreRoles())%>" <%= rolesSel.contains(r.getNombreRoles()) ? "checked" : "" %>> <%=Html.esc(r.getNombreRoles())%></label>
                <% } %>
              </div>
            </div>
          </div>

          <p class="form-section-label">MENSAJE</p>
          <div class="form-body" style="display:flex;flex-direction:column;gap:14px;">
            <div class="form-field">
              <label for="asunto">ASUNTO *</label>
              <input type="text" id="asunto" name="asunto" maxlength="150" required value="<%=Html.esc(asunto)%>">
            </div>
            <div class="form-field">
              <label for="mensaje">MENSAJE *</label>
              <textarea id="mensaje" name="mensaje" rows="8" maxlength="5000" required><%=Html.esc(mensaje)%></textarea>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-form-save" <%= smtp ? "" : "disabled" %>>ENVIAR COMUNICADO</button>
          </div>
        </form>

<jsp:include page="/WEB-INF/includes/fin.jsp"/>
