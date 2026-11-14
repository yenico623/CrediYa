
package vistas;

import modelos.Empleado;
import utilidades.ConsolaUtil;
import utilidades.MotorVisual;
import java.sql.SQLException;

public class MenuEmpleado {
    private final MenuPrestamo menuPrestamo;
    private final MenuPago menuPago;

    public MenuEmpleado() {
        this.menuPrestamo = new MenuPrestamo();
        this.menuPago = new MenuPago();
    }

    public void iniciar(Empleado empleadoEnSesion) throws SQLException {
        boolean salir = false;

        while (!salir) {
            String[] opciones = {
                "Gestion de prestamos (EXAMEN)",
                "Gestion de solicitudes de pago (EXAMEN)"
            };

            MotorVisual.dibujarMenu("PORTAL DE EMPLEADO: " + empleadoEnSesion.getNombre(), opciones, "Cerrar sesion");
            int opcion = ConsolaUtil.leerEntero("    > Seleccione una opcion: ");

            switch (opcion) {
                case 1 -> menuPrestamo.iniciar(empleadoEnSesion);
                case 2 -> menuPago.iniciar(empleadoEnSesion);
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
