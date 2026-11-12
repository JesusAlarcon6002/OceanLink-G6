package com.oceanlink.beans;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Servicio contratado por un cliente sobre una ruta, nacido de una solicitud con capacidad reservada. */
public class Servicios {
    private static final DateTimeFormatter PARA_TABLA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int idServicio;
    private String etiqueta;
    private Clientes cliente;
    private int idRuta;
    private String etiquetaRuta;
    private int idSolicitud;
    private String etiquetaSolicitud;
    private double capacidadContratadaGbps;
    private LocalDate fechaActivacion;
    private LocalDate fechaExpiracion;
    private String estado = "Pendiente";

    public int getIdServicio() { return idServicio; }
    public void setIdServicio(int idServicio) { this.idServicio = idServicio; }

    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }

    public Clientes getCliente() { return cliente; }
    public void setCliente(Clientes cliente) { this.cliente = cliente; }

    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }

    public String getEtiquetaRuta() { return etiquetaRuta; }
    public void setEtiquetaRuta(String etiquetaRuta) { this.etiquetaRuta = etiquetaRuta; }

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getEtiquetaSolicitud() { return etiquetaSolicitud; }
    public void setEtiquetaSolicitud(String etiquetaSolicitud) { this.etiquetaSolicitud = etiquetaSolicitud; }

    public double getCapacidadContratadaGbps() { return capacidadContratadaGbps; }
    public void setCapacidadContratadaGbps(double capacidadContratadaGbps) { this.capacidadContratadaGbps = capacidadContratadaGbps; }

    public LocalDate getFechaActivacion() { return fechaActivacion; }
    public void setFechaActivacion(LocalDate fechaActivacion) { this.fechaActivacion = fechaActivacion; }

    public LocalDate getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDate fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    /** yyyy-MM-dd para <input type="date"> (vacío si no hay fecha). */
    public String getFechaActivacionInput() { return fechaActivacion == null ? "" : fechaActivacion.toString(); }
    public String getFechaExpiracionInput() { return fechaExpiracion == null ? "" : fechaExpiracion.toString(); }

    public String getFechaActivacionTexto() { return fechaActivacion == null ? "—" : fechaActivacion.format(PARA_TABLA); }
    public String getFechaExpiracionTexto() { return fechaExpiracion == null ? "—" : fechaExpiracion.format(PARA_TABLA); }
}
