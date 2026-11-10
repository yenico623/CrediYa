package servicios;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import modelos.CuotaPlan;
import modelos.Prestamo;

public class ClienteService {

    
public Prestamo simularNuevoPrestamo(int clienteId, double monto, int cuotas) {
    if (monto <= 0 || cuotas <= 0) {
        throw new IllegalArgumentException("El monto y las cuotas deben ser mayores que cero.");
    }

    estrategias.EstrategiaInteres estrategia;

    if (cuotas <= 6) {
        estrategia = new estrategias.InteresSimple();
    } else {
        estrategia = new estrategias.InteresCompuesto();
    }

    double interes = estrategia.calcularInteres(monto, cuotas);

    Prestamo prestamoSimulado = new Prestamo();
    prestamoSimulado.setMonto(monto);
    prestamoSimulado.setSaldoCapital(monto);
    prestamoSimulado.setInteres(interes);
    prestamoSimulado.setCuotas(cuotas);

    SimuladorCredito simulador = new SimuladorCredito();
    List<CuotaPlan> plan = simulador.GenerarPlanDePagos(prestamoSimulado);

    if (!plan.isEmpty()) {
        double valorCuotaTotal = plan.get(0).getCapitalEsperado()
                + plan.get(0).getInteresEsperado();

        prestamoSimulado.setSaldoCapital(valorCuotaTotal);
    }

    return prestamoSimulado;
}


    public boolean confirmarSolicitudPrestamo(int clienteId, double monto, int cuotas, double interes) {
        String sql = "INSERT INTO prestamos (cliente_id, empleado_id, monto, saldo_capital, interes, cuotas, fecha_solicitud, estado) VALUES (?, NULL, ?, ?, ?, ?, CURDATE(), 'SOLICITADO')";

        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            ps.setDouble(2, monto);
            ps.setDouble(3, monto);
            ps.setDouble(4, interes);
            ps.setInt(5, cuotas);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error SQL al confirmar solicitud: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


public boolean registrarPagoPendiente(int prestamoId, double montoAbono, String metodo, String referencia) {
    String sqlPrestamo = "SELECT id FROM prestamos WHERE id = ? AND estado IN ('ACTIVO', 'EN_MORA') FOR UPDATE";
    String sqlPagoPendiente = "SELECT id FROM pagos WHERE prestamo_id = ? AND estado = 'EN_REVISION' LIMIT 1";
    String sqlInsertPago = "INSERT INTO pagos (prestamo_id, metodo_pago, numero_referencia, fecha_reporte, fecha_aplicacion, monto, estado, empleado_revisor_id) VALUES (?, ?, ?, CURDATE(), NULL, ?, 'EN_REVISION', NULL)";

    if (montoAbono <= 0 || metodo == null || referencia == null || referencia.trim().isEmpty()) {
        return false;
    }

    if (!metodo.trim().equalsIgnoreCase("TRANSFERENCIA")
            && !metodo.trim().equalsIgnoreCase("TARJETA")) {
        return false;
    }

    try (Connection con = utilidades.dbConnection.getInstancia().getConexion()) {
        con.setAutoCommit(false);

        try {
            try (PreparedStatement psPrestamo = con.prepareStatement(sqlPrestamo)) {
                psPrestamo.setInt(1, prestamoId);

                try (ResultSet rs = psPrestamo.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement psPendiente = con.prepareStatement(sqlPagoPendiente)) {
                psPendiente.setInt(1, prestamoId);

                try (ResultSet rs = psPendiente.executeQuery()) {
                    if (rs.next()) {
                        con.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement psIns = con.prepareStatement(sqlInsertPago)) {
                psIns.setInt(1, prestamoId);
                psIns.setString(2, metodo.trim().toUpperCase());
                psIns.setString(3, referencia.trim());
                psIns.setDouble(4, montoAbono);
                psIns.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            con.rollback();
            throw e;
        }

    } catch (SQLException e) {
        System.out.println("Error SQL al registrar pago pendiente: " + e.getMessage());
        return false;
    }
}

}