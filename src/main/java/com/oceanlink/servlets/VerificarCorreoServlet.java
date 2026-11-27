package com.oceanlink.servlets;

import com.oceanlink.services.VerificacionCorreo;
import com.oceanlink.util.Json;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Verificación de correo desde los formularios de usuarios y clientes (la llama verificar-correo.js).
 * POST accion=enviar  correo=...             -> valida formato y dominio y envía un código de 6 dígitos al correo
 * POST accion=confirmar correo=... codigo=... -> comprueba el código
 * Responde JSON: {"ok":true|false,"mensaje":"...","sinCodigo":true|false}
 */
@WebServlet(name = "VerificarCorreoServlet", value = "/VerificarCorreoServlet")
public class VerificarCorreoServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        String correo = request.getParameter("correo");

        boolean ok;
        String mensaje;
        boolean sinCodigo = false;
        if ("enviar".equals(accion)) {
            VerificacionCorreo.Resultado r = VerificacionCorreo.enviarCodigo(request.getSession(), correo,
                    "usuario".equals(request.getParameter("tipo")) ? com.oceanlink.util.ConfigCorreo.dominiosUsuarios() : null);
            ok = r.ok;
            mensaje = r.mensaje;
            sinCodigo = r.sinCodigo;
        } else if ("confirmar".equals(accion)) {
            VerificacionCorreo.Resultado r = VerificacionCorreo.confirmar(request.getSession(), correo, request.getParameter("codigo"));
            ok = r.ok;
            mensaje = r.mensaje;
        } else {
            ok = false;
            mensaje = "Acción no válida.";
        }

        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write("{\"ok\":" + ok + ",\"mensaje\":" + Json.texto(mensaje) + ",\"sinCodigo\":" + sinCodigo + "}");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
