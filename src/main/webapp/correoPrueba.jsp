<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda CorreoServlet
    String ctx = request.getContextPath();
    boolean configurado = Boolean.TRUE.equals(request.getAttribute("configurado"));
    String ok = (String) request.getAttribute("ok");
    String error = (String) request.getAttribute("error");
    String destino = request.getAttribute("destino") == null ? "" : (String) request.getAttribute("destino");
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Prueba de correo"/>
    <jsp:param name="encabezado" value="PRUEBA DE CORREO"/>
    <jsp:param name="subtitulo" value="Comprueba que el envío por SMTP funciona"/>
    <jsp:param name="rol" value="admin"/>
    <jsp:param name="activo" value="correo"/>
</jsp:include>

        <form class="form-panel-dark" method="post" action="<%=ctx%>/CorreoServlet" style="min-height:auto;">
          <div class="form-panel-header"><span style="color:#fff;font-size:16px;font-weight:700;">ENVIAR CORREO DE PRUEBA</span></div>

          <% if (ok != null) { %><p class="msg msg-ok"><%=Html.esc(ok)%></p><% } %>
          <% if (error != null) { %><p class="msg msg-error"><%=Html.esc(error)%></p><% } %>

          <p style="color:#c9d4e3;margin:0 0 12px;">
            Estado del servidor SMTP:
            <% if (configurado) { %>
              <span class="badge badge-success">Configurado</span>
              <%=Html.esc(request.getAttribute("smtpHost"))%>:<%=request.getAttribute("smtpPuerto")%>
              · remitente <%=Html.esc(request.getAttribute("smtpRemitente"))%>
            <% } else { %>
              <span class="badge badge-danger">Sin configurar</span>
              Define SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASSWORD y SMTP_FROM (ver guía) y reinicia Tomcat.
            <% } %>
          </p>

          <div class="form-field">
            <label for="destino">Correo de destino</label>
            <input type="email" id="destino" name="destino" value="<%=Html.esc(destino)%>" required placeholder="alguien@ejemplo.com">
          </div>
          <div class="form-actions">
            <button type="submit" class="btn-form-save">ENVIAR PRUEBA</button>
          </div>
        </form>

<jsp:include page="/WEB-INF/includes/fin.jsp"/>
