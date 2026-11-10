package vistas;
 
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import modelos.Cliente;
import modelos.Empleado;
import modelos.Prestamo;
import servicios.AdminService;
import utilidades.ConsolaUtil;
import utilidades.MotorVisual;
 
public class MenuReportes {
 
    private final AdminService adminService;
 
    public MenuReportes() {
        this.adminService = new AdminService();
    }
 
    public void iniciar() {
        boolean volver = false;
 
        while (!volver) {
            String[] opciones = {
                "Reporte general de clientes",
                "Reporte general de empleados",
                "Reporte general de prestamos",
                "Reporte de clientes morosos",
                "Reportes con Stream API"
            };
 
            MotorVisual.dibujarMenu("REPORTES", opciones, "Volver");
            int opcion = ConsolaUtil.leerEntero("   > Seleccione una opcion: ");
 
            switch (opcion) {
                case 1 -> reporteClientes();
                case 2 -> reporteEmpleados();
                case 3 -> reportePrestamos();
                case 4 -> reporteMorosos();
                case 5 -> reportesStream();
                case 6 -> volver = true;
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }
 
    private void reporteClientes() {
        System.out.println("\n  ===================================================");
        System.out.println("               REPORTE GENERAL DE CLIENTES           ");
        System.out.println("  ===================================================");
 
        try {
            List<Cliente> clientes = adminService.listarClientes();
 
            System.out.println("  ID\t|\tDOCUMENTO\t|\tNOMBRE\t\t|\tTELEFONO");
            System.out.println("  -------------------------------------------------------------------");
            for (Cliente cliente : clientes) {
                System.out.println("  " + cliente.getId() + "\t|\t" + cliente.getDocumento() + "\t|\t" + cliente.getNombre() + "\t|\t" + cliente.getTelefono());
            }
            System.out.println("  ===================================================================");
 
        } catch (SQLException e) {
            System.out.println("  [X] Error al generar reporte: " + e.getMessage());
        }
 
        ConsolaUtil.pausar();
    }
 
    private void reporteEmpleados() {
        System.out.println("\n  ===================================================");
        System.out.println("              REPORTE GENERAL DE EMPLEADOS           ");
        System.out.println("  ===================================================");
 
        try {
            List<Empleado> empleados = adminService.listarEmpleados();
 
            System.out.println("  ID\t|\tDOCUMENTO\t|\tNOMBRE\t\t|\tSALARIO");
            System.out.println("  -------------------------------------------------------------------");
            for (Empleado empleado : empleados) {
                System.out.println("  " + empleado.getIdEmpleado() + "\t|\t" + empleado.getDocumento() + "\t|\t" + empleado.getNombre() + "\t|\t$" + empleado.getSalario());
            }
            System.out.println("  ===================================================================");
 
        } catch (SQLException e) {
            System.out.println("  [X] Error al generar reporte: " + e.getMessage());
        }
 
        ConsolaUtil.pausar();
    }
 
    private void reportePrestamos() {
        System.out.println("\n=========================================================================");
        System.out.println("                      REPORTE GENERAL DE PRESTAMOS                       ");
        System.out.println("=========================================================================");
 
        try {
            List<Prestamo> prestamos = adminService.listarPrestamos();
 
            System.out.printf(" %-6s | %-20s | %-12s | %-12s | %-10s\n", "ID", "CLIENTE", "MONTO", "SALDO", "ESTADO");
            System.out.println("-------------------------------------------------------------------------");
 
            for (Prestamo prestamo : prestamos) {
                System.out.printf(" %-6d | %-20s | %-12s | %-12s | %-10s\n",
                        prestamo.getId(),
                        prestamo.getCliente().getNombre(),
                        ConsolaUtil.formatearMoneda(prestamo.getMonto()),
                        ConsolaUtil.formatearMoneda(prestamo.getSaldoCapital()),
                        prestamo.getEstado());
            }
 
            System.out.println("=========================================================================");
 
            if (prestamos.isEmpty()) {
                System.out.println("  [i] No hay prestamos registrados en el sistema.");
            }
 
        } catch (SQLException e) {
            System.out.println("  [X] Error al generar reporte de prestamos: " + e.getMessage());
        }
 
        ConsolaUtil.pausar();
    }
 
    private void reporteMorosos() {
        System.out.println("\n=========================================================================");
        System.out.println("             REPORTE DE CLIENTES MOROSOS (INACTIVIDAD)                   ");
        System.out.println("=========================================================================");
 
        try {
            List<String[]> morosos = adminService.reporteMorosos();
 
            System.out.printf(" %-10s | %-20s | %-12s | %-15s | %-10s\n", "PRESTAMO", "CLIENTE", "TELEFONO", "ULTIMO PAGO", "DIAS SIN PAGO");
            System.out.println("----------------------------------------------------------------------------------");
 
            for (String[] moroso : morosos) {
                System.out.printf(" %-10s | %-20s | %-12s | %-15s | %-10s\n",
                        moroso[0], moroso[1], moroso[2], moroso[3], moroso[4]);
            }
 
            System.out.println("==================================================================================");
 
            if (morosos.isEmpty()) {
                System.out.println("  [i] No se registran clientes en mora por inactividad.");
            }
 
        } catch (SQLException e) {
            System.out.println("  [X] Error al generar reporte de morosos: " + e.getMessage());
        }
 
        ConsolaUtil.pausar();
    }
 
    private void reportesStream() {
        System.out.println("\n=========================================================================");
        System.out.println("                        REPORTES CON STREAM API                          ");
        System.out.println("=========================================================================");
 
        try {
            List<Prestamo> prestamos = adminService.listarPrestamos();
 
            if (prestamos.isEmpty()) {
                System.out.println("  [i] No hay prestamos registrados en el sistema.");
                ConsolaUtil.pausar();
                return;
            }
 
            Map<String, Long> cantidadPorEstado = adminService.cantidadPrestamosPorEstado(prestamos);
            Map<String, Double> montoPorEstado = adminService.montoTotalPorEstado(prestamos);
 
            System.out.println("\n  [1] Prestamos por estado");
            System.out.printf("   %-12s | %-10s | %-15s\n", "ESTADO", "CANTIDAD", "MONTO TOTAL");
            System.out.println("  -------------------------------------------------");
            cantidadPorEstado.forEach((estado, cantidad) -> System.out.printf("   %-12s | %-10d | %-15s\n",
                    estado, cantidad, ConsolaUtil.formatearMoneda(montoPorEstado.get(estado))));
 
            System.out.println("\n  [2] Top 3 clientes por monto solicitado");
            adminService.topClientesPorMonto(prestamos, 3).forEach((cliente, total) ->
                    System.out.printf("   %-22s | %-15s\n", cliente, ConsolaUtil.formatearMoneda(total)));
 
            System.out.println("\n  [3] Monto promedio por prestamo");
            System.out.println("   " + ConsolaUtil.formatearMoneda(adminService.promedioMonto(prestamos)));
 
            System.out.println("\n  [4] Prestamos por empleado");
            adminService.cantidadPrestamosPorEmpleado(prestamos).forEach((empleado, cantidad) ->
                    System.out.printf("   %-22s | %-10d\n", empleado, cantidad));
 
            System.out.println("\n=========================================================================");
 
        } catch (SQLException e) {
            System.out.println("  [X] Error al generar reportes: " + e.getMessage());
        }
 
        ConsolaUtil.pausar();
    }
}
 