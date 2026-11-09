package vistas;

import modelos.Usuario;
import utilidades.ConsolaUtil;
import utilidades.MotorVisual;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MenuAdmin {

    public void iniciar(Usuario adminEnSesion) {
        boolean volver = false;
        
        while (!volver) {
            String[] opciones = {
                "Gestionar empleados (Eliminar)",
                "Reporte general de clientes",
                "Reporte general de empleados",
                "Reporte general de prestamos",
                "Reporte de clientes morosos"
            };

            MotorVisual.dibujarMenu("PANEL DE ADMINISTRACION", opciones, "Cerrar sesion");
            int opcion = ConsolaUtil.leerEntero("   > Seleccione una opcion: ");

            switch (opcion) {
                case 1 -> gestionarEmpleados();
                case 2 -> reporteClientes();
                case 3 -> reporteEmpleados();
                case 4 -> reportePrestamos();
                case 5 -> reporteMorosos();
                case 6 -> volver = true;
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }

    private void gestionarEmpleados() {
        System.out.println("\n  ===================================================");
        System.out.println("                 GESTION DE EMPLEADOS                ");
        System.out.println("  ===================================================");

        String sql = "SELECT e.id, p.nombre, p.documento, e.salario FROM empleados e INNER JOIN personas p ON e.persona_id = p.id";

        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("  ID EMPLEADO\t|\tDOCUMENTO\t|\tNOMBRE");
            System.out.println("  ---------------------------------------------------");
            boolean hayEmpleados = false;
            while (rs.next()) {
                hayEmpleados = true;
                System.out.println("  " + rs.getInt("id") + "\t\t|\t" + rs.getString("documento") + "\t|\t" + rs.getString("nombre"));
            }
            System.out.println("  ===================================================");

            if (!hayEmpleados) {
                System.out.println("  [i] No hay empleados registrados.");
                ConsolaUtil.pausar();
                return;
            }

        } catch (SQLException e) {
            System.out.println("  [X] Error al listar empleados: " + e.getMessage());
            ConsolaUtil.pausar();
            return;
        }

        int empleadoId = ConsolaUtil.leerEntero("\n    > Ingrese el ID del empleado a eliminar (o '0' para cancelar): ");
        if (empleadoId == 0) {
            return;
        }

        String confirmar = ConsolaUtil.leerTexto("    > Seguro que desea eliminar este empleado? (S/N): ");
        if (!confirmar.equalsIgnoreCase("s")) {
            System.out.println("  [i] Operacion cancelada.");
            ConsolaUtil.pausar();
            return;
        }

        String sqlDelete = "DELETE FROM empleados WHERE id = ?";
        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement psDel = con.prepareStatement(sqlDelete)) {
            psDel.setInt(1, empleadoId);
            int filas = psDel.executeUpdate();

            if (filas > 0) {
                System.out.println("  [OK] Empleado eliminado exitosamente del sistema.");
            } else {
                System.out.println("  [X] No se encontro un empleado con ese ID.");
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al eliminar : " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

    private void reporteClientes() {
        System.out.println("\n  ===================================================");
        System.out.println("               REPORTE GENERAL DE CLIENTES           ");
        System.out.println("  ===================================================");

        String sql = "SELECT c.id, p.nombre, p.documento, p.correo, p.telefono FROM clientes c INNER JOIN personas p ON c.persona_id = p.id";

        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("  ID\t|\tDOCUMENTO\t|\tNOMBRE\t\t|\tTELEFONO");
            System.out.println("  -------------------------------------------------------------------");
            while (rs.next()) {
                System.out.println("  " + rs.getInt("id") + "\t|\t" + rs.getString("documento") + "\t|\t" + rs.getString("nombre") + "\t|\t" + rs.getString("telefono"));
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

        String sql = "SELECT e.id, p.nombre, p.documento, e.salario FROM empleados e INNER JOIN personas p ON e.persona_id = p.id";

        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("  ID\t|\tDOCUMENTO\t|\tNOMBRE\t\t|\tSALARIO");
            System.out.println("  -------------------------------------------------------------------");
            while (rs.next()) {
                System.out.println("  " + rs.getInt("id") + "\t|\t" + rs.getString("documento") + "\t|\t" + rs.getString("nombre") + "\t|\t$" + rs.getDouble("salario"));
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

        String sql = "SELECT p.id, per.nombre as cliente, p.monto, p.saldo_capital, p.cuotas, p.estado FROM prestamos p INNER JOIN clientes c ON p.cliente_id = c.id INNER JOIN personas per ON c.persona_id = per.id";

        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.printf(" %-6s | %-20s | %-12s | %-12s | %-10s\n", "ID", "CLIENTE", "MONTO", "SALDO", "ESTADO");
            System.out.println("-------------------------------------------------------------------------");
            
            boolean hayPrestamos = false;
            while (rs.next()) {
                hayPrestamos = true;
                double montoDbl = rs.getDouble("monto");
                double saldoDbl = rs.getDouble("saldo_capital");
                
                String montoFmt = "$" + String.format("%,.0f", montoDbl).replace(",", ".");
                String saldoFmt = "$" + String.format("%,.0f", saldoDbl).replace(",", ".");
                
                // Filas formateadas con las mismas columnas fijas
                System.out.printf(" %-6d | %-20s | %-12s | %-12s | %-10s\n", 
                                  rs.getInt("id"), 
                                  rs.getString("cliente"), 
                                  montoFmt, 
                                  saldoFmt, 
                                  rs.getString("estado"));
            }
            
            System.out.println("=========================================================================");

            if (!hayPrestamos) {
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

        String sql = "SELECT p.id as prestamo_id, per.nombre as cliente_nombre, per.telefono, "
                   + "MAX(pa.fecha_reporte) as ultimo_pago, "
                   + "DATEDIFF(CURDATE(), MAX(pa.fecha_reporte)) as dias_sin_pagar "
                   + "FROM prestamos p "
                   + "INNER JOIN clientes c ON p.cliente_id = c.id "
                   + "INNER JOIN personas per ON c.persona_id = per.id "
                   + "LEFT JOIN pagos pa ON p.id = pa.prestamo_id "
                   + "GROUP BY p.id, per.nombre, per.telefono "
                   + "HAVING dias_sin_pagar > 30 OR ultimo_pago IS NULL";

        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.printf(" %-10s | %-20s | %-12s | %-15s | %-10s\n", "PRESTAMO", "CLIENTE", "TELEFONO", "ULTIMO PAGO", "DIAS SIN PAGO");
            System.out.println("----------------------------------------------------------------------------------");
            
            boolean hayMorosos = false;
            while (rs.next()) {
                hayMorosos = true;
                String ultimoPago = rs.getDate("ultimo_pago") != null ? rs.getDate("ultimo_pago").toString() : "SIN PAGOS";
                String dias = rs.getDate("ultimo_pago") != null ? rs.getInt("dias_sin_pagar") + " dias" : "N/A (Inactivo)";

                System.out.printf(" %-10d | %-20s | %-12s | %-15s | %-10s\n", 
                                  rs.getInt("prestamo_id"), 
                                  rs.getString("cliente_nombre"), 
                                  rs.getString("telefono"), 
                                  ultimoPago, 
                                  dias);
            }
            
            System.out.println("==================================================================================");

            if (!hayMorosos) {
                System.out.println("  [i] No se registran clientes en mora por inactividad.");
            }

        } catch (SQLException e) {
            System.out.println("  [X] Error al generar reporte de morosos: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }
}