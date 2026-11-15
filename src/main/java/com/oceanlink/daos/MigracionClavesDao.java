package com.oceanlink.daos;

import com.oceanlink.util.Seguridad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Convierte a hash las contraseñas que todavía estén en texto plano en la tabla usuarios. */
public class MigracionClavesDao extends DaoBase {

    /**
     * Cada usuario cuya password_hash no tenga el formato "pbkdf2$..." pasa a guardarse con hash.
     * Es seguro repetirlo: lo que ya está hasheado no se toca.
     * @return cuántas contraseñas se convirtieron
     */
    public int hashearPendientes() throws SQLException, ClassNotFoundException {
        Map<Integer, String> pendientes = new LinkedHashMap<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT idusuarios, password_hash FROM usuarios WHERE password_hash NOT LIKE 'pbkdf2$%'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String guardado = rs.getString(2);
                if (guardado != null && !guardado.isEmpty() && !Seguridad.esHash(guardado)) {
                    pendientes.put(rs.getInt(1), guardado);
                }
            }
        }
        int convertidas = 0;
        try (Connection conn = getConnection();
             PreparedStatement up = conn.prepareStatement(
                     "UPDATE usuarios SET password_hash = ? WHERE idusuarios = ? AND password_hash = ?")) {
            for (Map.Entry<Integer, String> e : pendientes.entrySet()) {
                up.setString(1, Seguridad.hash(e.getValue()));
                up.setInt(2, e.getKey());
                up.setString(3, e.getValue());   // solo si nadie la cambió mientras tanto
                convertidas += up.executeUpdate();
            }
        }
        return convertidas;
    }
}
