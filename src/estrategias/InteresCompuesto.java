
package estrategias;


public class InteresCompuesto implements EstrategiaInteres{
    private static final double TASA_MENSUAL = 0.02;
    
    @Override
    public double calcularInteres(double monto, int cuotas){
        double montoConInteres = monto *Math.pow(1+ TASA_MENSUAL, cuotas);
        return montoConInteres - monto;
    }
}
