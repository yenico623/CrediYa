
package vistas;
import utilidades.ConsolaUtil;
import utilidades.MotorVisual;

public class MenuPrincipal {
    public void iniciar(){
        boolean salir = false;
        
        while (!salir){
        String[] opcionesPrincipales = {
            "Portal de Clientes.",
            "Acceso Operativo."
        };
        
        MotorVisual.dibujarMenu(" ◈ SISTEMA FINANCIERO CREDIYA", opcionesPrincipales, "Salir del sistema.");
        
        int opcion = ConsolaUtil.leerEntero("   » Selecione una opcion: ");
        switch (opcion){
            case 1:
                accederPortalCliente();
                break;
            case 2: 
                accederPortalOperativo();
                break;
            case 3:
                System.out.println("Saliendo del sistema... ¡Hasta apronto!");
                salir = true;
                
                break;
        }
        }
    }
    
    private void accederPortalCliente(){
        boolean  volver = false;
        while(!volver){
            String[] opcionesCliente = {
            "Iniciar Sesion"
            };
            
            MotorVisual.dibujarMenu("BIENVENIDO AL PORTAL DE CLIENTES", opcionesCliente, "Volver al menu principal.");
            int opcion = ConsolaUtil.leerEntero("   »Seleccione una opcion: ");
            
            switch(opcion){
                case 1:
                    String documento = ConsolaUtil.leerTexto("     ↳ Digite su número de documento o 0 para salir: ");
                    if(documento.equals("0")){
                        break;
                    }
                    System.out.println("\n [OK] buscando cliente...");
                    ConsolaUtil.pausar();
                    break;
                case 2: 
                    volver = true;
                    iniciar();
                    break;

                      }
        }
        
    }
    
    private void accederPortalOperativo(){
        boolean volver = false;
        
        while(!volver){
            String[] OpcionesOperativo ={
                "Iniciar sesion."
            };
            
            MotorVisual.dibujarMenu("BIENVENIDO AL PORTAL OPERATIVO", OpcionesOperativo, "Volver al menú principal.");
            int opcion = ConsolaUtil.leerEntero("   »Seleccione una opcion: ");
            switch(opcion){
                case 1:
                    String usuario = ConsolaUtil.leerTexto("    ↳ Ingrese su usuario: ");
                    String password = ConsolaUtil.leerTexto("   ↳ Ingrese su contraseña: ");
                    System.out.println("[OK] Validando credenciales...");
                    ConsolaUtil.pausar();
                    break;
                case 2:
                    volver= true;
                    iniciar();
                    break;
            }
            
        }
    }

}
