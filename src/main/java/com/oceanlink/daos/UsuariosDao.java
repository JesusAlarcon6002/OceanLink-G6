package com.oceanlink.daos;
import com.oceanlink.beans.Roles;
import com.oceanlink.beans.Usuarios;
import java.sql.*;
import java.util.ArrayList;

public class UsuariosDao {
    public ArrayList<Usuarios> obtenerListaUsuarios(){
        ArrayList<Usuarios> listaUsuarios = new ArrayList<>();

        try{
            String user = "root";
            String pass = "12345678ABCD"; /*DEPENDE DE LA CONTRASEÑA DE TU WORKBENCH*/
            String url = "jdbc:mysql://localhost:3306/proyecto_siete";

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url,user,pass);
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT usr.idusuarios," +
                    " usr.etiqueta_usuario AS 'Etiqueta'," +
                    " CONCAT(usr.nombre,' ',usr.apellido) AS 'Nombre'," +
                    " usr.telefono AS 'Telefono'," +
                    " usr.correo AS 'Correo'," +
                    " rl.id_roles AS 'idRoles'," +
                    " rl.nombres_rol AS 'Rol'," +
                    " usr.estado AS 'Estado'" +
                    " FROM usuarios usr" +
                    " INNER JOIN" +
                    " roles rl ON usr.roles_idroles = rl.id_roles");

            while (rs.next()){
                Usuarios usr = new Usuarios();
                usr.setIdUsuario(rs.getInt("idusuarios"));
                usr.setEtiquetaUsuario(rs.getString("Etiqueta"));
                usr.setNombreCompleto(rs.getString("Nombre"));
                usr.setTelefono(rs.getString("Telefono"));
                usr.setCorreo(rs.getString("Correo"));
                usr.setEstado(rs.getString("Estado"));

                Roles roles = new Roles();
                roles.setNombreRoles(rs.getString("Rol"));
                roles.setIdRoles(rs.getInt("idRoles"));

                listaUsuarios.add(usr);
            }

        } catch(ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
        return listaUsuarios;
    }

    public void crearUsuarios(String etiqueta, String nombre, String apellido, String correo, String passwd,String telefono,String rol){
        try{
            String user = "root";
            String pass = "12345678ABCD";
            String url = "jdbc:mysql://localhost:3306/proyecto_siete";

            Class.forName("com.mysql.cj.jdbc.Driver");
            try(Connection conn = DriverManager.getConnection(url,user,pass);){
                String sql = "INSERT INTO usuarios (etiqueta_usuario, nombre, apellido,correo, password_hash,telefono,roles_idroles) VALUES (?,?,?,?,?,?,?)";
                try(PreparedStatement pstmt = conn.prepareStatement(sql)){
                    pstmt.setString(1,etiqueta);
                    pstmt.setString(2,nombre);
                    pstmt.setString(3,apellido);
                    pstmt.setString(4,correo);
                    pstmt.setString(5,passwd);
                    pstmt.setString(6,telefono);
                    pstmt.setString(7,rol);
                    pstmt.executeUpdate();
                }
            }
        } catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
    }

    public Usuarios obtenerUsuarios(int idUsuario){
        Usuarios usuarios = null;
        try{
            String user = "root";
            String pass = "12345678ABCD";
            String url = "jdbc:mysql://localhost:3306/proyecto_siete";

            Class.forName("com.mysql.cj.jdbc.Driver");
            try(Connection conn = DriverManager.getConnection(url,user,pass);
            PreparedStatement pstmt = conn.prepareStatement("SELECT usr.idusuarios," +
                    " usr.etiqueta_usuario AS 'Etiqueta'," +
                    " usr.nombre AS Nombre," +
                    " usr.apellido AS 'Apellido'," +
                    " usr.telefono AS 'Telefono'," +
                    " usr.correo AS 'Correo'," +
                    " rl.id_roles AS 'idRoles'," +
                    " rl.nombres_rol AS 'Rol'," +
                    " usr.estado AS 'Estado'" +
                    " FROM usuarios usr" +
                    " INNER JOIN" +
                    " roles rl ON usr.roles_idroles = rl.id_roles" +
                    " WHERE usr.idusuarios = ?;");) {
                pstmt.setInt(1,idUsuario);

                try(ResultSet rs = pstmt.executeQuery()){
                    if(rs.next()){
                        usuarios = new Usuarios();
                        usuarios.setIdUsuario(rs.getInt("idusuarios"));
                        usuarios.setEtiquetaUsuario(rs.getString("Etiqueta"));
                        usuarios.setNombre(rs.getString("Nombre"));
                        usuarios.setApellido(rs.getString("Apellido"));
                        usuarios.setTelefono(rs.getString("Telefono"));
                        usuarios.setCorreo(rs.getString("Correo"));
                        usuarios.setEstado(rs.getString("Estado"));
                        Roles roles = new Roles();
                        roles.setIdRoles(rs.getInt("idRoles"));
                        roles.setNombreRoles(rs.getString("Rol"));
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
        return usuarios;
    }

    public void actualizarUsuarios(int idUsuario, String etiqueta, String nombre, String apellido, String correo, String telefono, int rol) {
        try {
            String user = "root";
            String pass = "12345678ABCD";
            String url = "jdbc:mysql://127.0.0.1:3306/proyecto_siete";

            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, pass);) {
                String sql = "UPDATE usuarios SET etiqueta_usuario = ?, nombre = ?, apellido = ?, correo = ?, telefono = ?, roles_idroles = ? "
                        + "WHERE idusuarios = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, etiqueta);
                    pstmt.setString(2, nombre);
                    pstmt.setString(3, apellido);
                    pstmt.setString(4, correo);
                    pstmt.setString(5, telefono);
                    pstmt.setInt(6, rol);
                    pstmt.setInt(7, idUsuario);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void borrarUsuarios(int idUsuario) {
        try {
            String user = "root";
            String pass = "12345678ABCD";
            String url = "jdbc:mysql://127.0.0.1:3306/proyecto_siete";

            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                String sql = "DELETE FROM usuarios WHERE idusuarios = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, idUsuario);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
