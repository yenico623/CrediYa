package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import modelos.Cliente;
import utilidades.dbConnection;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import utilidades.GestorErrores;

public class ClienteDao {

    public Cliente buscarClientePorId(int clienteId) throws SQLException {
        Cliente clienteEncontrado = null;
        String sql = "SELECT p.id as persona_id, p.nombre, p.documento, p.correo, p.telefono, p.fecha_nacimiento, c.id as cliente_id "
                   + "FROM personas p "
                   + "INNER JOIN clientes c ON p.id = c.persona_id "
                   + "WHERE c.id = ?";
        
        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalDate fechaNacimiento = rs.getDate("fecha_nacimiento").toLocalDate();
                    clienteEncontrado = new Cliente(
                        rs.getInt("cliente_id"),
                        null,
                        rs.getString("nombre"),
                        rs.getString("documento"),
                        rs.getString("correo"),
                        rs.getString("telefono"),
                        fechaNacimiento
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println(GestorErrores.mostrarError(e));
        }
        return clienteEncontrado;
    }
    
    public Cliente buscarCliente(String documento) throws SQLException {
        Cliente clienteEncontrado = null;
        String sql = "SELECT p.id as persona_id, p.nombre, p.documento, p.correo, p.telefono, p.fecha_nacimiento, c.id as cliente_id "
                   + "FROM personas p "
                   + "INNER JOIN clientes c ON p.id = c.persona_id "
                   + "WHERE p.documento = ?";
        
        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, documento);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalDate fechaNacimiento = rs.getDate("fecha_nacimiento").toLocalDate();
                    clienteEncontrado = new Cliente(
                        rs.getInt("cliente_id"),
                        null,
                        rs.getString("nombre"),
                        rs.getString("documento"),
                        rs.getString("correo"),
                        rs.getString("telefono"),
                        fechaNacimiento
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println(GestorErrores.mostrarError(e));
        }
        return clienteEncontrado;
    }
    
    public List<Cliente> listarClientes() throws SQLException {
        List<Cliente> listaCliente = new ArrayList<>();
        String sql = "SELECT c.id, p.nombre, p.documento FROM clientes c INNER JOIN personas p ON c.persona_id = p.id";
        
        Connection con = dbConnection.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setId(rs.getInt("id"));
                cliente.setNombre(rs.getString("nombre"));
                cliente.setDocumento(rs.getString("documento"));
                listaCliente.add(cliente);
            }
        }
        return listaCliente;
    }
    
    public boolean registrarCliente(Cliente cliente, int idEmpleadoAsesor) {
        String sqlUsuario = "INSERT INTO usuarios (username, password, rol) VALUES (?, ?, 'CLIENTE')";
        String sqlPersona = "INSERT INTO personas (usuario_id, documento, nombre, correo, telefono, fecha_nacimiento) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlCliente = "INSERT INTO clientes (persona_id, empleado_id) VALUES (?, ?)";
        
        Connection con = utilidades.dbConnection.getInstancia().getConexion();
        try {
            con.setAutoCommit(false);
            
            int usuarioId = 0;
            try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                psUsuario.setString(1, cliente.getDocumento());
                psUsuario.setString(2, cliente.getDocumento());
                psUsuario.executeUpdate();
                try (ResultSet rs = psUsuario.getGeneratedKeys()) {
                    if (rs.next()) {
                        usuarioId = rs.getInt(1);
                    }
                }
            }

            int personaId = 0;
            try (PreparedStatement psPersona = con.prepareStatement(sqlPersona, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                psPersona.setInt(1, usuarioId);
                psPersona.setString(2, cliente.getDocumento());
                psPersona.setString(3, cliente.getNombre());
                psPersona.setString(4, cliente.getCorreo());
                psPersona.setString(5, cliente.getTelefono());
                psPersona.setDate(6, java.sql.Date.valueOf(cliente.getFechaNacimiento()));
             
                int filasPersona = psPersona.executeUpdate();
                if (filasPersona > 0) {
                    try (ResultSet rs = psPersona.getGeneratedKeys()) {
                        if (rs.next()) {
                            personaId = rs.getInt(1);
                        }
                    }
                }
            }

            try (PreparedStatement psCliente = con.prepareStatement(sqlCliente)) {
                psCliente.setInt(1, personaId);
                psCliente.setInt(2, idEmpleadoAsesor);
                psCliente.executeUpdate();
            }

            con.commit();
            con.setAutoCommit(true);
            return true;
            
        } catch (SQLException e) {
            try { 
                con.rollback(); 
                con.setAutoCommit(true); 
            } catch (SQLException ex) {}
            
            System.err.println(utilidades.GestorErrores.mostrarError(e));
            return false;
        }
    }
    
    public boolean actualizarContacto(String documento, String nuevoCorreo, String nuevoTelefono) throws SQLException {
        String sql = "UPDATE personas SET correo = ?, telefono = ? WHERE documento = ?";
        
        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, nuevoCorreo);
            ps.setString(2, nuevoTelefono);
            ps.setString(3, documento);
            
            return ps.executeUpdate() > 0;
        }
    }
}