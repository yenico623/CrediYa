
package servicios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import utilidades.dbConnection;
import utilidades.GestorErrores;

public class EmpleadoService {

    public boolean registrarPagoEmpleado(int prestamoId, int empleadoId, double montoAbono, String metodo, String referencia) {
        BigDecimal monto = BigDecimal.valueOf(montoAbono).setScale(2, RoundingMode.HALF_UP);
        String metodoBD = metodo.equalsIgnoreCase("NEQUI") ? "TRANSFERENCIA" : metodo.toUpperCase();

        if (prestamoId <= 0 || empleadoId <= 0 || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (!metodoBD.equals("EFECTIVO") && !metodoBD.equals("TRANSFERENCIA") && !metodoBD.equals("TARJETA")) {
            return false;
        }

        if (!metodoBD.equals("EFECTIVO") && (referencia == null || referencia.trim().isEmpty())) {
            return false;
        }

        String sqlVerificar = "SELECT id FROM prestamos "
                            + "WHERE id = ? AND empleado_id = ? "
                            + "AND estado IN ('ACTIVO', 'EN_MORA') FOR UPDATE";

        String sqlInsertPago = "INSERT INTO pagos "
                             + "(prestamo_id, metodo_pago, numero_referencia, fecha_reporte, fecha_aplicacion, monto, estado, empleado_revisor_id) "
                             + "VALUES (?, ?, ?, CURDATE(), NOW(), ?, 'APROBADO', ?)";

        try (Connection con = dbConnection.getInstancia().getConexion()) {
            boolean autoCommitAnterior = con.getAutoCommit();

            try {
                con.setAutoCommit(false);

                try (PreparedStatement ps = con.prepareStatement(sqlVerificar)) {
                    ps.setInt(1, prestamoId);
                    ps.setInt(2, empleadoId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                    }
                }

                int pagoId;

                try (PreparedStatement ps = con.prepareStatement(sqlInsertPago, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, prestamoId);
                    ps.setString(2, metodoBD);
                    ps.setString(3, referencia);
                    ps.setBigDecimal(4, monto);
                    ps.setInt(5, empleadoId);

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }

                        pagoId = rs.getInt(1);
                    }
                }

                BigDecimal capitalAplicado = aplicarDistribucion(con, prestamoId, pagoId, monto);

                if (capitalAplicado == null) {
                    con.rollback();
                    return false;
                }

                actualizarEstadoPrestamo(con, prestamoId, capitalAplicado);

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                System.err.println(GestorErrores.mostrarError(e));
                return false;
            } finally {
                con.setAutoCommit(autoCommitAnterior);
            }
        } catch (SQLException e) {
            System.err.println(GestorErrores.mostrarError(e));
            return false;
        }
    }

    
public boolean procesarPagoPendiente(int pagoId, int empleadoId, boolean aprobar) {
    if (pagoId <= 0 || empleadoId <= 0) {
        return false;
    }

    String sqlObtenerPago = "SELECT pa.prestamo_id, pa.monto, pa.estado "
                          + "FROM pagos pa "
                          + "INNER JOIN prestamos p ON pa.prestamo_id = p.id "
                          + "WHERE pa.id = ? AND p.empleado_id = ? "
                          + "FOR UPDATE";

    String sqlVerificarPrestamo = "SELECT id FROM prestamos "
                                + "WHERE id = ? AND empleado_id = ? "
                                + "AND estado IN ('ACTIVO', 'EN_MORA') FOR UPDATE";

    String sqlRechazar = "UPDATE pagos SET estado = 'ERROR', empleado_revisor_id = ? "
                       + "WHERE id = ? AND estado = 'EN_REVISION'";

    String sqlAprobar = "UPDATE pagos SET estado = 'APROBADO', empleado_revisor_id = ?, fecha_aplicacion = NOW() "
                      + "WHERE id = ? AND estado = 'EN_REVISION'";

    try (Connection con = dbConnection.getInstancia().getConexion()) {
        boolean autoCommitAnterior = con.getAutoCommit();

        try {
            con.setAutoCommit(false);

            int prestamoId;
            BigDecimal monto;

            try (PreparedStatement ps = con.prepareStatement(sqlObtenerPago)) {
                ps.setInt(1, pagoId);
                ps.setInt(2, empleadoId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !"EN_REVISION".equals(rs.getString("estado"))) {
                        con.rollback();
                        return false;
                    }

                    prestamoId = rs.getInt("prestamo_id");
                    monto = rs.getBigDecimal("monto");
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlVerificarPrestamo)) {
                ps.setInt(1, prestamoId);
                ps.setInt(2, empleadoId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                }
            }

            if (!aprobar) {
                try (PreparedStatement ps = con.prepareStatement(sqlRechazar)) {
                    ps.setInt(1, empleadoId);
                    ps.setInt(2, pagoId);

                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return false;
                    }
                }

                con.commit();
                return true;
            }

            BigDecimal capitalAplicado = aplicarDistribucion(con, prestamoId, pagoId, monto);

            if (capitalAplicado == null) {
                System.err.println("ERROR: aplicarDistribucion devolvió null.");
                con.rollback();
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sqlAprobar)) {
                ps.setInt(1, empleadoId);
                ps.setInt(2, pagoId);

                if (ps.executeUpdate() != 1) {
                    System.err.println("ERROR: No se pudo actualizar el estado del pago.");
                    con.rollback();
                    return false;
                }
            }

            actualizarEstadoPrestamo(con, prestamoId, capitalAplicado);

            con.commit();
            return true;

        } catch (SQLException e) {
            con.rollback();
            System.err.println("ERROR SQL AL PROCESAR EL PAGO:");
            e.printStackTrace();
            return false;

        } catch (Exception e) {
            con.rollback();
            System.err.println("ERROR GENERAL AL PROCESAR EL PAGO:");
            e.printStackTrace();
            return false;

        } finally {
            con.setAutoCommit(autoCommitAnterior);
        }

    } catch (SQLException e) {
        System.err.println("ERROR AL OBTENER O CERRAR LA CONEXIÓN:");
        e.printStackTrace();
        return false;
    }
}




private BigDecimal aplicarDistribucion(Connection con, int prestamoId, int pagoId, BigDecimal monto) throws SQLException {
    String sqlCuotas = "SELECT id, capital_pendiente, interes_pendiente, mora_acumulada "
                     + "FROM cuotas_plan "
                     + "WHERE prestamo_id = ? AND estado IN ('PENDIENTE', 'ABONADA') "
                     + "ORDER BY numero_cuota ASC FOR UPDATE";

    String sqlActualizarCuota = "UPDATE cuotas_plan "
                              + "SET capital_pendiente = ?, interes_pendiente = ?, mora_acumulada = ?, estado = ? "
                              + "WHERE id = ?";

    String sqlDetalle = "INSERT INTO detalles_distribucion_pago "
                      + "(pago_id, cuota_id, monto_a_mora, monto_a_interes, monto_a_capital) "
                      + "VALUES (?, ?, ?, ?, ?)";

    List<CuotaPendiente> cuotas = new ArrayList<>();
    BigDecimal totalPendiente = BigDecimal.ZERO;

    try (PreparedStatement ps = con.prepareStatement(sqlCuotas)) {
        ps.setInt(1, prestamoId);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CuotaPendiente cuota = new CuotaPendiente(
                        rs.getInt("id"),
                        rs.getBigDecimal("capital_pendiente"),
                        rs.getBigDecimal("interes_pendiente"),
                        rs.getBigDecimal("mora_acumulada")
                );

                cuotas.add(cuota);

                totalPendiente = totalPendiente
                        .add(cuota.capitalPendiente)
                        .add(cuota.interesPendiente)
                        .add(cuota.moraPendiente);
            }
        }
    }

    if (cuotas.isEmpty()) {
        return null;
    }

    if (monto.compareTo(totalPendiente) > 0) {
        return null;
    }

    BigDecimal dineroRestante = monto;
    BigDecimal capitalAplicado = BigDecimal.ZERO;

    for (CuotaPendiente cuota : cuotas) {
        if (dineroRestante.compareTo(BigDecimal.ZERO) <= 0) {
            break;
        }

        BigDecimal abonoMora = dineroRestante.min(cuota.moraPendiente);
        dineroRestante = dineroRestante.subtract(abonoMora);
        cuota.moraPendiente = cuota.moraPendiente.subtract(abonoMora);

        BigDecimal abonoInteres = dineroRestante.min(cuota.interesPendiente);
        dineroRestante = dineroRestante.subtract(abonoInteres);
        cuota.interesPendiente = cuota.interesPendiente.subtract(abonoInteres);

        BigDecimal abonoCapital = dineroRestante.min(cuota.capitalPendiente);
        dineroRestante = dineroRestante.subtract(abonoCapital);
        cuota.capitalPendiente = cuota.capitalPendiente.subtract(abonoCapital);

        capitalAplicado = capitalAplicado.add(abonoCapital);

        if (abonoMora.compareTo(BigDecimal.ZERO) > 0
                || abonoInteres.compareTo(BigDecimal.ZERO) > 0
                || abonoCapital.compareTo(BigDecimal.ZERO) > 0) {

            String estado = cuota.capitalPendiente.compareTo(BigDecimal.ZERO) == 0
                    && cuota.interesPendiente.compareTo(BigDecimal.ZERO) == 0
                    && cuota.moraPendiente.compareTo(BigDecimal.ZERO) == 0
                    ? "PAGADA" : "ABONADA";

            try (PreparedStatement ps = con.prepareStatement(sqlActualizarCuota)) {
                ps.setBigDecimal(1, cuota.capitalPendiente);
                ps.setBigDecimal(2, cuota.interesPendiente);
                ps.setBigDecimal(3, cuota.moraPendiente);
                ps.setString(4, estado);
                ps.setInt(5, cuota.id);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                ps.setInt(1, pagoId);
                ps.setInt(2, cuota.id);
                ps.setBigDecimal(3, abonoMora);
                ps.setBigDecimal(4, abonoInteres);
                ps.setBigDecimal(5, abonoCapital);
                ps.executeUpdate();
            }
        }
    }

    if (dineroRestante.compareTo(BigDecimal.ZERO) > 0) {
        return null;
    }

    return capitalAplicado;
}


    private void actualizarEstadoPrestamo(Connection con, int prestamoId, BigDecimal capitalAplicado) throws SQLException {
        String sqlConsultarCuotas = "SELECT COUNT(*) AS pendientes, MIN(fecha_vencimiento) AS proxima_fecha "
                                  + "FROM cuotas_plan "
                                  + "WHERE prestamo_id = ? AND estado IN ('PENDIENTE', 'ABONADA')";

        String sqlActualizar = "UPDATE prestamos "
                             + "SET saldo_capital = GREATEST(0, saldo_capital - ?), "
                             + "estado = ?, proxima_fecha_pago = ? "
                             + "WHERE id = ?";

        int pendientes;
        LocalDate proximaFecha = null;

        try (PreparedStatement ps = con.prepareStatement(sqlConsultarCuotas)) {
            ps.setInt(1, prestamoId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                pendientes = rs.getInt("pendientes");

                java.sql.Date fecha = rs.getDate("proxima_fecha");

                if (fecha != null) {
                    proximaFecha = fecha.toLocalDate();
                }
            }
        }

        String estado;

        if (pendientes == 0) {
            estado = "PAGADO";
        } else if (proximaFecha != null && proximaFecha.isBefore(LocalDate.now())) {
            estado = "EN_MORA";
        } else {
            estado = "ACTIVO";
        }

        try (PreparedStatement ps = con.prepareStatement(sqlActualizar)) {
            ps.setBigDecimal(1, capitalAplicado);
            ps.setString(2, estado);

            if (proximaFecha != null) {
                ps.setDate(3, java.sql.Date.valueOf(proximaFecha));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }

            ps.setInt(4, prestamoId);
            ps.executeUpdate();
        }
    }

    private static class CuotaPendiente {
        private final int id;
        private BigDecimal capitalPendiente;
        private BigDecimal interesPendiente;
        private BigDecimal moraPendiente;

        private CuotaPendiente(int id, BigDecimal capitalPendiente, BigDecimal interesPendiente, BigDecimal moraPendiente) {
            this.id = id;
            this.capitalPendiente = capitalPendiente;
            this.interesPendiente = interesPendiente;
            this.moraPendiente = moraPendiente;
        }
    }
}
