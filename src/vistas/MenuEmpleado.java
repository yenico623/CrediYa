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

    public MenuEmpleado() {
        this.clienteDao = new ClienteDao();
    }
    
    public void iniciar(Empleado empleadoEnSesion) throws SQLException{
        boolean salir = false;
        while(!salir){
            String[] opciones = {
                "Modulo de Clientes.",
                "Modulo de Prestamos.",
                "Modulo de Pagos"
            };
            
            MotorVisual.dibujarMenu("PANEL DE ASESOR: ", opciones, "Cerrar sesion.");
            int opcion = ConsolaUtil.leerEntero("   » Seleccione una opcion: ");
            
            switch(opcion){
                case 1 -> abrirModuloCliente(empleadoEnSesion);
                case 2 -> ConsolaUtil.pausar();
                case 3 -> ConsolaUtil.pausar();
                case 4 -> {
                    System.out.println("Cerrando sesion...");
                    salir = true;
                }
                default -> {
                    System.out.println("[!] ERROR: Ingrese una opcion valida");
                    ConsolaUtil.pausar();
                }
            }
        }
    }
    
    private void abrirModuloCliente(Empleado empleado) throws SQLException{
        boolean volver = false;
        while(!volver){
            String[] opcionesCliente = {
                "Resgistrar cliente",
                "Buscar cliente",
                "Actualizar datos"
            };
            
            MotorVisual.dibujarMenu("MODULO CLIENTES", opcionesCliente, "← Volver");
            int opcion = ConsolaUtil.leerEntero("   » Selecione una opcion: ");
            
            switch(opcion){
                case 1 -> {
                    System.out.println("Pendiente");
                    ConsolaUtil.pausar();
                }
                case 2 -> buscarCliente(empleado);
                case 3 -> {
                    System.out.println("pendiente");
                    ConsolaUtil.pausar();
                }
                case 4 -> volver = true;
                default -> {
                    System.out.println("[!] ERROR: Ingrese una  opcion valida");
                    ConsolaUtil.pausar();
                }
            }
        }
    }

    private void buscarCliente(Empleado empleado) throws SQLException {
        System.out.println("\n  ===================================================");
        System.out.println("               LISTA DE CLIENTES ASIGNADOS           ");
        System.out.println("  ===================================================");
        
        List<Cliente> misClientes = clienteDao.listarClientePorEmpleado(empleado.getId());
        
        if (misClientes.isEmpty()) {
            System.out.println("  [i] No tienes clientes asignados actualmente.");
            ConsolaUtil.pausar();
            return; 
        }
        
        System.out.println("  ID\t|\tDOCUMENTO\t|\tNOMBRE");
        System.out.println("  ---------------------------------------------------");
        for (Cliente c : misClientes) {
            System.out.println("  " + c.getId() + "\t|\t" + c.getDocumento() + "\t|\t" + c.getNombre());
        }
        System.out.println("  ===================================================");
        
        int idCliente = ConsolaUtil.leerEntero("\n    ↳ Ingrese el ID del cliente (o '0' para cancelar): ");
        
        if (idCliente == 0) {
            return; 
        }
        
        
        System.out.println("    Reporte en construcción...");
        ConsolaUtil.pausar();
    }
}