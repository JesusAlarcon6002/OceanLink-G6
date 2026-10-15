<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Landing_Stations" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    ArrayList<Landing_Stations> listaLanding_Stations = (ArrayList<Landing_Stations>) request.getAttribute("listaLanding_Stations");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css"
          integrity="sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65"
          rel="stylesheet" crossorigin="anonymous">
    <title>Lista de Landing Stations</title>
</head>
<body>
<div class='container'>
    <h1 class='mb-3'>Lista de Landing Stations</h1>
    <a class="btn btn-primary" href="<%=request.getContextPath()%>/EstacionesServlet?action=formCrear">Crear Landing Station</a>
    <table>
        <tr>
            <th>Etiqueta</th>
            <th>Ubicación</th>
            <th>Estado</th>

        </tr>
        <% for (Landing_Stations station : listaLanding_Stations) { %>
        <tr>
            <td><%=station.getEtiqueta_station()%></td>
            <td><%=station.getUbicacion()%></td>
            <td><%=station.getEstado()%></td>
            <td><a href="<%=request.getContextPath()%>/EstacionesServlet?action=editar&id=<%=station.getId_landing_stations()%>">Editar</a></td>
            <td><a href="<%=request.getContextPath()%>/EstacionesServlet?action=borrar&id=<%=station.getId_landing_stations()%>">Borrar</a></td>

        </tr>
        <%  }  %>
    </table>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-kenU1KFdBIe4zVF0s0G1M5b4hcpxyD9F7jL+jjXkk+Q2h455rYXK/7HAuoJl+0I4"
        crossorigin="anonymous"></script>
</body>
</html>