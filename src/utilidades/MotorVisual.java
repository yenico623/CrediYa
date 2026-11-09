
package utilidades;


public class MotorVisual {
    public static void  dibujarMenu(String titulo, String[] opciones, String opcionFinal ){
        int ancho = Math.max(42, titulo.length()+4);
        for (String op : opciones){
            ancho = Math.max(ancho,op.length() + 8);
            
        }
        ancho = Math.max(ancho, opcionFinal.length() + 8);
        System.out.println("\n  ╭" + "─".repeat(ancho) + "╮");
        imprimirLinea("  " + titulo, ancho);
        System.out.println("  ├" + "─".repeat(ancho) + "┤");

        for (int i = 0; i < opciones.length; i++) {
            String fila = String.format("  (%d) %s", i + 1, opciones[i]);
            imprimirLinea(fila, ancho);
        }

        
        imprimirLinea("  " + "·".repeat(ancho - 4), ancho);

        String filaFinal = String.format("  (%d) %s", opciones.length + 1, opcionFinal);
        imprimirLinea(filaFinal, ancho);

        System.out.println("  ╰" + "─".repeat(ancho) + "╯");
    }

    
    private static void imprimirLinea(String contenido, int anchoTotal) {
        int espacios = anchoTotal - contenido.length();
        System.out.println("  │" + contenido + " ".repeat(Math.max(0, espacios)) + "│");
    }
        
        
   

    
}
