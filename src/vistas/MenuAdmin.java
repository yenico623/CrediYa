package vistas;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import modelos.Cliente;
import modelos.Empleado;
import modelos.Usuario;
import servicios.AdminService;
import utilidades.ConsolaUtil;
import utilidades.MotorVisual;

public class MenuAdmin {

    private final AdminService adminService;
    private final MenuSolicitudes menuSolicitudes;
    private final MenuReportes menuReportes;

    public MenuAdmin() {
        this.adminService = new AdminService();
        this.menuSolicitudes = new MenuSolicitudes();
        this.menuReportes = new MenuReportes();
    }

    public void iniciar(Usuario adminEnSesion) {
        boolean volver = false;

        while (!volver) {
            String[] opciones = {
                "Gestion de empleados",
                "Gestion de clientes",
                "Solicitudes de credito",
                "Reportes"
            };

            MotorVisual.dibujarMenu("PANEL DE ADMINISTRACION", opciones, "Cerrar sesion");
            int opcion = ConsolaUtil.leerEntero("   > Seleccione una opcion: ");

            switch (opcion) {
                case 1 -> menuEmpleados();
                case 2 -> menuClientes();
                case 3 -> menuSolicitudes.iniciar();
                case 4 -> menuReportes.iniciar();
                case 5 -> volver = true;
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }

    private void menuEmpleados() {
        boolean volver = false;

        while (!volver) {
            String[] opciones = {
                "Crear empleado",
                "Listar empleados",
                "Consultar empleado",
                "Actualizar empleado"
            };

            MotorVisual.dibujarMenu("GESTION DE EMPLEADOS", opciones, "Volver");
            int opcion = ConsolaUtil.leerEntero("   > Seleccione una opcion: ");

            switch (opcion) {
                case 1 -> crearEmpleado();
                case 2 -> listarEmpleados();
                case 3 -> consultarEmpleado();
                case 4 -> actualizarEmpleado();
                case 5 -> volver = true;
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }

    private void menuClientes() {
        boolean volver = false;

        while (!volver) {
            String[] opciones = {
                "Crear cliente",
                "Listar clientes",
                "Consultar cliente",
                "Actualizar cliente"
            };

            MotorVisual.dibujarMenu("GESTION DE CLIENTES", opciones, "Volver");
            int opcion = ConsolaUtil.leerEntero("   > Seleccione una opcion: ");

            switch (opcion) {
                case 1 -> crearCliente();
                case 2 -> listarClientes();
                case 3 -> consultarCliente();
                case 4 -> actualizarCliente();
                case 5 -> volver = true;
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }

    private void crearEmpleado() {
        System.out.println("\n  ===================================================");
        System.out.println("                    CREAR EMPLEADO                   ");
        System.out.println("  ===================================================");

        String nombre = ConsolaUtil.leerTexto("    > Nombre completo: ");
        String documento = ConsolaUtil.leerTexto("    > Documento: ");
        String correo = ConsolaUtil.leerTexto("    > Correo: ");
        String telefono = ConsolaUtil.leerTexto("    > Telefono: ");
        LocalDate fechaNacimiento = ConsolaUtil.leerFecha("    > Fecha de nacimiento (AAAA-MM-DD): ");
        double salario = ConsolaUtil.leerDecimal("    > Salario ($): ");
        String username = ConsolaUtil.leerTexto("    > Usuario de acceso: ");
        String password = ConsolaUtil.leerTexto("    > Contrasena de acceso: ");

        Empleado empleado = new Empleado(0, 0, null, nombre, documento, correo, telefono, fechaNacimiento, salario);

        if (adminService.crearEmpleado(empleado, username, password)) {
            System.out.println("  [OK] Empleado creado correctamente.");
        } else {
            System.out.println("  [X] No fue posible crear el empleado. Verifique que el salario sea mayor a cero y que el documento, correo y usuario no esten registrados.");
        }

        ConsolaUtil.pausar();
    }

    private void listarEmpleados() {
        System.out.println("\n  ===================================================================================");
        System.out.println("                                  LISTADO DE EMPLEADOS                             ");
        System.out.println("  ===================================================================================");

        try {
            List<Empleado> empleados = adminService.listarEmpleados();

            System.out.printf(" %-6s | %-12s | %-22s | %-26s | %-12s | %-12s\n",
                    "ID", "DOCUMENTO", "NOMBRE", "CORREO", "TELEFONO", "SALARIO");
            System.out.println("  -----------------------------------------------------------------------------------");

            for (Empleado empleado : empleados) {
                System.out.printf(" %-6d | %-12s | %-22s | %-26s | %-12s | %-12s\n",
                        empleado.getIdEmpleado(),
                        empleado.getDocumento(),
                        empleado.getNombre(),
                        empleado.getCorreo(),
                        empleado.getTelefono(),
                        ConsolaUtil.formatearMoneda(empleado.getSalario()));
            }

            System.out.println("  ===================================================================================");

            if (empleados.isEmpty()) {
                System.out.println("  [i] No hay empleados registrados.");
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al listar empleados: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

    private void consultarEmpleado() {
        String documento = ConsolaUtil.leerTexto("    > Documento del empleado: ");

        try {
            Empleado empleado = adminService.consultarEmpleado(documento);

            if (empleado == null) {
                System.out.println("  [X] No existe un empleado con ese documento.");
            } else {
                imprimirEmpleado(empleado);
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al consultar el empleado: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

    private void actualizarEmpleado() {
        String documento = ConsolaUtil.leerTexto("    > Documento del empleado a actualizar: ");

        try {
            Empleado empleado = adminService.consultarEmpleado(documento);

            if (empleado == null) {
                System.out.println("  [X] No existe un empleado con ese documento.");
                ConsolaUtil.pausar();
                return;
            }

            imprimirEmpleado(empleado);

            String correo = ConsolaUtil.leerTexto("    > Nuevo correo: ");
            String telefono = ConsolaUtil.leerTexto("    > Nuevo telefono: ");
            double salario = ConsolaUtil.leerDecimal("    > Nuevo salario ($): ");

            if (adminService.actualizarEmpleado(documento, correo, telefono, salario)) {
                System.out.println("  [OK] Empleado actualizado correctamente.");
            } else {
                System.out.println("  [X] No fue posible actualizar el empleado. El salario debe ser mayor a cero.");
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al actualizar el empleado: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

    private void imprimirEmpleado(Empleado empleado) {
        System.out.println("\n  ---------------------------------------------------");
        System.out.println("    ID Empleado      : " + empleado.getIdEmpleado());
        System.out.println("    Nombre           : " + empleado.getNombre());
        System.out.println("    Documento        : " + empleado.getDocumento());
        System.out.println("    Correo           : " + empleado.getCorreo());
        System.out.println("    Telefono         : " + empleado.getTelefono());
        System.out.println("    Fecha nacimiento : " + empleado.getFechaNacimiento());
        System.out.println("    Salario          : " + ConsolaUtil.formatearMoneda(empleado.getSalario()));
        System.out.println("  ---------------------------------------------------");
    }

    private void crearCliente() {
        System.out.println("\n  ===================================================");
        System.out.println("                    CREAR CLIENTE                    ");
        System.out.println("  ===================================================");

        String nombre = ConsolaUtil.leerTexto("    > Nombre completo: ");
        String documento = ConsolaUtil.leerTexto("    > Documento: ");
        String correo = ConsolaUtil.leerTexto("    > Correo: ");
        String telefono = ConsolaUtil.leerTexto("    > Telefono: ");
        LocalDate fechaNacimiento = ConsolaUtil.leerFecha("    > Fecha de nacimiento (AAAA-MM-DD): ");

        Cliente cliente = new Cliente(0, null, nombre, documento, correo, telefono, fechaNacimiento);

        if (adminService.crearCliente(cliente)) {
            System.out.println("  [OK] Cliente creado correctamente. Su acceso es el numero de documento.");
        } else {
            System.out.println("  [X] No fue posible crear el cliente. Verifique que el documento y el correo no esten registrados.");
        }

        ConsolaUtil.pausar();
    }

    private void listarClientes() {
        System.out.println("\n  =====================================================================================");
        System.out.println("                                   LISTADO DE CLIENTES                               ");
        System.out.println("  =====================================================================================");

        try {
            List<Cliente> clientes = adminService.listarClientes();

            System.out.printf(" %-6s | %-12s | %-22s | %-26s | %-12s\n",
                    "ID", "DOCUMENTO", "NOMBRE", "CORREO", "TELEFONO");
            System.out.println("  -------------------------------------------------------------------------------------");

            for (Cliente cliente : clientes) {
                System.out.printf(" %-6d | %-12s | %-22s | %-26s | %-12s\n",
                        cliente.getId(),
                        cliente.getDocumento(),
                        cliente.getNombre(),
                        cliente.getCorreo(),
                        cliente.getTelefono());
            }

            System.out.println("  =====================================================================================");

            if (clientes.isEmpty()) {
                System.out.println("  [i] No hay clientes registrados.");
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al listar clientes: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

    private void consultarCliente() {
        String documento = ConsolaUtil.leerTexto("    > Documento del cliente: ");

        try {
            Cliente cliente = adminService.consultarCliente(documento);

            if (cliente == null) {
                System.out.println("  [X] No existe un cliente con ese documento.");
            } else {
                imprimirCliente(cliente);
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al consultar el cliente: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

    private void actualizarCliente() {
        String documento = ConsolaUtil.leerTexto("    > Documento del cliente a actualizar: ");

        try {
            Cliente cliente = adminService.consultarCliente(documento);

            if (cliente == null) {
                System.out.println("  [X] No existe un cliente con ese documento.");
                ConsolaUtil.pausar();
                return;
            }

            imprimirCliente(cliente);

            String correo = ConsolaUtil.leerTexto("    > Nuevo correo: ");
            String telefono = ConsolaUtil.leerTexto("    > Nuevo telefono: ");

            if (adminService.actualizarCliente(documento, correo, telefono)) {
                System.out.println("  [OK] Cliente actualizado correctamente.");
            } else {
                System.out.println("  [X] No fue posible actualizar el cliente.");
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al actualizar el cliente: " + e.getMessage());
        }

        ConsolaUtil.pausar();
    }

    private void imprimirCliente(Cliente cliente) {
        System.out.println("\n  ---------------------------------------------------");
        System.out.println("    ID Cliente       : " + cliente.getId());
        System.out.println("    Nombre           : " + cliente.getNombre());
        System.out.println("    Documento        : " + cliente.getDocumento());
        System.out.println("    Correo           : " + cliente.getCorreo());
        System.out.println("    Telefono         : " + cliente.getTelefono());
        System.out.println("    Fecha nacimiento : " + cliente.getFechaNacimiento());
        System.out.println("  ---------------------------------------------------");
    }
}
