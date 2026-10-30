package com.oceanlink.beans;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Solicitud de capacidad de un cliente entre dos Landing Stations. */
public class Solicitudes {
    private static final DateTimeFormatter PARA_INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter PARA_TABLA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int idSolicitud;
    private String etiqueta;
    private Clientes cliente;                 // bean dentro del bean: así la lista muestra el nombre y no el ID
    private Landing_Stations estacionOrigen;
    private Landing_Stations estacionDestino;
    private double capacidadSolicitadaGbps;
    private LocalDateTime fechaSolicitud;
    private String estado;
    private String observaciones;

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }

    public Clientes getCliente() { return cliente; }
    public void setCliente(Clientes cliente) { this.cliente = cliente; }

    public Landing_Stations getEstacionOrigen() { return estacionOrigen; }
    public void setEstacionOrigen(Landing_Stations estacionOrigen) { this.estacionOrigen = estacionOrigen; }

    public Landing_Stations getEstacionDestino() { return estacionDestino; }
    public void setEstacionDestino(Landing_Stations estacionDestino) { this.estacionDestino = estacionDestino; }

    public double getCapacidadSolicitadaGbps() { return capacidadSolicitadaGbps; }
    public void setCapacidadSolicitadaGbps(double capacidadSolicitadaGbps) { this.capacidadSolicitadaGbps = capacidadSolicitadaGbps; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    // ---- Ayudas para los JSP ----

    /** Valor para un input datetime-local, ej: 2026-10-23T14:30 */
    public String getFechaSolicitudInput() {
        return fechaSolicitud == null ? "" : fechaSolicitud.format(PARA_INPUT);
    }

    /** Texto para la tabla, ej: 23/10/2026 */
    public String getFechaSolicitudTexto() {
        return fechaSolicitud == null ? "" : fechaSolicitud.format(PARA_TABLA);
    }
}
