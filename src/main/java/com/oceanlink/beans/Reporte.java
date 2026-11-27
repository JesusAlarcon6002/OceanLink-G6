package com.oceanlink.beans;

import java.util.ArrayList;
import java.util.List;

/** Resultado de un reporte: título, columnas y filas de texto. Sirve para la tabla en pantalla y para el CSV. */
public class Reporte {
    private final String titulo;
    private final List<String> columnas;
    private final List<List<String>> filas = new ArrayList<>();

    public Reporte(String titulo, List<String> columnas) {
        this.titulo = titulo;
        this.columnas = columnas;
    }

    public String getTitulo() { return titulo; }

    public List<String> getColumnas() { return columnas; }

    public List<List<String>> getFilas() { return filas; }

    public void agregarFila(List<String> fila) {
        filas.add(fila);
    }

    /** CSV con comillas y escapes correctos (se abre bien en Excel; el servlet agrega el BOM UTF-8). */
    public String aCsv() {
        StringBuilder sb = new StringBuilder();
        linea(sb, columnas);
        for (List<String> f : filas) {
            linea(sb, f);
        }
        return sb.toString();
    }

    private static void linea(StringBuilder sb, List<String> celdas) {
        for (int i = 0; i < celdas.size(); i++) {
            if (i > 0) sb.append(',');
            String c = celdas.get(i) == null ? "" : celdas.get(i);
            // evita que Excel interprete el texto como fórmula (inyección de CSV)
            if (!c.isEmpty() && "=+-@".indexOf(c.charAt(0)) >= 0 && !c.matches("-?\\d+([.,]\\d+)?")) {
                c = "'" + c;
            }
            sb.append('"').append(c.replace("\"", "\"\"")).append('"');
        }
        sb.append("\r\n");
    }
}
