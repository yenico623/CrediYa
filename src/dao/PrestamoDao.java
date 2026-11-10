
package dao;

import modelos.Prestamo;
import utilidades.dbConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import modelos.CuotaPlan;
import servicios.SimuladorCredito;
import utilidades.GestorErrores;

public class PrestamoDao {

    public int registrarPrestamo(Prestamo prestamo) throws SQLException {
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
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, prestamo.getCliente().getId());
            ps.setInt(2, prestamo.getEmpleado().getId());
            ps.setDouble(3, prestamo.getMonto());
            ps.setDouble(4, prestamo.getSaldoCapital());
            ps.setDouble(5, prestamo.getInteres());
            ps.setInt(6, prestamo.getCuotas());
            ps.setDate(7, java.sql.Date.valueOf(prestamo.getFechaSolicitud()));
            ps.setDate(8, java.sql.Date.valueOf(prestamo.getFechaAprobacion()));
            ps.setString(9, prestamo.getEstado());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(GestorErrores.mostrarError(e));
        }

        return idGenerado;
    }

    public int registrarPrestamoConCuotas(Prestamo prestamo) throws SQLException {
        int idGenerado = 0;

        String sqlPrestamo = "INSERT INTO prestamos (cliente_id, empleado_id, monto, saldo_capital, interes, cuotas, fecha_solicitud, fecha_aprobacion, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlCuota = "INSERT INTO cuotas_plan (prestamo_id, numero_cuota, fecha_vencimiento, capital_esperado, interes_esperado, capital_pendiente, interes_pendiente, estado) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDIENTE')";

        Connection con = dbConnection.getInstancia().getConexion();

        try {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlPrestamo, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, prestamo.getCliente().getId());
                ps.setInt(2, prestamo.getEmpleado().getId());
                ps.setDouble(3, prestamo.getMonto());
                ps.setDouble(4, prestamo.getSaldoCapital());
                ps.setDouble(5, prestamo.getInteres());
                ps.setInt(6, prestamo.getCuotas());
                ps.setDate(7, java.sql.Date.valueOf(prestamo.getFechaSolicitud()));
                ps.setDate(8, java.sql.Date.valueOf(prestamo.getFechaAprobacion()));
                ps.setString(9, prestamo.getEstado());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    }
                }
            }

            SimuladorCredito simulador = new SimuladorCredito();
            List<CuotaPlan> plan = simulador.GenerarPlanDePagos(prestamo);

            for (CuotaPlan cuota : plan) {
                try (PreparedStatement psCuota = con.prepareStatement(sqlCuota)) {
                    psCuota.setInt(1, idGenerado);
                    psCuota.setInt(2, cuota.getNumeroCuota());
                    psCuota.setDate(3, java.sql.Date.valueOf(cuota.getFechaVencimiento()));
                    psCuota.setDouble(4, cuota.getCapitalEsperado());
                    psCuota.setDouble(5, cuota.getInteresEsperado());
                    psCuota.setDouble(6, cuota.getCapitalPendiente());
                    psCuota.setDouble(7, cuota.getInteresPendiente());
                    psCuota.executeUpdate();
                }
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

    public List<String[]> listarPrestamosAsignadosEmpleado(int empleadoId) throws SQLException {
        List<String[]> lista = new ArrayList<>();

        String sql = "SELECT p.id, per.nombre AS cliente, p.saldo_capital, p.estado "
                + "FROM prestamos p "
                + "INNER JOIN clientes c ON p.cliente_id = c.id "
                + "INNER JOIN personas per ON c.persona_id = per.id "
                + "WHERE p.empleado_id = ? AND p.estado IN ('ACTIVO', 'EN_MORA') "
                + "ORDER BY p.id";

        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, empleadoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("cliente"),
                        String.valueOf(rs.getDouble("saldo_capital")),
                        rs.getString("estado")
                    });
                }
            }
        }

        return lista;
    }

    public String[] obtenerPrestamoParaPago(int prestamoId, int empleadoId) throws SQLException {
        String sql = "SELECT p.id, per.nombre AS cliente, "
                + "(SELECT COUNT(*) FROM cuotas_plan cp "
                + "WHERE cp.prestamo_id = p.id "
                + "AND cp.estado IN ('PENDIENTE', 'ABONADA')) AS cuotas_pendientes "
                + "FROM prestamos p "
                + "INNER JOIN clientes c ON p.cliente_id = c.id "
                + "INNER JOIN personas per ON c.persona_id = per.id "
                + "WHERE p.id = ? AND p.empleado_id = ? "
                + "AND p.estado IN ('ACTIVO', 'EN_MORA')";

        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, prestamoId);
            ps.setInt(2, empleadoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("cliente"),
                        String.valueOf(rs.getInt("cuotas_pendientes"))
                    };
                }
            }
        }

        return null;
    }
}
