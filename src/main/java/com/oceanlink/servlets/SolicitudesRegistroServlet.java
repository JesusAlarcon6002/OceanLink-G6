package com.oceanlink.servlets;

import com.oceanlink.beans.UsuarioSesion;
import com.oceanlink.daos.CuentasDao;
import com.oceanlink.daos.RolesDao;
import com.oceanlink.services.CorreoService;
import com.oceanlink.services.NotificadorCorreos;
import com.oceanlink.util.Html;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

/**
 * Solicitudes de registro: solo Network Admin. Es la ÚNICA forma de crear usuarios nuevos.
 *   GET                              -> lista (pendientes primero)
 *   POST action=aprobar id idRol     -> crea el usuario con el rol elegido y le avisa por correo
 *   POST action=rechazar id motivo   -> marca la solicitud como rechazada y le avisa por correo
 */
@WebServlet(name = "SolicitudesRegistroServlet", value = "/SolicitudesRegistroServlet")
public class SolicitudesRegistroServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        cargar(request);
        request.getRequestDispatcher("solicitudesRegistro.jsp").forward(request, response);
    }

    private void cargar(HttpServletRequest request) {
        request.setAttribute("lista", new CuentasDao().listar());
        request.setAttribute("listaRoles", new RolesDao().obtenerListaRoles());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        UsuarioSesion admin = (UsuarioSesion) request.getSession().getAttribute("usuario");
        CuentasDao dao = new CuentasDao();
        String action = request.getParameter("action") == null ? "" : request.getParameter("action");
        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/SolicitudesRegistroServlet");
            return;
        }
        CuentasDao.Solicitud s = dao.obtener(id);
        String error = null;
        String ok = null;
        if (s == null || !"Pendiente".equals(s.estado)) {
            error = "Esa solicitud ya no está pendiente.";
        } else if ("aprobar".equals(action)) {
            int idRol = 0;
            try {
                idRol = Integer.parseInt(request.getParameter("idRol"));
            } catch (NumberFormatException ignorar) {
                // se valida abajo
            }
            boolean rolValido = false;
            for (com.oceanlink.beans.Roles r : new RolesDao().obtenerListaRoles()) {
                if (r.getIdRoles() == idRol) rolValido = true;
            }
            if (!rolValido) {
                error = "Elige el rol que tendrá el usuario.";
            } else {
                error = dao.aprobar(id, idRol, admin.getIdUsuario());
                if (error == null) {
                    ok = "Cuenta creada para " + s.correo + ".";
                    CorreoService.enviarAsync(Collections.singletonList(s.correo), "[OceanLink] Tu solicitud fue aprobada",
                            NotificadorCorreos.plantilla("Solicitud aprobada",
                                    "<p style=\"margin:0 0 8px\">Hola " + Html.esc(s.nombre) + ", un administrador aprobó tu cuenta.</p>"
                                            + "<p style=\"margin:0\">Ya puedes ingresar a OceanLink con tu correo y la contraseña que elegiste.</p>"));
                }
            }
        } else if ("rechazar".equals(action)) {
            String motivo = request.getParameter("motivo") == null ? "" : request.getParameter("motivo").trim();
            if (motivo.length() > 200) motivo = motivo.substring(0, 200);
            if (dao.rechazar(id, admin.getIdUsuario(), motivo)) {
                ok = "Solicitud rechazada.";
                CorreoService.enviarAsync(Collections.singletonList(s.correo), "[OceanLink] Tu solicitud no fue aprobada",
                        NotificadorCorreos.plantilla("Solicitud rechazada",
                                "<p style=\"margin:0 0 8px\">Hola " + Html.esc(s.nombre) + ", un administrador no aprobó tu solicitud de cuenta.</p>"
                                        + (motivo.isEmpty() ? "" : "<p style=\"margin:0\">Motivo: " + Html.esc(motivo) + "</p>")));
            } else {
                error = "No se pudo rechazar la solicitud.";
            }
        } else {
            error = "Acción no válida.";
        }
        request.setAttribute("error", error);
        request.setAttribute("ok", ok);
        cargar(request);
        request.getRequestDispatcher("solicitudesRegistro.jsp").forward(request, response);
    }
}
