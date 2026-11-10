
package dao;

import modelos.Empleado;
import modelos.Usuario;
import utilidades.dbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmpleadoDao {

    public Empleado obtenerPorUsuarioId(int usuarioId) throws SQLException {
        String sql = "SELECT e.id AS empleado_id, p.id AS persona_id, "
                   + "p.nombre, p.documento, p.correo, p.telefono, "
                   + "p.fecha_nacimiento, e.salario, "
                   + "u.id AS usuario_id, u.username, u.rol "
                   + "FROM empleados e "
                   + "INNER JOIN personas p ON e.persona_id = p.id "
                   + "INNER JOIN usuarios u ON p.usuario_id = u.id "
                   + "WHERE u.id = ? AND u.rol = 'EMPLEADO'";

        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt("usuario_id"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setRol(rs.getString("rol"));

                    Empleado empleado = new Empleado();
                    empleado.setId(rs.getInt("persona_id"));
                    empleado.setIdEmpleado(rs.getInt("empleado_id"));
                    empleado.setUsuario(usuario);
                    empleado.setNombre(rs.getString("nombre"));
                    empleado.setDocumento(rs.getString("documento"));
                    empleado.setCorreo(rs.getString("correo"));
                    empleado.setTelefono(rs.getString("telefono"));
                    empleado.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
                    empleado.setSalario(rs.getDouble("salario"));

                    return empleado;
                }
            }
        }

        return null;
    }
}
