package vistas;

import modelos.Cliente;
import utilidades.ConsolaUtil;
import utilidades.MotorVisual;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MenuCliente {

    public void iniciar(Cliente clienteEnSesion) {
        boolean volver = false;
        
        while (!volver) {
            String[] opciones = {
                "Ver mi informacion",
                "Ver mis prestamos"
            };

            MotorVisual.dibujarMenu("MODULO DE CLIENTES", opciones, "Cerrar sesion");
            int opcion = ConsolaUtil.leerEntero("   > Seleccione una opcion: ");

            switch (opcion) {
                case 1 -> verInformacionPersonal(clienteEnSesion);
                case 2 -> verMisPrestamos(clienteEnSesion);
                case 3 -> {
                    System.out.println("Cerrando Sesion... ");
                    volver = true;}
                
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }

    private void verInformacionPersonal(Cliente cliente) {
        System.out.println("\n  ===================================================");
        System.out.println("                 INFORMACION PERSONAL                ");
        System.out.println("  ===================================================");

        String sql = "SELECT p.nombre, p.documento, p.correo, p.telefono, p.fecha_nacimiento "
                   + "FROM clientes c INNER JOIN personas p ON c.persona_id = p.id WHERE c.id = ?";

        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, cliente.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("  Nombre           : " + rs.getString("nombre"));
                    System.out.println("  Documento        : " + rs.getString("documento"));
                    System.out.println("  Correo           : " + rs.getString("correo"));
                    System.out.println("  Telefono         : " + rs.getString("telefono"));
                    System.out.println("  F. Nacimiento    : " + rs.getDate("fecha_nacimiento"));
                } else {
                    System.out.println("  [X] No se encontro informacion del cliente.");
                }
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error de base de datos: " + e.getMessage());
        }
        System.out.println("  ===================================================");
        ConsolaUtil.pausar();
    }

    private void verMisPrestamos(Cliente cliente) {
        System.out.println("\n  ===================================================");
        System.out.println("                 MIS PRESTAMOS                       ");
        System.out.println("  ===================================================");

        String sqlPrestamos = "SELECT id, monto, saldo_capital, interes, cuotas, estado FROM prestamos WHERE cliente_id = ?";

        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sqlPrestamos)) {
            
            ps.setInt(1, cliente.getId());
            try (ResultSet rs = ps.executeQuery()) {
                boolean hayPrestamos = false;
                
                while (rs.next()) {
                    hayPrestamos = true;
                    int prestamoId = rs.getInt("id");
                    double montoDbl = rs.getDouble("monto");
                    double saldoDbl = rs.getDouble("saldo_capital");
                    int numCuotas = rs.getInt("cuotas");
                    
                    double valorCuotaMensual = (numCuotas > 0) ? (saldoDbl / numCuotas) : 0;

                    System.out.println("  > Prestamo ID     : " + prestamoId);
                    System.out.println("    Monto Solicitado: $" + String.format("%,.0f", montoDbl).replace(",", "."));
                    System.out.println("    Saldo Pendiente : $" + String.format("%,.0f", saldoDbl).replace(",", "."));
                    System.out.println("    Valor Cuota Mens: $" + String.format("%,.0f", valorCuotaMensual).replace(",", "."));
                    System.out.println("    Numero de Cuotas: " + numCuotas);
                    System.out.println("    Estado          : " + rs.getString("estado"));
                    System.out.println("  ===================================================");
                }

                if (!hayPrestamos) {
                    System.out.println("  [i] No registra prestamos activos en el sistema.");
                    System.out.println("  ===================================================");
                }

            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al consultar los prestamos: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }
}