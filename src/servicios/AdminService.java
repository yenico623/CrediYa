package servicios;

import dao.ClienteDao;
import dao.CuotaPlanDao;
import dao.EmpleadoDao;
import dao.PrestamoDao;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import modelos.Cliente;
import modelos.CuotaPlan;
import modelos.Empleado;
import modelos.Prestamo;
import utilidades.GestorErrores;
import utilidades.dbConnection;

public class AdminService {

    private static final String SIN_ASIGNAR = "Sin asignar";

    private final ClienteDao clienteDao;
    private final EmpleadoDao empleadoDao;
    private final PrestamoDao prestamoDao;
    private final CuotaPlanDao cuotaPlanDao;
    private final SimuladorCredito simuladorCredito;

    public AdminService() {
        this.clienteDao = new ClienteDao();
        this.empleadoDao = new EmpleadoDao();
        this.prestamoDao = new PrestamoDao();
        this.cuotaPlanDao = new CuotaPlanDao();
        this.simuladorCredito = new SimuladorCredito();
    }

    public boolean crearEmpleado(Empleado empleado, String username, String password) {
        if (empleado == null || empleado.getSalario() <= 0) {
            return false;
        }

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }

        return empleadoDao.registrarEmpleado(empleado, username.trim(), password.trim());
    }

    public List<Empleado> listarEmpleados() throws SQLException {
        return empleadoDao.listarEmpleados();
    }

    public Empleado consultarEmpleado(String documento) throws SQLException {
        return empleadoDao.buscarPorDocumento(documento);
    }

    public boolean actualizarEmpleado(String documento, String correo, String telefono, double salario) throws SQLException {
        if (salario <= 0 || empleadoDao.buscarPorDocumento(documento) == null) {
            return false;
        }

        return empleadoDao.actualizarEmpleado(documento, correo, telefono, salario);
    }

    public boolean crearCliente(Cliente cliente) {
        if (cliente == null) {
            return false;
        }

        return clienteDao.registrarCliente(cliente);
    }

    public List<Cliente> listarClientes() throws SQLException {
        return clienteDao.listarClientes();
    }

    public Cliente consultarCliente(String documento) throws SQLException {
        return clienteDao.buscarCliente(documento);
    }

    public boolean actualizarCliente(String documento, String correo, String telefono) throws SQLException {
        if (clienteDao.buscarCliente(documento) == null) {
            return false;
        }

        return clienteDao.actualizarContacto(documento, correo, telefono);
    }

    public List<Prestamo> listarSolicitudes() throws SQLException {
        return prestamoDao.listarPorEstado("SOLICITADO");
    }

    public Prestamo consultarSolicitud(int prestamoId) throws SQLException {
        return prestamoDao.obtenerPorId(prestamoId);
    }

    public boolean aprobarSolicitud(int prestamoId) {
        try (Connection con = dbConnection.getInstancia().getConexion()) {
            boolean autoCommitAnterior = con.getAutoCommit();

            try {
                con.setAutoCommit(false);

                Prestamo prestamo = prestamoDao.obtenerSolicitudParaActualizar(con, prestamoId);

                if (prestamo == null) {
                    con.rollback();
                    return false;
                }

                List<CuotaPlan> plan = simuladorCredito.GenerarPlanDePagos(prestamo);

                if (plan.isEmpty()) {
                    con.rollback();
                    return false;
                }

                cuotaPlanDao.insertarCuotas(con, prestamoId, plan);

                if (!prestamoDao.aprobarSolicitud(con, prestamoId, plan.get(0).getFechaVencimiento())) {
                    con.rollback();
                    return false;
                }

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

    public boolean rechazarSolicitud(int prestamoId) throws SQLException {
        return prestamoDao.rechazarSolicitud(prestamoId);
    }

    public List<Prestamo> listarPrestamosPorAsignar() throws SQLException {
        return prestamoDao.listarAprobadosSinEmpleado();
    }

    public boolean asignarEmpleado(int prestamoId, int empleadoId) throws SQLException {
        boolean empleadoExiste = empleadoDao.listarEmpleados().stream()
                .anyMatch(empleado -> empleado.getIdEmpleado() == empleadoId);

        if (!empleadoExiste) {
            return false;
        }

        return prestamoDao.asignarEmpleado(prestamoId, empleadoId);
    }

    public List<Prestamo> listarPrestamos() throws SQLException {
        return prestamoDao.listarTodos();
    }

    public List<String[]> reporteMorosos() throws SQLException {
        return prestamoDao.listarMorosos();
    }

    public Map<String, Long> cantidadPrestamosPorEstado(List<Prestamo> prestamos) {
        return prestamos.stream()
                .collect(Collectors.groupingBy(Prestamo::getEstado, TreeMap::new, Collectors.counting()));
    }

    public Map<String, Double> montoTotalPorEstado(List<Prestamo> prestamos) {
        return prestamos.stream()
                .collect(Collectors.groupingBy(Prestamo::getEstado, TreeMap::new, Collectors.summingDouble(Prestamo::getMonto)));
    }

    public Map<String, Double> topClientesPorMonto(List<Prestamo> prestamos, int limite) {
        Map<String, Double> totalPorCliente = prestamos.stream()
                .collect(Collectors.groupingBy(prestamo -> prestamo.getCliente().getNombre(), Collectors.summingDouble(Prestamo::getMonto)));

        return totalPorCliente.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limite)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    public double promedioMonto(List<Prestamo> prestamos) {
        return prestamos.stream()
                .mapToDouble(Prestamo::getMonto)
                .average()
                .orElse(0);
    }

    public Map<String, Long> cantidadPrestamosPorEmpleado(List<Prestamo> prestamos) {
        return prestamos.stream()
                .collect(Collectors.groupingBy(
                        prestamo -> prestamo.getEmpleado() != null ? prestamo.getEmpleado().getNombre() : SIN_ASIGNAR,
                        TreeMap::new,
                        Collectors.counting()));
    }
}
