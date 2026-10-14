package com.oceanlink.beans;

public class Landing_Stations {
    private int id_landing_stations;
    private String etiqueta_station;
    private String ubicacion;

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    private String estado;

    public int getId_landing_stations() {
        return id_landing_stations;
    }

    public void setId_landing_stations(int id_landing_stations) {
        this.id_landing_stations = id_landing_stations;
    }

    public String getEtiqueta_station() {
        return etiqueta_station;
    }

    public void setEtiqueta_station(String etiqueta_station) {
        this.etiqueta_station = etiqueta_station;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
}
