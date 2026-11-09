
package controladores;
import dao.ClienteDao;
import dao.PrestamoDao;
import dao.CuotaPlanDao;
import servicios.SimuladorCredito;
import modelos.Cliente;
import modelos.Empleado;
import modelos.Prestamo;
import estrategias.EstrategiaInteres;
import estrategias.InteresCompuesto;
import estrategias.InteresSimple;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import modelos.CuotaPlan;

public class CreditoControllador {
    private ClienteDao clienteDao;
    private PrestamoDao prestamoDao;
    private CuotaPlanDao cuotaPlanDao;
    private SimuladorCredito simuladorCredito;

    public CreditoControllador() {
        this.clienteDao = new ClienteDao();
        this.prestamoDao = new PrestamoDao();
        this.cuotaPlanDao = new CuotaPlanDao();
        this.simuladorCredito = new SimuladorCredito();
        
    }
    public void procesarSolicitudCredito(String documentoCliente,Empleado empleado, double monto, double cuotas ) throws SQLException{
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
                empleado,
                monto,
                saldoCapitalTotal,
                totalInteres, (int) cuotas,
                LocalDate.now(),
                LocalDate.now(),
                "ACTIVO"
                
                
        );
        
        int idGenerado = prestamoDao.registrarPrestamo(nuevoPrestamo);
        
        if(idGenerado>0){
            nuevoPrestamo.setId(idGenerado);
            List<CuotaPlan> planPagos = simuladorCredito.GenerarPlanDePagos(nuevoPrestamo);
            boolean exitoCuotas = true;
            
            for (CuotaPlan cuota : planPagos){
                if (!cuotaPlanDao.insertarCuota(cuota)){
                    exitoCuotas = false;
                }
            }
            
            if (exitoCuotas){
                imprimirComprobante(idGenerado, cliente, tipoEstrategia, monto, totalInteres, saldoCapitalTotal, (int) cuotas);
                
            } else{
                System.out.println("[!] ERROR: No se pudo registrar el plan de pagos.");
                
            }} else{
            
            System.out.println("[!] ERRO: No se pudo registrar el préstamo. ");
        
        }
    }
    
 public void imprimirComprobante(int idGenerado, Cliente cliente, String tipoEstrategia,double monto, double totalInteres, double saldoCapitalTotal, int cuotas ){
        
        
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
                                    cuotas
                                );
            System.out.println(comprobante);
        }}
        
        
            
        
    
    
    
    
    
