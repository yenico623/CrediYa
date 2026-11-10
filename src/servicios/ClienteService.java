package servicios;

import dao.CuotaPlanDao;
import dao.PagoDao;
import dao.PrestamoDao;
import estrategias.EstrategiaInteres;
import estrategias.InteresCompuesto;
import estrategias.InteresSimple;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelos.CuotaPlan;
import modelos.Prestamo;
import utilidades.dbConnection;

public class ClienteService {

    private final PrestamoDao prestamoDao;
    private final PagoDao pagoDao;
    private final CuotaPlanDao cuotaPlanDao;

    public ClienteService() {
        this.prestamoDao = new PrestamoDao();
        this.pagoDao = new PagoDao();
        this.cuotaPlanDao = new CuotaPlanDao();
    }

    private EstrategiaInteres seleccionarEstrategia(int cuotas) {
        if (cuotas <= 6) {
            return new InteresSimple();
        }
        return new InteresCompuesto();
    }

    public Prestamo simularNuevoPrestamo(int clienteId, double monto, int cuotas) {
        if (monto <= 0 || cuotas <= 0) {
            throw new IllegalArgumentException("El monto y las cuotas deben ser mayores que cero.");
        }

        EstrategiaInteres estrategia = seleccionarEstrategia(cuotas);

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
        try {
            prestamoDao.insertarSolicitud(clienteId, monto, cuotas, interes);
            return true;
        } catch (SQLException e) {
            System.out.println("Error SQL al confirmar solicitud: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean registrarPagoPendiente(int prestamoId, double montoAbono, String metodo, String referencia) {
        if (montoAbono <= 0 || metodo == null || referencia == null || referencia.trim().isEmpty()) {
            return false;
        }

        if (!metodo.trim().equalsIgnoreCase("TRANSFERENCIA")
                && !metodo.trim().equalsIgnoreCase("TARJETA")) {
            return false;
        }

        try (Connection con = dbConnection.getInstancia().getConexion()) {
            con.setAutoCommit(false);

            try {
                if (!prestamoDao.bloquearActivo(con, prestamoId)) {
                    con.rollback();
                    return false;
                }

                if (pagoDao.existePagoEnRevision(con, prestamoId)) {
                    con.rollback();
                    return false;
                }

                pagoDao.insertarPagoEnRevision(con, prestamoId, metodo.trim().toUpperCase(), referencia.trim(), montoAbono);

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

    public List<String[]> listarMisCreditos(int clienteId) throws SQLException {
        return prestamoDao.listarPorCliente(clienteId);
    }

    public List<String[]> listarPrestamosParaPago(int clienteId) throws SQLException {
        return prestamoDao.listarActivosPorCliente(clienteId);
    }

    public boolean prestamoActivoDelCliente(int prestamoId, int clienteId) throws SQLException {
        return prestamoDao.existeActivoDelCliente(prestamoId, clienteId);
    }

    public boolean tienePagoEnRevision(int prestamoId) throws SQLException {
        return pagoDao.existePagoEnRevision(prestamoId);
    }

    public int contarCuotasPendientes(int prestamoId) throws SQLException {
        return cuotaPlanDao.contarPendientes(prestamoId);
    }

    public List<CuotaPlan> consultarCuotasAPagar(int prestamoId, int cantidad) throws SQLException {
        return cuotaPlanDao.listarPrimerasPendientes(prestamoId, cantidad);
    }

    public BigDecimal calcularValorCuota(CuotaPlan cuota) {
        return BigDecimal.valueOf(cuota.getCapitalPendiente())
                .add(BigDecimal.valueOf(cuota.getInteresPendiente()))
                .add(BigDecimal.valueOf(cuota.getMoraAcumulada()));
    }

    public BigDecimal calcularTotalAPagar(List<CuotaPlan> cuotas) {
        BigDecimal total = BigDecimal.ZERO;

        for (CuotaPlan cuota : cuotas) {
            total = total.add(calcularValorCuota(cuota));
        }

        return total;
    }

    public List<String[]> listarHistorialPagos(int clienteId) throws SQLException {
        return pagoDao.listarHistorialPorCliente(clienteId);
    }
}
