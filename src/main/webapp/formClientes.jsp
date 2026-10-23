<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Clientes" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Un solo formulario para CREAR y EDITAR (lo decide el atributo "modo"). Lo manda ClientesServlet.
    Clientes c = (Clientes) request.getAttribute("cliente");
    boolean editando = "editar".equals(request.getAttribute("modo"));
    ArrayList<String> errores = (ArrayList<String>) request.getAttribute("errores");
    String ctx = request.getContextPath();
    String hoy = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Registrar Cliente"/>
    <jsp:param name="encabezado" value="REGISTRAR/EDITAR CLIENTE"/>
    <jsp:param name="subtitulo" value="Gestiona los clientes"/>
    <jsp:param name="rol" value="capacity-planner"/>
    <jsp:param name="activo" value="clientes"/>
</jsp:include>

        <form class="form-panel-dark" method="post" action="<%=ctx%>/ClientesServlet?action=guardar">
          <input type="hidden" name="id" value="<%= editando ? c.getIdCliente() : "" %>">

          <div class="form-panel-header">
            <span style="color:#fff;font-size:16px;font-weight:700;"><%= editando ? Html.esc(c.getNombre()) : "NUEVO CLIENTE" %></span>
            <span class="date-label">Fecha: <%=hoy%></span>
          </div>

          <% if (errores != null && !errores.isEmpty()) { %>
          <div class="msg msg-error">
            No se pudo guardar. Corrige lo siguiente:
            <ul>
              <% for (String e : errores) { %><li><%=Html.esc(e)%></li><% } %>
            </ul>
          </div>
          <% } %>

          <p class="form-section-label">INFORMACIÓN GENERAL</p>

          <div class="form-body">
            <div class="form-grid-2" style="grid-template-columns:1fr 1fr;">
              <div class="form-field">
                <label for="nombre">NOMBRE *</label>
                <input type="text" id="nombre" name="nombre" maxlength="150" required
                       placeholder="Empresa Andina" value="<%=Html.esc(c.getNombre())%>">
              </div>
              <div class="form-field">
                <label for="etiqueta">IDENTIFICADOR</label>
                <input type="text" id="etiqueta" name="etiqueta" maxlength="45"
                       placeholder="CLI-001" value="<%=Html.esc(c.getEtiqueta())%>">
              </div>
              <div class="form-field">
                <label for="correo">CORREO</label>
                <input type="email" id="correo" name="correo" maxlength="150"
                       placeholder="contacto@empresaandina.com" value="<%=Html.esc(c.getCorreo())%>">
              </div>
              <div class="form-field">
                <label for="telefono">TELÉFONO</label>
                <input type="text" id="telefono" name="telefono" maxlength="20"
                       placeholder="+51 999 999 999" value="<%=Html.esc(c.getTelefono())%>">
              </div>
              <div class="form-field">
                <label for="estado">ESTADO</label>
                <select id="estado" name="estado">
                  <option value="Activo" <%= "Activo".equals(c.getEstado()) ? "selected" : "" %>>Activo</option>
                  <option value="Inactivo" <%= "Inactivo".equals(c.getEstado()) ? "selected" : "" %>>Inactivo</option>
                </select>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-form-save">GUARDAR</button>
            <a href="<%=ctx%>/ClientesServlet" class="btn-form-cancel" style="text-decoration:none;display:inline-flex;align-items:center;">CANCELAR</a>
          </div>
        </form>

<jsp:include page="/WEB-INF/includes/acciones-clientes.jsp"/>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
