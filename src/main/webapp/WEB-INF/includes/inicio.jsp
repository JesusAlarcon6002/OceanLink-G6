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
<%
    String ctx = request.getContextPath();
    String titulo = request.getParameter("titulo") == null ? "" : request.getParameter("titulo");
    String encabezado = request.getParameter("encabezado") == null ? "" : request.getParameter("encabezado");
    String subtitulo = request.getParameter("subtitulo") == null ? "" : request.getParameter("subtitulo");
    String rol = request.getParameter("rol") == null ? "admin" : request.getParameter("rol");
    String activo = request.getParameter("activo") == null ? "" : request.getParameter("activo");
%>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>OceanLink · <%=Html.esc(titulo)%></title>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
<link href="<%=ctx%>/assets/css/base.css" rel="stylesheet">
<link href="<%=ctx%>/assets/css/dashboard-admin.css" rel="stylesheet">
<link href="<%=ctx%>/assets/css/oceanlink-jsp.css" rel="stylesheet">
</head>
<body>

<div class="app-shell">
  <div id="sidebar-mount" data-role="<%=Html.esc(rol)%>" data-active="<%=Html.esc(activo)%>" data-ctx="<%=ctx%>"></div>

  <div class="dark-dashboard">
    <div class="dark-content">
    <div class="dark-scroll-area">

      <div class="dark-header">
        <div class="dark-header-text">
        <h1><%=Html.esc(encabezado)%></h1>
        <p><%=Html.esc(subtitulo)%></p>
        </div>
        <div class="header-actions">
          <button class="header-icon-btn" type="button" aria-label="Notificaciones">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/></svg>
          </button>
          <button class="header-icon-btn" type="button" id="header-menu-toggle" aria-label="Mostrar/ocultar menú">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" x2="20" y1="6" y2="6"/><line x1="4" x2="20" y1="12" y2="12"/><line x1="4" x2="20" y1="18" y2="18"/></svg>
          </button>
        </div>
      </div>

      <div class="middle-row">
