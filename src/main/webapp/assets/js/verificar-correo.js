/* Verificación de correo en los formularios de OceanLink.
 * Uso: <input type="email" id="correo" name="correo" data-verificar-correo data-original="correo@actual.com">
 *  - data-original: correo que ya estaba guardado (si no cambia, no se pide verificar).
 * Pide al servidor (VerificarCorreoServlet) que compruebe que el dominio existe y, si hay servidor de
 * correo configurado, que envíe un código de 6 dígitos para confirmar que el buzón es de verdad. */
(function () {
  'use strict';
  var input = document.querySelector('input[data-verificar-correo]');
  if (!input) return;
  var original = (input.getAttribute('data-original') || '').trim().toLowerCase();
  var url = (document.body.getAttribute('data-ctx') || '') + '/VerificarCorreoServlet';
  if (!document.body.getAttribute('data-ctx')) {
    var m = document.querySelector('script[src*="verificar-correo.js"]');
    if (m) url = m.getAttribute('src').replace(/\/assets\/js\/verificar-correo\.js.*$/, '') + '/VerificarCorreoServlet';
  }

  var caja = document.createElement('div');
  caja.className = 'vc-caja';
  caja.innerHTML =
    '<div class="vc-fila">' +
      '<button type="button" class="vc-btn vc-enviar">ENVIAR CÓDIGO</button>' +
      '<span class="vc-codigo" hidden>' +
        '<input type="text" class="vc-input" inputmode="numeric" maxlength="6" placeholder="000000" autocomplete="one-time-code">' +
        '<button type="button" class="vc-btn vc-confirmar">CONFIRMAR</button>' +
      '</span>' +
    '</div>' +
    '<div class="vc-estado" role="status" aria-live="polite"></div>';
  input.parentNode.appendChild(caja);

  var btnEnviar = caja.querySelector('.vc-enviar');
  var btnConf = caja.querySelector('.vc-confirmar');
  var spanCod = caja.querySelector('.vc-codigo');
  var inpCod = caja.querySelector('.vc-input');
  var estado = caja.querySelector('.vc-estado');
  var enviadoA = '';

  function mostrar(texto, tipo) {
    estado.textContent = texto || '';
    estado.className = 'vc-estado' + (tipo ? ' vc-' + tipo : '');
  }

  function llamar(datos, cb) {
    var cuerpo = Object.keys(datos).map(function (k) {
      return encodeURIComponent(k) + '=' + encodeURIComponent(datos[k]);
    }).join('&');
    var x = new XMLHttpRequest();
    x.open('POST', url);
    x.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
    x.onload = function () {
      try { cb(JSON.parse(x.responseText)); }
      catch (e) { cb({ ok: false, mensaje: 'No se pudo verificar (respuesta inesperada del servidor).' }); }
    };
    x.onerror = function () { cb({ ok: false, mensaje: 'No se pudo conectar con el servidor.' }); };
    x.send(cuerpo);
  }

  function actual() { return input.value.trim().toLowerCase(); }
  function cambio() { return actual() !== '' && actual() !== original; }

  function refrescar() {
    var visible = cambio();
    caja.hidden = !visible;
    if (actual() !== enviadoA) { spanCod.hidden = true; inpCod.value = ''; }
    if (!visible) mostrar('');
  }
  input.addEventListener('input', function () {
    if (actual() !== enviadoA) mostrar(cambio() ? 'Valida el correo antes de guardar.' : '');
    refrescar();
  });

  btnEnviar.addEventListener('click', function () {
    var correo = input.value.trim();
    if (!correo) { mostrar('Escribe un correo.', 'error'); return; }
    btnEnviar.disabled = true;
    mostrar('Comprobando…', 'info');
    llamar({ accion: 'enviar', correo: correo, tipo: input.getAttribute('data-tipo') || '' }, function (r) {
      btnEnviar.disabled = false;
      if (!r.ok) { mostrar(r.mensaje, 'error'); return; }
      enviadoA = correo.toLowerCase();
      if (r.sinCodigo) { spanCod.hidden = true; mostrar(r.mensaje, 'ok'); return; }
      spanCod.hidden = false;
      inpCod.focus();
      mostrar(r.mensaje, 'info');
      btnEnviar.textContent = 'REENVIAR CÓDIGO';
    });
  });

  function confirmar() {
    btnConf.disabled = true;
    llamar({ accion: 'confirmar', correo: input.value.trim(), codigo: inpCod.value.trim() }, function (r) {
      btnConf.disabled = false;
      mostrar(r.mensaje, r.ok ? 'ok' : 'error');
      if (r.ok) spanCod.hidden = true;
    });
  }
  btnConf.addEventListener('click', confirmar);
  inpCod.addEventListener('keydown', function (e) {
    if (e.key === 'Enter') { e.preventDefault(); confirmar(); }
  });

  refrescar();
})();
