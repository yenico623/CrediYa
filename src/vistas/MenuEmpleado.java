package vistas;

import dao.ClienteDao;
import java.sql.SQLException;
import modelos.Cliente;
import modelos.Empleado;
import utilidades.ConsolaUtil;
import utilidades.MotorVisual;
import java.util.List;

public class MenuEmpleado {
    private final ClienteDao clienteDao;
    private MenuPrestamo menuPrestamo;
    private MenuPago menuPago;

    public MenuEmpleado() {
        this.clienteDao = new ClienteDao();
        this.menuPrestamo = new MenuPrestamo();
        this.menuPago =  new MenuPago();
    }
    
    public void iniciar(Empleado empleadoEnSesion) throws SQLException {
        boolean salir = false;
        while (!salir) {
            String[] opciones = {
                "Modulo de Clientes.",
                "Modulo de Prestamos.",
                "Modulo de Pagos"
            };
            
            MotorVisual.dibujarMenu("PANEL DE ASESOR: ", opciones, "Cerrar sesion.");
            int opcion = ConsolaUtil.leerEntero("   > Seleccione una opcion: ");
            
            switch (opcion) {
                case 1 -> abrirModuloCliente(empleadoEnSesion);
                case 2 -> menuPrestamo.iniciar(empleadoEnSesion);
                case 3 -> menuPago.iniciar(empleadoEnSesion);
                
                case 4 -> {
                    System.out.println("  Cerrando sesion...");
                    salir = true;
                }
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida");
                    ConsolaUtil.pausar();
                }
            }
        }
    }
    
    private void abrirModuloCliente(Empleado empleado) throws SQLException {
        boolean volver = false;
        while (!volver) {
            String[] opcionesCliente = {
                "Registrar cliente",
                "Buscar cliente"
            };
            
            MotorVisual.dibujarMenu("MODULO CLIENTES", opcionesCliente, "< Volver");
            int opcion = ConsolaUtil.leerEntero("   > Seleccione una opcion: ");
            
            switch (opcion) {
                case 1 -> registrarNuevoCliente(empleado);
                case 2 -> buscarCliente(empleado);
                case 3 -> volver = true;
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida");
                    ConsolaUtil.pausar();
                }
            }
        }
    }

private void buscarCliente(Empleado empleado) throws SQLException {
        System.out.println("\n  ===================================================");
        System.out.println("                 LISTA GENERAL DE CLIENTES           ");
        System.out.println("  ===================================================");
        
        List<Cliente> misClientes = clienteDao.listarClientes();
        
        if (misClientes.isEmpty()) {
            System.out.println("  [i] No hay clientes registrados actualmente.");
            ConsolaUtil.pausar();
            return; 
        }
        
        System.out.println("  ID\t|\tDOCUMENTO\t|\tNOMBRE");
        System.out.println("  ---------------------------------------------------");
        for (Cliente c : misClientes) {
            System.out.println("  " + c.getId() + "\t|\t" + c.getDocumento() + "\t|\t" + c.getNombre());
        }
        System.out.println("  ===================================================");
        
        int idCliente = ConsolaUtil.leerEntero("\n    > Ingrese el ID del cliente a consultar (o 0 para cancelar): ");
        if (idCliente == 0) {
            return; 
        }
        
        Cliente cli = clienteDao.buscarClientePorId(idCliente);
        
        if (cli != null) {
            System.out.println("\n  ===================================================");
            System.out.println("                 INFORMACION DEL CLIENTE             ");
            System.out.println("  ===================================================");
            System.out.println("  Nombre: " + cli.getNombre());
            System.out.println("  Documento: " + cli.getDocumento());
            System.out.println("  Correo: " + cli.getCorreo());
            System.out.println("  Teléfono: " + cli.getTelefono());
            System.out.println("  Fecha Nacimiento: " + cli.getFechaNacimiento());
            
            System.out.println("\n  ---------------------------------------------------");
            System.out.println("                 PRESTAMOS ASOCIADOS                 ");
            System.out.println("  ---------------------------------------------------");
            
            String sqlPrestamos = "SELECT id, monto, saldo_capital, interes, cuotas, estado, fecha_aprobacion FROM prestamos WHERE cliente_id = ?";
            boolean tienePrestamos = false;
            
            try (java.sql.Connection con = utilidades.dbConnection.getInstancia().getConexion();
                 java.sql.PreparedStatement ps = con.prepareStatement(sqlPrestamos)) {
                ps.setInt(1, idCliente);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        tienePrestamos = true;
                        System.out.println("  > ID Préstamo: " + rs.getInt("id"));
                        System.out.println("    Monto: $" + rs.getDouble("monto"));
                        System.out.println("    Saldo Capital: $" + rs.getDouble("saldo_capital"));
                        System.out.println("    Interés: " + rs.getDouble("interes") + "%");
                        System.out.println("    Cuotas: " + rs.getInt("cuotas"));
                        System.out.println("    Estado: " + rs.getString("estado"));
                        System.out.println("    Fecha Aprobación: " + rs.getDate("fecha_aprobacion"));
                        System.out.println("  ---------------------------------------------------");
                    }
                }
            } catch (Exception e) {}
            
            if (!tienePrestamos) {
                System.out.println("  [i] Este cliente no registra préstamos activos.");
            }
            
        } else {
            System.out.println("  [X] Cliente no encontrado.");
        }
        ConsolaUtil.pausar();
    }

private void registrarNuevoCliente(Empleado empleadoEnSesion) throws SQLException {
        System.out.println("\n  ===================================================");
        System.out.println("                 REGISTRO DE NUEVO CLIENTE           ");
        System.out.println("  ===================================================");
        
        String documento = ConsolaUtil.leerTexto("    > Documento (o '0' para cancelar): ");
        if (documento.equals("0")) {
            return; 
        }
        
        String nombre = ConsolaUtil.leerTextoAlfabetico("    > Nombre Completo: ");
        String correo = ConsolaUtil.leerTexto("    > Correo Electronico: ");
        String telefono = ConsolaUtil.leerTexto("    > Teléfono de Contacto: ");
        
        String fechaNacimientoStr = ConsolaUtil.leerTexto("    > Fecha Nacimiento (Ej: YYYY-MM-DD o DD/MM/YYYY): ");
        java.time.LocalDate fechaNacimiento = null;
        
        String[] formatosAceptados = {"yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy/MM/dd"};
        for (String formato : formatosAceptados) {
            try {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(formato);
                fechaNacimiento = java.time.LocalDate.parse(fechaNacimientoStr.trim(), formatter);
                break; 
            } catch (Exception e) {}
        }
        
        if (fechaNacimiento == null) {
            System.out.println("    [!] ERROR: Formato de fecha no valido. Use YYYY-MM-DD o DD/MM/YYYY.");
            ConsolaUtil.pausar();
            return;
        }
        
        int edad = java.time.Period.between(fechaNacimiento, java.time.LocalDate.now()).getYears();
        if (edad < 18) {
            System.out.println("    [!] ERROR: El cliente debe ser mayor de 18 años (Edad detectada: " + edad + " años).");
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

        System.out.println("\n    [!] Guardando información en el sistema...");
        
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setDocumento(documento);
        nuevoCliente.setNombre(nombre);
        nuevoCliente.setCorreo(correo);
        nuevoCliente.setTelefono(telefono);
        nuevoCliente.setFechaNacimiento(fechaNacimiento); 
        
        boolean exito = clienteDao.registrarCliente(nuevoCliente, empleadoIdReal);
        
        if (exito) {
            System.out.println("    [OK] Cliente registrado exitosamente bajo su portafolio.");
        } else {
            System.out.println("    [X] No se pudo registrar el cliente. Verifique los datos.");
        }
        
        ConsolaUtil.pausar();
    }
}