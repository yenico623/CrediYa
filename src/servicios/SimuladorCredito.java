
package servicios;
import java.time.LocalDate;
import modelos.CuotaPlan;
import modelos.Prestamo;
import java.util.ArrayList;
import java.util.List;

public class SimuladorCredito {
    public List<CuotaPlan> GenerarPlanDePagos (Prestamo prestamo){
        List<CuotaPlan> plan = new ArrayList<>();
        int cuotas = prestamo.getCuotas();
        double capitalMensual = prestamo.getMonto()/cuotas;
        double interesMensual  = prestamo.getInteres() /cuotas;
        
        for (int i =1; i<=cuotas; i++){
            CuotaPlan cuota = new CuotaPlan();
            cuota.setPrestamo(prestamo);
            cuota.setNumeroCuota(i);
            cuota.setCapitalEsperado(capitalMensual);
            cuota.setInteresEsperado(interesMensual);
            cuota.setCapitalPendiente(capitalMensual);
            cuota.setInteresPendiente(interesMensual);
            cuota.setMoraAcumulada(0.00);
            cuota.setEstado("PENDIENTE");
            cuota.setFechaVencimiento(LocalDate.now().plusMonths(i));
            
            plan.add(cuota);
        
        } return plan;
    }
}
