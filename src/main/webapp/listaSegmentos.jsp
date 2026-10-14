<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Segmentos" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    ArrayList<Segmentos> listaSegmentos = (ArrayList<Segmentos>) request.getAttribute("listaSegmentos");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css"
          integrity="sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65"
          rel="stylesheet" crossorigin="anonymous">
    <title>Lista de Segmentos</title>
</head>
<body>
<div class='container'>
    <h1 class='mb-3'>Lista de Segmentos</h1>
    <table>
        <tr>
            <th>Etiqueta del segmento</th>
            <th>Estación origen</th>
            <th>Estación destino</th>
            <th>Capacidad total (Gbps)</th>
            <th>Capacidad reservada (Gbps)</th>
            <th>Capacidad utilizada (Gbps)</th>
            <th>Estado</th>
        </tr>
        <% for (Segmentos sg : listaSegmentos) { %>
        <tr>
            <td><%=sg.getEtiquetaSegmento()%></td>
            <td><%=sg.getEstacionOrigen()%></td>
            <td><%=sg.getEstacionDestino()%></td>
            <td><%=sg.getCapacidadTotalGbps()%></td>
            <td><%=sg.getCapacidadReservadaGbps()%></td>
            <td><%=sg.getCapacidadUtilizadaGbps()%></td>
            <td><%=sg.getEstado()%></td>
        </tr>
        <%  }  %>
    </table>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-kenU1KFdBIe4zVF0s0G1M5b4hcpxyD9F7jL+jjXkk+Q2h455rYXK/7HAuoJl+0I4"
        crossorigin="anonymous"></script>
</body>
</html>