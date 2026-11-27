/* Ventana de confirmación de OceanLink (reemplaza al confirm() del navegador).
 * Uso: <form ... data-confirmar="¿Seguro que quieres desactivar este cliente?"
 *             data-confirmar-btn="Desactivar" data-confirmar-titulo="Desactivar cliente">
 * data-confirmar-btn y data-confirmar-titulo son opcionales. */
(function () {
  'use strict';
  var overlay, tituloEl, textoEl, btnOk, btnCancelar, formPendiente = null, foco = null;

  function crear() {
    overlay = document.createElement('div');
    overlay.className = 'ol-modal-overlay';
    overlay.setAttribute('role', 'dialog');
    overlay.setAttribute('aria-modal', 'true');
    overlay.setAttribute('aria-labelledby', 'ol-modal-titulo');
    overlay.innerHTML =
      '<div class="ol-modal">' +
        '<div class="ol-modal-icono" aria-hidden="true">!</div>' +
        '<h3 class="ol-modal-titulo" id="ol-modal-titulo"></h3>' +
        '<p class="ol-modal-texto"></p>' +
        '<div class="ol-modal-acciones">' +
          '<button type="button" class="btn-form-cancel ol-modal-cancelar">CANCELAR</button>' +
          '<button type="button" class="ol-modal-ok">ACEPTAR</button>' +
        '</div>' +
      '</div>';
    document.body.appendChild(overlay);
    tituloEl = overlay.querySelector('.ol-modal-titulo');
    textoEl = overlay.querySelector('.ol-modal-texto');
    btnOk = overlay.querySelector('.ol-modal-ok');
    btnCancelar = overlay.querySelector('.ol-modal-cancelar');

    btnCancelar.addEventListener('click', cerrar);
    overlay.addEventListener('mousedown', function (e) { if (e.target === overlay) cerrar(); });
    btnOk.addEventListener('click', function () {
      var f = formPendiente;
      cerrar();
      if (f) { f.setAttribute('data-confirmado', '1'); f.submit(); }
    });
    document.addEventListener('keydown', function (e) {
      if (!overlay.classList.contains('ol-abierto')) return;
      if (e.key === 'Escape') { cerrar(); }
      if (e.key === 'Tab') {   // el foco se queda dentro de la ventana
        e.preventDefault();
        (document.activeElement === btnOk ? btnCancelar : btnOk).focus();
      }
    });
  }

  function abrir(form) {
    if (!overlay) crear();
    formPendiente = form;
    foco = document.activeElement;
    tituloEl.textContent = form.getAttribute('data-confirmar-titulo') || 'Confirmar acción';
    textoEl.textContent = form.getAttribute('data-confirmar');
    var normal = form.getAttribute('data-confirmar-tipo') === 'normal';
    overlay.firstChild.className = 'ol-modal' + (normal ? ' ol-normal' : '');
    overlay.querySelector('.ol-modal-icono').textContent = normal ? '?' : '!';
    btnOk.textContent = (form.getAttribute('data-confirmar-btn') || 'Aceptar').toUpperCase();
    overlay.classList.add('ol-abierto');
    btnCancelar.focus();
  }

  function cerrar() {
    if (!overlay) return;
    overlay.classList.remove('ol-abierto');
    formPendiente = null;
    if (foco && foco.focus) { try { foco.focus(); } catch (e) { /* nada */ } }
  }

  document.addEventListener('submit', function (e) {
    var form = e.target;
    if (!form || !form.getAttribute || !form.getAttribute('data-confirmar')) return;
    if (form.getAttribute('data-confirmado') === '1') return;
    e.preventDefault();
    abrir(form);
  }, true);
})();
