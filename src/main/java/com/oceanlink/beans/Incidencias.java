package com.oceanlink.beans;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Incidencias {
    private static final DateTimeFormatter PARA_INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter PARA_TABLA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int idIncidencia;
    private String etiqueta;
    private String titulo;
    private String descripcion;
    private String severidad;
    private String estado;
    private LocalDateTime fechaDeteccion;
    private LocalDateTime fechaRestauracion;
    private String solucion;
    private String observaciones;

    // Infraestructura afectada: un segmento O una estación (tablas incidencia_segmento / incidencia_landing_station)
    private String tipoInfra;      // "Segmento", "Estación" o null
    private int idInfra;
    private String etiquetaInfra;

    public int getIdIncidencia() { return idIncidencia; }
    public void setIdIncidencia(int idIncidencia) { this.idIncidencia = idIncidencia; }

    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getSeveridad() { return severidad; }
    public void setSeveridad(String severidad) { this.severidad = severidad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaDeteccion() { return fechaDeteccion; }
    public void setFechaDeteccion(LocalDateTime fechaDeteccion) { this.fechaDeteccion = fechaDeteccion; }

    public LocalDateTime getFechaRestauracion() { return fechaRestauracion; }
    public void setFechaRestauracion(LocalDateTime fechaRestauracion) { this.fechaRestauracion = fechaRestauracion; }

    public String getSolucion() { return solucion; }
    public void setSolucion(String solucion) { this.solucion = solucion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getTipoInfra() { return tipoInfra; }
    public void setTipoInfra(String tipoInfra) { this.tipoInfra = tipoInfra; }

    public int getIdInfra() { return idInfra; }
    public void setIdInfra(int idInfra) { this.idInfra = idInfra; }

    public String getEtiquetaInfra() { return etiquetaInfra; }
    public void setEtiquetaInfra(String etiquetaInfra) { this.etiquetaInfra = etiquetaInfra; }

    // ---- Ayudas para los JSP (no son columnas de la BD) ----

    public String getFechaDeteccionInput() {
        return fechaDeteccion == null ? "" : fechaDeteccion.format(PARA_INPUT);
    }

    public String getFechaRestauracionInput() {
        return fechaRestauracion == null ? "" : fechaRestauracion.format(PARA_INPUT);
    }

    public String getFechaDeteccionTexto() {
        return fechaDeteccion == null ? "" : fechaDeteccion.format(PARA_TABLA);
    }

    /** Valor del select de infraestructura: "S:5" (segmento 5), "E:2" (estación 2) o "". */
    public String getInfraValor() {
        if (tipoInfra == null) {
            return "";
        }
        return ("Segmento".equals(tipoInfra) ? "S:" : "E:") + idInfra;
    }

    /** Los ENUM de la BD no llevan tildes; este método devuelve el texto bonito para mostrar. */
    public static String textoVisible(String valorBD) {
        if (valorBD == null) return "";
        switch (valorBD) {
            case "Critica": return "Crítica";
            case "En analisis": return "En análisis";
            case "Reparacion programada": return "Reparación programada";
            case "En reparacion": return "En reparación";
            default: return valorBD;
        }
    }
}
