<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    String ctx = request.getContextPath();
    String error = (String) request.getAttribute("error");
    String aviso = (String) request.getAttribute("aviso");
    boolean paso2 = Boolean.TRUE.equals(request.getAttribute("paso2"));
%>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>OceanLink · Recuperar contraseña</title>
<link rel="icon" type="image/png" href="<%=ctx%>/assets/img/favicon.png">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
<link href="<%=ctx%>/assets/css/base.css" rel="stylesheet">
<link href="<%=ctx%>/assets/css/dashboard-admin.css" rel="stylesheet">
<link href="<%=ctx%>/assets/css/oceanlink-jsp.css" rel="stylesheet">
</head>
<body class="pagina-login">
<main class="login-wrap">
<%
    String correo = (String) request.getAttribute("correo");
    boolean smtp = Boolean.TRUE.equals(request.getAttribute("smtp"));
%>
  <div class="login-card">
    <div class="login-brand">
      <img src="<%=ctx%>/assets/img/logo-oceanlink.png" alt="Ocean Link">
      <h1>OCEAN LINK</h1>
      <p>Recuperar contraseña</p>
    </div>
    <% if (error != null) { %><div class="login-aviso login-error" role="alert"><%=Html.esc(error)%></div><% } %>
    <% if (aviso != null) { %><div class="login-aviso login-ok"><%=Html.esc(aviso)%></div><% } %>

    <% if (!paso2) { %>
    <form method="post" action="<%=ctx%>/RecuperarServlet" style="display:contents;">
      <input type="hidden" name="accion" value="pedir">
      <label for="correo">Tu correo</label>
      <input type="email" id="correo" name="correo" value="<%=Html.esc(correo)%>" required autofocus placeholder="usuario@oceanlink.net">
      <small>Te enviaremos un código de 6 dígitos para elegir una contraseña nueva.</small>
      <button type="submit" class="login-btn" <%= smtp ? "" : "disabled" %>>ENVIAR CÓDIGO</button>
    </form>
    <% } else { %>
    <form method="post" action="<%=ctx%>/RecuperarServlet" style="display:contents;" autocomplete="off">
      <input type="hidden" name="accion" value="cambiar">
      <label>Correo</label>
      <input type="email" value="<%=Html.esc(correo)%>" disabled>
      <label for="codigo">Código de 6 dígitos</label>
      <input type="text" id="codigo" name="codigo" inputmode="numeric" pattern="[0-9]{6}" maxlength="6" required autofocus placeholder="000000">
      <label for="clave">Contraseña nueva</label>
      <input type="password" id="clave" name="clave" required minlength="8" maxlength="64" autocomplete="new-password">
      <label for="clave2">Repite la contraseña</label>
      <input type="password" id="clave2" name="clave2" required minlength="8" maxlength="64" autocomplete="new-password">
      <small>8 a 64 caracteres, con al menos una letra y un número.</small>
      <button type="submit" class="login-btn">CAMBIAR CONTRASEÑA</button>
    </form>
    <form method="post" action="<%=ctx%>/RecuperarServlet" style="display:contents;">
      <input type="hidden" name="accion" value="pedir">
      <input type="hidden" name="correo" value="<%=Html.esc(correo)%>">
      <button type="submit" class="login-btn login-btn-sec">REENVIAR CÓDIGO</button>
    </form>
    <% } %>
    <div class="login-links"><a href="<%=ctx%>/LoginServlet">Volver a iniciar sesión</a></div>
  </div>
</main>
</body>
</html>
