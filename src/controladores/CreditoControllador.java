
package controladores;
import dao.ClienteDao;
import dao.PrestamoDao;
import modelos.Cliente;
import modelos.Empleado;
import modelos.Prestamo;
import estrategias.EstrategiaInteres;
import estrategias.InteresCompuesto;
import estrategias.InteresSimple;
import java.sql.SQLException;
import java.time.LocalDate;

public class CreditoControllador {
    private ClienteDao clienteDao;
    private PrestamoDao prestamoDao;

    public CreditoControllador() {
        this.clienteDao = new ClienteDao();
        this.prestamoDao = new PrestamoDao();
    }
    public void procesarSolicitudCredito(String documentoCliente,Empleado empleadoAtiende, double monto, double cuotas ) throws SQLException{
        Cliente cliente = clienteDao.buscarCliente(documentoCliente);
        
        if (cliente == null){
            System.out.println("[!] ERROR: No se encontró ningún cliente con el id: "+ documentoCliente);
            return;
        }
        
        EstrategiaInteres estrategia;
        String tipoEstrategia;
        if (cuotas <=6){
            estrategia = new InteresSimple();
            tipoEstrategia = "Interés Simple [CORTO PLAZO]";
        }else{
            estrategia = new InteresCompuesto();
            tipoEstrategia = "Interés Compuesto [LARGO PLAZO]";
        }
        
        double totalInteres = estrategia.calcularInteres(monto, (int) cuotas);
        double saldoCapitalTotal = monto + totalInteres;
        
        Prestamo nuevoPrestamo = new Prestamo(
                0,
                cliente,
                empleadoAtiende,
                monto,
                saldoCapitalTotal,
                totalInteres, (int) cuotas,
                LocalDate.now(),
                LocalDate.now(),
                "ACTIVO"
                
                
        );
        
        int idGenerado = prestamoDao.registrarPrestamo(nuevoPrestamo);
        
        if(idGenerado>0){
            String comprobante ="""
                                [OK]¡CREDITO APROBADO Y REGISTRADO EXITOSAMENTE!
                                ------------------------------------------------
                                - ID del prestamo:    %d
                                - Cliente:            %s
                                - Modalidad aplicada: %s
                                - Monto solicitado:   $%,.2f
                                - Intereses generados: $%,.2f
                                - Total a pagar: $%,.2f en %d cuotas
                                ------------------------------------------------
                                """.formatted(
                                    idGenerado,
                                    cliente.getNombre(),
                                    tipoEstrategia,
                                    monto,
                                    totalInteres,
                                    saldoCapitalTotal,
                                    (int)  cuotas
                                );
            System.out.println(comprobante);
            
        }else{
            System.out.println("[!]ERROR: No se pudo registrar el préstamo.");
        }
        
    }
    
    
    
    
}
