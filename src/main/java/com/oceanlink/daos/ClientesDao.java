package com.oceanlink.daos;

import com.oceanlink.beans.Clientes;

import java.sql.*;
import java.util.ArrayList;

public class ClientesDao {
    public ArrayList<Clientes> getListaClientes(){
        ArrayList<Clientes> listaClientes = new ArrayList();
        try{
            String user = "root";
            String pass = "12345678ABCD"; /*DEPENDE DE LA CONTRASEÑA DE TU WORKBENCH*/
            String url = "jdbc:mysql://localhost:3306/proyecto_siete";

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url,user,pass);
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT id_clientes," +
                    " nombres," +
                    " correos," +
                    " telefonos," +
                    " estado," +
                    " etiqueta_cliente" +
                    " FROM clientes");

            while(rs.next()){
                Clientes clientes = new Clientes();
                clientes.setIdCliente(rs.getInt("id_clientes"));
                clientes.setNombre(rs.getString("nombres"));
                clientes.setCorreo(rs.getString("correos"));
                clientes.setEstado(rs.getString("estado"));
                clientes.setTelefono(rs.getString("telefonos"));
                clientes.setEtiqueta(rs.getString("etiqueta_cliente"));
                listaClientes.add(clientes);
            }
        }catch(ClassNotFoundException | SQLException e){
            e.printStackTrace();
        } return listaClientes;
    }

    public void crearClientes(String etiqueta, String nombre, String correo, String telefono, String estado) {
        try {
            String user = "root";
            String pass = "12345678ABCD";
            String url = "jdbc:mysql://localhost:3306/proyecto_siete";

            Class.forName("com.mysql.cj.jdbc.Driver");

            try(Connection conn = DriverManager.getConnection(url,user,pass);){
                String sql = "INSERT INTO clientes(etiqueta_cliente,nombres,correos,telefonos,estado) VALUES(?,?,?,?,?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)){
                    pstmt.setString(1, etiqueta);
                    pstmt.setString(2, nombre);
                    pstmt.setString(3, correo);
                    pstmt.setString(4, telefono);
                    pstmt.setString(5, estado);
                    pstmt.executeUpdate();
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public Clientes getClientes(int idCliente){
        Clientes clientes = null;
        try{
            String user = "root";
            String pass = "12345678ABCD";
            String url = "jdbc:mysql://localhost:3306/proyecto_siete";

            Class.forName("com.mysql.cj.jdbc.Driver");
            try(Connection conn = DriverManager.getConnection(url,user,pass);
                PreparedStatement pstmt = conn.prepareStatement("SELECT id_clientes," +
                        " nombres," +
                        " correos," +
                        " telefonos," +
                        " etiqueta_cliente," +
                        " estado" +
                        " FROM clientes" +
                        " WHERE id_clientes=?;");){
                pstmt.setInt(1, idCliente);
                try(ResultSet rs = pstmt.executeQuery()){
                    if(rs.next()){
                        clientes = new Clientes();
                        clientes.setIdCliente(rs.getInt("id_clientes"));
                        clientes.setNombre(rs.getString("nombres"));
                        clientes.setCorreo(rs.getString("correos"));
                        clientes.setTelefono(rs.getString("telefonos"));
                        clientes.setEtiqueta(rs.getString("etiqueta_cliente"));
                        clientes.setEstado(rs.getString("estado"));
                    }
                }
            }
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
        return clientes;
    }

    public void actualizarClientes(int idCliente, String etiqueta, String nombre, String correo, String telefono, String estado) {
        try {
            String user = "root";
            String pass = "12345678ABCD";
            String url = "jdbc:mysql://127.0.0.1:3306/proyecto_siete";

            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, pass);) {
               String sql = "UPDATE clientes SET etiqueta_clientes=?, nombres = ?, correos = ?, telefono = ?, estado =?" +
                       " WHERE id_clientes = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)){
                    pstmt.setString(1, etiqueta);
                    pstmt.setString(2, nombre);
                    pstmt.setString(3, correo);
                    pstmt.setString(4, telefono);
                    pstmt.setString(5, estado);
                    pstmt.setInt(6, idCliente);
                    pstmt.executeUpdate();
                }
            }
        }catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void borrarClientes(int idCliente) {
        try {
            String user = "root";
            String pass = "12345678ABCD";
            String url = "jdbc:mysql://127.0.0.1:3306/proyecto_siete";

            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                String sql = "DELETE FROM clientes WHERE id_clientes = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, idCliente);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
