package com.oceanlink.util;

import java.util.Collection;
import java.util.Locale;

/** Mini ayudante para pasar datos de Java a los gráficos (Chart.js) sin librerías extra. */
public final class Json {

    private Json() {
    }

    /** "texto" entre comillas, con escapes; también escapa < y > para que sea seguro dentro de un &lt;script&gt;. */
    public static String texto(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '<': sb.append("\\u003c"); break;
                case '>': sb.append("\\u003e"); break;
                case '&': sb.append("\\u0026"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    public static String listaTextos(Collection<String> valores) {
        StringBuilder sb = new StringBuilder("[");
        boolean primero = true;
        for (String v : valores) {
            if (!primero) sb.append(',');
            sb.append(texto(v));
            primero = false;
        }
        return sb.append(']').toString();
    }

    public static String listaNumeros(Collection<? extends Number> valores) {
        StringBuilder sb = new StringBuilder("[");
        boolean primero = true;
        for (Number v : valores) {
            if (!primero) sb.append(',');
            sb.append(String.format(Locale.US, "%.2f", v.doubleValue()));
            primero = false;
        }
        return sb.append(']').toString();
    }
}
