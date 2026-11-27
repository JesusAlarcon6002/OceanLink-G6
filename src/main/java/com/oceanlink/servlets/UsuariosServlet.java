package com.oceanlink.servlets;

import com.oceanlink.beans.Usuarios;
import com.oceanlink.daos.RolesDao;
import com.oceanlink.daos.UsuariosDao;
import com.oceanlink.services.NotificadorCorreos;
import com.oceanlink.services.VerificacionCorreo;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "UsuariosServlet", value = "/UsuariosServlet")

public class UsuariosServlet  extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action")==null ? "lista":request.getParameter("action");

        request.setAttribute("soloLectura", "ver".equals(action)); // action=ver: el mismo formulario de editar, pero solo para mirar
        UsuariosDao usuariosDao = new UsuariosDao();
        RolesDao rolesDao = new RolesDao();
        RequestDispatcher view;

        switch (action){
            case "lista":
                ArrayList<Usuarios> listaUsuarios = usuariosDao.obtenerListaUsuarios();
                request.setAttribute("lista", listaUsuarios);

                view = request.getRequestDispatcher("listaUsuarios.jsp");
                view.forward(request, response);
                break;
            case "formCrear":
                // Los usuarios nuevos ya no los crea el admin a mano: llegan por "Solicitudes de registro"
                response.sendRedirect(request.getContextPath() + "/SolicitudesRegistroServlet");
                break;
            case "ver":
            case "editar":
                String Strg = request.getParameter("id");
                int idUsuario = Integer.parseInt(Strg);
                Usuarios Usuarios =usuariosDao.obtenerUsuarios(idUsuario);
                if(Usuarios==null){
                    response.sendRedirect(request.getContextPath()+"/UsuariosServlet");
                }else {
                    request.setAttribute("usuarios",Usuarios);
                    request.setAttribute("listaRoles", rolesDao.obtenerListaRoles());
                    view = request.getRequestDispatcher("editarUsuarios.jsp");
                    view.forward(request, response);
                }
                break;
            case "borrar":
                // Borrar ya no se hace por GET (un enlace/URL no debe poder eliminar datos): solo por POST
                response.sendRedirect(request.getContextPath() + "/UsuariosServlet");
                break;
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action") ==null ? "lista":request.getParameter("action");
        UsuariosDao usuariosDao = new UsuariosDao();
        RequestDispatcher view;

        switch (action){
            case "borrar": {
                int idBorrar = Integer.parseInt(request.getParameter("id"));
                // un usuario no puede desactivarse a sí mismo: se quedaría sin acceso al sistema
                com.oceanlink.beans.UsuarioSesion actual = (com.oceanlink.beans.UsuarioSesion) request.getSession().getAttribute("usuario");
                if (actual != null && actual.getIdUsuario() == idBorrar) {
                    response.sendRedirect(request.getContextPath() + "/UsuariosServlet");
                    break;
                }
                Usuarios aDesactivar = usuariosDao.obtenerUsuarios(idBorrar);
                if (aDesactivar != null) {
                    usuariosDao.borrarUsuarios(idBorrar);
                    if (!"Inactivo".equals(aDesactivar.getEstado())) {
                        NotificadorCorreos.cuentaCambioEstado(aDesactivar.getNombre(), aDesactivar.getCorreo(), "Inactivo");
                    }
                }
                response.sendRedirect(request.getContextPath() + "/UsuariosServlet");
                break;
            }
            case "crear":
                String idUsuarioStr = request.getParameter("id");
                String etiqueta = request.getParameter("etiqueta");
                String nombre = request.getParameter("nombre");
                String apellido = request.getParameter("apellido");
                String correo = request.getParameter("correo");
                String passwd = request.getParameter("passwd");
                String telefono = request.getParameter("telefono");
                String estado = request.getParameter("estado");
                String idRolStr = request.getParameter("idRol");

                int idUsuario = (idUsuarioStr != null && !idUsuarioStr.isEmpty()) ? Integer.parseInt(idUsuarioStr) : 0;
                int idRol = (idRolStr != null && !idRolStr.isEmpty()) ? Integer.parseInt(idRolStr) : 0;

                Usuarios usuario = usuariosDao.obtenerUsuarios(idUsuario);
                boolean esNuevoUsuario = usuario == null;
                if (esNuevoUsuario) {
                    // crear usuarios directamente está desactivado (solo por solicitud de registro)
                    response.sendRedirect(request.getContextPath() + "/SolicitudesRegistroServlet");
                    break;
                }

                // ---- validación
                ArrayList<String> errores = new ArrayList<>();
                etiqueta = etiqueta == null ? "" : etiqueta.trim();
                correo = correo == null ? "" : correo.trim();
                if (etiqueta.isEmpty()) errores.add("El usuario es obligatorio.");
                if (idRol <= 0) errores.add("Elige un rol.");
                if (esNuevoUsuario && (passwd == null || passwd.isEmpty())) {
                    errores.add("La contraseña es obligatoria.");
                } else if (passwd != null && !passwd.isEmpty()) {
                    // política de contraseñas (también al cambiarla en la edición)
                    String errorClave = com.oceanlink.util.Seguridad.validarClave(passwd);
                    if (errorClave != null) errores.add(errorClave);
                    String confirmacion = request.getParameter("passwd2");
                    if (confirmacion != null && !confirmacion.equals(passwd)) errores.add("Las contraseñas no coinciden.");
                }
                if (correo.isEmpty()) {
                    errores.add("El correo es obligatorio.");
                } else {
                    // el correo se verifica (dominio real + código) solo si es nuevo o cambió
                    boolean cambioCorreo = esNuevoUsuario || usuario.getCorreo() == null
                            || !usuario.getCorreo().trim().equalsIgnoreCase(correo);
                    if (cambioCorreo) {
                        String errorCorreo = VerificacionCorreo.errorAlGuardar(request.getSession(), correo, com.oceanlink.util.ConfigCorreo.dominiosUsuarios());
                        if (errorCorreo != null) errores.add(errorCorreo);
                    }
                }
                if (errores.isEmpty()) {
                    // se guarda aquí; si la BD rechaza (correo o usuario repetido) se vuelve al formulario con el mensaje
                    String errorGuardar = esNuevoUsuario
                            ? usuariosDao.crearUsuarios(etiqueta, nombre, apellido, correo, passwd, telefono, String.valueOf(idRol))
                            : usuariosDao.actualizarUsuarios(idUsuario, etiqueta, nombre, apellido, correo, telefono, idRol, passwd, estado);
                    if (errorGuardar != null) errores.add(errorGuardar);
                }
                if (!errores.isEmpty()) {
                    Usuarios devuelto = new Usuarios();
                    devuelto.setIdUsuario(idUsuario);
                    devuelto.setEtiquetaUsuario(etiqueta);
                    devuelto.setNombre(nombre);
                    devuelto.setApellido(apellido);
                    devuelto.setCorreo(correo);
                    devuelto.setTelefono(telefono);
                    devuelto.setEstado(estado);
                    com.oceanlink.beans.Roles rolDevuelto = new com.oceanlink.beans.Roles();
                    rolDevuelto.setIdRoles(idRol);
                    devuelto.setIdroles(rolDevuelto);
                    request.setAttribute("errores", errores);
                    request.setAttribute("usuarios", devuelto);
                    request.setAttribute("listaRoles", new RolesDao().obtenerListaRoles());
                    request.getRequestDispatcher(esNuevoUsuario ? "nuevoUsuarios.jsp" : "editarUsuarios.jsp").forward(request, response);
                    break;
                }

                if (esNuevoUsuario) {
                    String rolNombre = "";
                    for (com.oceanlink.beans.Roles r : new RolesDao().obtenerListaRoles()) {
                        if (r.getIdRoles() == idRol) rolNombre = r.getNombreRoles();
                    }
                    String completo = ((nombre == null ? "" : nombre) + " " + (apellido == null ? "" : apellido)).trim();
                    NotificadorCorreos.usuarioCreado(completo.isEmpty() ? etiqueta : completo, correo, etiqueta, rolNombre);
                } else {
                    // correos de seguridad (al correo que tenía guardado: si lo cambió, el aviso va al nuevo)
                    if (passwd != null && !passwd.isEmpty()) {
                        NotificadorCorreos.contrasenaCambiada(nombre, correo);
                    }
                    if (estado != null && usuario.getEstado() != null && !estado.equals(usuario.getEstado())) {
                        NotificadorCorreos.cuentaCambioEstado(nombre, correo, estado);
                    }
                }
                VerificacionCorreo.olvidar(request.getSession(), correo);
                response.sendRedirect(request.getContextPath() + "/UsuariosServlet");
                break;
        }
    }
}
