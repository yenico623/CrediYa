package vistas;

import java.sql.SQLException;
import java.util.List;
import modelos.Empleado;
import modelos.Prestamo;
import servicios.AdminService;
import utilidades.ConsolaUtil;
import utilidades.MotorVisual;

public class MenuSolicitudes {

    private final AdminService adminService;

    public MenuSolicitudes() {
        this.adminService = new AdminService();
    }

    public void iniciar() {
        boolean volver = false;

        while (!volver) {
            String[] opciones = {
                "Listar solicitudes",
                "Consultar solicitud",
                "Aprobar solicitud",
                "Rechazar solicitud",
                "Asignar empleado"
            };

            MotorVisual.dibujarMenu("GESTION DE SOLICITUDES DE CREDITO", opciones, "Volver");
            int opcion = ConsolaUtil.leerEntero("   > Seleccione una opcion: ");

            switch (opcion) {
                case 1 -> listarSolicitudes();
                case 2 -> consultarSolicitud();
                case 3 -> aprobarSolicitud();
                case 4 -> rechazarSolicitud();
                case 5 -> asignarEmpleado();
                case 6 -> volver = true;
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }

    private boolean imprimirSolicitudes() throws SQLException {
        List<Prestamo> solicitudes = adminService.listarSolicitudes();

        System.out.println("\n=========================================================================");
        System.out.println("                      SOLICITUDES PENDIENTES                             ");
        System.out.println("=========================================================================");
        System.out.printf(" %-6s | %-20s | %-12s | %-8s | %-12s\n", "ID", "CLIENTE", "MONTO", "CUOTAS", "FECHA");
        System.out.println("-------------------------------------------------------------------------");

        for (Prestamo solicitud : solicitudes) {
            System.out.printf(" %-6d | %-20s | %-12s | %-8d | %-12s\n",
                    solicitud.getId(),
                    solicitud.getCliente().getNombre(),
                    ConsolaUtil.formatearMoneda(solicitud.getMonto()),
                    solicitud.getCuotas(),
                    solicitud.getFechaSolicitud());
        }

        System.out.println("=========================================================================");

        if (solicitudes.isEmpty()) {
            System.out.println("  [i] No hay solicitudes pendientes.");
        }

        return !solicitudes.isEmpty();
    }

    private void listarSolicitudes() {
        try {
            imprimirSolicitudes();
        } catch (SQLException e) {
            System.out.println("  [X] Error al listar solicitudes: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

    private void consultarSolicitud() {
        int prestamoId = ConsolaUtil.leerEntero("    > Ingrese el ID de la solicitud: ");

        try {
            Prestamo prestamo = adminService.consultarSolicitud(prestamoId);

            if (prestamo == null) {
                System.out.println("  [X] No existe una solicitud con ese ID.");
            } else {
                System.out.println("\n  ---------------------------------------------------");
                System.out.println("    ID Prestamo      : " + prestamo.getId());
                System.out.println("    Cliente          : " + prestamo.getCliente().getNombre());
                System.out.println("    Documento        : " + prestamo.getCliente().getDocumento());
                System.out.println("    Monto            : " + ConsolaUtil.formatearMoneda(prestamo.getMonto()));
                System.out.println("    Interes total    : " + ConsolaUtil.formatearMoneda(prestamo.getInteres()));
                System.out.println("    Cuotas           : " + prestamo.getCuotas());
                System.out.println("    Fecha solicitud  : " + prestamo.getFechaSolicitud());
                System.out.println("    Fecha aprobacion : " + (prestamo.getFechaAprobacion() != null ? prestamo.getFechaAprobacion() : "N/A"));
                System.out.println("    Empleado         : " + (prestamo.getEmpleado() != null ? prestamo.getEmpleado().getNombre() : "Sin asignar"));
                System.out.println("    Estado           : " + prestamo.getEstado());
                System.out.println("  ---------------------------------------------------");
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al consultar la solicitud: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

    private void aprobarSolicitud() {
        try {
            if (!imprimirSolicitudes()) {
                ConsolaUtil.pausar();
                return;
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al listar solicitudes: " + e.getMessage());
            ConsolaUtil.pausar();
            return;
        }

        int prestamoId = ConsolaUtil.leerEntero("    > ID de la solicitud a aprobar (0 para cancelar): ");

        if (prestamoId == 0) {
            System.out.println("  [i] Operacion cancelada.");
            ConsolaUtil.pausar();
            return;
        }

        String confirmacion = ConsolaUtil.leerTexto("    > Confirma la aprobacion? (SI / NO): ");

        if (!confirmacion.equalsIgnoreCase("SI")) {
            System.out.println("  [i] Operacion cancelada.");
            ConsolaUtil.pausar();
            return;
        }

        if (adminService.aprobarSolicitud(prestamoId)) {
            System.out.println("  [OK] Solicitud aprobada y plan de cuotas generado.");
            System.out.println("  [i] Asigne un empleado para activar el credito.");
        } else {
            System.out.println("  [X] No fue posible aprobar. Verifique que la solicitud exista y siga pendiente.");
        }

        ConsolaUtil.pausar();
    }

    private void rechazarSolicitud() {
        try {
            if (!imprimirSolicitudes()) {
                ConsolaUtil.pausar();
                return;
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al listar solicitudes: " + e.getMessage());
            ConsolaUtil.pausar();
            return;
        }

        int prestamoId = ConsolaUtil.leerEntero("    > ID de la solicitud a rechazar (0 para cancelar): ");

        if (prestamoId == 0) {
            System.out.println("  [i] Operacion cancelada.");
            ConsolaUtil.pausar();
            return;
        }

        String confirmacion = ConsolaUtil.leerTexto("    > Confirma el rechazo? (SI / NO): ");

        if (!confirmacion.equalsIgnoreCase("SI")) {
            System.out.println("  [i] Operacion cancelada.");
            ConsolaUtil.pausar();
            return;
        }

        try {
            if (adminService.rechazarSolicitud(prestamoId)) {
                System.out.println("  [OK] Solicitud rechazada.");
            } else {
                System.out.println("  [X] No fue posible rechazar. Verifique que la solicitud exista y siga pendiente.");
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al rechazar la solicitud: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

    private void asignarEmpleado() {
        try {
            List<Prestamo> aprobados = adminService.listarPrestamosPorAsignar();

            System.out.println("\n=========================================================================");
            System.out.println("                  PRESTAMOS APROBADOS SIN EMPLEADO                       ");
            System.out.println("=========================================================================");
            System.out.printf(" %-6s | %-20s | %-12s | %-8s | %-12s\n", "ID", "CLIENTE", "MONTO", "CUOTAS", "APROBACION");
            System.out.println("-------------------------------------------------------------------------");

            for (Prestamo prestamo : aprobados) {
                System.out.printf(" %-6d | %-20s | %-12s | %-8d | %-12s\n",
                        prestamo.getId(),
                        prestamo.getCliente().getNombre(),
                        ConsolaUtil.formatearMoneda(prestamo.getMonto()),
                        prestamo.getCuotas(),
                        prestamo.getFechaAprobacion());
            }

            System.out.println("=========================================================================");

            if (aprobados.isEmpty()) {
                System.out.println("  [i] No hay prestamos aprobados pendientes de asignacion.");
                ConsolaUtil.pausar();
                return;
            }

            List<Empleado> empleados = adminService.listarEmpleados();

            System.out.println("\n  EMPLEADOS DISPONIBLES");
            System.out.printf(" %-6s | %-22s | %-12s\n", "ID", "NOMBRE", "DOCUMENTO");
            System.out.println("-------------------------------------------------------------------------");

            for (Empleado empleado : empleados) {
                System.out.printf(" %-6d | %-22s | %-12s\n",
                        empleado.getIdEmpleado(),
                        empleado.getNombre(),
                        empleado.getDocumento());
            }

            System.out.println("=========================================================================");

            int prestamoId = ConsolaUtil.leerEntero("    > ID del prestamo (0 para cancelar): ");

            if (prestamoId == 0) {
                System.out.println("  [i] Operacion cancelada.");
                ConsolaUtil.pausar();
                return;
            }

            int empleadoId = ConsolaUtil.leerEntero("    > ID del empleado: ");

            if (adminService.asignarEmpleado(prestamoId, empleadoId)) {
                System.out.println("  [OK] Empleado asignado. El credito queda ACTIVO.");
            } else {
                System.out.println("  [X] No fue posible asignar. Verifique el ID del prestamo aprobado y el ID del empleado.");
            }

        } catch (SQLException e) {
            System.out.println("  [X] Error al asignar empleado: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }
}