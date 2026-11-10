package vistas;

import servicios.ClienteService;
import modelos.Cliente;
import modelos.CuotaPlan;
import utilidades.ConsolaUtil;
import utilidades.MotorVisual;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.List;

public class MenuPagoCliente {

    private final ClienteService clienteService;

    public MenuPagoCliente() {
        this.clienteService = new ClienteService();
    }

    public void iniciar(Cliente clienteEnSesion) {
        boolean volver = false;

        while (!volver) {
            String[] opciones = {
                "Registrar pago",
                "Ver historial de pagos"
            };

            MotorVisual.dibujarMenu(
                    "MODULO DE PAGOS - CLIENTE",
                    opciones,
                    "Volver al menu principal"
            );

            int opcion = ConsolaUtil.leerEntero(
                    "    > Seleccione una opcion: "
            );

            switch (opcion) {
                case 1 -> registrarPagoCliente(clienteEnSesion);
                case 2 -> verHistorialPagos(clienteEnSesion);
                case 3 -> volver = true;
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }

    private void registrarPagoCliente(Cliente clienteEnSesion) {
        System.out.println("\n=========================================================================");
        System.out.println("                         REGISTRAR PAGO DE CUOTAS                        ");
        System.out.println("=========================================================================");

        try {
            List<String[]> prestamos = clienteService.listarPrestamosParaPago(clienteEnSesion.getId());

            System.out.printf("%-10s | %-14s | %-14s | %-10s | %-15s%n",
                    "PRESTAMO", "MONTO", "SALDO CAPITAL", "CUOTAS", "ESTADO");
            System.out.println("-------------------------------------------------------------------------");

            for (String[] prestamo : prestamos) {
                String estado = Boolean.parseBoolean(prestamo[5]) ? "EN REVISION" : prestamo[4];

                System.out.printf("%-10d | $%-13.0f | $%-13.0f | %-10d | %-15s%n",
                        Integer.parseInt(prestamo[0]),
                        Double.parseDouble(prestamo[1]),
                        Double.parseDouble(prestamo[2]),
                        Integer.parseInt(prestamo[3]),
                        estado);
            }

            System.out.println("=========================================================================");

            if (prestamos.isEmpty()) {
                System.out.println("  [i] No registra prestamos activos para realizar pagos.");
                ConsolaUtil.pausar();
                return;
            }

            String entradaPrestamo = ConsolaUtil.leerTexto(
                    "    > Ingrese el ID del prestamo a abonar o CANCELAR: ");

            if (entradaPrestamo.equalsIgnoreCase("CANCELAR")) {
                System.out.println("  [i] Operacion cancelada.");
                ConsolaUtil.pausar();
                return;
            }

            int prestamoId;

            try {
                prestamoId = Integer.parseInt(entradaPrestamo);

                if (prestamoId <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                System.out.println("  [!] ID de prestamo no valido.");
                ConsolaUtil.pausar();
                return;
            }

            if (!clienteService.prestamoActivoDelCliente(prestamoId, clienteEnSesion.getId())) {
                System.out.println("  [!] El prestamo no existe, no pertenece al cliente o no esta activo.");
                ConsolaUtil.pausar();
                return;
            }

            if (clienteService.tienePagoEnRevision(prestamoId)) {
                System.out.println("  [!] Este prestamo ya tiene un pago pendiente de revision.");
                System.out.println("  [i] Espere a que el empleado revise el pago anterior.");
                ConsolaUtil.pausar();
                return;
            }

            int totalCuotas = clienteService.contarCuotasPendientes(prestamoId);

            if (totalCuotas == 0) {
                System.out.println("  [i] Este prestamo no tiene cuotas pendientes.");
                ConsolaUtil.pausar();
                return;
            }

            System.out.println("\n  Cuotas disponibles para pagar: " + totalCuotas);

            int numeroCuotas;

            while (true) {
                String entradaCuotas = ConsolaUtil.leerTexto(
                        "    > Numero de cuotas que desea pagar o abonar (CANCELAR): ");

                if (entradaCuotas.equalsIgnoreCase("CANCELAR")) {
                    System.out.println("  [i] Operacion cancelada.");
                    ConsolaUtil.pausar();
                    return;
                }

                try {
                    numeroCuotas = Integer.parseInt(entradaCuotas);

                    if (numeroCuotas >= 1 && numeroCuotas <= totalCuotas) {
                        break;
                    }

                    System.out.println("  [!] Seleccione entre 1 y " + totalCuotas + " cuotas.");

                } catch (NumberFormatException e) {
                    System.out.println("  [!] Ingrese un numero entero valido.");
                }
            }

            List<CuotaPlan> cuotas = clienteService.consultarCuotasAPagar(prestamoId, numeroCuotas);
            BigDecimal totalPagar = clienteService.calcularTotalAPagar(cuotas);
            StringBuilder detalleCuotas = new StringBuilder();

            for (CuotaPlan cuota : cuotas) {
                detalleCuotas.append("    Cuota #")
                        .append(cuota.getNumeroCuota())
                        .append(" | Capital: $")
                        .append(String.format("%,.2f", BigDecimal.valueOf(cuota.getCapitalPendiente())))
                        .append(" | Interes: $")
                        .append(String.format("%,.2f", BigDecimal.valueOf(cuota.getInteresPendiente())))
                        .append(" | Mora: $")
                        .append(String.format("%,.2f", BigDecimal.valueOf(cuota.getMoraAcumulada())))
                        .append(" | Total: $")
                        .append(String.format("%,.2f", clienteService.calcularValorCuota(cuota)))
                        .append("\n");
            }

            if (totalPagar.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("  [!] No se encontro un saldo valido para pagar.");
                ConsolaUtil.pausar();
                return;
            }

            System.out.println("\n=========================================================================");
            System.out.println("                         RESUMEN DEL PAGO                               ");
            System.out.println("=========================================================================");
            System.out.println("    Prestamo seleccionado: " + prestamoId);
            System.out.println("    Numero de cuotas: " + numeroCuotas);
            System.out.println("-------------------------------------------------------------------------");
            System.out.print(detalleCuotas);
            System.out.println("-------------------------------------------------------------------------");
            System.out.printf("    TOTAL A PAGAR: $%,.2f%n", totalPagar);
            System.out.println("=========================================================================");

            String metodo;

            while (true) {
                metodo = ConsolaUtil.leerTexto(
                        "    > Metodo (TRANSFERENCIA / TARJETA) o CANCELAR: "
                ).toUpperCase();

                if (metodo.equals("CANCELAR")) {
                    System.out.println("  [i] Operacion cancelada.");
                    ConsolaUtil.pausar();
                    return;
                }

                if (metodo.equals("TRANSFERENCIA") || metodo.equals("TARJETA")) {
                    break;
                }

                System.out.println("  [!] Metodo de pago no valido.");
            }

            String referencia = ConsolaUtil.leerTexto(
                    "    > Numero de referencia o CANCELAR: "
            );

            if (referencia.equalsIgnoreCase("CANCELAR")) {
                System.out.println("  [i] Operacion cancelada.");
                ConsolaUtil.pausar();
                return;
            }

            if (referencia.trim().isEmpty()) {
                System.out.println("  [!] La referencia no puede estar vacia.");
                ConsolaUtil.pausar();
                return;
            }

            System.out.println("\n=========================================================================");
            System.out.println("                       CONFIRMACION DEL REPORTE                          ");
            System.out.println("=========================================================================");
            System.out.println("    Prestamo       : " + prestamoId);
            System.out.println("    Cuotas         : " + numeroCuotas);
            System.out.printf("    Valor a reportar: $%,.2f%n", totalPagar);
            System.out.println("    Metodo de pago : " + metodo);
            System.out.println("    Referencia     : " + referencia);
            System.out.println("    Estado inicial : EN_REVISION");
            System.out.println("=========================================================================");

            String confirmacion = ConsolaUtil.leerTexto(
                    "    > ¿Confirma el reporte? (SI / NO): ");

            if (!confirmacion.equalsIgnoreCase("SI")) {
                System.out.println("  [i] Reporte cancelado. No se registro ningun pago.");
                ConsolaUtil.pausar();
                return;
            }

            boolean exitoPago = clienteService.registrarPagoPendiente(
                    prestamoId,
                    totalPagar.doubleValue(),
                    metodo,
                    referencia
            );

            if (exitoPago) {
                System.out.println("\n  [OK] Pago reportado correctamente.");
                System.out.println("  [i] El pago quedo EN_REVISION para aprobacion del empleado.");
            } else {
                System.out.println("\n  [X] No fue posible registrar el pago.");
                System.out.println("  [i] Verifique que no exista otro pago pendiente para este prestamo.");
            }

        } catch (SQLException e) {
            System.out.println("  [X] Error al procesar el reporte: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

    private void verHistorialPagos(Cliente clienteEnSesion) {
        System.out.println("\n=========================================================================");
        System.out.println("                         HISTORIAL DE PAGOS                              ");
        System.out.println("=========================================================================");

        try {
            List<String[]> pagos = clienteService.listarHistorialPagos(clienteEnSesion.getId());

            System.out.printf(" %-6s | %-10s | %-12s | %-15s | %-12s\n",
                    "PAGO ID", "PRESTAMO", "MONTO", "ESTADO", "FECHA");
            System.out.println("-------------------------------------------------------------------------");

            for (String[] pago : pagos) {
                System.out.printf(" %-6d | %-10d | $%-11.0f | %-15s | %-12s\n",
                        Integer.parseInt(pago[0]),
                        Integer.parseInt(pago[1]),
                        Double.parseDouble(pago[2]),
                        pago[3],
                        pago[4]);
            }

            System.out.println("=========================================================================");

            if (pagos.isEmpty()) {
                System.out.println("  [i] No registra pagos historicos.");
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al consultar historial: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }
}
