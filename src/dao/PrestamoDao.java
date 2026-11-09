
package dao;

import  modelos.Prestamo;
import utilidades.dbConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import utilidades.GestorErrores;
public class PrestamoDao {
    public int registrarPrestamo(Prestamo prestamo) throws SQLException{
        int idGenerado = 0;
        
        String sql = "INSERT INTO prestamos("
                + " cliente_id,"
                + " empleado_id,"
                + " monto,"
                + " saldo_capital,"
                + " interes,"
                + " cuotas,"
                + " fecha_solicitud,"
                + " fecha_aprobacion,"
                + " estado )"
                + " VALUES (? ,? ,? ,? ,? ,? ,? ,? ,? )";
       
        try (Connection con = dbConnection.getInstancia().getConexion();
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
        
        ps.setInt(1, prestamo.getCliente().getId());
        ps.setInt(2, prestamo.getEmpleado().getId());
        ps.setDouble(3, prestamo.getMonto());
        ps.setDouble(4, prestamo.getSaldoCapital());
        ps.setInt(5, prestamo.getCuotas());
        ps.setDouble(6,prestamo.getInteres());
        ps.setDate(7, java.sql.Date.valueOf(prestamo.getFechaSolicitud()));
        ps.setDate(8, java.sql.Date.valueOf(prestamo.getFechaAprobacion()));
        ps.setString(8, prestamo.getEstado());
        
        int filasAfectadas = ps.executeUpdate();
        
        if (filasAfectadas > 0){
            try(ResultSet rs = ps.getGeneratedKeys()){
                if(rs.next()){
                    idGenerado = rs.getInt(1);
                }
            
            }
        } } 
        catch(SQLException e){
            System.err.println(GestorErrores.mostrarError(e));
                }
        
        return idGenerado;
        
    }
    
}
