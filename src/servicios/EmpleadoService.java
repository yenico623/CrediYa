
package servicios;

import dao.CuotaPlanDao;
import dao.PagoDao;
import dao.PrestamoDao;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelos.CuotaPlan;
import modelos.Pago;
import utilidades.dbConnection;
import utilidades.GestorErrores;

public class EmpleadoService {

    private final PrestamoDao prestamoDao;
    private final PagoDao pagoDao;
    private final CuotaPlanDao cuotaPlanDao;

    public EmpleadoService() {
        this.prestamoDao = new PrestamoDao();
        this.pagoDao = new PagoDao();
        this.cuotaPlanDao = new CuotaPlanDao();
    }

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

        try (Connection con = dbConnection.getInstancia().getConexion()) {
            boolean autoCommitAnterior = con.getAutoCommit();

            try {
                con.setAutoCommit(false);

                if (!prestamoDao.bloquearActivoDeEmpleado(con, prestamoId, empleadoId)) {
                    con.rollback();
                    return false;
                }

                int pagoId = pagoDao.insertarPagoAprobado(con, prestamoId, metodoBD, referencia, monto, empleadoId);

                if (pagoId == 0) {
                    con.rollback();
                    return false;
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

        try (Connection con = dbConnection.getInstancia().getConexion()) {
            boolean autoCommitAnterior = con.getAutoCommit();

            try {
                con.setAutoCommit(false);

                Pago pago = pagoDao.obtenerPagoParaProcesar(con, pagoId, empleadoId);

                if (pago == null || !"EN_REVISION".equals(pago.getEstado())) {
                    con.rollback();
                    return false;
                }

                int prestamoId = pago.getPrestamo().getId();
                BigDecimal monto = BigDecimal.valueOf(pago.getMonto()).setScale(2, RoundingMode.HALF_UP);

                if (!prestamoDao.bloquearActivoDeEmpleado(con, prestamoId, empleadoId)) {
                    con.rollback();
                    return false;
                }

                if (!aprobar) {
                    if (!pagoDao.marcarPagoRechazado(con, pagoId, empleadoId)) {
                        con.rollback();
                        return false;
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

                if (!pagoDao.marcarPagoAprobado(con, pagoId, empleadoId)) {
                    System.err.println("ERROR: No se pudo actualizar el estado del pago.");
                    con.rollback();
                    return false;
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

    public List<String[]> listarPrestamosAsignados(int empleadoId) throws SQLException {
        return prestamoDao.listarPorEmpleado(empleadoId);
    }

    public List<CuotaPlan> consultarPlanDePrestamo(int prestamoId, int empleadoId) throws SQLException {
        return cuotaPlanDao.listarPlanPorPrestamoYEmpleado(prestamoId, empleadoId);
    }

    public List<String[]> listarPrestamosActivosAsignados(int empleadoId) throws SQLException {
        return prestamoDao.listarPrestamosAsignadosEmpleado(empleadoId);
    }

    public String[] obtenerPrestamoParaPago(int prestamoId, int empleadoId) throws SQLException {
        return prestamoDao.obtenerPrestamoParaPago(prestamoId, empleadoId);
    }

    public double calcularTotalCuotas(int prestamoId, int cantidadCuotas) throws SQLException {
        return cuotaPlanDao.calcularTotalCuotas(prestamoId, cantidadCuotas);
    }

    public List<String[]> listarPagosPendientes(int empleadoId) throws SQLException {
        return pagoDao.listarPagosPendientesEmpleado(empleadoId);
    }

    public List<String[]> listarPagosProcesados() throws SQLException {
        return pagoDao.listarPagosProcesados();
    }

    private BigDecimal aplicarDistribucion(Connection con, int prestamoId, int pagoId, BigDecimal monto) throws SQLException {
        List<CuotaPendiente> cuotas = new ArrayList<>();
        BigDecimal totalPendiente = BigDecimal.ZERO;

        for (CuotaPlan cuotaPlan : cuotaPlanDao.listarPendientesParaActualizar(con, prestamoId)) {
            CuotaPendiente cuota = new CuotaPendiente(
                    cuotaPlan.getId(),
                    BigDecimal.valueOf(cuotaPlan.getCapitalPendiente()).setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(cuotaPlan.getInteresPendiente()).setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(cuotaPlan.getMoraAcumulada()).setScale(2, RoundingMode.HALF_UP)
            );

            cuotas.add(cuota);

            totalPendiente = totalPendiente
                    .add(cuota.capitalPendiente)
                    .add(cuota.interesPendiente)
                    .add(cuota.moraPendiente);
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

                cuotaPlanDao.actualizarCuota(con, cuota.id, cuota.capitalPendiente, cuota.interesPendiente, cuota.moraPendiente, estado);
                pagoDao.insertarDetalleDistribucion(con, pagoId, cuota.id, abonoMora, abonoInteres, abonoCapital);
            }
        }

        if (dineroRestante.compareTo(BigDecimal.ZERO) > 0) {
            return null;
        }

        return capitalAplicado;
    }

    private void actualizarEstadoPrestamo(Connection con, int prestamoId, BigDecimal capitalAplicado) throws SQLException {
        int pendientes = cuotaPlanDao.contarPendientes(con, prestamoId);
        LocalDate proximaFecha = cuotaPlanDao.obtenerProximaFechaPendiente(con, prestamoId);

        String estado;

        if (pendientes == 0) {
            estado = "PAGADO";
        } else if (proximaFecha != null && proximaFecha.isBefore(LocalDate.now())) {
            estado = "EN_MORA";
        } else {
            estado = "ACTIVO";
        }

        prestamoDao.actualizarSaldoYEstado(con, prestamoId, capitalAplicado, estado, proximaFecha);
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
