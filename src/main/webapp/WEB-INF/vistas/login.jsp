<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    String ctx = request.getContextPath();
    String error = (String) request.getAttribute("error");
    String correo = request.getAttribute("correo") == null ? "" : (String) request.getAttribute("correo");
    boolean salio = request.getParameter("salio") != null;
%>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>OceanLink · Iniciar sesión</title>
<link rel="icon" type="image/png" href="<%=ctx%>/assets/img/favicon.png">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
<link href="<%=ctx%>/assets/css/base.css" rel="stylesheet">
<link href="<%=ctx%>/assets/css/dashboard-admin.css" rel="stylesheet">
<link href="<%=ctx%>/assets/css/oceanlink-jsp.css" rel="stylesheet">
</head>
<body class="pagina-login">
<main class="login-wrap">
  <form class="login-card" method="post" action="<%=ctx%>/LoginServlet" autocomplete="on">
    <div class="login-brand">
      <img src="<%=ctx%>/assets/img/logo-oceanlink.png" alt="Ocean Link">
      <h1>OCEAN LINK</h1>
      <p>Sistema de gestión de infraestructura submarina</p>
    </div>

    <% if (error != null) { %>
      <div class="login-aviso login-error" role="alert"><%=Html.esc(error)%></div>
    <% } else if (salio) { %>
      <div class="login-aviso login-ok">Cerraste sesión correctamente.</div>
    <% } %>

    <label for="correo">Correo</label>
    <input type="email" id="correo" name="correo" value="<%=Html.esc(correo)%>" required autofocus placeholder="usuario@oceanlink.net">

    <label for="clave">Contraseña</label>
    <input type="password" id="clave" name="clave" required placeholder="••••••••">

    <button type="submit" class="login-btn">INGRESAR</button>
  </form>
</main>
</body>
</html>
