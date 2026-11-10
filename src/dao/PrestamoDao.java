
package dao;
 
import modelos.Prestamo;
import modelos.Cliente;
import modelos.Empleado;
import java.math.BigDecimal;
import java.time.LocalDate;
import utilidades.dbConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import modelos.CuotaPlan;
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
            ps.setInt(2, prestamo.getEmpleado().getIdEmpleado());
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
 
    public int registrarPrestamoConCuotas(Prestamo prestamo, List<CuotaPlan> plan) throws SQLException {
        int idGenerado = 0;
 
        String sqlPrestamo = "INSERT INTO prestamos (cliente_id, empleado_id, monto, saldo_capital, interes, cuotas, fecha_solicitud, fecha_aprobacion, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlCuota = "INSERT INTO cuotas_plan (prestamo_id, numero_cuota, fecha_vencimiento, capital_esperado, interes_esperado, capital_pendiente, interes_pendiente, estado) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDIENTE')";
 
        Connection con = dbConnection.getInstancia().getConexion();
 
        try {
            con.setAutoCommit(false);
 
            try (PreparedStatement ps = con.prepareStatement(sqlPrestamo, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, prestamo.getCliente().getId());
                ps.setInt(2, prestamo.getEmpleado().getIdEmpleado());
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
                + "AND cp.estado IN ('PENDIENTE', 'ABONADA') "
                + "AND (COALESCE(cp.capital_pendiente, 0) + COALESCE(cp.interes_pendiente, 0) + COALESCE(cp.mora_acumulada, 0)) > 0) AS cuotas_pendientes "
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
 
    private static final String SELECT_PRESTAMOS = "SELECT p.id, p.monto, p.saldo_capital, p.interes, p.cuotas, p.estado, p.fecha_solicitud, p.fecha_aprobacion, "
            + "c.id AS cliente_id, per.nombre AS cliente_nombre, per.documento AS cliente_documento, "
            + "e.id AS empleado_id, pe.nombre AS empleado_nombre "
            + "FROM prestamos p "
            + "INNER JOIN clientes c ON p.cliente_id = c.id "
            + "INNER JOIN personas per ON c.persona_id = per.id "
            + "LEFT JOIN empleados e ON p.empleado_id = e.id "
            + "LEFT JOIN personas pe ON e.persona_id = pe.id ";
 
    private Prestamo mapearPrestamo(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente(
                rs.getInt("cliente_id"),
                null,
                rs.getString("cliente_nombre"),
                rs.getString("cliente_documento"),
                null,
                null,
                null);
 
        Empleado empleado = null;
        int empleadoId = rs.getInt("empleado_id");
 
        if (!rs.wasNull()) {
            empleado = new Empleado();
            empleado.setIdEmpleado(empleadoId);
            empleado.setNombre(rs.getString("empleado_nombre"));
        }
 
        java.sql.Date fechaAprobacion = rs.getDate("fecha_aprobacion");
 
        return new Prestamo(
                rs.getInt("id"),
                cliente,
                empleado,
                rs.getDouble("monto"),
                rs.getDouble("saldo_capital"),
                rs.getDouble("interes"),
                rs.getInt("cuotas"),
                rs.getDate("fecha_solicitud").toLocalDate(),
                fechaAprobacion != null ? fechaAprobacion.toLocalDate() : null,
                rs.getString("estado"));
    }
 
    private List<Prestamo> consultar(String condicion, Object parametro) throws SQLException {
        List<Prestamo> lista = new ArrayList<>();
        String sql = SELECT_PRESTAMOS + condicion + " ORDER BY p.id";
 
        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            if (parametro != null) {
                ps.setObject(1, parametro);
            }
 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPrestamo(rs));
                }
            }
        }
 
        return lista;
    }
 
    public List<Prestamo> listarTodos() throws SQLException {
        return consultar("", null);
    }
 
    public List<Prestamo> listarPorEstado(String estado) throws SQLException {
        return consultar("WHERE p.estado = ?", estado);
    }
 
    public List<Prestamo> listarAprobadosSinEmpleado() throws SQLException {
        return consultar("WHERE p.estado = 'APROBADO'", null);
    }
 
    public Prestamo obtenerPorId(int prestamoId) throws SQLException {
        List<Prestamo> lista = consultar("WHERE p.id = ?", prestamoId);
        return lista.isEmpty() ? null : lista.get(0);
    }
 
    public List<String[]> listarMorosos() throws SQLException {
        List<String[]> lista = new ArrayList<>();
 
        String sql = "SELECT p.id as prestamo_id, per.nombre as cliente_nombre, per.telefono, "
                   + "MAX(pa.fecha_reporte) as ultimo_pago, "
                   + "DATEDIFF(CURDATE(), MAX(pa.fecha_reporte)) as dias_sin_pagar "
                   + "FROM prestamos p "
                   + "INNER JOIN clientes c ON p.cliente_id = c.id "
                   + "INNER JOIN personas per ON c.persona_id = per.id "
                   + "LEFT JOIN pagos pa ON p.id = pa.prestamo_id "
                   + "GROUP BY p.id, per.nombre, per.telefono "
                   + "HAVING dias_sin_pagar > 30 OR ultimo_pago IS NULL";
 
        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                boolean conPagos = rs.getDate("ultimo_pago") != null;
 
                lista.add(new String[]{
                    String.valueOf(rs.getInt("prestamo_id")),
                    rs.getString("cliente_nombre"),
                    rs.getString("telefono"),
                    conPagos ? rs.getDate("ultimo_pago").toString() : "SIN PAGOS",
                    conPagos ? rs.getInt("dias_sin_pagar") + " dias" : "N/A (Inactivo)"
                });
            }
        }
 
        return lista;
    }
 
    public List<String[]> listarPorCliente(int clienteId) throws SQLException {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT id, monto, saldo_capital, estado, proxima_fecha_pago FROM prestamos WHERE cliente_id = ?";
 
        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, clienteId);
 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        String.valueOf(rs.getDouble("monto")),
                        String.valueOf(rs.getDouble("saldo_capital")),
                        rs.getString("estado"),
                        rs.getDate("proxima_fecha_pago") != null ? rs.getDate("proxima_fecha_pago").toString() : "N/A"
                    });
                }
            }
        }
 
        return lista;
    }
 
    public List<String[]> listarPorEmpleado(int empleadoId) throws SQLException {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT p.id, per.nombre AS cliente, p.saldo_capital, p.estado, p.proxima_fecha_pago "
                   + "FROM prestamos p "
                   + "INNER JOIN clientes c ON p.cliente_id = c.id "
                   + "INNER JOIN personas per ON c.persona_id = per.id "
                   + "WHERE p.empleado_id = ?";
 
        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, empleadoId);
 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("cliente"),
                        String.valueOf(rs.getDouble("saldo_capital")),
                        rs.getString("estado"),
                        rs.getDate("proxima_fecha_pago") != null ? rs.getDate("proxima_fecha_pago").toString() : "N/A"
                    });
                }
            }
        }
 
        return lista;
    }
 
    public List<String[]> listarActivosPorCliente(int clienteId) throws SQLException {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT p.id, p.monto, p.saldo_capital, p.cuotas, p.estado, "
                   + "EXISTS (SELECT 1 FROM pagos pg WHERE pg.prestamo_id = p.id AND pg.estado = 'EN_REVISION') AS pago_pendiente "
                   + "FROM prestamos p WHERE p.cliente_id = ? AND p.estado IN ('ACTIVO', 'EN_MORA') "
                   + "ORDER BY p.id";
 
        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, clienteId);
 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        String.valueOf(rs.getDouble("monto")),
                        String.valueOf(rs.getDouble("saldo_capital")),
                        String.valueOf(rs.getInt("cuotas")),
                        rs.getString("estado"),
                        String.valueOf(rs.getBoolean("pago_pendiente"))
                    });
                }
            }
        }
 
        return lista;
    }
 
    public boolean existeActivoDelCliente(int prestamoId, int clienteId) throws SQLException {
        String sql = "SELECT id FROM prestamos WHERE id = ? AND cliente_id = ? AND estado IN ('ACTIVO', 'EN_MORA')";
 
        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, prestamoId);
            ps.setInt(2, clienteId);
 
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
 
    public boolean bloquearActivo(Connection con, int prestamoId) throws SQLException {
        String sql = "SELECT id FROM prestamos WHERE id = ? AND estado IN ('ACTIVO', 'EN_MORA') FOR UPDATE";
 
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, prestamoId);
 
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
 
    public boolean bloquearActivoDeEmpleado(Connection con, int prestamoId, int empleadoId) throws SQLException {
        String sql = "SELECT id FROM prestamos "
                   + "WHERE id = ? AND empleado_id = ? "
                   + "AND estado IN ('ACTIVO', 'EN_MORA') FOR UPDATE";
 
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, prestamoId);
            ps.setInt(2, empleadoId);
 
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
 
    public void actualizarSaldoYEstado(Connection con, int prestamoId, BigDecimal capitalAplicado, String estado, LocalDate proximaFecha) throws SQLException {
        String sql = "UPDATE prestamos "
                   + "SET saldo_capital = GREATEST(0, saldo_capital - ?), "
                   + "estado = ?, proxima_fecha_pago = ? "
                   + "WHERE id = ?";
 
        try (PreparedStatement ps = con.prepareStatement(sql)) {
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
 
    public void insertarSolicitud(int clienteId, double monto, int cuotas, double interes) throws SQLException {
        String sql = "INSERT INTO prestamos (cliente_id, empleado_id, monto, saldo_capital, interes, cuotas, fecha_solicitud, estado) VALUES (?, NULL, ?, ?, ?, ?, CURDATE(), 'SOLICITADO')";
 
        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            ps.setDouble(2, monto);
            ps.setDouble(3, monto);
            ps.setDouble(4, interes);
            ps.setInt(5, cuotas);
            ps.executeUpdate();
        }
    }
 
    public Prestamo obtenerSolicitudParaActualizar(Connection con, int prestamoId) throws SQLException {
        String sql = "SELECT id, monto, interes, cuotas FROM prestamos WHERE id = ? AND estado = 'SOLICITADO' FOR UPDATE";
 
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, prestamoId);
 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Prestamo prestamo = new Prestamo();
                    prestamo.setId(rs.getInt("id"));
                    prestamo.setMonto(rs.getDouble("monto"));
                    prestamo.setInteres(rs.getDouble("interes"));
                    prestamo.setCuotas(rs.getInt("cuotas"));
                    return prestamo;
                }
            }
        }
 
        return null;
    }
 
    public boolean aprobarSolicitud(Connection con, int prestamoId, LocalDate proximaFecha) throws SQLException {
        String sql = "UPDATE prestamos SET estado = 'APROBADO', fecha_aprobacion = CURDATE(), proxima_fecha_pago = ? "
                   + "WHERE id = ? AND estado = 'SOLICITADO'";
 
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(proximaFecha));
            ps.setInt(2, prestamoId);
            return ps.executeUpdate() == 1;
        }
    }
 
    public boolean rechazarSolicitud(int prestamoId) throws SQLException {
        String sql = "UPDATE prestamos SET estado = 'RECHAZADO' WHERE id = ? AND estado = 'SOLICITADO'";
 
        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, prestamoId);
            return ps.executeUpdate() == 1;
        }
    }
 
    public boolean asignarEmpleado(int prestamoId, int empleadoId) throws SQLException {
        String sql = "UPDATE prestamos SET empleado_id = ?, estado = 'ACTIVO' "
                   + "WHERE id = ? AND estado = 'APROBADO'";
 
        try (Connection con = dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, empleadoId);
            ps.setInt(2, prestamoId);
            return ps.executeUpdate() == 1;
        }
    }
}