package com.oceanlink.servlets;

import com.oceanlink.beans.Clientes;
import com.oceanlink.daos.ClientesDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@WebServlet(name = "ClientesServlet", value = "/ClientesServlet")
public class ClientesServlet extends HttpServlet {

    private static final List<String> ESTADOS = Arrays.asList("Activo", "Inactivo");
    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+()\\-\\s]{6,20}$");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action") == null ? "lista" : request.getParameter("action");
        ClientesDao dao = new ClientesDao();

        switch (action) {
            case "formCrear":
                Clientes nuevo = new Clientes();
                nuevo.setEstado("Activo");
                mostrarFormulario(request, response, nuevo, "crear");
                break;
            case "editar":
                int id = leerId(request.getParameter("id"));
                Clientes existente = id > 0 ? dao.getClientes(id) : null;
                if (existente == null) {
                    response.sendRedirect(request.getContextPath() + "/ClientesServlet");
                } else {
                    mostrarFormulario(request, response, existente, "editar");
                }
                break;
            case "lista":
            default:
                request.setAttribute("listaClientes", dao.getListaClientes());
                request.getRequestDispatcher("listaClientes.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action") == null ? "lista" : request.getParameter("action");
        ClientesDao dao = new ClientesDao();

        switch (action) {
            case "guardar":
                guardar(request, response, dao);
                break;
            case "borrar":
                int id = leerId(request.getParameter("id"));
                boolean borrado = id > 0 && dao.borrarClientes(id);
                response.sendRedirect(request.getContextPath() + "/ClientesServlet?" + (borrado ? "ok=borrado" : "err=borrar"));
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/ClientesServlet");
        }
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response, ClientesDao dao)
            throws ServletException, IOException {
        ArrayList<String> errores = new ArrayList<>();

        Clientes c = new Clientes();
        c.setIdCliente(Math.max(leerId(request.getParameter("id")), 0));
        c.setEtiqueta(texto(request.getParameter("etiqueta")));
        c.setNombre(texto(request.getParameter("nombre")));
        c.setCorreo(texto(request.getParameter("correo")));
        c.setTelefono(texto(request.getParameter("telefono")));
        c.setEstado(texto(request.getParameter("estado")));

        if (c.getNombre() == null) {
            errores.add("El nombre del cliente es obligatorio.");
        } else if (c.getNombre().length() > 150) {
            errores.add("El nombre no puede pasar de 150 caracteres.");
        }
        if (c.getEtiqueta() != null && c.getEtiqueta().length() > 45) {
            errores.add("El identificador no puede pasar de 45 caracteres.");
        }
        if (c.getCorreo() != null && (c.getCorreo().length() > 150 || !CORREO.matcher(c.getCorreo()).matches())) {
            errores.add("El correo no tiene un formato válido.");
        }
        if (c.getTelefono() != null && !TELEFONO.matcher(c.getTelefono()).matches()) {
            errores.add("El teléfono solo puede tener números, espacios, + ( ) - (6 a 20 caracteres).");
        }
        if (c.getEstado() == null || !ESTADOS.contains(c.getEstado())) {
            errores.add("El estado debe ser Activo o Inactivo.");
        }

        boolean esNuevo = c.getIdCliente() == 0;

        if (errores.isEmpty()) {
            boolean ok = esNuevo
                    ? dao.crearClientes(c.getEtiqueta(), c.getNombre(), c.getCorreo(), c.getTelefono(), c.getEstado())
                    : dao.actualizarClientes(c.getIdCliente(), c.getEtiqueta(), c.getNombre(), c.getCorreo(), c.getTelefono(), c.getEstado());
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/ClientesServlet?ok=" + (esNuevo ? "creado" : "actualizado"));
                return;
            }
            errores.add("No se pudo guardar en la base de datos. Revisa la consola del servidor.");
        }

        request.setAttribute("errores", errores);
        mostrarFormulario(request, response, c, esNuevo ? "crear" : "editar");
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   Clientes c, String modo) throws ServletException, IOException {
        request.setAttribute("cliente", c);
        request.setAttribute("modo", modo);
        request.getRequestDispatcher("formClientes.jsp").forward(request, response);
    }

    private String texto(String valor) {
        if (valor == null) return null;
        valor = valor.trim();
        return valor.isEmpty() ? null : valor;
    }

    private int leerId(String valor) {
        try {
            return Integer.parseInt(valor == null ? "" : valor.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
