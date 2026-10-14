package com.oceanlink.beans;

public class Segmentos {
    private int idSegmento;
    private String etiquetaSegmento;
    private int idEstacionOrigen;
    private int idEstacionDestino;
    private double capacidadTotalGbps;
    private double capacidadUtilizadaGbps;
    private double capacidadReservadaGbps;
    private String estado;

    private String estacionOrigen;
    private String estacionDestino;

    public int getIdSegmento() {
        return idSegmento;
    }

    public void setIdSegmento(int idSegmento) {
        this.idSegmento = idSegmento;
    }

    public String getEtiquetaSegmento() {
        return etiquetaSegmento;
    }

    public void setEtiquetaSegmento(String etiquetaSegmento) {
        this.etiquetaSegmento = etiquetaSegmento;
    }

    public int getIdEstacionOrigen() {
        return idEstacionOrigen;
    }

    public void setIdEstacionOrigen(int idEstacionOrigen) {
        this.idEstacionOrigen = idEstacionOrigen;
    }

    public int getIdEstacionDestino() {
        return idEstacionDestino;
    }

    public void setIdEstacionDestino(int idEstacionDestino) {
        this.idEstacionDestino = idEstacionDestino;
    }

    public double getCapacidadTotalGbps() {
        return capacidadTotalGbps;
    }

    public void setCapacidadTotalGbps(double capacidadTotalGbps) {
        this.capacidadTotalGbps = capacidadTotalGbps;
    }

    public double getCapacidadUtilizadaGbps() {
        return capacidadUtilizadaGbps;
    }

    public void setCapacidadUtilizadaGbps(double capacidadUtilizadaGbps) {
        this.capacidadUtilizadaGbps = capacidadUtilizadaGbps;
    }

    public double getCapacidadReservadaGbps() {
        return capacidadReservadaGbps;
    }

    public void setCapacidadReservadaGbps(double capacidadReservadaGbps) {
        this.capacidadReservadaGbps = capacidadReservadaGbps;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstacionOrigen() {
        return estacionOrigen;
    }

    public void setEstacionOrigen(String estacionOrigen) {
        this.estacionOrigen = estacionOrigen;
    }

    public String getEstacionDestino() {
        return estacionDestino;
    }

    public void setEstacionDestino(String estacionDestino) {
        this.estacionDestino = estacionDestino;
    }
}
