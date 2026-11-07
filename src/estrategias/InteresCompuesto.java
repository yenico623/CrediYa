
package estrategias;


public class InteresCompuesto implements EstrategiaInteres{
    private static final double TASA_MENSUAL = 0.02;
    
    @Override
    public double calcularInteres(double monto, double cuotas){
        return monto * TASA_MENSUAL * cuotas;
    }
}
