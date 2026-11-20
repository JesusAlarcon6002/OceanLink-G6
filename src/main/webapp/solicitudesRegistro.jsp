<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.oceanlink.beans.Roles" %>
<%@ page import="com.oceanlink.daos.CuentasDao" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda SolicitudesRegistroServlet
    String ctx = request.getContextPath();
    List<CuentasDao.Solicitud> lista = (List<CuentasDao.Solicitud>) request.getAttribute("lista");
    List<Roles> roles = (List<Roles>) request.getAttribute("listaRoles");
    String error = (String) request.getAttribute("error");
    String ok = (String) request.getAttribute("ok");
    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Solicitudes de registro"/>
    <jsp:param name="encabezado" value="SOLICITUDES DE REGISTRO"/>
    <jsp:param name="subtitulo" value="Acepta o rechaza a quienes piden una cuenta"/>
    <jsp:param name="activo" value="solicitudes-registro"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <% if (ok != null) { %><p class="msg msg-ok"><%=Html.esc(ok)%></p><% } %>
          <% if (error != null) { %><p class="msg msg-error"><%=Html.esc(error)%></p><% } %>
          <div class="dark-table-wrap">
            <table>
              <thead><tr><th>FECHA</th><th>NOMBRE</th><th>CORREO</th><th>TELÉFONO</th><th>ESTADO</th><th>DECISIÓN</th></tr></thead>
              <tbody>
                <% if (lista == null || lista.isEmpty()) { %>
                <tr class="fila-vacia"><td colspan="6">No hay solicitudes de registro.</td></tr>
                <% } else for (CuentasDao.Solicitud s : lista) {
                       boolean pendiente = "Pendiente".equals(s.estado);
                       String clase = pendiente ? "badge-warning" : ("Aprobada".equals(s.estado) ? "badge-success" : "badge-neutral");
                %>
                <tr>
                  <td><%=fmt.format(s.fecha)%></td>
                  <td><%=Html.esc(s.nombre + " " + s.apellido)%></td>
                  <td><%=Html.esc(s.correo)%></td>
                  <td><%= s.telefono == null ? "—" : Html.esc(s.telefono) %></td>
                  <td><span class="badge <%=clase%>"><%=Html.esc(s.estado)%></span>
                      <% if (s.motivoRechazo != null) { %><br><small><%=Html.esc(s.motivoRechazo)%></small><% } %></td>
                  <td>
                    <% if (pendiente) { %>
                    <form method="post" action="<%=ctx%>/SolicitudesRegistroServlet" class="fila-inline"
                          data-confirmar="¿Aprobar esta solicitud y crear la cuenta con el rol elegido?" data-confirmar-btn="Aprobar" data-confirmar-titulo="Aprobar solicitud">
                      <input type="hidden" name="action" value="aprobar">
                      <input type="hidden" name="id" value="<%=s.id%>">
                      <select name="idRol" required>
                        <option value="">Rol...</option>
                        <% if (roles != null) for (Roles r : roles) { %>
                        <option value="<%=r.getIdRoles()%>"><%=Html.esc(r.getNombreRoles())%></option>
                        <% } %>
                      </select>
                      <button type="submit" class="btn-edit-white">Aprobar</button>
                    </form>
                    <form method="post" action="<%=ctx%>/SolicitudesRegistroServlet" class="fila-inline" style="margin-top:6px;"
                          data-confirmar="¿Rechazar esta solicitud?" data-confirmar-btn="Rechazar" data-confirmar-titulo="Rechazar solicitud">
                      <input type="hidden" name="action" value="rechazar">
                      <input type="hidden" name="id" value="<%=s.id%>">
                      <input type="text" name="motivo" maxlength="200" placeholder="Motivo (opcional)">
                      <button type="submit" class="btn-delete-red">Rechazar</button>
                    </form>
                    <% } else { %>
                    <%= s.fechaResolucion == null ? "—" : fmt.format(s.fechaResolucion) %>
                    <% } %>
                  </td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
        </div>

<jsp:include page="/WEB-INF/includes/fin.jsp"/>
