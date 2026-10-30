package com.oceanlink.beans;

import com.oceanlink.util.GrafoRutas;

import java.util.ArrayList;
import java.util.List;

/** Ruta entre dos Landing Stations: una lista ORDENADA de segmentos (tabla ruta_segmento). */
public class Rutas {
    private int idRuta;
    private String etiqueta;
    private Landing_Stations estacionOrigen;
    private Landing_Stations estacionDestino;
    private String estado = "Activa";                 // interruptor manual: Activa / Inactiva
    private List<GrafoRutas.Tramo> segmentos = new ArrayList<>();
    private GrafoRutas.Evaluacion evaluacion;          // se calcula al consultar, no se guarda
    private boolean tieneServicios;

    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }

    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }

    public Landing_Stations getEstacionOrigen() { return estacionOrigen; }
    public void setEstacionOrigen(Landing_Stations estacionOrigen) { this.estacionOrigen = estacionOrigen; }

    public Landing_Stations getEstacionDestino() { return estacionDestino; }
    public void setEstacionDestino(Landing_Stations estacionDestino) { this.estacionDestino = estacionDestino; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<GrafoRutas.Tramo> getSegmentos() { return segmentos; }
    public void setSegmentos(List<GrafoRutas.Tramo> segmentos) { this.segmentos = segmentos; }

    public GrafoRutas.Evaluacion getEvaluacion() { return evaluacion; }
    public void setEvaluacion(GrafoRutas.Evaluacion evaluacion) { this.evaluacion = evaluacion; }

    public boolean isTieneServicios() { return tieneServicios; }
    public void setTieneServicios(boolean tieneServicios) { this.tieneServicios = tieneServicios; }

    // ---- Ayudas para los JSP / servlets ----

    public int getIdOrigen() { return estacionOrigen == null ? 0 : estacionOrigen.getId_landing_stations(); }
    public int getIdDestino() { return estacionDestino == null ? 0 : estacionDestino.getId_landing_stations(); }

    /** "SEG-B1 → SEG-B2 → SEG-B4" */
    public String getSegmentosTexto() {
        StringBuilder sb = new StringBuilder();
        for (GrafoRutas.Tramo t : segmentos) {
            if (sb.length() > 0) sb.append(" → ");
            sb.append(t.etiqueta);
        }
        return sb.toString();
    }

    public List<Integer> getIdsSegmentos() {
        List<Integer> ids = new ArrayList<>();
        for (GrafoRutas.Tramo t : segmentos) ids.add(t.id);
        return ids;
    }

    /** Capacidad disponible del segmento más limitado. */
    public double getCapacidadDisponible() {
        return evaluacion == null ? 0 : evaluacion.cuello;
    }

    /** Estado real = interruptor manual + salud calculada. */
    public String getEstadoEfectivo() {
        if (!"Activa".equals(estado)) return "Inactiva";
        if (evaluacion == null) return "Activa";
        if (evaluacion.caida) return "Caída";
        if (evaluacion.bloqueaNuevos) return "En mantenimiento";
        if (evaluacion.advertencia) return "Degradada";
        return "Operativa";
    }

    /** Se puede usar para reservar capacidad o crear servicios nuevos. */
    public boolean isUsableParaNuevos() {
        return "Activa".equals(estado) && evaluacion != null && evaluacion.usableParaNuevos();
    }

    public String getMotivo() {
        return evaluacion == null ? null : evaluacion.motivo;
    }
}
