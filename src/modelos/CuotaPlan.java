
package modelos;

import java.time.LocalDate;


public class CuotaPlan {
    private int id, numeroCuota;
    private Prestamo prestamo;
    private LocalDate fechaVencimiento;
    private double capitalEsperado, interesEsperado, interesPendiente, capitalPendiente, moraAcumulada;
    private String estado;
    
    public CuotaPlan(){
    }

    public CuotaPlan(int id, int numeroCuota, Prestamo prestamo, LocalDate fechaVencimiento, double capitalEsperado, double interesEsperado, double interesPendiente, double capitalPendiente, double moraAcumulada, String estado) {
        this.id = id;
        this.numeroCuota = numeroCuota;
        this.prestamo = prestamo;
        this.fechaVencimiento = fechaVencimiento;
        this.capitalEsperado = capitalEsperado;
        this.interesEsperado = interesEsperado;
        this.interesPendiente = interesPendiente;
        this.capitalPendiente = capitalPendiente;
        this.moraAcumulada = moraAcumulada;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumeroCuota() {
        return numeroCuota;
    }

    public void setNumeroCuota(int numeroCuota) {
        this.numeroCuota = numeroCuota;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public double getCapitalEsperado() {
        return capitalEsperado;
    }

    public void setCapitalEsperado(double capitalEsperado) {
        this.capitalEsperado = capitalEsperado;
    }

    public double getInteresEsperado() {
        return interesEsperado;
    }

    public void setInteresEsperado(double interesEsperado) {
        this.interesEsperado = interesEsperado;
    }

    public double getInteresPendiente() {
        return interesPendiente;
    }

    public void setInteresPendiente(double interesPendiente) {
        this.interesPendiente = interesPendiente;
    }

    public double getCapitalPendiente() {
        return capitalPendiente;
    }

    public void setCapitalPendiente(double capitalPendiente) {
        this.capitalPendiente = capitalPendiente;
    }

    public double getMoraAcumulada() {
        return moraAcumulada;
    }

    public void setMoraAcumulada(double moraAcumulada) {
        this.moraAcumulada = moraAcumulada;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
}
