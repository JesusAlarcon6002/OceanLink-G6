package com.oceanlink.servlets;

import com.oceanlink.daos.CuentasDao;
import com.oceanlink.daos.NotificacionDao;
import com.oceanlink.services.CorreoService;
import com.oceanlink.services.NotificadorCorreos;
import com.oceanlink.services.VerificacionCorreo;
import com.oceanlink.util.ConfigCorreo;
import com.oceanlink.util.Html;
import com.oceanlink.util.LimiteTasa;
import com.oceanlink.util.Seguridad;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Solicitud de registro (pública). Quien quiere una cuenta llena el formulario, confirma su correo con un código
 * y queda una solicitud PENDIENTE que un Network Admin acepta o rechaza (SolicitudesRegistroServlet).
 *   POST accion=enviar     -> valida los datos y manda el código al correo (si no hay servidor de correo se guarda directo)
 *   POST accion=confirmar  -> con el código correcto se guarda la solicitud
 * La contraseña se guarda ya con hash. Límite: 5 formularios cada 15 minutos por IP. Solo correos de los dominios
 * permitidos para usuarios (ConfigCorreo.dominiosUsuarios()).
 */
@WebServlet(name = "RegistrarseServlet", value = "/RegistrarseServlet")
public class RegistrarseServlet extends HttpServlet {

    private static final String ATRIBUTO = "registroPendiente";
    private static final Pattern NOMBRE = Pattern.compile("^[\\p{L}][\\p{L} '.-]{0,59}$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+ ()-]{6,20}$");

    private static final class Datos implements Serializable {
        String nombre, apellido, correo, telefono, hash;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        mostrar(request, response, null, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        if ("enviar".equals(accion)) {
            enviar(request, response);
        } else if ("confirmar".equals(accion)) {
            confirmar(request, response);
        } else {
            mostrar(request, response, null, null, null);
        }
    }

    private static String p(HttpServletRequest r, String nombre) {
        String v = r.getParameter(nombre);
        return v == null ? "" : v.trim();
    }

    private void enviar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Datos d = new Datos();
        d.nombre = p(request, "nombre");
        d.apellido = p(request, "apellido");
        d.correo = p(request, "correo");
        d.telefono = p(request, "telefono");
        String clave = request.getParameter("clave");
        String clave2 = request.getParameter("clave2");

        String error = null;
        if (!NOMBRE.matcher(d.nombre).matches()) error = "Escribe tu nombre (solo letras).";
        else if (!NOMBRE.matcher(d.apellido).matches()) error = "Escribe tu apellido (solo letras).";
        else if (!d.telefono.isEmpty() && !TELEFONO.matcher(d.telefono).matches()) error = "El teléfono no es válido.";
        else if (clave == null || clave.isEmpty()) error = "Elige una contraseña.";
        else if (Seguridad.validarClave(clave) != null) error = Seguridad.validarClave(clave);
        else if (!clave.equals(clave2)) error = "Las contraseñas no coinciden.";
        if (error != null) {
            mostrar(request, response, error, null, d);
            return;
        }
        if (!LimiteTasa.permitir("reg-ip|" + request.getRemoteAddr(), 5, 15 * 60_000L)) {
            mostrar(request, response, "Hiciste muchos intentos. Espera unos minutos e inténtalo de nuevo.", null, d);
            return;
        }
        CuentasDao dao = new CuentasDao();
        if (dao.correoEnUso(d.correo)) {
            mostrar(request, response, "Ese correo ya tiene una cuenta. Si olvidaste la contraseña, usa \"Olvidé mi contraseña\".", null, d);
            return;
        }
        if (dao.hayPendiente(d.correo)) {
            mostrar(request, response, "Ya hay una solicitud pendiente con ese correo. Espera a que un administrador la revise.", null, d);
            return;
        }
        HttpSession sesion = request.getSession();
        VerificacionCorreo.Resultado r = VerificacionCorreo.enviarCodigo(sesion, d.correo, ConfigCorreo.dominiosUsuarios());
        if (!r.ok) {
            mostrar(request, response, r.mensaje, null, d);
            return;
        }
        d.hash = Seguridad.hash(clave);
        if (r.sinCodigo) {
            // sin servidor de correo no hay forma de enviar código: se guarda directo (el admin igual decide)
            guardar(request, response, d);
            return;
        }
        sesion.setAttribute(ATRIBUTO, d);
        mostrar(request, response, null, r.mensaje, d);
    }

    private void confirmar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sesion = request.getSession();
        Datos d = (Datos) sesion.getAttribute(ATRIBUTO);
        if (d == null) {
            mostrar(request, response, "Llena primero el formulario.", null, null);
            return;
        }
        VerificacionCorreo.Resultado r = VerificacionCorreo.confirmar(sesion, d.correo, request.getParameter("codigo"));
        if (!r.ok) {
            mostrar(request, response, r.mensaje, null, d);
            return;
        }
        sesion.removeAttribute(ATRIBUTO);
        VerificacionCorreo.olvidar(sesion, d.correo);
        guardar(request, response, d);
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response, Datos d) throws ServletException, IOException {
        String error = new CuentasDao().crearSolicitudConHash(d.nombre, d.apellido, d.correo, d.telefono, d.hash);
        if (error != null) {
            request.getSession().removeAttribute(ATRIBUTO);
            mostrar(request, response, error, null, d);
            return;
        }
        // aviso a los Network Admin (correo + campana)
        List<String> admins = new NotificacionDao().correosPorRoles("Network Admin");
        CorreoService.enviarAsync(admins, "[OceanLink] Nueva solicitud de registro: " + d.nombre + " " + d.apellido,
                NotificadorCorreos.plantilla("Nueva solicitud de registro",
                        "<p style=\"margin:0 0 8px\">" + Html.esc(d.nombre + " " + d.apellido) + " (" + Html.esc(d.correo)
                                + ") pidió una cuenta en OceanLink.</p><p style=\"margin:0\">Revísala en el menú \"Solicitudes de registro\".</p>"));
        response.sendRedirect(request.getContextPath() + "/LoginServlet?ok=solicitud");
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response, String error, String aviso, Datos d)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession();
        boolean paso2 = sesion.getAttribute(ATRIBUTO) != null && VerificacionCorreo.exigeCodigo()
                && d != null && d.hash != null;
        request.setAttribute("error", error);
        request.setAttribute("aviso", aviso);
        request.setAttribute("paso2", paso2);
        request.setAttribute("datos", d);
        request.setAttribute("dominios", String.join(", ", ConfigCorreo.dominiosUsuarios()));
        request.getRequestDispatcher("/WEB-INF/vistas/registrarse.jsp").forward(request, response);
    }

    /** Para la vista: campos del formulario sin exponer la clase. */
    public static String[] campos(Object datos) {
        Datos d = (Datos) datos;
        return d == null ? new String[]{"", "", "", ""} : new String[]{d.nombre, d.apellido, d.correo, d.telefono};
    }
}
