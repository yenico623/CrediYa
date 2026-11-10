
package modelos;

import java.time.LocalDate;

public class Empleado extends Persona {
    private int idEmpleado;
    private double salario;

    public Empleado() {
    }

    public Empleado(int idEmpleado, int idPersona, Usuario usuario, String nombre, String documento, String correo, String telefono, LocalDate fechaNacimiento, double salario) {
        super(idPersona, usuario, nombre, documento, correo, telefono, fechaNacimiento);
        this.idEmpleado = idEmpleado;
        this.salario = salario;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }
}
