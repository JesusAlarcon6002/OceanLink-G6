<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.TimeZone" %>
<%@ page import="com.oceanlink.daos.PerfilDao" %>
<%@ page import="com.oceanlink.util.Html" %>
<%@ page import="com.oceanlink.util.Seguridad" %>
<%
    // Lo manda PerfilServlet. Muestra y edita solo los datos de la persona que inició sesión.
    String ctx = request.getContextPath();
    PerfilDao.Perfil p = (PerfilDao.Perfil) request.getAttribute("perfil");
    List<String> errores = (List<String>) request.getAttribute("errores");
    String errorClave = (String) request.getAttribute("errorClave");
    boolean guardado = Boolean.TRUE.equals(request.getAttribute("guardado"));
    boolean claveCambiada = Boolean.TRUE.equals(request.getAttribute("claveCambiada"));
    String[] f = (String[]) request.getAttribute("form"); // lo que la persona escribió si hubo error
    String vNombre = f != null ? f[0] : (p == null ? "" : p.nombre);
    String vApellido = f != null ? f[1] : (p == null ? "" : p.apellido);
    String vTelefono = f != null ? f[2] : (p == null || p.telefono == null ? "" : p.telefono);
    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    fmt.setTimeZone(TimeZone.getTimeZone("America/Lima"));
    String inicioSesion = fmt.format(new Date(session.getCreationTime()));
    int minutosInactividad = session.getMaxInactiveInterval() / 60;
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Perfil"/>
    <jsp:param name="encabezado" value="MI PERFIL"/>
    <jsp:param name="subtitulo" value="Tus datos, tu contraseña y la apariencia"/>
    <jsp:param name="activo" value="perfil"/>
</jsp:include>

        <div class="perfil-grid">
        <div class="form-panel-dark" style="min-height:auto;">
          <% if (guardado) { %><div class="msg msg-ok">Tus datos se guardaron.</div><% } %>
          <% if (errores != null && !errores.isEmpty()) { %>
            <div class="msg msg-error">Revisa lo siguiente:<ul><% for (String e : errores) { %><li><%=Html.esc(e)%></li><% } %></ul></div>
          <% } %>
          <% if (p == null) { %>
            <div class="msg msg-error">No se encontró tu cuenta.</div>
          <% } else { %>
          <div class="perfil-cabecera">
            <div class="perfil-avatar"><%=Html.esc(p.nombre.isEmpty() ? "?" : p.nombre.substring(0, 1).toUpperCase())%></div>
            <div>
              <h3 class="perfil-nombre"><%=Html.esc(p.nombre + " " + p.apellido)%></h3>
              <span class="perfil-rol"><%=Html.esc(p.rol)%></span>
            </div>
          </div>

          <form method="post" action="<%=ctx%>/PerfilServlet" autocomplete="off">
            <div class="form-grid-2">
              <div class="form-field"><label for="nombre">NOMBRE</label>
                <input id="nombre" name="nombre" type="text" maxlength="100" required value="<%=Html.esc(vNombre)%>"></div>
              <div class="form-field"><label for="apellido">APELLIDO</label>
                <input id="apellido" name="apellido" type="text" maxlength="100" required value="<%=Html.esc(vApellido)%>"></div>
              <div class="form-field"><label for="telefono">TELÉFONO</label>
                <input id="telefono" name="telefono" type="tel" maxlength="20" value="<%=Html.esc(vTelefono)%>" placeholder="Opcional"></div>
              <div class="form-field"><label>CORREO</label>
                <input type="text" value="<%=Html.esc(p.correo)%>" disabled></div>
              <div class="form-field"><label>USUARIO</label>
                <input type="text" value="<%=Html.esc(p.etiqueta)%>" disabled></div>
              <div class="form-field"><label>ROL</label>
                <input type="text" value="<%=Html.esc(p.rol)%>" disabled></div>
            </div>
            <div class="form-actions" style="margin-top:20px;">
              <button class="btn-form-save" type="submit">GUARDAR CAMBIOS</button>
            </div>
          </form>
          <p class="table-footer-note">El correo, el usuario y el rol los administra el Network Admin (Usuarios &rarr; Editar).</p>
          <% } %>
        </div>

        <div class="form-panel-dark" style="min-height:auto;">
          <h3 class="seccion-titulo">Cambiar contraseña</h3>
          <% if (claveCambiada) { %><div class="msg msg-ok">Tu contraseña se cambió. Te avisamos también por correo.</div><% } %>
          <% if (errorClave != null) { %><div class="msg msg-error"><%=Html.esc(errorClave)%></div><% } %>
          <form method="post" action="<%=ctx%>/PerfilServlet" autocomplete="off">
            <input type="hidden" name="action" value="clave">
            <div class="form-grid-2">
              <div class="form-field campo-ancho"><label for="claveActual">CONTRASEÑA ACTUAL</label>
                <input id="claveActual" name="claveActual" type="password" maxlength="64" required autocomplete="current-password"></div>
              <div class="form-field"><label for="claveNueva">CONTRASEÑA NUEVA</label>
                <input id="claveNueva" name="claveNueva" type="password" maxlength="64" required autocomplete="new-password"></div>
              <div class="form-field"><label for="claveRepite">REPITE LA CONTRASEÑA NUEVA</label>
                <input id="claveRepite" name="claveRepite" type="password" maxlength="64" required autocomplete="new-password"></div>
            </div>
            <p class="table-footer-note"><%=Html.esc(Seguridad.MENSAJE_POLITICA_CLAVE)%></p>
            <div class="form-actions" style="margin-top:14px;">
              <button class="btn-form-save" type="submit">CAMBIAR CONTRASEÑA</button>
            </div>
          </form>
        </div>

        <div class="form-panel-dark" style="min-height:auto;">
          <h3 class="seccion-titulo">Apariencia</h3>
          <div class="tema-opciones" role="radiogroup" aria-label="Modo de la interfaz">
            <label class="tema-opcion"><input type="radio" name="tema" value="oscuro"><span class="tema-muestra tema-muestra-oscuro"></span><span>Modo oscuro</span></label>
            <label class="tema-opcion"><input type="radio" name="tema" value="claro"><span class="tema-muestra tema-muestra-claro"></span><span>Modo claro</span></label>
          </div>
          <label class="pref-fila">
            <input type="checkbox" id="pref-menu">
            <span><b>Ocultar el menú lateral al entrar</b><br><small>Se guarda solo en este navegador. Siempre puedes abrirlo con el botón de las tres rayas.</small></span>
          </label>
          <p class="table-footer-note">Estas preferencias se guardan en este navegador. También puedes cambiar el modo con el botón del sol o la luna, arriba a la derecha.</p>
        </div>

        <div class="form-panel-dark" style="min-height:auto;">
          <h3 class="seccion-titulo">Tu sesión</h3>
          <div class="form-grid-2 una-columna">
            <div class="form-field"><label>SESIÓN INICIADA</label><input type="text" disabled value="<%=Html.esc(inicioSesion)%> (hora de Lima)"></div>
            <div class="form-field"><label>SE CIERRA POR INACTIVIDAD</label><input type="text" disabled value="<%=minutosInactividad%> minutos"></div>
          </div>
        </div>
        </div>

<script>
(function () {
  function leer(k) { try { return localStorage.getItem(k); } catch (e) { return null; } }
  function guardar(k, v) { try { if (v) localStorage.setItem(k, v); else localStorage.removeItem(k); } catch (e) {} }
  var temaActual = leer("ol_tema") === "claro" ? "claro" : "oscuro";
  document.querySelectorAll('input[name="tema"]').forEach(function (r) {
    r.checked = r.value === temaActual;
    r.addEventListener("change", function () { if (window.olAplicarTema) window.olAplicarTema(r.value); });
  });
  var chk = document.getElementById("pref-menu");
  chk.checked = leer("ol_menu_oculto") === "1";
  chk.addEventListener("change", function () { guardar("ol_menu_oculto", chk.checked ? "1" : null); });
})();
</script>

<jsp:include page="/WEB-INF/includes/fin.jsp"/>
