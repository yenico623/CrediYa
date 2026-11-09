package vistas;

import utilidades.ConsolaUtil;
import utilidades.MotorVisual;
import modelos.Cliente;
import modelos.Persona;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MenuPrincipal {
    public void iniciar() {
        boolean salir = false;
        
        while (!salir) {
            String[] opcionesPrincipales = {
                "Portal de Clientes",
                "Acceso Operativo"
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
    
    private void accederPortalCliente() {
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
                    
                    Cliente clienteEncontrado = buscarClientePorDocumento(documento);
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
    
    private Cliente buscarClientePorDocumento(String documento) {
        Cliente cliente = null;
        String sql = "SELECT c.id, p.nombre, p.documento, p.correo, p.telefono FROM clientes c INNER JOIN personas p ON c.persona_id = p.id WHERE p.documento = ?";
        
        try (Connection con = utilidades.dbConnection.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cliente = new Cliente();
                    cliente.setId(rs.getInt("id"));
                    cliente.setNombre(rs.getString("nombre"));
                    cliente.setDocumento(rs.getString("documento"));
                }
            }
        } catch (SQLException e) {
            System.out.println("  [X] Error al buscar cliente: " + e.getMessage());
        }
        return cliente;
    }
    
    private void accederPortalOperativo() {
        boolean volver = false;
        dao.UsuarioDao usuarioDao = new dao.UsuarioDao();
        
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
                        modelos.Usuario usuarioLogueado = usuarioDao.autenticarUsuario(usuario, password);
                        
                        if (usuarioLogueado != null) {
                            System.out.println("  [OK] Acceso concedido.");
                            ConsolaUtil.pausar();
                            
                            String rol = usuarioLogueado.getRol();
                            
                            if (rol != null && rol.equalsIgnoreCase("ADMIN")) {
                                vistas.MenuAdmin menuAdmin = new vistas.MenuAdmin();
                                menuAdmin.iniciar(usuarioLogueado);
                            } else {
                                modelos.Empleado empleado = new modelos.Empleado();
                                empleado.setId(usuarioLogueado.getId());
                                vistas.MenuEmpleado menuEmp = new vistas.MenuEmpleado();
                                menuEmp.iniciar(empleado);
                            }
                            
                            volver = true; 
                        } else {
                            System.out.println("  [X] Usuario o contrasena incorrectos.");
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