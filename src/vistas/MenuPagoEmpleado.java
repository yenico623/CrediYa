

package vistas;

import servicios.EmpleadoService;
import modelos.Empleado;
import utilidades.ConsolaUtil;
import utilidades.MotorVisual;
import java.sql.SQLException;
import java.util.List;

public class MenuPagoEmpleado {

    private final EmpleadoService empleadoService;

    public MenuPagoEmpleado() {
        this.empleadoService = new EmpleadoService();
    }

    public void iniciar(Empleado empleadoEnSesion) {
        boolean volver = false;

        while (!volver) {
            String[] opciones = {
                "Registrar pago de prestamos (EXAMEN)",
                "Ver solicitudes de pago pendientes",
                "Listar pagos procesados"
            };

            MotorVisual.dibujarMenu(
                    "MODULO DE PAGOS - EMPLEADO",
                    opciones,
                    "Volver al menu principal"
            );

            int opcion = ConsolaUtil.leerEntero(
                    "    > Seleccione una opcion: "
            );

            switch (opcion) {
                case 1 -> registrarPagoAsignado(empleadoEnSesion);
                case 2 -> gestionarPagosPendientes(empleadoEnSesion);
                case 3 -> listarPagosProcesados();
                case 4 -> volver = true;
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }

    private void registrarPagoAsignado(Empleado empleadoEnSesion) {
        System.out.println("\n=========================================================================");
        System.out.println("                    REGISTRAR PAGO DE PRESTAMO ASIGNADO                  ");
        System.out.println("=========================================================================");

        if (empleadoEnSesion == null || empleadoEnSesion.getIdEmpleado() <= 0) {
            System.out.println("  [X] No se pudo identificar al empleado autenticado.");
            ConsolaUtil.pausar();
            return;
        }

        int empId = empleadoEnSesion.getIdEmpleado();

        try {
            List<String[]> prestamos = empleadoService.listarPrestamosActivosAsignados(empId);

            System.out.printf(" %-6s | %-18s | %-12s | %-10s\n",
                    "ID", "CLIENTE", "SALDO PEND.", "ESTADO");
            System.out.println("-------------------------------------------------------------------------");

            for (String[] prestamo : prestamos) {
                System.out.printf(" %-6s | %-18s | %-12s | %-10s\n",
                        prestamo[0],
                        prestamo[1],
                        "$" + String.format("%,.0f", Double.parseDouble(prestamo[2])),
                        prestamo[3]);
            }

            System.out.println("=========================================================================");

            if (prestamos.isEmpty()) {
                System.out.println("  [i] No tiene prestamos activos o en mora asignados.");
                ConsolaUtil.pausar();
                return;
            }

            int prestamoId = ConsolaUtil.leerEntero(
                    "    > Ingrese el ID del prestamo a abonar: ");

            String[] prestamo = empleadoService.obtenerPrestamoParaPago(prestamoId, empId);

            if (prestamo == null) {
                System.out.println("  [X] El prestamo no existe, no esta activo o no esta asignado a usted.");
                ConsolaUtil.pausar();
                return;
            }

            String clienteNombre = prestamo[1];
            int cuotasPendientes = Integer.parseInt(prestamo[2]);

            if (cuotasPendientes == 0) {
                System.out.println("  [i] Este prestamo no tiene cuotas pendientes.");
                ConsolaUtil.pausar();
                return;
            }

            int cantidadCuotas;

            while (true) {
                cantidadCuotas = ConsolaUtil.leerEntero(
                        "    > Ingrese el numero de cuotas a pagar (1-" + cuotasPendientes + "): ");

                if (cantidadCuotas >= 1 && cantidadCuotas <= cuotasPendientes) {
                    break;
                }

                System.out.println("  [!] Ingrese una cantidad entre 1 y " + cuotasPendientes + ".");
            }

            double valorAPagar = empleadoService.calcularTotalCuotas(prestamoId, cantidadCuotas);

            if (valorAPagar <= 0) {
                System.out.println("  [X] El valor calculado no es valido.");
                ConsolaUtil.pausar();
                return;
            }

            System.out.println("\n=========================================================================");
            System.out.println("                         RESUMEN DEL PAGO                                ");
            System.out.println("=========================================================================");
            System.out.println("    ID Prestamo    : " + prestamoId);
            System.out.println("    Cliente        : " + clienteNombre);
            System.out.println("    Cuotas a pagar : " + cantidadCuotas);
            System.out.println("    Valor a pagar  : $" + String.format("%,.2f", valorAPagar));
            System.out.println("=========================================================================");

            String confirmar;

            while (true) {
                confirmar = ConsolaUtil.leerTexto(
                        "    > Escriba CONFIRMAR para continuar o CANCELAR para salir: ").trim();

                if (confirmar.equalsIgnoreCase("confirmar")
                        || confirmar.equalsIgnoreCase("c")) {
                    break;
                }

                if (confirmar.equalsIgnoreCase("cancelar")) {
                    System.out.println("  [i] Transaccion cancelada por el usuario.");
                    ConsolaUtil.pausar();
                    return;
                }

                System.out.println("  [!] Opcion no valida. Intente nuevamente.");
            }

            String metodo;

            while (true) {
                metodo = ConsolaUtil.leerTexto(
                        "    > Metodo de pago (EFECTIVO / TRANSFERENCIA / NEQUI / TARJETA): "
                ).trim().toUpperCase();

                if (metodo.equals("EFECTIVO")
                        || metodo.equals("TRANSFERENCIA")
                        || metodo.equals("NEQUI")
                        || metodo.equals("TARJETA")) {
                    break;
                }

                if (metodo.equals("CANCELAR")) {
                    System.out.println("  [i] Transaccion cancelada por el usuario.");
                    ConsolaUtil.pausar();
                    return;
                }

                System.out.println("  [!] Metodo no valido. Intente nuevamente o escriba CANCELAR.");
            }

            String referencia = null;

            if (!metodo.equals("EFECTIVO")) {
                while (true) {
                    referencia = ConsolaUtil.leerTexto(
                            "    > Ingrese el numero de referencia o comprobante: ").trim();

                    if (!referencia.isEmpty()) {
                        break;
                    }

                    System.out.println("  [!] La referencia no puede estar vacia.");
                }
            }

            String metodoBD = metodo.equals("NEQUI") ? "TRANSFERENCIA" : metodo;

            boolean exito = empleadoService.registrarPagoEmpleado(
                    prestamoId,
                    empId,
                    valorAPagar,
                    metodoBD,
                    referencia
            );

            if (exito) {
                System.out.println("\n  [OK] El pago fue registrado y aplicado correctamente.");
            } else {
                System.out.println("\n  [X] No fue posible registrar el pago.");
            }

        } catch (SQLException e) {
            System.out.println("  [X] Error al consultar o registrar el pago: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [X] Error inesperado: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

    private void gestionarPagosPendientes(Empleado empleadoEnSesion) {
        System.out.println("\n=========================================================================");
        System.out.println("                         PAGOS PENDIENTES DE REVISION                    ");
        System.out.println("=========================================================================");

        try {
            List<String[]> pagos = empleadoService.listarPagosPendientes(
                    empleadoEnSesion.getIdEmpleado()
            );

            System.out.printf(" %-6s | %-10s | %-16s | %-10s | %-12s\n",
                    "PAGO ID", "PRESTAMO", "CLIENTE", "MONTO", "REFERENCIA");
            System.out.println("-------------------------------------------------------------------------");

            for (String[] pago : pagos) {
                System.out.printf(" %-6s | %-10s | %-16s | $%-9.0f | %-12s\n",
                        pago[0],
                        pago[1],
                        pago[2],
                        Double.parseDouble(pago[3]),
                        pago[5]);
            }

            System.out.println("=========================================================================");

            if (pagos.isEmpty()) {
                System.out.println("  [i] No hay pagos pendientes en revision.");
                ConsolaUtil.pausar();
                return;
            }

            int pagoId = ConsolaUtil.leerEntero(
                    "    > Ingrese el ID del pago a procesar: "
            );

            String decision = ConsolaUtil.leerTexto(
                    "    > Desea APROBAR o RECHAZAR este pago?: "
            ).trim();

            boolean aprobar = decision.equalsIgnoreCase("aprobar")
                    || decision.equalsIgnoreCase("a")
                    || decision.equals("1");

            if (!aprobar && !decision.equalsIgnoreCase("rechazar")
                    && !decision.equalsIgnoreCase("r")
                    && !decision.equals("2")) {
                System.out.println("  [!] Decision no valida.");
                ConsolaUtil.pausar();
                return;
            }

            boolean exito = empleadoService.procesarPagoPendiente(
                    pagoId,
                    empleadoEnSesion.getIdEmpleado(),
                    aprobar
            );

            if (exito) {
                System.out.println("  [OK] Solicitud de pago procesada exitosamente.");
            } else {
                System.out.println("  [X] Error al procesar el pago.");
            }

        } catch (SQLException e) {
            System.out.println("  [X] Error al consultar pagos pendientes: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [!] Error de formato en los datos ingresados.");
        }

        ConsolaUtil.pausar();
    }

    private void listarPagosProcesados() {
        System.out.println("\n=========================================================================");
        System.out.println("                         HISTORIAL DE PAGOS PROCESADOS                   ");
        System.out.println("=========================================================================");

        try {
            List<String[]> pagos = empleadoService.listarPagosProcesados();

            System.out.printf(" %-6s | %-10s | %-10s | %-12s | %-15s\n",
                    "PAGO ID", "PRESTAMO", "MONTO", "ESTADO", "REVISOR");
            System.out.println("-------------------------------------------------------------------------");

            for (String[] pago : pagos) {
                System.out.printf(" %-6s | %-10s | $%-9.0f | %-12s | %-15s\n",
                        pago[0],
                        pago[1],
                        Double.parseDouble(pago[2]),
                        pago[3],
                        pago[4] != null ? pago[4] : "Sistema");
            }

            System.out.println("=========================================================================");

            if (pagos.isEmpty()) {
                System.out.println("  [i] No registra pagos procesados.");
            }

        } catch (SQLException e) {
            System.out.println("  [X] Error al consultar pagos procesados: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }
}
