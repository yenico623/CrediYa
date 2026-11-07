
package modelos;

public class DetalleDistribucionPago {
    private int id;
    private Pago pago;
    private CuotaPlan cuotaPlan;
    private double montoMora, montoInteres, montoCapital;
    
    public DetalleDistribucionPago(){}

    public DetalleDistribucionPago(int id, Pago pago, CuotaPlan cuotaPlan, double montoMora, double montoInteres, double montoCapital) {
        this.id = id;
        this.pago = pago;
        this.cuotaPlan = cuotaPlan;
        this.montoMora = montoMora;
        this.montoInteres = montoInteres;
        this.montoCapital = montoCapital;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public CuotaPlan getCuotaPlan() {
        return cuotaPlan;
    }

    public void setCuotaPlan(CuotaPlan cuotaPlan) {
        this.cuotaPlan = cuotaPlan;
    }

    public double getMontoMora() {
        return montoMora;
    }

    public void setMontoMora(double montoMora) {
        this.montoMora = montoMora;
    }

    public double getMontoInteres() {
        return montoInteres;
    }

    public void setMontoInteres(double montoInteres) {
        this.montoInteres = montoInteres;
    }

    public double getMontoCapital() {
        return montoCapital;
    }

    public void setMontoCapital(double montoCapital) {
        this.montoCapital = montoCapital;
    }
    
    
}
