
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
import java.sql.Statement;

public class ClienteDao {
    public Cliente buscarCliente(String documento) throws SQLException{
        Cliente clienteEncontrado= null;
        String sql = "SELECT p.id as persona_id, p.nombre, p.documento, p.correo, p.telefono, p.fecha_nacimiento"
                + "c.id as cliente_id"
                + "FROM personas p"
                + "INNER JOIN clientes c ON p.id = c.persona_id"
                + "WHERE p.documento = ?";
        
        try (Connection con = dbConnection.getInstancia().getConexion();
            PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, documento);
            
            try(ResultSet rs = ps.executeQuery()){
            
                if (rs.next()){
                LocalDate fechaNacimiento = rs.getDate("fecha_nacimiento").toLocalDate();
                clienteEncontrado = new Cliente(
                  rs.getInt("persona_id"),
                  null,
                  rs.getString("nombre"),
                  rs.getString("documento"),
                  rs.getString("correo"),
                  rs.getString("telefono"),
                  fechaNacimiento
                );
                }
            }
        }catch (SQLException e){
            System.err.println(GestorErrores.mostrarError(e));
        }
        
        
        return clienteEncontrado;
    
    }
    
    public List<Cliente> listarClientePorEmpleado(int empleadoId) throws SQLException{
        List<Cliente> listaCliente = new ArrayList<>();
        
        String sql = " SELECT id, nombre, documento FROM clientes WHERE empleado_id = ?";
        
        try (Connection con = dbConnection.getInstancia().getConexion();
            PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1, empleadoId);
            
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    Cliente cliente = new Cliente();
                    
                    cliente.setId(rs.getInt("id"));
                    cliente.setNombre(rs.getString("nombre"));
                    cliente.setDocumento(rs.getString("documento"));
                    
                    listaCliente.add(cliente);
                    
                }
            }catch (SQLException e){
                System.out.println(GestorErrores.mostrarError(e));
            }
        
        }
        return listaCliente;               
    }
    
    public boolean registrarCliente(Cliente cliente, int idEmpleadoAsesor) throws SQLException {
        String sqlPersona = "INSERT INTO personas (documento, nombre, correo, telefono, fecha_nacimiento) VALUES (?, ?, ?, ?, ?)";
        String sqlCliente = "INSERT INTO clientes (persona_id, empleado_id) VALUES (?,?)";
        
        try (Connection con = dbConnection.getInstancia().getConexion()) {
            con.setAutoCommit(false);
            
            try (PreparedStatement psPersona = con.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {
                psPersona.setString(1, cliente.getDocumento());
                psPersona.setString(2, cliente.getNombre());
                psPersona.setString(3, cliente.getCorreo());
                psPersona.setString(4, cliente.getTelefono());
                psPersona.setDate(5, java.sql.Date.valueOf(cliente.getFechaNacimiento()));
             
                int filasPersona = psPersona.executeUpdate();
                if (filasPersona > 0) {
                    try (ResultSet rs = psPersona.getGeneratedKeys()) {
                        if (rs.next()) {
                            int personaId = rs.getInt(1);
                            
                            try (PreparedStatement psCliente = con.prepareStatement(sqlCliente)) {
                                psCliente.setInt(1, personaId);
                                psCliente.setInt(2, idEmpleadoAsesor);
                                psCliente.executeUpdate();
                            }
                        }
                    }
                }
                con.commit();
                return true;
                
            } catch (SQLException e) {
                // CATCH INTERNO: Si falla el SQL, hacemos rollback
                con.rollback();
                System.err.println(GestorErrores.mostrarError(e));
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println(GestorErrores.mostrarError(e));
            return false;
        }
    }
    
   
    
}
