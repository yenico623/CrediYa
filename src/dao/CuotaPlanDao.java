
package dao;

import modelos.CuotaPlan;
import utilidades.GestorErrores;
import utilidades.dbConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CuotaPlanDao {

    public boolean insertarCuota(CuotaPlan cuota) throws SQLException {
        String sql = "INSERT INTO cuotas_plan ("
                + "prestamo_id, "
                + "numero_cuota, "
                + "fecha_vencimiento, "
                + "capital_esperado, "
                + "interes_esperado, "
                + "capital_pendiente, "
                + "interes_pendiente, "
                + "mora_acumulada, "
                + "estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cuota.getPrestamo().getId());
            ps.setInt(2, cuota.getNumeroCuota());
            ps.setDate(3, java.sql.Date.valueOf(cuota.getFechaVencimiento()));
            ps.setDouble(4, cuota.getCapitalEsperado());
            ps.setDouble(5, cuota.getInteresEsperado());
            ps.setDouble(6, cuota.getCapitalPendiente());
            ps.setDouble(7, cuota.getInteresPendiente());
            ps.setDouble(8, cuota.getMoraAcumulada());
            ps.setString(9, cuota.getEstado());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println(GestorErrores.mostrarError(e));
            return false;
        }
    }

    public double calcularTotalCuotas(int prestamoId, int cantidadCuotas) throws SQLException {
        String sql = "SELECT COALESCE(SUM(cp.capital_pendiente + cp.interes_pendiente + cp.mora_acumulada), 0) AS total_cuotas "
                + "FROM cuotas_plan cp "
                + "WHERE cp.id IN ("
                + "SELECT seleccion.id FROM ("
                + "SELECT id FROM cuotas_plan "
                + "WHERE prestamo_id = ? "
                + "AND estado IN ('PENDIENTE', 'ABONADA') "
                + "ORDER BY numero_cuota ASC "
                + "LIMIT ?) seleccion)";

        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, prestamoId);
            ps.setInt(2, cantidadCuotas);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_cuotas");
                }
            }
        }

        return 0;
    }
}
