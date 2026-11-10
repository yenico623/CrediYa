
package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelos.Pago;
import modelos.Prestamo;
import utilidades.dbConnection;
import utilidades.GestorErrores;

public class PagoDao {

    public int registrarPagoConDetalle(int prestamoId, String metodoPago, String referencia, double monto, int empleadoId) throws SQLException {
        int idGenerado = 0;
        String sqlPago = "INSERT INTO pagos (prestamo_id, metodo_pago, numero_referencia, fecha_reporte, monto, estado, empleado_revisor_id) VALUES (?, ?, ?, ?, ?, 'APROBADO', ?)";
        String sqlBuscarCuota = "SELECT id, capital_pendiente, interes_pendiente FROM cuotas_plan WHERE prestamo_id = ? AND estado != 'PAGADA' ORDER BY numero_cuota ASC LIMIT 1";
        String sqlUpdateCuota = "UPDATE cuotas_plan SET capital_pendiente = ?, interes_pendiente = ?, estado = ? WHERE id = ?";
        String sqlDetalle = "INSERT INTO detalles_distribucion_pago (pago_id, cuota_id, monto_a_interes, monto_a_capital) VALUES (?, ?, ?, ?)";
        String sqlUpdatePrestamo = "UPDATE prestamos SET saldo_capital = GREATEST(0, saldo_capital - ?) WHERE id = ?";

        Connection con = dbConnection.getInstancia().getConexion();

        try {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlPago, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, prestamoId);
                ps.setString(2, metodoPago);
                ps.setString(3, referencia);
                ps.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
                ps.setDouble(5, monto);
                ps.setInt(6, empleadoId);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    }
                }
            }

            int cuotaId = 0;
            double capPendiente = 0;
            double intPendiente = 0;

            try (PreparedStatement psCuota = con.prepareStatement(sqlBuscarCuota)) {
                psCuota.setInt(1, prestamoId);

                try (ResultSet rs = psCuota.executeQuery()) {
                    if (rs.next()) {
                        cuotaId = rs.getInt("id");
                        capPendiente = rs.getDouble("capital_pendiente");
                        intPendiente = rs.getDouble("interes_pendiente");
                    }
                }
            }

            if (cuotaId > 0) {
                double dineroRestante = monto;
                double abonoInteres = Math.min(dineroRestante, intPendiente);
                dineroRestante -= abonoInteres;
                double abonoCapital = Math.min(dineroRestante, capPendiente);

                double nuevoIntPendiente = intPendiente - abonoInteres;
                double nuevoCapPendiente = capPendiente - abonoCapital;
                String nuevoEstado = (nuevoIntPendiente <= 0 && nuevoCapPendiente <= 0) ? "PAGADA" : "ABONADA";

                try (PreparedStatement psUpCuota = con.prepareStatement(sqlUpdateCuota)) {
                    psUpCuota.setDouble(1, nuevoCapPendiente);
                    psUpCuota.setDouble(2, nuevoIntPendiente);
                    psUpCuota.setString(3, nuevoEstado);
                    psUpCuota.setInt(4, cuotaId);
                    psUpCuota.executeUpdate();
                }

                try (PreparedStatement psDet = con.prepareStatement(sqlDetalle)) {
                    psDet.setInt(1, idGenerado);
                    psDet.setInt(2, cuotaId);
                    psDet.setDouble(3, abonoInteres);
                    psDet.setDouble(4, abonoCapital);
                    psDet.executeUpdate();
                }
            }

            try (PreparedStatement psPrestamo = con.prepareStatement(sqlUpdatePrestamo)) {
                psPrestamo.setDouble(1, monto);
                psPrestamo.setInt(2, prestamoId);
                psPrestamo.executeUpdate();
            }

            con.commit();
            con.setAutoCommit(true);

        } catch (SQLException e) {
            con.rollback();
            con.setAutoCommit(true);
            System.err.println(GestorErrores.mostrarError(e));
        }

        return idGenerado;
    }

    public List<String[]> listarPagos() throws SQLException {
        List<String[]> lista = new ArrayList<>();

        String sql = "SELECT pa.id, pa.prestamo_id, pa.metodo_pago, pa.monto, pa.fecha_reporte, per.nombre AS cliente_nombre "
                + "FROM pagos pa "
                + "INNER JOIN prestamos pr ON pa.prestamo_id = pr.id "
                + "INNER JOIN clientes c ON pr.cliente_id = c.id "
                + "INNER JOIN personas per ON c.persona_id = per.id";

        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    String.valueOf(rs.getInt("prestamo_id")),
                    rs.getString("cliente_nombre"),
                    String.valueOf(rs.getDouble("monto")),
                    rs.getString("metodo_pago"),
                    String.valueOf(rs.getDate("fecha_reporte"))
                });
            }
        }

        return lista;
    }

    public List<String[]> listarPagosPendientesEmpleado(int empleadoId) throws SQLException {
        List<String[]> lista = new ArrayList<>();

        String sql = "SELECT pa.id, pa.prestamo_id, per.nombre AS cliente, pa.monto, pa.metodo_pago, pa.numero_referencia, pa.fecha_reporte "
                + "FROM pagos pa "
                + "INNER JOIN prestamos p ON pa.prestamo_id = p.id "
                + "INNER JOIN clientes c ON p.cliente_id = c.id "
                + "INNER JOIN personas per ON c.persona_id = per.id "
                + "WHERE pa.estado = 'EN_REVISION' AND p.empleado_id = ?";

        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, empleadoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        String.valueOf(rs.getInt("prestamo_id")),
                        rs.getString("cliente"),
                        String.valueOf(rs.getDouble("monto")),
                        rs.getString("metodo_pago"),
                        rs.getString("numero_referencia"),
                        String.valueOf(rs.getDate("fecha_reporte"))
                    });
                }
            }
        }

        return lista;
    }

    public List<String[]> listarPagosProcesados() throws SQLException {
        List<String[]> lista = new ArrayList<>();

        String sql = "SELECT pa.id, pa.prestamo_id, pa.monto, pa.estado, pa.fecha_aplicacion, per.nombre AS revisor "
                + "FROM pagos pa "
                + "LEFT JOIN empleados e ON pa.empleado_revisor_id = e.id "
                + "LEFT JOIN personas per ON e.persona_id = per.id "
                + "WHERE pa.estado != 'EN_REVISION'";

        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    String.valueOf(rs.getInt("prestamo_id")),
                    String.valueOf(rs.getDouble("monto")),
                    rs.getString("estado"),
                    rs.getString("revisor")
                });
            }
        }

        return lista;
    }

    public boolean existePagoEnRevision(Connection con, int prestamoId) throws SQLException {
        String sql = "SELECT id FROM pagos WHERE prestamo_id = ? AND estado = 'EN_REVISION' LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, prestamoId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existePagoEnRevision(int prestamoId) throws SQLException {
        try (Connection con = dbConnection.getInstancia().getConexion()) {
            return existePagoEnRevision(con, prestamoId);
        }
    }

    public void insertarPagoEnRevision(Connection con, int prestamoId, String metodo, String referencia, double monto) throws SQLException {
        String sql = "INSERT INTO pagos (prestamo_id, metodo_pago, numero_referencia, fecha_reporte, fecha_aplicacion, monto, estado, empleado_revisor_id) "
                   + "VALUES (?, ?, ?, CURDATE(), NULL, ?, 'EN_REVISION', NULL)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, prestamoId);
            ps.setString(2, metodo);
            ps.setString(3, referencia);
            ps.setDouble(4, monto);
            ps.executeUpdate();
        }
    }

    public int insertarPagoAprobado(Connection con, int prestamoId, String metodo, String referencia, BigDecimal monto, int empleadoId) throws SQLException {
        String sql = "INSERT INTO pagos "
                   + "(prestamo_id, metodo_pago, numero_referencia, fecha_reporte, fecha_aplicacion, monto, estado, empleado_revisor_id) "
                   + "VALUES (?, ?, ?, CURDATE(), NOW(), ?, 'APROBADO', ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, prestamoId);
            ps.setString(2, metodo);
            ps.setString(3, referencia);
            ps.setBigDecimal(4, monto);
            ps.setInt(5, empleadoId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    public Pago obtenerPagoParaProcesar(Connection con, int pagoId, int empleadoId) throws SQLException {
        String sql = "SELECT pa.prestamo_id, pa.monto, pa.estado "
                   + "FROM pagos pa "
                   + "INNER JOIN prestamos p ON pa.prestamo_id = p.id "
                   + "WHERE pa.id = ? AND p.empleado_id = ? "
                   + "FOR UPDATE";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pagoId);
            ps.setInt(2, empleadoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Prestamo prestamo = new Prestamo();
                    prestamo.setId(rs.getInt("prestamo_id"));

                    Pago pago = new Pago();
                    pago.setId(pagoId);
                    pago.setPrestamo(prestamo);
                    pago.setMonto(rs.getBigDecimal("monto").doubleValue());
                    pago.setEstado(rs.getString("estado"));
                    return pago;
                }
            }
        }

        return null;
    }

    public boolean marcarPagoRechazado(Connection con, int pagoId, int empleadoId) throws SQLException {
        String sql = "UPDATE pagos SET estado = 'ERROR', empleado_revisor_id = ? "
                   + "WHERE id = ? AND estado = 'EN_REVISION'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, empleadoId);
            ps.setInt(2, pagoId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean marcarPagoAprobado(Connection con, int pagoId, int empleadoId) throws SQLException {
        String sql = "UPDATE pagos SET estado = 'APROBADO', empleado_revisor_id = ?, fecha_aplicacion = NOW() "
                   + "WHERE id = ? AND estado = 'EN_REVISION'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, empleadoId);
            ps.setInt(2, pagoId);
            return ps.executeUpdate() == 1;
        }
    }

    public void insertarDetalleDistribucion(Connection con, int pagoId, int cuotaId, BigDecimal mora, BigDecimal interes, BigDecimal capital) throws SQLException {
        String sql = "INSERT INTO detalles_distribucion_pago "
                   + "(pago_id, cuota_id, monto_a_mora, monto_a_interes, monto_a_capital) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pagoId);
            ps.setInt(2, cuotaId);
            ps.setBigDecimal(3, mora);
            ps.setBigDecimal(4, interes);
            ps.setBigDecimal(5, capital);
            ps.executeUpdate();
        }
    }

    public List<String[]> listarHistorialPorCliente(int clienteId) throws SQLException {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT pa.id, pa.prestamo_id, pa.monto, pa.fecha_reporte, pa.estado "
                   + "FROM pagos pa "
                   + "INNER JOIN prestamos p ON pa.prestamo_id = p.id "
                   + "WHERE p.cliente_id = ?";

        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clienteId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        String.valueOf(rs.getInt("prestamo_id")),
                        String.valueOf(rs.getDouble("monto")),
                        rs.getString("estado"),
                        rs.getDate("fecha_reporte").toString()
                    });
                }
            }
        }

        return lista;
    }
}


