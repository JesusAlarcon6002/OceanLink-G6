package com.oceanlink.servlets;

import com.oceanlink.beans.UsuarioSesion;
import com.oceanlink.daos.PerfilDao;
import com.oceanlink.services.NotificadorCorreos;
import com.oceanlink.util.LimiteTasa;
import com.oceanlink.util.Seguridad;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Perfil de la persona que inició sesión (todos los roles; el filtro lo deja pasar a cualquier usuario con sesión).
 *  GET                  -> muestra sus datos (correo, rol y usuario son de solo lectura), el cambio de contraseña y la apariencia
 *  POST (sin action)    -> guarda nombre, apellido y teléfono
 *  POST action=clave    -> cambia la contraseña: pide la actual, aplica la política de contraseñas, máx. 5 intentos cada 15 min
 *                          y avisa por correo
 * Solo toca la cuenta de la sesión.
 */
@WebServlet(name = "PerfilServlet", value = "/PerfilServlet")
public class PerfilServlet extends HttpServlet {

    static final int MAX_INTENTOS_CLAVE = 5;
    static final long VENTANA_CLAVE_MS = 15 * 60_000L;

    private static final Pattern TELEFONO = Pattern.compile("^[0-9+()\\-\\s]{6,20}$");

    private static UsuarioSesion actual(HttpServletRequest request) {
        return (UsuarioSesion) request.getSession().getAttribute("usuario");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        mostrar(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        UsuarioSesion u = actual(request);
        if ("clave".equals(request.getParameter("action"))) {
            cambiarClave(request, response, u);
            return;
        }
        String nombre = limpio(request.getParameter("nombre"));
        String apellido = limpio(request.getParameter("apellido"));
        String telefono = limpio(request.getParameter("telefono"));

        List<String> errores = new ArrayList<>();
        if (nombre.isEmpty()) errores.add("El nombre es obligatorio.");
        if (apellido.isEmpty()) errores.add("El apellido es obligatorio.");
        if (nombre.length() > 100 || apellido.length() > 100) errores.add("Nombre y apellido admiten hasta 100 caracteres.");
        if (!telefono.isEmpty() && !TELEFONO.matcher(telefono).matches()) {
            errores.add("El teléfono solo puede llevar números, espacios, +, - y paréntesis (de 6 a 20 caracteres).");
        }
        if (!errores.isEmpty()) {
            request.setAttribute("errores", errores);
            request.setAttribute("form", new String[]{nombre, apellido, telefono});
            mostrar(request, response, errores);
            return;
        }

        PerfilDao dao = new PerfilDao();
        if (!dao.actualizarDatos(u.getIdUsuario(), nombre, apellido, telefono)) {
            errores.add("No se pudo guardar. Inténtalo de nuevo.");
            request.setAttribute("form", new String[]{nombre, apellido, telefono});
            mostrar(request, response, errores);
            return;
        }
        // el nombre de la sesión se refresca para que el menú lateral lo muestre al instante
        request.getSession().setAttribute("usuario",
                new UsuarioSesion(u.getIdUsuario(), (nombre + " " + apellido).trim(), u.getCorreo(), u.getRol()));
        response.sendRedirect(request.getContextPath() + "/PerfilServlet?ok=1");
    }

    private void cambiarClave(HttpServletRequest request, HttpServletResponse response, UsuarioSesion u)
            throws ServletException, IOException {
        String actualClave = request.getParameter("claveActual") == null ? "" : request.getParameter("claveActual");
        String nueva = request.getParameter("claveNueva") == null ? "" : request.getParameter("claveNueva");
        String repite = request.getParameter("claveRepite") == null ? "" : request.getParameter("claveRepite");

        String error = null;
        if (actualClave.isEmpty() || nueva.isEmpty() || repite.isEmpty()) {
            error = "Completa los tres campos.";
        } else if (!LimiteTasa.permitir("clave|" + u.getIdUsuario(), MAX_INTENTOS_CLAVE, VENTANA_CLAVE_MS)) {
            error = "Demasiados intentos. Espera unos minutos e inténtalo de nuevo.";
        } else {
            PerfilDao dao = new PerfilDao();
            if (!dao.claveActualCorrecta(u.getIdUsuario(), actualClave)) {
                error = "La contraseña actual no es correcta.";
            } else if (!nueva.equals(repite)) {
                error = "La contraseña nueva y su confirmación no coinciden.";
            } else if (nueva.equals(actualClave)) {
                error = "La contraseña nueva debe ser distinta de la actual.";
            } else {
                error = Seguridad.validarClave(nueva);
            }
            if (error == null && !dao.cambiarClave(u.getIdUsuario(), nueva)) {
                error = "No se pudo cambiar la contraseña. Inténtalo de nuevo.";
            }
        }
        if (error != null) {
            request.setAttribute("errorClave", error);
            mostrar(request, response, null);
            return;
        }
        NotificadorCorreos.contrasenaCambiadaPorUsuario(u.getNombreCompleto(), u.getCorreo());
        response.sendRedirect(request.getContextPath() + "/PerfilServlet?clave=1");
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response, List<String> errores)
            throws ServletException, IOException {
        UsuarioSesion u = actual(request);
        request.setAttribute("perfil", new PerfilDao().obtener(u.getIdUsuario()));
        request.setAttribute("guardado", "1".equals(request.getParameter("ok")));
        request.setAttribute("claveCambiada", "1".equals(request.getParameter("clave")));
        request.setAttribute("errores", errores);
        request.getRequestDispatcher("perfil.jsp").forward(request, response);
    }

    private static String limpio(String s) {
        return s == null ? "" : s.replaceAll("[\\r\\n\\t]+", " ").trim();
    }
}
