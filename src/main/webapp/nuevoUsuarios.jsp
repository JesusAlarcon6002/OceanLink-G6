<%@ page import="com.oceanlink.beans.Roles" %>
<%@ page import="java.util.ArrayList" %>
<%
    ArrayList<Roles> listaRoles = (ArrayList<Roles>) request.getAttribute("listaRoles");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css"
          integrity="sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65"
          rel="stylesheet" crossorigin="anonymous">
    <title>Crear un nuevo usuarios</title>
</head>
<body>
<div class='container'>
    <h1 class='mb-3'>Crear un nuevo usuario</h1>
    <form method="POST" action="<%=request.getContextPath()%>/UsuariosServlet?action=crear">
        <div class="form-group">
            <label for="etiqueta">Etiqueta</label>
            <input type="text" class="form-control" id="etiqueta" name="etiqueta">
        </div>

        <div class="form-group">
            <label for="nombre">Nombre</label>
            <input type="text" class="form-control" id="nombre" name="nombre">
        </div>

        <div class="form-group">
            <label for="apellido">Apellido</label>
            <input type="text" class="form-control" id="apellido" name="apellido">
        </div>

        <div class="form-group">
            <label for="correo">Correo</label>
            <input type="email" class="form-control" id="correo" name="correo">
        </div>

        <div class="form-group">
            <label for="passwd">Contraseña</label>
            <input type="password" class="form-control" id="passwd" name="passwd">
        </div>

        <div class="form-group">
            <label for="telefono">Telefono</label>
            <input type="text" class="form-control" id="telefono" name="telefono">
        </div>

        <div class="form-group">
            <label for="rol">Rol</label>
            <select name="idRol" id="rol" class="form-select">
                <% if (listaRoles != null) {
                    for (Roles r : listaRoles) { %>
                <option value="<%= r.getIdRoles() %>"><%= r.getNombreRoles()%></option>
                <%     }
                } %>
            </select>
        </div>

        <a href="<%=request.getContextPath()%>/UsuariosServlet" class="btn btn-danger">Regresar</a>
        <button type="submit" class="btn btn-primary">Submit</button>

    </form>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-kenU1KFdBIe4zVF0s0G1M5b4hcpxyD9F7jL+jjXkk+Q2h455rYXK/7HAuoJl+0I4"
        crossorigin="anonymous"></script>
</body>
</html>