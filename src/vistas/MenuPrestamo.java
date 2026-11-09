package vistas;

import dao.PrestamoDao;
import modelos.Empleado;
import modelos.Cliente;
import modelos.Prestamo;
import utilidades.ConsolaUtil;
import utilidades.MotorVisual;
import java.sql.SQLException;
import java.time.LocalDate;

public class MenuPrestamo {
    
    private PrestamoDao prestamoDao;

    public MenuPrestamo() {
        this.prestamoDao = new PrestamoDao();
    }

    public void iniciar(Empleado empleadoEnSesion) {
        boolean volver = false;
        
        while (!volver) {
            String[] opciones = {
                "Registrar nuevo prestamo",
                "Consultar prestamos"
            };

            MotorVisual.dibujarMenu("MODULO DE PRESTAMOS", opciones, "< Volver al Panel Principal");
            int opcion = ConsolaUtil.leerEntero("   > Seleccione una opcion: ");

            switch (opcion) {
                case 1 -> otorgarPrestamo(empleadoEnSesion);
                case 2 -> consultarPrestamos();
                case 3 -> volver = true;
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }

    private void otorgarPrestamo(Empleado empleadoEnSesion) {
        System.out.println("\n  ===================================================");
        System.out.println("                    NUEVO PRESTAMO                     ");
        System.out.println("  ===================================================");

        String sqlClientes = "SELECT c.id, p.nombre, p.documento FROM clientes c INNER JOIN personas p ON c.persona_id = p.id";
        try (java.sql.Connection con = utilidades.dbConnection.getInstancia().getConexion();
             java.sql.PreparedStatement psCli = con.prepareStatement(sqlClientes);
             java.sql.ResultSet rsCli = psCli.executeQuery()) {
            
            System.out.println("  ID\t|\tDOCUMENTO\t|\tNOMBRE");
            System.out.println("  ---------------------------------------------------");
            boolean hayClientes = false;
            while (rsCli.next()) {
                hayClientes = true;
                System.out.println("  " + rsCli.getInt("id") + "\t|\t" + rsCli.getString("documento") + "\t|\t" + rsCli.getString("nombre"));
            }
            System.out.println("  ===================================================");
            
            if (!hayClientes) {
                System.out.println("  [i] No hay clientes registrados para asociar un prestamo.");
                ConsolaUtil.pausar();
                return;
            }
            
        } catch (SQLException e) {
            System.out.println("  [X] Error al listar clientes: " + e.getMessage());
            ConsolaUtil.pausar();
            return;
        }

        int clienteId = ConsolaUtil.leerEntero("\n    > Ingrese el ID del Cliente (o '0' para cancelar): ");
        if (clienteId == 0) {
            return;
        }

        try {
            double monto = Double.parseDouble(ConsolaUtil.leerTexto("    > Monto del prestamo: "));
            int cuotas = ConsolaUtil.leerEntero("    > Numero de cuotas: ");

            estrategias.EstrategiaInteres estrategia;
            String tipoInteres = "";
            double tasaPorcentaje = 2.0; 

            if (cuotas <= 6) {
                estrategia = new estrategias.InteresSimple();
                tipoInteres = "SIMPLE";
            } else {
                estrategia = new estrategias.InteresCompuesto();
                tipoInteres = "COMUESTO";
            }

            double valorInteresCalculado = estrategia.calcularInteres(monto, cuotas);
            double montoFinalCalculado = monto + valorInteresCalculado;
            
            double valorCuotaMensual = montoFinalCalculado / cuotas;

            System.out.println("\n  ===================================================");
            System.out.println("               RESUMEN DE LA SOLICITUD               ");
            System.out.println("  ===================================================");
            System.out.println("  Modalidad Interes  : " + tipoInteres);
            System.out.println("  Monto Solicitado   : $" + String.format("%,.0f", monto).replace(",", "."));
            System.out.println("  Numero de Cuotas   : " + cuotas);
            System.out.println("  Valor del Interes  : $" + String.format("%,.0f", valorInteresCalculado).replace(",", "."));
            System.out.println("  Monto Total Final  : $" + String.format("%,.0f", montoFinalCalculado).replace(",", "."));
            System.out.println("  Valor Cuota Mensual: $" + String.format("%,.0f", valorCuotaMensual).replace(",", "."));
            System.out.println("  ===================================================");
            
            String confirmar = ConsolaUtil.leerTexto("    > Desea confirmar y aprobar el prestamo? (S/N): ");
            if (!confirmar.equalsIgnoreCase("s")) {
                System.out.println("  [i] Operacion cancelada.");
                ConsolaUtil.pausar();
                return;
            }

            int empleadoIdReal = empleadoEnSesion.getId();
            String sqlVerificar = "SELECT id FROM empleados WHERE persona_id IN (SELECT id FROM personas WHERE usuario_id = ?)";
            try (java.sql.Connection con = utilidades.dbConnection.getInstancia().getConexion();
                 java.sql.PreparedStatement ps = con.prepareStatement(sqlVerificar)) {
                ps.setInt(1, empleadoEnSesion.getId());
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        empleadoIdReal = rs.getInt("id");
                    }
                }
            } catch (Exception e) {}

            System.out.println("\n  [!] Procesando solicitud de credito...");

            Cliente cliente = new Cliente();
            cliente.setId(clienteId);

            Empleado empleadoReal = new Empleado();
            empleadoReal.setId(empleadoIdReal);

            Prestamo nuevoPrestamo = new Prestamo();
            nuevoPrestamo.setCliente(cliente);
            nuevoPrestamo.setEmpleado(empleadoReal);
            nuevoPrestamo.setMonto(monto);
            nuevoPrestamo.setSaldoCapital(montoFinalCalculado);
            nuevoPrestamo.setInteres(tasaPorcentaje);
            nuevoPrestamo.setCuotas(cuotas);
            nuevoPrestamo.setFechaSolicitud(LocalDate.now());
            nuevoPrestamo.setFechaAprobacion(LocalDate.now());
            nuevoPrestamo.setEstado("ACTIVO");

            int idGenerado = prestamoDao.registrarPrestamo(nuevoPrestamo);
            
            if (idGenerado > 0) {
                System.out.println("  [OK] Prestamo registrado y aprobado exitosamente. ID: " + idGenerado);
            } else {
                System.out.println("  [X] Error al registrar el prestamo en la base de datos.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("  [!] ERROR: Digite valores numericos validos.");
        } catch (SQLException e) {
            System.out.println("  [X] Error de base de datos: " + e.getMessage());
        }
        
        ConsolaUtil.pausar();
    }
    private void consultarPrestamos() {
        System.out.println("\n  ===================================================");
        System.out.println("                 LISTA GENERAL DE PRESTAMOS          ");
        System.out.println("  ===================================================");
        
        String sql = "SELECT p.id, p.monto, p.saldo_capital, p.estado, per.nombre as cliente_nombre "
                   + "FROM prestamos p "
                   + "INNER JOIN clientes c ON p.cliente_id = c.id "
                   + "INNER JOIN personas per ON c.persona_id = per.id";
        
        boolean hayPrestamos = false;
        
        try (java.sql.Connection con = utilidades.dbConnection.getInstancia().getConexion();
             java.sql.PreparedStatement ps = con.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            
            System.out.println("  ID\t|\tCLIENTE\t\t|\tMONTO\t|\tESTADO");
            System.out.println("  ---------------------------------------------------");
            
            while (rs.next()) {
                hayPrestamos = true;
                double montoDbl = rs.getDouble("monto");
                String montoFmt = "$" + String.format("%,.0f", montoDbl).replace(",", ".");
                System.out.println("  " + rs.getInt("id") + "\t|\t" + rs.getString("cliente_nombre") + "\t|\t" + montoFmt + "\t|\t" + rs.getString("estado"));
            }
            
            System.out.println("  ===================================================");
            
            if (!hayPrestamos) {
                System.out.println("  [i] No hay prestamos registrados en el sistema.");
                ConsolaUtil.pausar();
                return;
            }
            
            int prestamoId = ConsolaUtil.leerEntero("\n    > Ingrese el ID del prestamo a consultar (o '0' para cancelar): ");
            if (prestamoId == 0) {
                return;
            }
            
            String sqlDetalle = "SELECT p.*, per.nombre as cliente_nombre, per.documento as cliente_doc "
                              + "FROM prestamos p "
                              + "INNER JOIN clientes c ON p.cliente_id = c.id "
                              + "INNER JOIN personas per ON c.persona_id = per.id "
                              + "WHERE p.id = ?";
                              
            try (java.sql.PreparedStatement psDetalle = con.prepareStatement(sqlDetalle)) {
                psDetalle.setInt(1, prestamoId);
                try (java.sql.ResultSet rsDetalle = psDetalle.executeQuery()) {
                    if (rsDetalle.next()) {
                        double montoDet = rsDetalle.getDouble("monto");
                        String montoDetFmt = "$" + String.format("%,.0f", montoDet).replace(",", ".");
                        
                        double saldoDet = rsDetalle.getDouble("saldo_capital");
                        String saldoDetFmt = "$" + String.format("%,.0f", saldoDet).replace(",", ".");

                        System.out.println("\n  ===================================================");
                        System.out.println("                 DETALLE DEL PRESTAMO                ");
                        System.out.println("  ===================================================");
                        System.out.println("  ID Prestamo    : " + rsDetalle.getInt("id"));
                        System.out.println("  Cliente        : " + rsDetalle.getString("cliente_nombre") + " (Doc: " + rsDetalle.getString("cliente_doc") + ")");
                        System.out.println("  Monto          : " + montoDetFmt);
                        System.out.println("  Saldo Capital  : " + saldoDetFmt);
                        System.out.println("  Tasa de Interes: " + rsDetalle.getDouble("interes") + "%");
                        System.out.println("  Numero Cuotas  : " + rsDetalle.getInt("cuotas"));
                        System.out.println("  F. Solicitud   : " + rsDetalle.getDate("fecha_solicitud"));
                        System.out.println("  F. Aprobacion  : " + rsDetalle.getDate("fecha_aprobacion"));
                        System.out.println("  Estado         : " + rsDetalle.getString("estado"));
                        System.out.println("  ===================================================");
                    } else {
                        System.out.println("  [X] Prestamo no encontrado.");
                    }
                }
            }
            
        } catch (SQLException e) {
            System.out.println("  [X] Error al consultar los prestamos: " + e.getMessage());
        }
        
        ConsolaUtil.pausar();
    }
}