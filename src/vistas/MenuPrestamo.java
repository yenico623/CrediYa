
package vistas;

import servicios.ClienteService;
import modelos.Cliente;
import modelos.Empleado;
import modelos.Prestamo;
import utilidades.ConsolaUtil;
import utilidades.MotorVisual;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MenuPrestamo {
    private final ClienteService clienteService;

    public MenuPrestamo() {
        this.clienteService = new ClienteService();
    }

    public void iniciar(Empleado empleadoEnSesion) {
        boolean volver = false;

        while (!volver) {
            String[] opciones = {
                "Listar prestamos asignados",
                "Consultar detalle de prestamo"
            };

            MotorVisual.dibujarMenu("MODULO DE PRESTAMOS - EMPLEADO", opciones, "Volver al menu principal");
            int opcion = ConsolaUtil.leerEntero("    > Seleccione una opcion: ");

            switch (opcion) {
                case 1 -> listarPrestamosAsignados(empleadoEnSesion);
                case 2 -> consultarDetallePrestamo(empleadoEnSesion);
                case 3 -> volver = true;
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }

    public void iniciar(Cliente clienteEnSesion) throws SQLException {
        boolean volver = false;

        while (!volver) {
            String[] opciones = {
                "Ver mis creditos",
                "Solicitar credito"
            };

            MotorVisual.dibujarMenu("MODULO DE CREDITOS", opciones, "Volver al menu principal");
            int opcion = ConsolaUtil.leerEntero("    > Seleccione una opcion: ");

            switch (opcion) {
                case 1 -> verMisCreditos(clienteEnSesion);
                case 2 -> solicitarPrestamo(clienteEnSesion);
                case 3 -> volver = true;
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }

    private void listarPrestamosAsignados(Empleado empleadoEnSesion) {
        System.out.println("\n=========================================================================");
        System.out.println("                         PRESTAMOS ASIGNADOS A MI GESTION                ");
        System.out.println("=========================================================================");

        String sql = "SELECT p.id, per.nombre AS cliente, p.monto, p.saldo_capital, p.estado, p.proxima_fecha_pago "
                + "FROM prestamos p "
                + "INNER JOIN clientes c ON p.cliente_id = c.id "
                + "INNER JOIN personas per ON c.persona_id = per.id "
                + "WHERE p.empleado_id = ?";

        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, empleadoEnSesion.getIdEmpleado());

            try (ResultSet rs = ps.executeQuery()) {
                boolean hay = false;

                System.out.printf(" %-6s | %-18s | %-12s | %-10s | %-12s\n",
                        "ID", "CLIENTE", "SALDO", "ESTADO", "PROX. PAGO");
                System.out.println("-------------------------------------------------------------------------");

                while (rs.next()) {
                    hay = true;

                    System.out.printf(" %-6d | %-18s | $%-11.0f | %-10s | %-12s\n",
                            rs.getInt("id"),
                            rs.getString("cliente"),
                            rs.getDouble("saldo_capital"),
                            rs.getString("estado"),
                            rs.getDate("proxima_fecha_pago") != null
                                    ? rs.getDate("proxima_fecha_pago").toString()
                                    : "N/A");
                }

                System.out.println("=========================================================================");

                if (!hay) {
                    System.out.println("  [i] No tiene prestamos asignados actualmente.");
                }
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al consultar prestamos asignados: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

    private void consultarDetallePrestamo(Empleado empleadoEnSesion) {
        System.out.println("\n=========================================================================");
        System.out.println("                         CONSULTAR DETALLE DE CREDITO                    ");
        System.out.println("=========================================================================");

        int prestamoId = ConsolaUtil.leerEntero("    > Ingrese el ID del prestamo a consultar: ");

        String sql = "SELECT cp.numero_cuota, cp.fecha_vencimiento, cp.capital_esperado, cp.interes_esperado, cp.estado "
                + "FROM cuotas_plan cp "
                + "INNER JOIN prestamos p ON cp.prestamo_id = p.id "
                + "WHERE cp.prestamo_id = ? AND p.empleado_id = ? "
                + "ORDER BY cp.numero_cuota";

        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, prestamoId);
            ps.setInt(2, empleadoEnSesion.getIdEmpleado());

            try (ResultSet rs = ps.executeQuery()) {
                boolean hay = false;

                System.out.printf(" %-8s | %-12s | %-10s | %-10s | %-10s\n",
                        "CUOTA #", "VENCIMIENTO", "CAPITAL", "INTERES", "ESTADO");
                System.out.println("-------------------------------------------------------------------------");

                while (rs.next()) {
                    hay = true;

                    System.out.printf(" %-8d | %-12s | $%-9.0f | $%-9.0f | %-10s\n",
                            rs.getInt("numero_cuota"),
                            rs.getDate("fecha_vencimiento").toString(),
                            rs.getDouble("capital_esperado"),
                            rs.getDouble("interes_esperado"),
                            rs.getString("estado"));
                }

                System.out.println("=========================================================================");

                if (!hay) {
                    System.out.println("  [i] No se encontro plan de cuotas para este prestamo.");
                }
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al consultar detalle: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

    private void verMisCreditos(Cliente clienteEnSesion) {
        System.out.println("\n=========================================================================");
        System.out.println("                           MIS CREDITOS ASOCIADOS                        ");
        System.out.println("=========================================================================");

        String sql = "SELECT id, monto, saldo_capital, interes, cuotas, estado, fecha_solicitud, fecha_aprobacion, proxima_fecha_pago "
                + "FROM prestamos WHERE cliente_id = ?";

        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clienteEnSesion.getId());

            try (ResultSet rs = ps.executeQuery()) {
                boolean hayPrestamos = false;

                System.out.printf(" %-8s | %-12s | %-12s | %-10s | %-12s\n",
                        "ID", "MONTO", "SALDO", "ESTADO", "PROX. PAGO");
                System.out.println("-------------------------------------------------------------------------");

                while (rs.next()) {
                    hayPrestamos = true;

                    System.out.printf(" %-8d | $%-11.0f | $%-11.0f | %-10s | %-12s\n",
                            rs.getInt("id"),
                            rs.getDouble("monto"),
                            rs.getDouble("saldo_capital"),
                            rs.getString("estado"),
                            rs.getDate("proxima_fecha_pago") != null
                                    ? rs.getDate("proxima_fecha_pago").toString()
                                    : "N/A");
                }

                System.out.println("=========================================================================");

                if (!hayPrestamos) {
                    System.out.println("  [i] No registra creditos en el sistema.");
                }
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al consultar creditos: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

    private void solicitarPrestamo(Cliente clienteEnSesion) {
        System.out.println("\n=========================================================================");
        System.out.println("                       SIMULACION DE NUEVO PRESTAMO                      ");
        System.out.println("=========================================================================");

        double monto = ConsolaUtil.leerDecimal("    > Ingrese el monto que desea solicitar ($): ");
        int cuotas = ConsolaUtil.leerEntero("    > Ingrese el numero de cuotas deseadas: ");

        Prestamo simulacion = clienteService.simularNuevoPrestamo(
                clienteEnSesion.getId(), monto, cuotas);

        double valorCuotaMensual = simulacion.getSaldoCapital();

        System.out.println("\n  ---------------------------------------------------------------------");
        System.out.println("                         RESUMEN DE LA SIMULACION                      ");
        System.out.println("  ---------------------------------------------------------------------");
        System.out.println("    Monto Solicitado : $" + String.format("%,.0f", monto).replace(",", "."));
        System.out.println("    Interes Total    : $" + String.format("%,.0f", simulacion.getInteres()).replace(",", "."));
        System.out.println("    Numero de Cuotas : " + cuotas);
        System.out.println("    Valor Cuota Aprox: $" + String.format("%,.0f", valorCuotaMensual).replace(",", "."));
        System.out.println("  ---------------------------------------------------------------------");

        String deseaEnviar = ConsolaUtil.leerTexto(
                "    > Desea presentar esta solicitud? (SI / NO): ");

        if (deseaEnviar.equalsIgnoreCase("si")) {
            boolean exito = clienteService.confirmarSolicitudPrestamo(
                    clienteEnSesion.getId(),
                    monto,
                    cuotas,
                    simulacion.getInteres());

            if (exito) {
                System.out.println("  [OK] Solicitud de prestamo enviada con exito. Pendiente de revision.");
            } else {
                System.out.println("  [X] Error al enviar solicitud.");
            }
        } else {
            System.out.println("  [i] Solicitud cancelada por el usuario.");
        }

        ConsolaUtil.pausar();
    }
}
