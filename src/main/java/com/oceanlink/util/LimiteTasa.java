package com.oceanlink.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/** Limitador simple en memoria: "como máximo N acciones cada X tiempo" por clave (IP, usuario...). Se reinicia al reiniciar Tomcat. */
public final class LimiteTasa {

    private static final Map<String, Deque<Long>> REGISTROS = new HashMap<>();

    private LimiteTasa() {
    }

    /** Anota la acción y devuelve true si está permitida; false (sin anotarla) si ya llegó al máximo. */
    public static synchronized boolean permitir(String clave, int maximo, long ventanaMs) {
        return permitir(clave, maximo, ventanaMs, System.currentTimeMillis());
    }

    static synchronized boolean permitir(String clave, int maximo, long ventanaMs, long ahora) {
        if (REGISTROS.size() > 5000) {
            REGISTROS.values().removeIf(d -> d.isEmpty() || ahora - d.peekLast() > 3_600_000L);
        }
        Deque<Long> d = REGISTROS.computeIfAbsent(clave, k -> new ArrayDeque<>());
        while (!d.isEmpty() && ahora - d.peekFirst() > ventanaMs) d.pollFirst();
        if (d.size() >= maximo) return false;
        d.addLast(ahora);
        return true;
    }

    static synchronized void reiniciar() {
        REGISTROS.clear();
    }
}
