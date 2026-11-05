
package modelos;

import java.time.LocalDate;


public class Empleado extends Persona{
    private double salario;
    
    public  Empleado (){
    
    }
    
    public Empleado(int id, Usuario usuario, String nombre, String documento, String correo, String telefono, LocalDate fechaNacimiento,double salario){
    super(id, usuario, nombre, documento, correo, telefono, fechaNacimiento);
    this.salario = salario;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }
    
}
