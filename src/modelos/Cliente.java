
package modelos;

import java.time.LocalDate;

public class Cliente extends Persona{
    public Cliente (){}
    
    public Cliente(int id, Usuario usuario, String nombre, String documento, String correo, String telefono, LocalDate fechaNacimiento){
    super(id, usuario, nombre, documento, correo, telefono, fechaNacimiento);
    
    }
    
}
