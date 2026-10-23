package com.oceanlink.daos;
import com.oceanlink.beans.Roles;
import com.oceanlink.beans.Usuarios;
import java.sql.*;
import java.util.ArrayList;

public class UsuariosDao extends DaoBase {
    public ArrayList<Usuarios> obtenerListaUsuarios(){
        ArrayList<Usuarios> listaUsuarios = new ArrayList<>();

        try{

            Connection conn = getConnection();
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

                usr.setIdroles(roles);

                listaUsuarios.add(usr);
            }

        } catch(ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
        return listaUsuarios;
    }

    public void crearUsuarios(String etiqueta, String nombre, String apellido, String correo, String passwd,String telefono,String rol){
        try{

            try(Connection conn = getConnection();){
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

            try(Connection conn = getConnection();
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

                        usuarios.setIdroles(roles);
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
        return usuarios;
    }

    public void actualizarUsuarios(int idUsuario, String etiqueta, String nombre, String apellido, String correo, String telefono, int rol, String passwd, String estado) {
        try {

            try (Connection conn = getConnection();) {
                String sql = "UPDATE usuarios SET etiqueta_usuario = ?, nombre = ?, apellido = ?, correo = ?, telefono = ?,password_hash = COALESCE(NULLIF(?, ''), password_hash),roles_idroles = ?, estado = ? "
                        + "WHERE idusuarios = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, etiqueta);
                    pstmt.setString(2, nombre);
                    pstmt.setString(3, apellido);
                    pstmt.setString(4, correo);
                    pstmt.setString(5, telefono);
                    pstmt.setString(6, passwd);
                    pstmt.setInt(7, rol);
                    pstmt.setString(8, estado);
                    pstmt.setInt(9, idUsuario);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void borrarUsuarios(int idUsuario) {
        try {

            try (Connection conn = getConnection()) {
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
