package com.oceanlink.beans;

public class Segmentos {
    private int idSegmento;
    private String etiquetaSegmento;
    private Landing_Stations idEstacionOrigen;
    private Landing_Stations idEstacionDestino;
    private double capacidadTotalGbps;
    private double capacidadUtilizadaGbps;
    private double capacidadReservadaGbps;
    private String estado;


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

    public Landing_Stations getIdEstacionOrigen() {
        return idEstacionOrigen;
    }

    public void setIdEstacionOrigen(Landing_Stations idEstacionOrigen) {
        this.idEstacionOrigen = idEstacionOrigen;
    }

    public Landing_Stations getIdEstacionDestino() {
        return idEstacionDestino;
    }

    public void setIdEstacionDestino(Landing_Stations idEstacionDestino) {
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

}
