
package estrategias;

public class InteresSimple implements EstrategiaInteres {
    private static final double TASA_MENSUAL = 0.02;
    
    @Override
    public double calcularInteres(double monto, int cuotas){
    return monto * TASA_MENSUAL * cuotas;
    }
    
}
