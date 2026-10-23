package com.oceanlink.beans;

/** Números del resumen que se muestra en el pie de todas las pantallas. */
public class Resumen {
    private int landingStations;
    private int segmentos;
    private double capacidadTotalGbps;

    public int getLandingStations() {
        return landingStations;
    }

    public void setLandingStations(int landingStations) {
        this.landingStations = landingStations;
    }

    public int getSegmentos() {
        return segmentos;
    }

    public void setSegmentos(int segmentos) {
        this.segmentos = segmentos;
    }

    public double getCapacidadTotalGbps() {
        return capacidadTotalGbps;
    }

    public void setCapacidadTotalGbps(double capacidadTotalGbps) {
        this.capacidadTotalGbps = capacidadTotalGbps;
    }
}
