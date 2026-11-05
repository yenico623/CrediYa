
package modelos;

import java.time.LocalDate;

public class Prestamo {
    private int id, cuotas;
    private Cliente cliente;
    private Empleado empleado;
    private double monto, saldoCapital, interes;
    private LocalDate fechaSolicitud, fechaAprobacion;
    private String estado;
    
    public Prestamo(){}
    
    public Prestamo(int id, Cliente cliente, Empleado empleado, double monto, double saldoCapital, double interes, int cuotas, LocalDate fechaSolicitud, LocalDate fechaAprobacion, String estado){
        this.id = id;
        this.cliente = cliente;
        this.empleado = empleado;
        this.monto = monto;
        this.saldoCapital = saldoCapital;
        this.interes = interes;
        this.cuotas = cuotas;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaAprobacion = fechaAprobacion;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCuotas() {
        return cuotas;
    }

    public void setCuotas(int cuotas) {
        this.cuotas = cuotas;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public double getSaldoCapital() {
        return saldoCapital;
    }

    public void setSaldoCapital(double saldoCapital) {
        this.saldoCapital = saldoCapital;
    }

    public double getInteres() {
        return interes;
    }

    public void setInteres(double interes) {
        this.interes = interes;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDate getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDate fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    
}
