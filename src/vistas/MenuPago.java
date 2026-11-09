package vistas;

import dao.PagoDao;
import modelos.Empleado;
import utilidades.ConsolaUtil;
import utilidades.MotorVisual;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class MenuPago {
    private PagoDao pagoDao;

    public MenuPago() {
        this.pagoDao = new PagoDao();
    }

    public void iniciar(Empleado empleadoEnSesion) {
        boolean volver = false;
        while (!volver) {
            String[] opciones = {
                "Registrar pago",
                "Consultar pagos"
            };

            MotorVisual.dibujarMenu("MODULO DE PAGOS", opciones, "< Volver al Panel Principal");
            int opcion = ConsolaUtil.leerEntero("   > Seleccione una opcion: ");

            switch (opcion) {
                case 1 -> registrarPago(empleadoEnSesion);
                case 2 -> consultarPagos();
                case 3 -> volver = true;
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }

    private void registrarPago(Empleado empleado) {
        System.out.println("\n  ===================================================");
        System.out.println("                 REGISTRAR NUEVO PAGO                ");
        System.out.println("  ===================================================");

        String sqlPrestamos = "SELECT p.id, p.monto, p.saldo_capital, per.nombre as cliente_nombre FROM prestamos p INNER JOIN clientes c ON p.cliente_id = c.id INNER JOIN personas per ON c.persona_id = per.id WHERE p.estado = 'ACTIVO'";
        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sqlPrestamos);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("  ID PRESTAMO\t|\tCLIENTE\t\t|\tSALDO PENDIENTE");
            System.out.println("  ---------------------------------------------------");
            boolean hayPrestamos = false;
            while (rs.next()) {
                hayPrestamos = true;
                System.out.println("  " + rs.getInt("id") + "\t\t|\t" + rs.getString("cliente_nombre") + "\t|\t$" + rs.getDouble("saldo_capital"));
            }
            System.out.println("  ===================================================");

            if (!hayPrestamos) {
                System.out.println("  [i] No hay prestamos activos para registrar pagos.");
                ConsolaUtil.pausar();
                return;
            }

        } catch (SQLException e) {
            System.out.println("  [X] Error al listar prestamos: " + e.getMessage());
            ConsolaUtil.pausar();
            return;
        }

        int prestamoId = ConsolaUtil.leerEntero("\n    > Ingrese el ID del prestamo a abonar (o '0' para cancelar): ");
        if (prestamoId == 0) {
            return;
        }

        try {
            double monto = Double.parseDouble(ConsolaUtil.leerTexto("    > Monto del pago: "));
            
            System.out.println("\n    > Seleccione el metodo de pago:");
            System.out.println("    1. Efectivo");
            System.out.println("    2. Transferencia / Nequi");
            System.out.println("    3. Tarjeta");
            int opcionMetodo = ConsolaUtil.leerEntero("    > Opcion: ");

            String metodoPago = "EFECTIVO";
            if (opcionMetodo == 2) metodoPago = "TRANSFERENCIA";
            if (opcionMetodo == 3) metodoPago = "TARJETA";

            String referencia = "";
            if (!metodoPago.equals("EFECTIVO")) {
                referencia = ConsolaUtil.leerTexto("    > Numero de referencia o comprobante: ");
            } else {
                referencia = "EFECTIVO-" + System.currentTimeMillis();
            }

            int empleadoIdReal = empleado.getId();
            String sqlVerificar = "SELECT id FROM empleados WHERE persona_id IN (SELECT id FROM personas WHERE usuario_id = ?)";
            try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
                 PreparedStatement ps = con.prepareStatement(sqlVerificar)) {
                ps.setInt(1, empleado.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        empleadoIdReal = rs.getInt("id");
                    }
                }
            } catch (Exception e) {}

            int idGenerado = pagoDao.registrarPagoConDetalle(prestamoId, metodoPago, referencia, monto, empleadoIdReal);

            if (idGenerado > 0) {
                System.out.println("\n  [OK] Pago registrado exitosamente con ID: " + idGenerado);
                System.out.println("  [i] Se ha descontado el abono al saldo capital del prestamo y actualizado la cuota.");
            } else {
                System.out.println("\n  [X] Error al registrar el pago.");
            }

        } catch (NumberFormatException e) {
            System.out.println("  [!] ERROR: Ingrese valores numericos validos.");
        } catch (SQLException e) {
            System.out.println("  [X] Error de base de datos: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

   private void consultarPagos() {
        System.out.println("\n=========================================================================");
        System.out.println("                         LISTA GENERAL DE PAGOS                          ");
        System.out.println("=========================================================================");

        try {
            List<String[]> lista = pagoDao.listarPagos();

            if (lista.isEmpty()) {
                System.out.println("  [i] No hay pagos registrados en el sistema.");
            } else {
                System.out.printf(" %-8s | %-10s | %-20s | %-12s | %-12s | %-10s\n", 
                                  "ID PAGO", "PRESTAMO", "CLIENTE", "MONTO", "METODO", "FECHA");
                System.out.println("-------------------------------------------------------------------------");
                
                for (String[] p : lista) {
                    double montoDbl = Double.parseDouble(p[3]);
                    String montoFmt = "$" + String.format("%,.0f", montoDbl).replace(",", ".");

                    System.out.printf(" %-8s | %-10s | %-20s | %-12s | %-12s | %-10s\n", 
                                      p[0], p[1], p[2], montoFmt, p[4], p[5]);
                }
                System.out.println("=========================================================================");
            }

        } catch (SQLException e) {
            System.out.println("  [X] Error al consultar los pagos: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }
}
