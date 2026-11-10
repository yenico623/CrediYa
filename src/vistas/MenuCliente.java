
package vistas;

import modelos.Cliente;
import utilidades.ConsolaUtil;
import utilidades.MotorVisual;
import java.sql.SQLException;

public class MenuCliente {
    private final MenuPrestamo menuPrestamo;
    private final MenuPago menuPago;

    public MenuCliente() {
        this.menuPrestamo = new MenuPrestamo();
        this.menuPago = new MenuPago();
    }

    public void iniciar(Cliente clienteEnSesion) throws SQLException {
        boolean salir = false;

        while (!salir) {
            String[] opciones = {
                "Mis creditos",
                "Pagos"
            };

            MotorVisual.dibujarMenu(
                    "PORTAL DE CLIENTE: " + clienteEnSesion.getNombre(),
                    opciones,
                    "< Cerrar sesion");

            int opcion = ConsolaUtil.leerEntero("    > Seleccione una opcion: ");

            switch (opcion) {
                case 1 -> menuPrestamo.iniciar(clienteEnSesion);
                case 2 -> menuPago.iniciar(clienteEnSesion);
                case 3 -> {
                    System.out.println("  Cerrando sesion...");
                    salir = true;
                }
                default -> {
                    System.out.println("  [!] ERROR: Ingrese una opcion valida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }
}
