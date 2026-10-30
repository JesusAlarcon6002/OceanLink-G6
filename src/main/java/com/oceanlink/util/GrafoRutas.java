package com.oceanlink.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lógica de rutas SIN base de datos (así se puede probar sola).
 *
 * Modelo: las Landing Stations son los nodos y los segmentos son las aristas.
 * Los segmentos se tratan como BIDIRECCIONALES: un cable físico se usa en los dos sentidos,
 * aunque en la tabla tenga un "origen" y un "destino".
 *
 * Hace tres cosas:
 *  1) validarContinuidad: revisa que una lista ordenada de segmentos forme un camino de A a B.
 *  2) evaluar: calcula el cuello de botella (capacidad disponible) y si la ruta está caída,
 *     en mantenimiento o degradada, según el estado de sus segmentos y de las estaciones por las que pasa.
 *  3) sugerir: busca todos los caminos posibles entre dos estaciones y los ordena del mejor al peor.
 */
public final class GrafoRutas {

    public static final String FUERA_DE_SERVICIO = "Fuera de servicio";
    public static final String EN_MANTENIMIENTO = "En mantenimiento";
    public static final String DEGRADADO = "Degradado";

    /** Límites para que la búsqueda de caminos nunca se vuelva pesada. */
    private static final int MAX_SALTOS = 8;
    private static final int MAX_CAMINOS = 500;

    // ------------------------------------------------------------------ modelo

    public static final class Estacion {
        public final int id;
        public final String etiqueta;
        public final String estado;

        public Estacion(int id, String etiqueta, String estado) {
            this.id = id;
            this.etiqueta = etiqueta;
            this.estado = estado;
        }
    }

    public static final class Tramo {
        public final int id;
        public final String etiqueta;
        public final int idOrigen;
        public final int idDestino;
        public final String estado;
        public final double total;
        public final double usada;
        public final double reservada;

        public Tramo(int id, String etiqueta, int idOrigen, int idDestino, String estado,
                     double total, double usada, double reservada) {
            this.id = id;
            this.etiqueta = etiqueta;
            this.idOrigen = idOrigen;
            this.idDestino = idDestino;
            this.estado = estado;
            this.total = total;
            this.usada = usada;
            this.reservada = reservada;
        }

        /** Capacidad libre = total - usada - reservada (nunca negativa). */
        public double disponible() {
            return Math.max(0, total - usada - reservada);
        }

        public boolean toca(int idEstacion) {
            return idOrigen == idEstacion || idDestino == idEstacion;
        }

        /** Si entro por una estación, por cuál salgo (bidireccional). */
        public int otroExtremo(int idEstacion) {
            return idOrigen == idEstacion ? idDestino : idOrigen;
        }
    }

    /** Resultado de evaluar una ruta. */
    public static final class Evaluacion {
        /** Hay un segmento o una estación Fuera de servicio: la ruta no se puede usar. */
        public boolean caida;
        /** Hay algo En mantenimiento: sirve para lo que ya existe, pero no para servicios nuevos. */
        public boolean bloqueaNuevos;
        /** Hay un segmento Degradado: se puede usar, pero se avisa. */
        public boolean advertencia;
        /** Capacidad disponible del segmento más limitado (cuello de botella). */
        public double cuello;
        public int saltos;
        /** Texto que explica por qué no está operativa (o null si todo está bien). */
        public String motivo;
        /** Puntaje para ordenar sugerencias (los Degradados cuentan la mitad). */
        public double puntaje;

        /** true si se puede usar para reservar capacidad o crear servicios nuevos. */
        public boolean usableParaNuevos() {
            return !caida && !bloqueaNuevos;
        }
    }

    /** Un camino sugerido con su evaluación. */
    public static final class Candidato {
        public final List<Tramo> tramos;
        public final Evaluacion evaluacion;
        public final boolean cumpleCapacidad;

        Candidato(List<Tramo> tramos, Evaluacion evaluacion, boolean cumpleCapacidad) {
            this.tramos = tramos;
            this.evaluacion = evaluacion;
            this.cumpleCapacidad = cumpleCapacidad;
        }

        /** "3,5,7": los IDs de los segmentos en orden (para mandarlos por un formulario). */
        public String idsTexto() {
            StringBuilder sb = new StringBuilder();
            for (Tramo t : tramos) {
                if (sb.length() > 0) sb.append(',');
                sb.append(t.id);
            }
            return sb.toString();
        }

        /** "SEG-B1 → SEG-B2 → SEG-B4" */
        public String etiquetasTexto() {
            StringBuilder sb = new StringBuilder();
            for (Tramo t : tramos) {
                if (sb.length() > 0) sb.append(" → ");
                sb.append(t.etiqueta);
            }
            return sb.toString();
        }
    }

    // ------------------------------------------------------------------ datos

    private final Map<Integer, Tramo> tramos = new LinkedHashMap<>();
    private final Map<Integer, Estacion> estaciones = new HashMap<>();

    public GrafoRutas(Collection<Tramo> tramos, Collection<Estacion> estaciones) {
        for (Tramo t : tramos) {
            this.tramos.put(t.id, t);
        }
        for (Estacion e : estaciones) {
            this.estaciones.put(e.id, e);
        }
    }

    public Tramo tramo(int id) {
        return tramos.get(id);
    }

    /** Todos los segmentos (para armar los combos del formulario). */
    public Collection<Tramo> todosLosTramos() {
        return tramos.values();
    }

    public String etiquetaEstacion(int id) {
        Estacion e = estaciones.get(id);
        return e == null ? ("#" + id) : e.etiqueta;
    }

    // ------------------------------------------------------------------ 1) continuidad

    /**
     * Revisa que los segmentos (en el orden dado) formen un camino continuo de idOrigen a idDestino.
     * Devuelve la lista de problemas en español; si está vacía, la ruta es válida.
     */
    public List<String> validarContinuidad(int idOrigen, int idDestino, List<Integer> idsEnOrden) {
        List<String> errores = new ArrayList<>();
        if (idOrigen == idDestino) {
            errores.add("El origen y el destino deben ser estaciones distintas.");
            return errores;
        }
        if (idsEnOrden == null || idsEnOrden.isEmpty()) {
            errores.add("La ruta debe tener al menos un segmento.");
            return errores;
        }

        Set<Integer> segmentosUsados = new HashSet<>();
        Set<Integer> estacionesVisitadas = new HashSet<>();
        int actual = idOrigen;
        estacionesVisitadas.add(actual);

        int posicion = 0;
        for (int id : idsEnOrden) {
            posicion++;
            Tramo t = tramos.get(id);
            if (t == null) {
                errores.add("El segmento #" + posicion + " no existe.");
                return errores;
            }
            if (!segmentosUsados.add(id)) {
                errores.add("El segmento " + t.etiqueta + " está repetido.");
                return errores;
            }
            if (!t.toca(actual)) {
                errores.add("El segmento " + t.etiqueta + " (posición " + posicion + ") no conecta con "
                        + etiquetaEstacion(actual) + ". Los segmentos deben ir en orden, uno a continuación del otro.");
                return errores;
            }
            actual = t.otroExtremo(actual);
            if (!estacionesVisitadas.add(actual)) {
                errores.add("La ruta pasa dos veces por " + etiquetaEstacion(actual) + ".");
                return errores;
            }
        }
        if (actual != idDestino) {
            errores.add("La ruta termina en " + etiquetaEstacion(actual) + ", pero el destino elegido es "
                    + etiquetaEstacion(idDestino) + ".");
        }
        return errores;
    }

    // ------------------------------------------------------------------ 2) evaluar

    public Evaluacion evaluar(List<Tramo> camino) {
        Evaluacion ev = new Evaluacion();
        ev.saltos = camino.size();
        double cuello = Double.MAX_VALUE;
        double puntaje = Double.MAX_VALUE;
        String motivoCaida = null, motivoMant = null, motivoDegradado = null;

        Set<Integer> idsEstaciones = new java.util.LinkedHashSet<>();
        for (Tramo t : camino) {
            cuello = Math.min(cuello, t.disponible());
            double factor = DEGRADADO.equals(t.estado) ? 0.5 : 1.0;
            puntaje = Math.min(puntaje, t.disponible() * factor);
            idsEstaciones.add(t.idOrigen);
            idsEstaciones.add(t.idDestino);

            if (FUERA_DE_SERVICIO.equals(t.estado) && motivoCaida == null) {
                motivoCaida = "Segmento " + t.etiqueta + " fuera de servicio";
            } else if (EN_MANTENIMIENTO.equals(t.estado) && motivoMant == null) {
                motivoMant = "Segmento " + t.etiqueta + " en mantenimiento";
            } else if (DEGRADADO.equals(t.estado) && motivoDegradado == null) {
                motivoDegradado = "Segmento " + t.etiqueta + " degradado";
            }
        }
        // las estaciones por las que pasa salen de los extremos de sus segmentos
        for (int idEst : idsEstaciones) {
            Estacion e = estaciones.get(idEst);
            if (e == null) continue;
            if (FUERA_DE_SERVICIO.equals(e.estado) && motivoCaida == null) {
                motivoCaida = "Estación " + e.etiqueta + " fuera de servicio";
            } else if (EN_MANTENIMIENTO.equals(e.estado) && motivoMant == null) {
                motivoMant = "Estación " + e.etiqueta + " en mantenimiento";
            }
        }

        ev.cuello = camino.isEmpty() ? 0 : cuello;
        ev.puntaje = camino.isEmpty() ? 0 : puntaje;
        ev.caida = motivoCaida != null;
        ev.bloqueaNuevos = motivoMant != null;
        ev.advertencia = motivoDegradado != null;
        ev.motivo = motivoCaida != null ? motivoCaida
                : motivoMant != null ? motivoMant
                : motivoDegradado;
        return ev;
    }

    /** Evalúa una ruta dada por los IDs de sus segmentos (los que no existan se ignoran). */
    public Evaluacion evaluarIds(List<Integer> ids) {
        List<Tramo> camino = new ArrayList<>();
        for (int id : ids) {
            Tramo t = tramos.get(id);
            if (t != null) camino.add(t);
        }
        return evaluar(camino);
    }

    // ------------------------------------------------------------------ 3) sugerir

    /**
     * Busca los caminos entre dos estaciones y los ordena (el mejor primero).
     * Se descartan los caminos con algo fuera de servicio. Si capacidadRequerida > 0,
     * los que no alcanzan van al final, marcados como "no cumple".
     *
     * Orden: cumple la capacidad → no bloqueados por mantenimiento → mayor puntaje
     * (cuello de botella; los Degradados cuentan la mitad) → menos saltos.
     */
    public List<Candidato> sugerir(int idOrigen, int idDestino, double capacidadRequerida) {
        List<Candidato> resultado = new ArrayList<>();
        if (idOrigen == idDestino) {
            return resultado;
        }
        Estacion eo = estaciones.get(idOrigen);
        Estacion ed = estaciones.get(idDestino);
        if (eo == null || ed == null
                || FUERA_DE_SERVICIO.equals(eo.estado) || FUERA_DE_SERVICIO.equals(ed.estado)) {
            return resultado;
        }

        // lista de adyacencia solo con los segmentos que no están caídos
        Map<Integer, List<Tramo>> ady = new HashMap<>();
        for (Tramo t : tramos.values()) {
            if (FUERA_DE_SERVICIO.equals(t.estado)) continue;
            ady.computeIfAbsent(t.idOrigen, k -> new ArrayList<>()).add(t);
            ady.computeIfAbsent(t.idDestino, k -> new ArrayList<>()).add(t);
        }

        List<List<Tramo>> caminos = new ArrayList<>();
        Set<Integer> visitadas = new HashSet<>();
        visitadas.add(idOrigen);
        buscar(idOrigen, idDestino, ady, visitadas, new ArrayList<>(), caminos);

        for (List<Tramo> camino : caminos) {
            Evaluacion ev = evaluar(camino);
            if (ev.caida) continue; // por si pasa por una estación caída
            boolean cumple = capacidadRequerida <= 0 || ev.cuello >= capacidadRequerida;
            resultado.add(new Candidato(camino, ev, cumple));
        }

        Collections.sort(resultado, new Comparator<Candidato>() {
            @Override
            public int compare(Candidato a, Candidato b) {
                if (a.cumpleCapacidad != b.cumpleCapacidad) return a.cumpleCapacidad ? -1 : 1;
                if (a.evaluacion.bloqueaNuevos != b.evaluacion.bloqueaNuevos) return a.evaluacion.bloqueaNuevos ? 1 : -1;
                int porPuntaje = Double.compare(b.evaluacion.puntaje, a.evaluacion.puntaje);
                if (porPuntaje != 0) return porPuntaje;
                int porSaltos = Integer.compare(a.evaluacion.saltos, b.evaluacion.saltos);
                if (porSaltos != 0) return porSaltos;
                return a.idsTexto().compareTo(b.idsTexto());
            }
        });
        return resultado;
    }

    /** Recorrido en profundidad: sin repetir estaciones, con límite de saltos y de caminos. */
    private void buscar(int actual, int destino, Map<Integer, List<Tramo>> ady, Set<Integer> visitadas,
                        List<Tramo> camino, List<List<Tramo>> encontrados) {
        if (encontrados.size() >= MAX_CAMINOS) return;
        List<Tramo> salidas = ady.get(actual);
        if (salidas == null) return;
        for (Tramo t : salidas) {
            int siguiente = t.otroExtremo(actual);
            if (visitadas.contains(siguiente)) continue;
            Estacion e = estaciones.get(siguiente);
            if (e != null && FUERA_DE_SERVICIO.equals(e.estado)) continue;

            camino.add(t);
            if (siguiente == destino) {
                encontrados.add(new ArrayList<>(camino));
            } else if (camino.size() < MAX_SALTOS) {
                visitadas.add(siguiente);
                buscar(siguiente, destino, ady, visitadas, camino, encontrados);
                visitadas.remove(siguiente);
            }
            camino.remove(camino.size() - 1);
            if (encontrados.size() >= MAX_CAMINOS) return;
        }
    }
}
