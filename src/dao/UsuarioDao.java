
package dao;
import modelos.Usuario;
import utilidades.dbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utilidades.GestorErrores;
public class UsuarioDao {
    public Usuario autenticarUsuario(String username, String password) throws SQLException{
        Usuario usuarioAutenticado = null;
        String sql = "SELECT * FROM usuarios WHERE username = ? AND password = ?";
        try (Connection con = dbConnection.getInstancia().getConexion();
            PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, username);
            ps.setString(2, password);
            
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    usuarioAutenticado = new  Usuario();
                    usuarioAutenticado.setId(rs.getInt("id"));
                    usuarioAutenticado.setUsername(rs.getString("username"));
                    usuarioAutenticado.setPassword(rs.getString("password"));
                    usuarioAutenticado.setRol(rs.getString("rol"));
                }
            }
            
        
        }catch (SQLException e){
            System.err.println(GestorErrores.mostrarError(e));
        }
        return usuarioAutenticado;
    }
    
}
