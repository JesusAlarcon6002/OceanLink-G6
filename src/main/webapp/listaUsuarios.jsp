<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Usuarios" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    ArrayList<Usuarios> listaUsuarios = (ArrayList<Usuarios>) request.getAttribute("lista");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css"
          integrity="sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65"
          rel="stylesheet" crossorigin="anonymous">
    <title>Lista de Usuarios</title>
</head>
<body>
<div class='container'>
    <h1 class='mb-3'>Lista de Usuarios</h1>
    <a class="btn btn-primary" href="<%=request.getContextPath()%>/UsuariosServlet?action=formCrear">Crear usuario</a>
    <table>
        <tr>
            <th>Etiqueta</th>
            <th>Nombres</th>
            <th>Telefono</th>
            <th>Correos</th>
            <th>Roles</th>
            <th>Estado</th>
        </tr>
        <% for (Usuarios usr : listaUsuarios) { %>
        <tr>
            <td><%=usr.getEtiquetaUsuario()%></td>
            <td><%=usr.getNombreCompleto()%></td>
            <td><%=usr.getTelefono()%></td>
            <td><%=usr.getCorreo()%></td>
            <td><%=usr.getNombreRoles()%></td>
            <td><%=usr.getEstado()%></td>
            <td><a href="<%=request.getContextPath()%>/UsuariosServlet?action=editar&id=<%=usr.getIdUsuario()%>">Editar</a></td>
            <td><a href="<%=request.getContextPath()%>/UsuariosServlet?action=borrar&id=<%=usr.getIdUsuario()%>">Borrar</a></td>
        </tr>
        <%  }  %>
    </table>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-kenU1KFdBIe4zVF0s0G1M5b4hcpxyD9F7jL+jjXkk+Q2h455rYXK/7HAuoJl+0I4"
        crossorigin="anonymous"></script>
</body>
</html>