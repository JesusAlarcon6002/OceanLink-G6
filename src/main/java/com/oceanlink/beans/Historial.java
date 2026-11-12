package com.oceanlink.beans;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Una fila de historial_utilizacion (solo lectura: la escribe el sistema al mover capacidad). */
public class Historial {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int idHistorial;
    private LocalDateTime fecha;
    private String etiquetaSegmento;
    private String tipoMovimiento;
    private double capacidadCambioGbps;
    private double porcentajeUtilizacion;
    private double capacidadResultanteGbps;
    private String etiquetaSolicitud;
    private String etiquetaServicio;      // null si el movimiento no es de un servicio (ej: reserva)

    public int getIdHistorial() { return idHistorial; }
    public void setIdHistorial(int idHistorial) { this.idHistorial = idHistorial; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getEtiquetaSegmento() { return etiquetaSegmento; }
    public void setEtiquetaSegmento(String etiquetaSegmento) { this.etiquetaSegmento = etiquetaSegmento; }

    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public double getCapacidadCambioGbps() { return capacidadCambioGbps; }
    public void setCapacidadCambioGbps(double capacidadCambioGbps) { this.capacidadCambioGbps = capacidadCambioGbps; }

    public double getPorcentajeUtilizacion() { return porcentajeUtilizacion; }
    public void setPorcentajeUtilizacion(double porcentajeUtilizacion) { this.porcentajeUtilizacion = porcentajeUtilizacion; }

    public double getCapacidadResultanteGbps() { return capacidadResultanteGbps; }
    public void setCapacidadResultanteGbps(double capacidadResultanteGbps) { this.capacidadResultanteGbps = capacidadResultanteGbps; }

    public String getEtiquetaSolicitud() { return etiquetaSolicitud; }
    public void setEtiquetaSolicitud(String etiquetaSolicitud) { this.etiquetaSolicitud = etiquetaSolicitud; }

    public String getEtiquetaServicio() { return etiquetaServicio; }
    public void setEtiquetaServicio(String etiquetaServicio) { this.etiquetaServicio = etiquetaServicio; }

    public String getFechaTexto() { return fecha == null ? "" : fecha.format(FORMATO); }
}
