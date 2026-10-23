package com.oceanlink.beans;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Un mantenimiento (tabla mantenimientos) junto con la infraestructura que
 * intervendrá (tabla mantenimiento_infraestructura: un segmento O una estación).
 */
public class Mantenimientos {

    private static final DateTimeFormatter PARA_INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter PARA_TABLA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int idMantenimiento;
    private String etiqueta;
    private String tipo;               // Preventivo | Correctivo
    private String descripcion;
    private LocalDateTime fechaProgramada;
    private double duracionEstimadaHoras;
    private String estado;             // Programado | En curso | Finalizado
    private LocalDateTime fechaFinalizacion;
    private String resultado;
    private String actividades;

    // Infraestructura afectada (opcional)
    private String tipoInfra;          // "Segmento", "Estación" o null
    private int idInfra;
    private String etiquetaInfra;

    public int getIdMantenimiento() {
        return idMantenimiento;
    }

    public void setIdMantenimiento(int idMantenimiento) {
        this.idMantenimiento = idMantenimiento;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(LocalDateTime fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public double getDuracionEstimadaHoras() {
        return duracionEstimadaHoras;
    }

    public void setDuracionEstimadaHoras(double duracionEstimadaHoras) {
        this.duracionEstimadaHoras = duracionEstimadaHoras;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getActividades() {
        return actividades;
    }

    public void setActividades(String actividades) {
        this.actividades = actividades;
    }

    public String getTipoInfra() {
        return tipoInfra;
    }

    public void setTipoInfra(String tipoInfra) {
        this.tipoInfra = tipoInfra;
    }

    public int getIdInfra() {
        return idInfra;
    }

    public void setIdInfra(int idInfra) {
        this.idInfra = idInfra;
    }

    public String getEtiquetaInfra() {
        return etiquetaInfra;
    }

    public void setEtiquetaInfra(String etiquetaInfra) {
        this.etiquetaInfra = etiquetaInfra;
    }

    // ---- Ayudas para los JSP (no son columnas de la BD) ----

    /** Valor para un input datetime-local, ej: 2026-10-23T14:30 */
    public String getFechaProgramadaInput() {
        return fechaProgramada == null ? "" : fechaProgramada.format(PARA_INPUT);
    }

    public String getFechaFinalizacionInput() {
        return fechaFinalizacion == null ? "" : fechaFinalizacion.format(PARA_INPUT);
    }

    /** Texto para la tabla, ej: 23/10/2026 14:30 */
    public String getFechaProgramadaTexto() {
        return fechaProgramada == null ? "" : fechaProgramada.format(PARA_TABLA);
    }

    /** Valor del select de infraestructura: "S:5" (segmento 5), "E:2" (estación 2) o "". */
    public String getInfraValor() {
        if (tipoInfra == null) {
            return "";
        }
        return ("Segmento".equals(tipoInfra) ? "S:" : "E:") + idInfra;
    }
}
