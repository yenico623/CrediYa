
package utilidades;
import java.sql.SQLException;

public class GestorErrores {
   public static String mostrarError(SQLException e){
       String mensaje;
       mensaje = switch (e.getErrorCode()) {
           case 1062 -> "[!] ERROR DEL SISTEMA: YA EXISTE UN REGISTRO CON ESTA INFORMACION, INTENTE DE NUEVO";
           case 1452 -> "[!] ERROR DEL SISTEMA: INTENTO DE REGISTRO INVALIDO";
           case 1045 -> "[!] ERROR DEL SISTEMA: ACCESO DENEGADO A LA BASE DE DATOS";
           case 1049 -> "[!] ERROR DEL SISTEMA: NO SE ENCUENTA EN LA BASE DE DATOS";
           default -> "[!] ERROR DEL SISTEMA: LA ACCION SOLICITADA NO PUEDE SER REALIZADA, CONTACTE A SOPORTE SI EL ERROR PERSISTE";
       };
       System.err.println("|CODIGO|: "+ e.getErrorCode()+ "|DETALLE|: "+ e.getMessage());
       return mensaje;
   
   } 
}