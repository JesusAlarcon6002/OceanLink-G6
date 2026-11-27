package com.oceanlink.servlets;

import com.oceanlink.beans.Clientes;
import com.oceanlink.beans.Roles;
import com.oceanlink.beans.UsuarioSesion;
import com.oceanlink.daos.ClientesDao;
import com.oceanlink.daos.NotificacionDao;
import com.oceanlink.daos.RolesDao;
import com.oceanlink.services.NotificadorCorreos;
import com.oceanlink.util.ConfigCorreo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * "Enviar comunicado": el Network Admin escribe un asunto y un mensaje y lo manda a
 *  - todos los clientes activos, o un cliente en particular,
 *  - todos los usuarios activos, o los usuarios activos de uno o más roles.
 * Cada destinatario recibe su propio correo (no ve a los demás).
 */
@WebServlet(name = "ComunicadosServlet", value = "/ComunicadosServlet")
public class ComunicadosServlet extends HttpServlet {

    private static final int MAX_DESTINATARIOS = 500;
    private static final int MAX_ASUNTO = 150;
    private static final int MAX_MENSAJE = 5000;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        mostrar(request, response, new ArrayList<>());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        ArrayList<String> errores = new ArrayList<>();

        String destino = param(request, "destino");
        String asunto = param(request, "asunto");
        String mensaje = param(request, "mensaje");
        String idClienteStr = param(request, "idCliente");
        String[] rolesElegidos = request.getParameterValues("roles");

        if (asunto.isEmpty()) errores.add("El asunto es obligatorio.");
        else if (asunto.length() > MAX_ASUNTO) errores.add("El asunto no puede pasar de " + MAX_ASUNTO + " caracteres.");
        if (mensaje.isEmpty()) errores.add("El mensaje es obligatorio.");
        else if (mensaje.length() > MAX_MENSAJE) errores.add("El mensaje no puede pasar de " + MAX_MENSAJE + " caracteres.");
        if (!ConfigCorreo.estaConfigurado()) {
            errores.add("El servidor de correo no está configurado: no se puede enviar (ver Registro de correos).");
        }

        NotificacionDao dao = new NotificacionDao();
        List<String> correos = new ArrayList<>();
        switch (destino) {
            case "clientes":
                correos = dao.correosClientesActivos();
                break;
            case "cliente": {
                int id;
                try {
                    id = Integer.parseInt(idClienteStr);
                } catch (NumberFormatException e) {
                    id = 0;
                }
                Clientes c = dao.clienteActivoConCorreo(id);
                if (c == null) errores.add("Elige un cliente activo que tenga correo.");
                else correos.add(c.getCorreo());
                break;
            }
            case "usuarios": {
                List<String> todos = new ArrayList<>();
                for (Roles r : new RolesDao().obtenerListaRoles()) todos.add(r.getNombreRoles());
                correos = dao.correosPorRoles(todos.toArray(new String[0]));
                break;
            }
            case "roles": {
                Set<String> validos = new HashSet<>();
                for (Roles r : new RolesDao().obtenerListaRoles()) validos.add(r.getNombreRoles());
                List<String> elegidos = new ArrayList<>();
                if (rolesElegidos != null) {
                    for (String r : Arrays.asList(rolesElegidos)) if (validos.contains(r)) elegidos.add(r);
                }
                if (elegidos.isEmpty()) errores.add("Elige al menos un rol.");
                else correos = dao.correosPorRoles(elegidos.toArray(new String[0]));
                break;
            }
            default:
                errores.add("Elige a quién enviar el comunicado.");
        }
        if (errores.isEmpty() && correos.isEmpty()) {
            errores.add("No hay destinatarios activos con correo para esa selección.");
        }
        if (errores.isEmpty() && correos.size() > MAX_DESTINATARIOS) {
            errores.add("Demasiados destinatarios (máximo " + MAX_DESTINATARIOS + " por envío).");
        }

        if (!errores.isEmpty()) {
            request.setAttribute("f_destino", destino);
            request.setAttribute("f_asunto", asunto);
            request.setAttribute("f_mensaje", mensaje);
            request.setAttribute("f_idCliente", idClienteStr);
            request.setAttribute("f_roles", rolesElegidos == null ? new String[0] : rolesElegidos);
            mostrar(request, response, errores);
            return;
        }

        UsuarioSesion u = (UsuarioSesion) request.getSession().getAttribute("usuario");
        int n = NotificadorCorreos.comunicado(correos, asunto, mensaje, u == null ? "un administrador" : u.getNombreCompleto());
        response.sendRedirect(request.getContextPath() + "/ComunicadosServlet?ok=" + n);
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response, ArrayList<String> errores)
            throws ServletException, IOException {
        request.setAttribute("errores", errores);
        request.setAttribute("clientes", new ClientesDao().getListaClientes());
        request.setAttribute("roles", new RolesDao().obtenerListaRoles());
        request.setAttribute("smtp", ConfigCorreo.estaConfigurado());
        request.getRequestDispatcher("comunicados.jsp").forward(request, response);
    }

    private String param(HttpServletRequest request, String nombre) {
        String v = request.getParameter(nombre);
        return v == null ? "" : v.trim();
    }
}
