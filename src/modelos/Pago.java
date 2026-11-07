
package modelos;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class Pago {
   private int id;
   private Prestamo prestamo;
   private String metodoPago;
   private String numeroReferencia;
   private LocalDate fechaReporte; 
   private LocalDateTime fechaAplicacion;
   private double monto;
   private String estado;
   private Empleado empleadoRevisor;
   
   public Pago(){}

    public Pago(int id, Prestamo prestamo, String metodoPago, String numeroReferencia, LocalDate fechaReporte, LocalDateTime fechaAplicacion, double monto, String estado, Empleado empleadoRevisor) {
        this.id = id;
        this.prestamo = prestamo;
        this.metodoPago = metodoPago;
        this.numeroReferencia = numeroReferencia;
        this.fechaReporte = fechaReporte;
        this.fechaAplicacion = fechaAplicacion;
        this.monto = monto;
        this.estado = estado;
        this.empleadoRevisor = empleadoRevisor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getNumeroReferencia() {
        return numeroReferencia;
    }

    public void setNumeroReferencia(String numeroReferencia) {
        this.numeroReferencia = numeroReferencia;
    }

    public LocalDate getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDate fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public LocalDateTime getFechaAplicacion() {
        return fechaAplicacion;
    }

    public void setFechaAplicacion(LocalDateTime fechaAplicacion) {
        this.fechaAplicacion = fechaAplicacion;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Empleado getEmpleadoRevisor() {
        return empleadoRevisor;
    }

    public void setEmpleadoRevisor(Empleado empleadoRevisor) {
        this.empleadoRevisor = empleadoRevisor;
    }
   
   
}
