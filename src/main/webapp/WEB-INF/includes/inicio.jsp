<%--
  Parte de ARRIBA del diseño de OceanLink (sidebar + cabecera). Se usa así:

  <jsp:include page="/WEB-INF/includes/inicio.jsp">
      <jsp:param name="titulo" value="Mantenimientos"/>        <!-- título de la pestaña -->
      <jsp:param name="encabezado" value="MANTENIMIENTOS"/>    <!-- título grande de la pantalla -->
      <jsp:param name="subtitulo" value="Lista de mantenimientos"/>
      <jsp:param name="rol" value="maintenance-coordinator"/>  <!-- qué menú lateral se muestra -->
      <jsp:param name="activo" value="historial-mant"/>        <!-- opción del menú que se pinta activa -->
  </jsp:include>

     ... aquí va el contenido de la pantalla (uno o más paneles) ...

  <jsp:include page="/WEB-INF/includes/fin.jsp"/>
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.oceanlink.util.Html" %>
<%@ page import="com.oceanlink.beans.UsuarioSesion" %>
<%@ page import="com.oceanlink.util.Rol" %>
<%
    String ctx = request.getContextPath();
    String titulo = request.getParameter("titulo") == null ? "" : request.getParameter("titulo");
    String encabezado = request.getParameter("encabezado") == null ? "" : request.getParameter("encabezado");
    String subtitulo = request.getParameter("subtitulo") == null ? "" : request.getParameter("subtitulo");
    // El menú lateral sale del ROL DE LA SESIÓN (el parámetro "rol" de cada JSP ya no manda).
    UsuarioSesion usuarioSesion = (UsuarioSesion) session.getAttribute("usuario");
    String rol = usuarioSesion == null ? "admin" : usuarioSesion.getRol().getClave();
    String nombreUsuario = usuarioSesion == null ? "" : usuarioSesion.getNombreCompleto();
    String correoUsuario = usuarioSesion == null ? "" : usuarioSesion.getCorreo();
    boolean soloLectura = usuarioSesion != null && usuarioSesion.getRol() == Rol.SUPERVISOR;
    String activo = request.getParameter("activo") == null ? "" : request.getParameter("activo");
%>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>OceanLink · <%=Html.esc(titulo)%></title>
<link rel="icon" type="image/png" href="<%=ctx%>/assets/img/favicon.png">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
<link href="<%=ctx%>/assets/css/base.css" rel="stylesheet">
<link href="<%=ctx%>/assets/css/dashboard-admin.css" rel="stylesheet">
<link href="<%=ctx%>/assets/css/oceanlink-jsp.css" rel="stylesheet">
<link href="<%=ctx%>/assets/css/tema-claro.css" rel="stylesheet">
<script>
/* Modo claro/oscuro: se lee del navegador ANTES de pintar, para que no parpadee. window.olAplicarTema lo usan el botón del encabezado y el Perfil. */
(function () {
  var t = "oscuro";
  try { if (localStorage.getItem("ol_tema") === "claro") t = "claro"; } catch (e) {}
  document.documentElement.setAttribute("data-tema", t);
  window.olAplicarTema = function (nuevo) {
    nuevo = nuevo === "claro" ? "claro" : "oscuro";
    document.documentElement.setAttribute("data-tema", nuevo);
    try { localStorage.setItem("ol_tema", nuevo); } catch (e) {}
    document.querySelectorAll('input[name="tema"]').forEach(function (r) { r.checked = r.value === nuevo; });
  };
})();
</script>
<script src="<%=ctx%>/assets/js/campana.js" defer data-ctx="<%=ctx%>"></script>
</head>
<body<%= soloLectura ? " class=\"solo-lectura\"" : "" %>>

<div class="app-shell">
  <div id="sidebar-mount" data-role="<%=Html.esc(rol)%>" data-active="<%=Html.esc(activo)%>" data-ctx="<%=ctx%>" data-user-name="<%=Html.esc(nombreUsuario)%>" data-user-email="<%=Html.esc(correoUsuario)%>"></div>

  <div class="dark-dashboard">
    <div class="dark-content">
    <div class="dark-scroll-area">

      <div class="dark-header">
        <div class="dark-header-text">
        <h1><%=Html.esc(encabezado)%></h1>
        <p><%=Html.esc(subtitulo)%></p>
        </div>
        <div class="header-actions">
          <a class="header-icon-btn campana-btn" id="campana" href="<%=ctx%>/BandejaServlet" aria-label="Notificaciones y mensajes" title="Notificaciones y mensajes">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/></svg>
            <span class="campana-num" id="campana-num" hidden></span>
          </a>
          <button class="header-icon-btn" type="button" id="tema-toggle" aria-label="Cambiar entre modo claro y oscuro" title="Modo claro / oscuro"
                  onclick="olAplicarTema(document.documentElement.getAttribute('data-tema') === 'claro' ? 'oscuro' : 'claro')">
            <svg class="ico-sol" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41"/></svg>
            <svg class="ico-luna" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9z"/></svg>
          </button>
          <button class="header-icon-btn" type="button" id="header-menu-toggle" aria-label="Mostrar/ocultar menú">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" x2="20" y1="6" y2="6"/><line x1="4" x2="20" y1="12" y2="12"/><line x1="4" x2="20" y1="18" y2="18"/></svg>
          </button>
        </div>
      </div>

      <div class="middle-row">
