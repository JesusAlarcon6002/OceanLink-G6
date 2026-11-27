<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="com.oceanlink.services.CorreoService" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda CorreoServlet
    boolean configurado = Boolean.TRUE.equals(request.getAttribute("configurado"));
    String redirigirA = (String) request.getAttribute("redirigirA");
    List<CorreoService.RegistroEnvio> registros = (List<CorreoService.RegistroEnvio>) request.getAttribute("registros");
    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Registro de correos"/>
    <jsp:param name="encabezado" value="REGISTRO DE CORREOS"/>
    <jsp:param name="subtitulo" value="Últimos correos enviados por el sistema"/>
    <jsp:param name="rol" value="admin"/>
    <jsp:param name="activo" value="correo"/>
</jsp:include>

        <div class="dash-col">
        <div class="form-panel-dark" style="min-height:auto;">
          <div class="form-panel-header"><span style="color:#fff;font-size:16px;font-weight:700;">SERVIDOR DE CORREO</span></div>
          <p style="color:#c9d4e3;margin:0 0 8px;">
            <% if (configurado) { %>
              <span class="badge badge-success">Configurado</span>
              <%=Html.esc(request.getAttribute("smtpHost"))%>:<%=request.getAttribute("smtpPuerto")%>
              · remitente <%=Html.esc(request.getAttribute("smtpRemitente"))%>
            <% } else { %>
              <span class="badge badge-neutral">Sin configurar</span>
              No se envían correos. Define SMTP_HOST, SMTP_PORT, SMTP_USER y SMTP_PASSWORD (ver guía) y reinicia Tomcat.
            <% } %>
          </p>
          <% if (redirigirA != null) { %>
          <p class="msg msg-error" style="margin:0 0 8px;">Modo demostración: todos los correos se entregan a <b><%=Html.esc(redirigirA)%></b> en vez de a sus destinatarios reales.</p>
          <% } %>
          <p style="color:#94a3b8;margin:0;font-size:13px;">Los correos salen solos cuando ocurren estos eventos: incidencia registrada o resuelta, solicitud recibida o con cambio de estado, cambio de estado de un servicio, mantenimiento programado, utilización crítica, cliente registrado y, para usuarios, cuenta habilitada, deshabilitada o contraseña cambiada (nunca se envía la contraseña). El registro se reinicia al apagar Tomcat.</p>
        </div>

        <form method="post" action="<%=request.getContextPath()%>/CorreoServlet?accion=vencimientos" style="margin:14px 0 0;display:flex;align-items:center;gap:12px;flex-wrap:wrap;">
          <button type="submit" class="btn-form-save" <%= configurado ? "" : "disabled" %>>REVISAR SERVICIOS POR VENCER AHORA</button>
          <span style="color:#94a3b8;font-size:12px;">Se revisa solo cada 24 h: avisa a los clientes con servicios que vencen en 30 o 7 días.</span>
          <% if (request.getAttribute("vencRevisados") != null) { %><span class="badge badge-success"><%=Html.esc(request.getAttribute("vencRevisados"))%> servicio(s) avisado(s)</span><% } %>
        </form>

        <div class="dark-table-wrap" style="margin-top:14px;">
          <table>
            <thead><tr><th>FECHA</th><th>PARA</th><th>ASUNTO</th><th>RESULTADO</th></tr></thead>
            <tbody>
              <% if (registros == null || registros.isEmpty()) { %>
              <tr><td colspan="4" style="text-align:center;color:#94a3b8;">Todavía no se envió ningún correo.</td></tr>
              <% } else { for (CorreoService.RegistroEnvio r : registros) { %>
              <tr>
                <td><%=fmt.format(new Date(r.hora))%></td>
                <td><%=Html.esc(r.para)%></td>
                <td><%=Html.esc(r.asunto)%></td>
                <td><span class="badge <%= r.ok ? "badge-success" : "badge-danger" %>"><%= r.ok ? "Enviado" : "Falló" %></span>
                    <span style="color:#94a3b8;font-size:12px;"><%=Html.esc(r.detalle)%></span></td>
              </tr>
              <% } } %>
            </tbody>
          </table>
        </div>
        </div><%-- cierra .dash-col --%>

<jsp:include page="/WEB-INF/includes/fin.jsp"/>
