
package vistas;

import servicios.ClienteService;
import modelos.Cliente;
import utilidades.ConsolaUtil;
import utilidades.MotorVisual;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;

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

        String sqlPrestamos = "SELECT p.id, p.monto, p.saldo_capital, p.cuotas, p.estado, "
                + "EXISTS (SELECT 1 FROM pagos pg WHERE pg.prestamo_id = p.id AND pg.estado = 'EN_REVISION') AS pago_pendiente "
                + "FROM prestamos p WHERE p.cliente_id = ? AND p.estado IN ('ACTIVO', 'EN_MORA') "
                + "ORDER BY p.id";

        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sqlPrestamos)) {

            ps.setInt(1, clienteEnSesion.getId());

            try (ResultSet rs = ps.executeQuery()) {
                boolean hayPrestamos = false;

                System.out.printf("%-10s | %-14s | %-14s | %-10s | %-15s%n",
                        "PRESTAMO", "MONTO", "SALDO CAPITAL", "CUOTAS", "ESTADO");
                System.out.println("-------------------------------------------------------------------------");

                while (rs.next()) {
                    hayPrestamos = true;

                    String estado = rs.getBoolean("pago_pendiente")
                            ? "EN REVISION"
                            : rs.getString("estado");

                    System.out.printf("%-10d | $%-13.0f | $%-13.0f | %-10d | %-15s%n",
                            rs.getInt("id"),
                            rs.getDouble("monto"),
                            rs.getDouble("saldo_capital"),
                            rs.getInt("cuotas"),
                            estado);
                }

                System.out.println("=========================================================================");

                if (!hayPrestamos) {
                    System.out.println("  [i] No registra prestamos activos para realizar pagos.");
                    ConsolaUtil.pausar();
                    return;
                }
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

            String sqlPrestamoValido = "SELECT id FROM prestamos WHERE id = ? AND cliente_id = ? "
                    + "AND estado IN ('ACTIVO', 'EN_MORA')";

            try (PreparedStatement psValido = con.prepareStatement(sqlPrestamoValido)) {
                psValido.setInt(1, prestamoId);
                psValido.setInt(2, clienteEnSesion.getId());

                try (ResultSet rs = psValido.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("  [!] El prestamo no existe, no pertenece al cliente o no esta activo.");
                        ConsolaUtil.pausar();
                        return;
                    }
                }
            }

            String sqlPagoPendiente = "SELECT id FROM pagos WHERE prestamo_id = ? AND estado = 'EN_REVISION' LIMIT 1";

            try (PreparedStatement psPendiente = con.prepareStatement(sqlPagoPendiente)) {
                psPendiente.setInt(1, prestamoId);

                try (ResultSet rs = psPendiente.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("  [!] Este prestamo ya tiene un pago pendiente de revision.");
                        System.out.println("  [i] Espere a que el empleado revise el pago anterior.");
                        ConsolaUtil.pausar();
                        return;
                    }
                }
            }

            String sqlContarCuotas = "SELECT COUNT(*) AS total FROM cuotas_plan "
                    + "WHERE prestamo_id = ? AND estado IN ('PENDIENTE', 'ABONADA')";

            int totalCuotas;

            try (PreparedStatement psCuotas = con.prepareStatement(sqlContarCuotas)) {
                psCuotas.setInt(1, prestamoId);

                try (ResultSet rs = psCuotas.executeQuery()) {
                    rs.next();
                    totalCuotas = rs.getInt("total");
                }
            }

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

            String sqlCuotas = "SELECT numero_cuota, capital_pendiente, interes_pendiente, mora_acumulada "
                    + "FROM cuotas_plan WHERE prestamo_id = ? AND estado IN ('PENDIENTE', 'ABONADA') "
                    + "ORDER BY numero_cuota ASC LIMIT ?";

            BigDecimal totalPagar = BigDecimal.ZERO;
            StringBuilder detalleCuotas = new StringBuilder();

            try (PreparedStatement psCuotas = con.prepareStatement(sqlCuotas)) {
                psCuotas.setInt(1, prestamoId);
                psCuotas.setInt(2, numeroCuotas);

                try (ResultSet rs = psCuotas.executeQuery()) {
                    while (rs.next()) {
                        BigDecimal capital = BigDecimal.valueOf(rs.getDouble("capital_pendiente"));
                        BigDecimal interes = BigDecimal.valueOf(rs.getDouble("interes_pendiente"));
                        BigDecimal mora = BigDecimal.valueOf(rs.getDouble("mora_acumulada"));
                        BigDecimal valorCuota = capital.add(interes).add(mora);

                        totalPagar = totalPagar.add(valorCuota);

                        detalleCuotas.append("    Cuota #")
                                .append(rs.getInt("numero_cuota"))
                                .append(" | Capital: $")
                                .append(String.format("%,.2f", capital))
                                .append(" | Interes: $")
                                .append(String.format("%,.2f", interes))
                                .append(" | Mora: $")
                                .append(String.format("%,.2f", mora))
                                .append(" | Total: $")
                                .append(String.format("%,.2f", valorCuota))
                                .append("\n");
                    }
                }
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

        String sql = "SELECT pa.id, pa.prestamo_id, pa.monto, pa.metodo_pago, pa.numero_referencia, pa.fecha_reporte, pa.estado "
                + "FROM pagos pa "
                + "INNER JOIN prestamos p ON pa.prestamo_id = p.id "
                + "WHERE p.cliente_id = ?";

        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clienteEnSesion.getId());

            try (ResultSet rs = ps.executeQuery()) {
                boolean hayPagos = false;

                System.out.printf(" %-6s | %-10s | %-12s | %-15s | %-12s\n",
                        "PAGO ID", "PRESTAMO", "MONTO", "ESTADO", "FECHA");
                System.out.println("-------------------------------------------------------------------------");

                while (rs.next()) {
                    hayPagos = true;

                    System.out.printf(" %-6d | %-10d | $%-11.0f | %-15s | %-12s\n",
                            rs.getInt("id"),
                            rs.getInt("prestamo_id"),
                            rs.getDouble("monto"),
                            rs.getString("estado"),
                            rs.getDate("fecha_reporte").toString());
                }

                System.out.println("=========================================================================");

                if (!hayPagos) {
                    System.out.println("  [i] No registra pagos historicos.");
                }
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al consultar historial: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }
}
