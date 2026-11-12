<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" isErrorPage="true" %>
<%@ page import="com.oceanlink.beans.UsuarioSesion" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    String ctx = request.getContextPath();
    UsuarioSesion u = (UsuarioSesion) session.getAttribute("usuario");
%>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>OceanLink · Acceso denegado</title>
<link rel="icon" type="image/png" href="<%=ctx%>/assets/img/favicon.png">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
<link href="<%=ctx%>/assets/css/base.css" rel="stylesheet">
<link href="<%=ctx%>/assets/css/dashboard-admin.css" rel="stylesheet">
<link href="<%=ctx%>/assets/css/oceanlink-jsp.css" rel="stylesheet">
</head>
<body class="pagina-login">
<main class="login-wrap">
  <div class="login-card">
    <div class="login-brand">
      <img src="<%=ctx%>/assets/img/logo-oceanlink.png" alt="Ocean Link">
      <h1>403 · ACCESO DENEGADO</h1>
      <p>No tienes permiso para ver este recurso<%= u == null ? "" : " con el rol " + Html.esc(u.getRol().getNombreBd()) %>.</p>
    </div>
    <a class="login-btn" style="display:block;text-align:center;text-decoration:none;" href="<%=ctx%>/InicioServlet">VOLVER AL INICIO</a>
  </div>
</main>
</body>
</html>
