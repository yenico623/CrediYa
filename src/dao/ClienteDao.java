
package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import modelos.Cliente;
import utilidades.dbConnection;
import java.sql.ResultSet;
import java.time.LocalDate;

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
            System.err.println("ERROR al buscar cliente "+ e.getMessage());
        }
        
        
        return clienteEncontrado;
    
    }
}
