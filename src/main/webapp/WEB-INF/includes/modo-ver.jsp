<%--
  Modo "VER" de los formularios: cuando el Servlet recibe action=ver pone el atributo "soloLectura" = true
  y reutiliza el MISMO formulario de editar. Aquí se bloquean los campos, se ocultan los botones de guardar
  y se quita cualquier campo de contraseña. (No es una barrera de seguridad: eso lo hace Permisos, que no deja
  guardar al Supervisor; esto solo cambia lo que se ve.)
  Se incluye justo antes de fin.jsp en cada formulario.
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<% if (Boolean.TRUE.equals(request.getAttribute("soloLectura"))) { %>
<script>
document.addEventListener('DOMContentLoaded', function () {
  var h1 = document.querySelector('.dark-header-text h1');
  if (h1) h1.textContent = h1.textContent.replace(/^(REGISTRAR\/EDITAR|EDITAR)\s*/, 'VER ');
  var sub = document.querySelector('.dark-header-text p');
  if (sub) sub.textContent = 'Detalle (solo lectura)';
  document.querySelectorAll('form.form-panel-dark').forEach(function (f) {
    // la contraseña nunca se muestra
    f.querySelectorAll('input[type=password]').forEach(function (p) {
      var campo = p.closest('.form-field') || p.parentElement;
      if (campo) campo.style.display = 'none';
      p.disabled = true;
    });
    f.querySelectorAll('input:not([type=hidden]), select, textarea, button').forEach(function (el) {
      if (el.classList.contains('btn-form-cancel')) return;
      el.disabled = true;
      el.removeAttribute('required');
    });
    f.querySelectorAll('button[type=submit], .btn-form-save, .btn-mini, .vc-caja, [data-solo-edicion]').forEach(function (el) {
      el.style.display = 'none';
    });
    f.addEventListener('submit', function (e) { e.preventDefault(); });
    f.querySelectorAll('.btn-form-cancel').forEach(function (a) { a.textContent = 'VOLVER'; });
  });
});
</script>
<% } %>
