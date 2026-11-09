
package utilidades;
import java.util.Scanner;

public class ConsolaUtil {
    private static final Scanner scanner = new Scanner(System.in);
    
    public static  String leerTexto(String mensaje){
        while(true){
            System.out.println(mensaje);
            String entrada = scanner.nextLine().trim();
            
            if(!entrada.isEmpty()){
                return entrada;
            }
            System.out.println("[!] ERROR: Campo obligatorio. Intente nuevamente.");
        }
    }
    
    public static String leerTextoAlfabetico( String mensaje){
        while(true){
        String entrada = leerTexto(mensaje);
        if (entrada.matches("[a-zA-áéíóúÁÉÍÓÚnÑ ]+")){
            return entrada;
        }
            System.out.println("[!] ERROR: Por favor ingrese un valor valido.");
        }
    }
    
    
    public static int leerEntero(String mensaje){
        while (true){
            try{
                System.out.println(mensaje);
                String entrada = scanner.nextLine().trim();
                return Integer.parseInt(entrada);
                
            }catch(NumberFormatException e){
                System.out.println("[!] ERROR: Digite un número entero.");
            }
        }}
        
        public static double leerDecimal(String mensaje){
            while(true){
                try{
                    System.out.println(mensaje);
                    String entrada = scanner.nextLine().trim();
                    entrada = entrada.replace(",",".");
                    return Double.parseDouble(entrada);
                    
                    
                }catch(NumberFormatException e){
                    System.out.println("[!]ERROR: Ingrese un monto válido.");
                }
            
            }
        }
        
        public static void pausar(){
            System.out.println("\n ---------- Presione ENTER para continua ----------");
            scanner.nextLine();
        }
}

