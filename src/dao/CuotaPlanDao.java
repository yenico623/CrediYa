
package dao;
import modelos.CuotaPlan;
import utilidades.GestorErrores;
import utilidades.dbConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CuotaPlanDao {
    public boolean insertarCuota(CuotaPlan cuota) throws SQLException{
        String sql = "INSERT INTO cuota_plan("
                + " prestamo_id,"
                + " numero_cuota,"
                + " fecha_vencimiento,"
                + " capital_esperado, "
                + " interes_esperado,"
                + " capital_pendiente,"
                + " interes_pediente,"
                + " mora_acumulada"
                + " estado)"+
                     "VALUES (? ,? ,? ,? ,? ,? ,? ,? ,?)";
        
        try(Connection con = dbConnection.getInstancia().getConexion();
            PreparedStatement ps = con.prepareStatement(sql)){
            
            ps.setInt(1, cuota.getPrestamo().getId());
            ps.setInt(2, cuota.getNumeroCuota());
            ps.setDate(3, java.sql.Date.valueOf(cuota.getFechaVencimiento()));
            ps.setDouble(4, cuota.getCapitalEsperado());
            ps.setDouble(5, cuota.getInteresEsperado());
            ps.setDouble(6, cuota.getCapitalPendiente());
            ps.setDouble(7, cuota.getInteresPendiente());
            ps.setDouble(7, cuota.getMoraAcumulada());
            ps.setString(9, cuota.getEstado());
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas >0;
            
        
        }catch (SQLException e){
            System.err.println(GestorErrores.mostrarError(e));
            return false;
        }
    }
    
}
