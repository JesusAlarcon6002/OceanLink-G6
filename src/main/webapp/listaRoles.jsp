<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.oceanlink.util.Html" %>
<%@ page import="com.oceanlink.util.Permisos" %>
<%@ page import="com.oceanlink.util.Rol" %>
<%
    // Lo manda RolesServlet. Solo consulta: los permisos son fijos (util.Permisos) y el rol de cada usuario se cambia en Usuarios > Editar.
    String ctx = request.getContextPath();
    Map<String, Integer> conteo = (Map<String, Integer>) request.getAttribute("conteoRoles");
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Roles y permisos"/>
    <jsp:param name="encabezado" value="ROLES Y PERMISOS"/>
    <jsp:param name="subtitulo" value="Qué puede hacer cada rol en OceanLink"/>
    <jsp:param name="activo" value="roles"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <div class="dark-table-wrap">
            <table>
              <thead><tr><th>ROL</th><th>USUARIOS ACTIVOS</th><th>ACCESO COMPLETO</th><th>SOLO LECTURA</th></tr></thead>
              <tbody>
                <% for (Rol r : Rol.values()) {
                       StringBuilder completo = new StringBuilder();
                       StringBuilder lectura = new StringBuilder();
                       for (Permisos.Modulo m : Permisos.todos()) {
                           if (m.duenios.contains(r)) {
                               if (completo.length() > 0) completo.append(", ");
                               completo.append(m.nombre);
                           } else if (Permisos.veModulo(r, m)) {
                               if (lectura.length() > 0) lectura.append(", ");
                               lectura.append(m.nombre);
                           }
                       }
                       int usuarios = conteo == null || conteo.get(r.getNombreBd()) == null ? 0 : conteo.get(r.getNombreBd());
                %>
                <tr>
                  <td><b><%=Html.esc(r.getNombreBd())%></b></td>
                  <td><%=usuarios%></td>
                  <td><%= completo.length() == 0 ? "—" : Html.esc(completo.toString()) %></td>
                  <td><%= lectura.length() == 0 ? "—" : Html.esc(lectura.toString()) %></td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
          <p class="table-footer-note">Todos los roles tienen además la campana y Mensajes y avisos. Para cambiar el rol de una persona usa
            <a href="<%=ctx%>/UsuariosServlet" style="color:#93c5fd;">Usuarios &rarr; Editar</a>; las cuentas nuevas se crean por Solicitudes de registro.</p>
        </div>

<jsp:include page="/WEB-INF/includes/fin.jsp"/>
