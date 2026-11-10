
package dao;

import modelos.CuotaPlan;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    public int contarPendientes(Connection con, int prestamoId) throws SQLException {
        String sql = "SELECT COUNT(*) AS pendientes FROM cuotas_plan WHERE prestamo_id = ? AND estado IN ('PENDIENTE', 'ABONADA')";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, prestamoId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("pendientes");
            }
        }
    }

    public int contarPendientes(int prestamoId) throws SQLException {
        try (Connection con = dbConnection.getInstancia().getConexion()) {
            return contarPendientes(con, prestamoId);
        }
    }

    public LocalDate obtenerProximaFechaPendiente(Connection con, int prestamoId) throws SQLException {
        String sql = "SELECT MIN(fecha_vencimiento) AS proxima_fecha FROM cuotas_plan WHERE prestamo_id = ? AND estado IN ('PENDIENTE', 'ABONADA')";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, prestamoId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                java.sql.Date fecha = rs.getDate("proxima_fecha");
                return fecha != null ? fecha.toLocalDate() : null;
            }
        }
    }

    public List<CuotaPlan> listarPrimerasPendientes(int prestamoId, int cantidad) throws SQLException {
        List<CuotaPlan> lista = new ArrayList<>();
        String sql = "SELECT numero_cuota, capital_pendiente, interes_pendiente, mora_acumulada "
                   + "FROM cuotas_plan WHERE prestamo_id = ? AND estado IN ('PENDIENTE', 'ABONADA') "
                   + "ORDER BY numero_cuota ASC LIMIT ?";

        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, prestamoId);
            ps.setInt(2, cantidad);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CuotaPlan cuota = new CuotaPlan();
                    cuota.setNumeroCuota(rs.getInt("numero_cuota"));
                    cuota.setCapitalPendiente(rs.getDouble("capital_pendiente"));
                    cuota.setInteresPendiente(rs.getDouble("interes_pendiente"));
                    cuota.setMoraAcumulada(rs.getDouble("mora_acumulada"));
                    lista.add(cuota);
                }
            }
        }

        return lista;
    }

    public List<CuotaPlan> listarPendientesParaActualizar(Connection con, int prestamoId) throws SQLException {
        List<CuotaPlan> lista = new ArrayList<>();
        String sql = "SELECT id, capital_pendiente, interes_pendiente, mora_acumulada "
                   + "FROM cuotas_plan "
                   + "WHERE prestamo_id = ? AND estado IN ('PENDIENTE', 'ABONADA') "
                   + "ORDER BY numero_cuota ASC FOR UPDATE";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, prestamoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CuotaPlan cuota = new CuotaPlan();
                    cuota.setId(rs.getInt("id"));
                    cuota.setCapitalPendiente(rs.getDouble("capital_pendiente"));
                    cuota.setInteresPendiente(rs.getDouble("interes_pendiente"));
                    cuota.setMoraAcumulada(rs.getDouble("mora_acumulada"));
                    lista.add(cuota);
                }
            }
        }

        return lista;
    }

    public void actualizarCuota(Connection con, int cuotaId, BigDecimal capitalPendiente, BigDecimal interesPendiente, BigDecimal moraAcumulada, String estado) throws SQLException {
        String sql = "UPDATE cuotas_plan "
                   + "SET capital_pendiente = ?, interes_pendiente = ?, mora_acumulada = ?, estado = ? "
                   + "WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, capitalPendiente);
            ps.setBigDecimal(2, interesPendiente);
            ps.setBigDecimal(3, moraAcumulada);
            ps.setString(4, estado);
            ps.setInt(5, cuotaId);
            ps.executeUpdate();
        }
    }

    public List<CuotaPlan> listarPlanPorPrestamoYEmpleado(int prestamoId, int empleadoId) throws SQLException {
        List<CuotaPlan> lista = new ArrayList<>();
        String sql = "SELECT cp.numero_cuota, cp.fecha_vencimiento, cp.capital_esperado, cp.interes_esperado, cp.estado "
                   + "FROM cuotas_plan cp "
                   + "INNER JOIN prestamos p ON cp.prestamo_id = p.id "
                   + "WHERE cp.prestamo_id = ? AND p.empleado_id = ? "
                   + "ORDER BY cp.numero_cuota";

        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, prestamoId);
            ps.setInt(2, empleadoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CuotaPlan cuota = new CuotaPlan();
                    cuota.setNumeroCuota(rs.getInt("numero_cuota"));
                    cuota.setFechaVencimiento(rs.getDate("fecha_vencimiento").toLocalDate());
                    cuota.setCapitalEsperado(rs.getDouble("capital_esperado"));
                    cuota.setInteresEsperado(rs.getDouble("interes_esperado"));
                    cuota.setEstado(rs.getString("estado"));
                    lista.add(cuota);
                }
            }
        }

        return lista;
    }

    public void insertarCuotas(Connection con, int prestamoId, List<CuotaPlan> plan) throws SQLException {
        String sql = "INSERT INTO cuotas_plan (prestamo_id, numero_cuota, fecha_vencimiento, capital_esperado, interes_esperado, capital_pendiente, interes_pendiente, estado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDIENTE')";

        for (CuotaPlan cuota : plan) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, prestamoId);
                ps.setInt(2, cuota.getNumeroCuota());
                ps.setDate(3, java.sql.Date.valueOf(cuota.getFechaVencimiento()));
                ps.setDouble(4, cuota.getCapitalEsperado());
                ps.setDouble(5, cuota.getInteresEsperado());
                ps.setDouble(6, cuota.getCapitalPendiente());
                ps.setDouble(7, cuota.getInteresPendiente());
                ps.executeUpdate();
            }
        }
    }
}
