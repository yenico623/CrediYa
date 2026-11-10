
package dao;

import modelos.Empleado;
import modelos.Usuario;
import utilidades.dbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import utilidades.GestorErrores;

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

    public List<Empleado> listarEmpleados() throws SQLException {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT e.id AS empleado_id, p.id AS persona_id, p.nombre, p.documento, p.correo, p.telefono, e.salario "
                   + "FROM empleados e INNER JOIN personas p ON e.persona_id = p.id ORDER BY e.id";

        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Empleado empleado = new Empleado();
                empleado.setId(rs.getInt("persona_id"));
                empleado.setIdEmpleado(rs.getInt("empleado_id"));
                empleado.setNombre(rs.getString("nombre"));
                empleado.setDocumento(rs.getString("documento"));
                empleado.setCorreo(rs.getString("correo"));
                empleado.setTelefono(rs.getString("telefono"));
                empleado.setSalario(rs.getDouble("salario"));
                lista.add(empleado);
            }
        }

        return lista;
    }

    public Empleado buscarPorDocumento(String documento) throws SQLException {
        String sql = "SELECT e.id AS empleado_id, p.id AS persona_id, p.nombre, p.documento, p.correo, p.telefono, "
                   + "p.fecha_nacimiento, e.salario "
                   + "FROM empleados e INNER JOIN personas p ON e.persona_id = p.id "
                   + "WHERE p.documento = ?";

        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, documento);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Empleado empleado = new Empleado();
                    empleado.setId(rs.getInt("persona_id"));
                    empleado.setIdEmpleado(rs.getInt("empleado_id"));
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

    public boolean registrarEmpleado(Empleado empleado, String username, String password) {
        String sqlUsuario = "INSERT INTO usuarios (username, password, rol) VALUES (?, ?, 'EMPLEADO')";
        String sqlPersona = "INSERT INTO personas (usuario_id, documento, nombre, correo, telefono, fecha_nacimiento) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlEmpleado = "INSERT INTO empleados (persona_id, salario) VALUES (?, ?)";

        Connection con = dbConnection.getInstancia().getConexion();

        try {
            con.setAutoCommit(false);

            int usuarioId = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        usuarioId = rs.getInt(1);
                    }
                }
            }

            int personaId = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, usuarioId);
                ps.setString(2, empleado.getDocumento());
                ps.setString(3, empleado.getNombre());
                ps.setString(4, empleado.getCorreo());
                ps.setString(5, empleado.getTelefono());
                ps.setDate(6, java.sql.Date.valueOf(empleado.getFechaNacimiento()));
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        personaId = rs.getInt(1);
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlEmpleado)) {
                ps.setInt(1, personaId);
                ps.setDouble(2, empleado.getSalario());
                ps.executeUpdate();
            }

            con.commit();
            con.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            try {
                con.rollback();
                con.setAutoCommit(true);
            } catch (SQLException ex) {}

            System.err.println(GestorErrores.mostrarError(e));
            return false;
        }
    }

    public boolean actualizarEmpleado(String documento, String correo, String telefono, double salario) throws SQLException {
        String sqlPersona = "UPDATE personas SET correo = ?, telefono = ? WHERE documento = ?";
        String sqlEmpleado = "UPDATE empleados SET salario = ? WHERE persona_id = (SELECT id FROM personas WHERE documento = ?)";

        Connection con = dbConnection.getInstancia().getConexion();

        try {
            con.setAutoCommit(false);

            int filas;
            try (PreparedStatement ps = con.prepareStatement(sqlPersona)) {
                ps.setString(1, correo);
                ps.setString(2, telefono);
                ps.setString(3, documento);
                filas = ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(sqlEmpleado)) {
                ps.setDouble(1, salario);
                ps.setString(2, documento);
                ps.executeUpdate();
            }

            con.commit();
            con.setAutoCommit(true);
            return filas > 0;

        } catch (SQLException e) {
            con.rollback();
            con.setAutoCommit(true);
            throw e;
        }
    }
}
