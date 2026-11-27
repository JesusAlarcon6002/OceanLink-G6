package com.oceanlink.servlets;

import com.oceanlink.beans.UsuarioSesion;
import com.oceanlink.daos.BandejaDao;
import com.oceanlink.services.CorreoService;
import com.oceanlink.services.NotificadorCorreos;
import com.oceanlink.util.Html;
import com.oceanlink.util.Json;
import com.oceanlink.util.LimiteTasa;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Campana y mensajes de TODOS los roles (el filtro lo deja pasar a cualquier usuario con sesión).
 *  GET  (sin action)        -> pantalla de la bandeja (avisos + mensajes recibidos + formulario para escribir)
 *  GET  action=resumen      -> JSON {"noLeidas":n,"items":[...]} para el numerito de la campana
 *  POST action=leer id=N    -> marca una como leída
 *  POST action=leerTodas    -> marca todas como leídas
 *  POST action=enviar       -> manda un mensaje a otro usuario (queda en su campana y le llega también por correo)
 * Cada quien solo ve y marca lo suyo (siempre se filtra por el usuario de la sesión).
 * Límite anti-abuso: 10 mensajes cada 10 minutos por usuario.
 */
@WebServlet(name = "BandejaServlet", value = "/BandejaServlet")
public class BandejaServlet extends HttpServlet {

    static final int MAX_MENSAJES = 10;
    static final long VENTANA_MS = 10 * 60_000L;

    private static UsuarioSesion actual(HttpServletRequest request) {
        return (UsuarioSesion) request.getSession().getAttribute("usuario");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        UsuarioSesion u = actual(request);
        BandejaDao dao = new BandejaDao();
        if ("resumen".equals(request.getParameter("action"))) {
            List<BandejaDao.Aviso> ultimos = dao.listar(u.getIdUsuario(), 6);
            StringBuilder sb = new StringBuilder("{\"noLeidas\":").append(dao.contarNoLeidas(u.getIdUsuario())).append(",\"items\":[");
            for (int i = 0; i < ultimos.size(); i++) {
                BandejaDao.Aviso a = ultimos.get(i);
                if (i > 0) sb.append(',');
                sb.append("{\"asunto\":").append(Json.texto(a.asunto)).append(",\"tipo\":").append(Json.texto(a.tipo))
                        .append(",\"leida\":").append(a.leida).append('}');
            }
            sb.append("]}");
            response.setContentType("application/json; charset=UTF-8");
            response.setHeader("Cache-Control", "no-store");
            response.getWriter().write(sb.toString());
            return;
        }
        request.setAttribute("avisos", dao.listar(u.getIdUsuario(), 100));
        request.setAttribute("destinatarios", dao.destinatarios(u.getIdUsuario()));
        request.getRequestDispatcher("bandeja.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        UsuarioSesion u = actual(request);
        BandejaDao dao = new BandejaDao();
        String action = request.getParameter("action") == null ? "" : request.getParameter("action");
        String base = request.getContextPath() + "/BandejaServlet";

        switch (action) {
            case "leer":
                try {
                    dao.marcarLeida(u.getIdUsuario(), Integer.parseInt(request.getParameter("id")));
                } catch (NumberFormatException ignorar) {
                    // id inválido: no se hace nada
                }
                response.sendRedirect(base);
                return;
            case "leerTodas":
                dao.marcarTodas(u.getIdUsuario());
                response.sendRedirect(base);
                return;
            case "enviar":
                enviar(request, response, u, dao, base);
                return;
            default:
                response.sendRedirect(base);
        }
    }

    private void enviar(HttpServletRequest request, HttpServletResponse response, UsuarioSesion u, BandejaDao dao, String base)
            throws ServletException, IOException {
        String asunto = request.getParameter("asunto") == null ? "" : request.getParameter("asunto").replaceAll("[\\r\\n]+", " ").trim();
        String mensaje = request.getParameter("mensaje") == null ? "" : request.getParameter("mensaje").trim();
        String error = null;
        int idDestino = 0;
        try {
            idDestino = Integer.parseInt(request.getParameter("idDestino"));
        } catch (NumberFormatException e) {
            error = "Elige a quién le escribes.";
        }
        if (error == null && idDestino == u.getIdUsuario()) error = "No puedes escribirte a ti mismo.";
        if (error == null && asunto.isEmpty()) error = "El asunto es obligatorio.";
        if (error == null && mensaje.isEmpty()) error = "El mensaje es obligatorio.";
        if (error == null && (asunto.length() > BandejaDao.MAX_ASUNTO || mensaje.length() > BandejaDao.MAX_MENSAJE)) {
            error = "El asunto admite hasta " + BandejaDao.MAX_ASUNTO + " caracteres y el mensaje hasta " + BandejaDao.MAX_MENSAJE + ".";
        }
        String[] destino = null;
        if (error == null) {
            destino = dao.correoYNombre(idDestino);
            if (destino == null) error = "Ese usuario no existe o está inactivo.";
        }
        if (error == null && !LimiteTasa.permitir("msg|" + u.getIdUsuario(), MAX_MENSAJES, VENTANA_MS)) {
            error = "Mandaste muchos mensajes seguidos. Espera unos minutos e inténtalo de nuevo.";
        }
        if (error == null && !dao.enviarMensaje(u.getIdUsuario(), idDestino, asunto, mensaje)) {
            error = "No se pudo enviar el mensaje. Inténtalo de nuevo.";
        }
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("f_idDestino", String.valueOf(idDestino));
            request.setAttribute("f_asunto", asunto);
            request.setAttribute("f_mensaje", mensaje);
            request.setAttribute("avisos", dao.listar(u.getIdUsuario(), 100));
            request.setAttribute("destinatarios", dao.destinatarios(u.getIdUsuario()));
            request.getRequestDispatcher("bandeja.jsp").forward(request, response);
            return;
        }
        // copia por correo real (el texto del usuario va escapado)
        String cuerpo = NotificadorCorreos.plantilla("Mensaje de " + u.getNombreCompleto(),
                "<p style=\"margin:0 0 8px\"><b>" + Html.esc(asunto) + "</b></p>"
                        + "<p style=\"margin:0;white-space:pre-wrap\">" + Html.esc(mensaje) + "</p>"
                        + "<p style=\"margin:12px 0 0;font-size:12px;color:#64748b\">Enviado desde OceanLink por "
                        + Html.esc(u.getNombreCompleto()) + " (" + Html.esc(u.getCorreo()) + ").</p>");
        CorreoService.enviarSinCampana(Collections.singletonList(destino[0]), "[OceanLink] Mensaje de " + u.getNombreCompleto() + ": " + asunto, cuerpo);
        response.sendRedirect(base + "?enviado=1");
    }
}
