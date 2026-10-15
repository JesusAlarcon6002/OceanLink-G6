package com.oceanlink.servlets;

import com.oceanlink.beans.Usuarios;
import com.oceanlink.daos.RolesDao;
import com.oceanlink.daos.UsuariosDao;
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
                request.setAttribute("listaRoles", rolesDao.obtenerListaRoles());
                view = request.getRequestDispatcher("nuevoUsuarios.jsp");
                view.forward(request, response);
                break;
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
                String idStr = request.getParameter("id");
                idUsuario = Integer.parseInt(idStr);
                if (usuariosDao.obtenerUsuarios(idUsuario) != null) {
                    usuariosDao.borrarUsuarios(idUsuario);
                }
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
            case "crear":
                String idUsuarioStr = request.getParameter("id");
                String etiqueta = request.getParameter("etiqueta");
                String nombre = request.getParameter("nombre");
                String apellido = request.getParameter("apellido");
                String correo = request.getParameter("correo");
                String passwd = request.getParameter("passwd");
                String telefono = request.getParameter("telefono");

                String idRolStr = request.getParameter("idRol");

                int idUsuario = (idUsuarioStr != null && !idUsuarioStr.isEmpty()) ? Integer.parseInt(idUsuarioStr) : 0;
                int idRol = (idRolStr != null && !idRolStr.isEmpty()) ? Integer.parseInt(idRolStr) : 0;

                Usuarios usuario = usuariosDao.obtenerUsuarios(idUsuario);

                if (usuario == null) {
                    usuariosDao.crearUsuarios(etiqueta, nombre, apellido, correo, passwd, telefono, String.valueOf(idRol));
                } else {
                    usuariosDao.actualizarUsuarios(idUsuario, etiqueta, nombre, apellido, correo, telefono, idRol,passwd);
                }
                response.sendRedirect(request.getContextPath() + "/UsuariosServlet");
                break;
        }
    }
}
