
package vistas;

import modelos.Cliente;
import modelos.Empleado;

public class MenuPago {

    private final MenuPagoCliente menuPagoCliente;
    private final MenuPagoEmpleado menuPagoEmpleado;

    public MenuPago() {
        this.menuPagoCliente = new MenuPagoCliente();
        this.menuPagoEmpleado = new MenuPagoEmpleado();
    }

    public void iniciar(Cliente clienteEnSesion) {
        menuPagoCliente.iniciar(clienteEnSesion);
    }

    public void iniciar(Empleado empleadoEnSesion) {
        menuPagoEmpleado.iniciar(empleadoEnSesion);
    }
}
