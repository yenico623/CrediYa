package vistas;

import utilidades.ConsolaUtil;
import utilidades.MotorVisual;
import modelos.Cliente;
import modelos.Empleado;
import modelos.Usuario;
import java.sql.SQLException;
import servicios.AutenticacionService;

public class MenuPrincipal {

    private final AutenticacionService autenticacionService;

    public MenuPrincipal() {
        this.autenticacionService = new AutenticacionService();
    }

    public void iniciar() throws SQLException {
        boolean salir = false;
        
        while (!salir) {
            String[] opcionesPrincipales = {
                "Portal de Clientes",
                "Acceso Operativo(EXAMEN)"
            };
            
            MotorVisual.dibujarMenu(" [$] SISTEMA FINANCIERO CREDIYA", opcionesPrincipales, "X Salir del sistema");
            
            int opcion = ConsolaUtil.leerEntero("   > Seleccione una opcion: ");
            switch (opcion) {
                case 1 -> accederPortalCliente();
                case 2 -> accederPortalOperativo();
                case 3 -> {
                    System.out.println("Saliendo del sistema... ¡Hasta pronto!");
                    salir = true;
                }
                default -> {
                    System.out.println("  [!] Opcion invalida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }
    
    private void accederPortalCliente() throws SQLException {
        boolean volver = false;
        while (!volver) {
            String[] opcionesCliente = {
                "Iniciar Sesion"
            };
            
            MotorVisual.dibujarMenu("BIENVENIDO AL PORTAL DE CLIENTES", opcionesCliente, "< Volver al menu principal");
            int opcion = ConsolaUtil.leerEntero("   > Seleccione una opcion: ");
            
            switch (opcion) {
                case 1 -> {
                    String documento = ConsolaUtil.leerTexto("    > Digite su numero de documento (o '0' para volver): ");
                    if (documento.equals("0")) {
                        break;
                    }
                    
                    Cliente clienteEncontrado = null;
                    try {
                        clienteEncontrado = autenticacionService.buscarClientePorDocumento(documento);
                    } catch (SQLException e) {
                        System.out.println("  [X] Error al buscar cliente: " + e.getMessage());
                    }

                    if (clienteEncontrado != null) {
                        System.out.println("  [OK] Acceso concedido.");
                        ConsolaUtil.pausar();
                        MenuCliente menuCliente = new MenuCliente();
                        menuCliente.iniciar(clienteEncontrado);
                    } else {
                        System.out.println("  [X] Cliente no encontrado con ese documento.");
                        ConsolaUtil.pausar();
                    }
                }
                case 2 -> volver = true;
                default -> {
                    System.out.println("  [!] Opcion invalida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }
    
    private void accederPortalOperativo() {
        boolean volver = false;

        while (!volver) {
            String[] opcionesOperativo = {
                "Iniciar sesion"
            };

            MotorVisual.dibujarMenu("BIENVENIDO AL PORTAL OPERATIVO", opcionesOperativo, "Volver al menu principal");
            int opcion = ConsolaUtil.leerEntero("   > Seleccione una opcion: ");

            switch (opcion) {
                case 1 -> {
                    String usuario = ConsolaUtil.leerTexto("    > Ingrese su usuario: ");
                    String password = ConsolaUtil.leerTexto("    > Ingrese su contrasena: ");
                    System.out.println("  [!] Validando credenciales...");

                    try {
                        Usuario usuarioLogueado = autenticacionService.autenticarUsuario(usuario, password);

                        if (usuarioLogueado == null) {
                            System.out.println("  [X] Usuario o contrasena incorrectos.");
                            ConsolaUtil.pausar();
                            break;
                        }

                        String rol = usuarioLogueado.getRol();

                        if (rol != null && rol.equalsIgnoreCase("ADMIN")) {
                            MenuAdmin menuAdmin = new MenuAdmin();
                            menuAdmin.iniciar(usuarioLogueado);

                        } else if (rol != null && rol.equalsIgnoreCase("EMPLEADO")) {
                            Empleado empleado = autenticacionService.obtenerEmpleadoPorUsuario(usuarioLogueado.getId());

                            if (empleado != null) {
                                MenuEmpleado menuEmp = new MenuEmpleado();
                                menuEmp.iniciar(empleado);
                            } else {
                                System.out.println("  [X] No se encontraron los datos del empleado.");
                                ConsolaUtil.pausar();
                            }

                        } else {
                            System.out.println("  [X] El usuario no tiene permisos para acceder al portal operativo.");
                            ConsolaUtil.pausar();
                        }

                    } catch (SQLException e) {
                        System.out.println("  [X] Error de conexion: " + e.getMessage());
                        ConsolaUtil.pausar();
                    }
                }
                case 2 -> volver = true;
                default -> {
                    System.out.println("  [!] Opcion invalida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }
}
