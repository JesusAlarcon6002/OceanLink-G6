package com.oceanlink.util;

import com.oceanlink.beans.Reporte;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Genera un PDF (A4 horizontal) con la tabla de un {@link Reporte}, sin librerías externas.
 * Usa las fuentes estándar Helvetica / Helvetica-Bold (WinAnsi, o sea, tildes y ñ funcionan),
 * repite el encabezado en cada página y numera las páginas ("página X de Y").
 */
public final class ExportadorPdf {

    public static final String CONTENT_TYPE = "application/pdf";

    private static final Charset WIN_ANSI = Charset.forName("windows-1252");
    private static final double ANCHO = 842, ALTO = 595, MARGEN = 36;
    private static final double TAM_TEXTO = 8, INTERLINEA = 10, PAD = 4;
    private static final double TAM_ENC = 8.5;

    // anchos AFM de Helvetica / Helvetica-Bold para los caracteres 32..126 (milésimas de em)
    private static final int[] HELV = {
        278,278,355,556,556,889,667,191,333,333,389,584,278,333,278,278,
        556,556,556,556,556,556,556,556,556,556,278,278,584,584,584,556,
        1015,667,667,722,722,667,611,778,722,278,500,667,556,833,722,778,
        667,778,722,667,611,722,667,944,667,667,611,278,278,278,469,556,
        333,556,556,500,556,556,278,556,556,222,222,500,222,833,556,556,
        556,556,333,500,278,556,500,722,500,500,500,334,260,334,584};
    private static final int[] HELV_B = {
        278,333,474,556,556,889,722,238,333,333,389,584,278,333,278,278,
        556,556,556,556,556,556,556,556,556,556,333,333,584,584,584,611,
        975,722,722,722,722,667,611,778,722,278,556,722,611,833,722,778,
        667,778,722,667,611,722,667,944,667,667,611,333,278,333,584,556,
        333,556,611,556,611,556,333,611,611,278,278,556,278,889,611,611,
        611,611,389,556,333,611,556,778,556,556,500,389,280,389,584};

    private ExportadorPdf() { }

    // ---------------------------------------------------------------- API

    public static byte[] generar(Reporte reporte, String detalleFiltros) throws IOException {
        List<String> cols = reporte.getColumnas();
        List<List<String>> filas = reporte.getFilas();
        int n = Math.max(cols.size(), 1);

        double[] anchos = calcularAnchos(cols, filas, n);

        // ---- 1) maquetar: lista de páginas, cada una con sus comandos de dibujo
        List<StringBuilder> paginas = new ArrayList<>();
        StringBuilder pag = null;
        double y = 0;
        String generado = "Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                + "   |   Registros: " + filas.size();

        double limiteInferior = MARGEN + 22;   // deja espacio al pie de página
        int indice = 0;
        do {
            if (pag == null || y < limiteInferior) {
                pag = new StringBuilder();
                paginas.add(pag);
                y = ALTO - MARGEN;
                if (paginas.size() == 1) {
                    texto(pag, "F2", 16, MARGEN, y - 14, 0.118, 0.227, 0.373, reporte.getTitulo());
                    y -= 22;
                    texto(pag, "F1", 8.5, MARGEN, y - 8, 0.39, 0.45, 0.55, generado);
                    y -= 12;
                    if (detalleFiltros != null && !detalleFiltros.isEmpty()) {
                        for (String l : partir("Filtros: " + detalleFiltros, "F1", 8.5, ANCHO - 2 * MARGEN)) {
                            texto(pag, "F1", 8.5, MARGEN, y - 8, 0.39, 0.45, 0.55, l);
                            y -= 11;
                        }
                    }
                    y -= 8;
                }
                y = encabezado(pag, cols, anchos, y);
            }
            if (filas.isEmpty()) break;
            List<String> f = filas.get(indice);
            List<List<String>> lineasCelda = new ArrayList<>();
            int maxLineas = 1;
            for (int c = 0; c < n; c++) {
                String v = c < f.size() && f.get(c) != null ? f.get(c) : "";
                List<String> ls = partir(v, "F1", TAM_TEXTO, anchos[c] - 2 * PAD);
                if (ls.size() > 30) ls = new ArrayList<>(ls.subList(0, 30));   // tope: una fila siempre cabe en una página
                lineasCelda.add(ls);
                maxLineas = Math.max(maxLineas, ls.size());
            }
            double alto = maxLineas * INTERLINEA + 2 * PAD - 2;
            if (y - alto < limiteInferior) {   // no cabe: pasa a otra página (y repite el encabezado)
                y = 0;
                continue;
            }
            // fondo alterno + bordes
            double x = MARGEN;
            if (indice % 2 == 1) {
                rect(pag, x, y - alto, sumar(anchos), alto, 0.937, 0.957, 0.98);
            }
            for (int c = 0; c < n; c++) {
                linea(pag, x, y - alto, x + anchos[c], y - alto);
                double ty = y - PAD - TAM_TEXTO + 1;
                for (String l : lineasCelda.get(c)) {
                    texto(pag, "F1", TAM_TEXTO, x + PAD, ty, 0.12, 0.16, 0.22, l);
                    ty -= INTERLINEA;
                }
                x += anchos[c];
            }
            y -= alto;
            indice++;
        } while (indice < filas.size());

        if (filas.isEmpty()) {
            texto(pag, "F1", 10, MARGEN, y - 16, 0.39, 0.45, 0.55, "Sin resultados para los filtros elegidos.");
        }

        // ---- 2) pie de página con "página X de Y"
        int total = paginas.size();
        for (int i = 0; i < total; i++) {
            StringBuilder p = paginas.get(i);
            linea(p, MARGEN, MARGEN + 12, ANCHO - MARGEN, MARGEN + 12);
            texto(p, "F1", 8, MARGEN, MARGEN, 0.39, 0.45, 0.55, "OceanLink - Sistema de gestión de infraestructura submarina");
            String num = "Página " + (i + 1) + " de " + total;
            texto(p, "F1", 8, ANCHO - MARGEN - ancho(num, "F1", 8), MARGEN, 0.39, 0.45, 0.55, num);
        }

        return armarPdf(paginas);
    }

    // ----------------------------------------------------- maquetación

    private static double[] calcularAnchos(List<String> cols, List<List<String>> filas, int n) {
        double disponible = ANCHO - 2 * MARGEN;
        double[] pref = new double[n];
        for (int c = 0; c < n; c++) {
            double max = ancho(c < cols.size() ? cols.get(c) : "", "F2", TAM_ENC) + 2 * PAD + 4;
            for (List<String> f : filas) {
                if (c < f.size() && f.get(c) != null) {
                    double w = 0;
                    for (String linea : f.get(c).split("\n")) w = Math.max(w, ancho(linea, "F1", TAM_TEXTO));
                    max = Math.max(max, w + 2 * PAD + 2);
                }
            }
            pref[c] = Math.min(max, 260);   // tope: las columnas largas se parten en varias líneas
        }
        double suma = sumar(pref);
        double[] res = new double[n];
        if (suma <= disponible) {
            double extra = (disponible - suma) / n;   // reparte el sobrante para llenar la página
            for (int c = 0; c < n; c++) res[c] = pref[c] + extra;
        } else {
            // reduce solo las columnas largas hasta que quepa; las cortas se respetan
            double piso = 46;
            double[] w = pref.clone();
            for (int vuelta = 0; vuelta < 50 && sumar(w) > disponible + 0.01; vuelta++) {
                double exceso = sumar(w) - disponible;
                double prom = sumar(w) / n;
                double reducible = 0;
                for (int c = 0; c < n; c++) if (w[c] > prom && w[c] > piso) reducible += w[c] - Math.max(prom, piso);
                if (reducible <= 0.001) break;
                double k = Math.min(1, exceso / reducible);
                for (int c = 0; c < n; c++) {
                    if (w[c] > prom && w[c] > piso) w[c] -= (w[c] - Math.max(prom, piso)) * k;
                }
            }
            double s = sumar(w);
            for (int c = 0; c < n; c++) res[c] = w[c] * disponible / s;
        }
        return res;
    }

    private static double encabezado(StringBuilder p, List<String> cols, double[] anchos, double y) {
        double alto = 20;
        rect(p, MARGEN, y - alto, sumar(anchos), alto, 0.118, 0.227, 0.373);
        double x = MARGEN;
        for (int c = 0; c < anchos.length; c++) {
            String t = c < cols.size() ? cols.get(c) : "";
            List<String> ls = partir(t, "F2", TAM_ENC, anchos[c] - 2 * PAD);
            texto(p, "F2", TAM_ENC, x + PAD, y - 13, 1, 1, 1, ls.isEmpty() ? "" : ls.get(0));
            x += anchos[c];
        }
        return y - alto;
    }

    /** Parte un texto en líneas que quepan en el ancho dado (por palabras; si una palabra es muy larga, la corta). */
    static List<String> partir(String texto, String fuente, double tam, double maxAncho) {
        List<String> res = new ArrayList<>();
        String t = texto == null ? "" : texto.replace("\r", "");
        for (String parrafo : t.split("\n", -1)) {
            StringBuilder actual = new StringBuilder();
            for (String palabra : parrafo.split(" ", -1)) {
                String candidata = actual.length() == 0 ? palabra : actual + " " + palabra;
                if (ancho(candidata, fuente, tam) <= maxAncho) {
                    actual.setLength(0);
                    actual.append(candidata);
                    continue;
                }
                if (actual.length() > 0) {
                    res.add(actual.toString());
                    actual.setLength(0);
                }
                // palabra más ancha que la columna: se corta por caracteres
                String resto = palabra;
                while (ancho(resto, fuente, tam) > maxAncho && resto.length() > 1) {
                    int k = resto.length() - 1;
                    while (k > 1 && ancho(resto.substring(0, k), fuente, tam) > maxAncho) k--;
                    res.add(resto.substring(0, k));
                    resto = resto.substring(k);
                }
                actual.append(resto);
            }
            res.add(actual.toString());
        }
        if (res.isEmpty()) res.add("");
        return res;
    }

    // ----------------------------------------------------- métricas

    static double ancho(String s, String fuente, double tam) {
        int[] tabla = "F2".equals(fuente) ? HELV_B : HELV;
        double w = 0;
        for (int i = 0; i < s.length(); i++) {
            w += anchoCaracter(s.charAt(i), tabla);
        }
        return w * tam / 1000.0 * 1.01;   // 1 % de margen de seguridad
    }

    private static int anchoCaracter(char c, int[] tabla) {
        if (c >= 32 && c <= 126) return tabla[c - 32];
        if (c == '\t') return tabla[0] * 4;
        // letras con tilde/diéresis/ñ miden igual que su letra base
        String base = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);
        char b = base.charAt(0);
        if (b >= 32 && b <= 126) return tabla[b - 32];
        return 556;   // símbolos restantes: ancho promedio
    }

    // ----------------------------------------------------- dibujo

    private static void texto(StringBuilder p, String fuente, double tam, double x, double y,
                              double r, double g, double b, String s) {
        p.append(num(r)).append(' ').append(num(g)).append(' ').append(num(b)).append(" rg\n");
        p.append("BT /").append(fuente).append(' ').append(num(tam)).append(" Tf ")
         .append(num(x)).append(' ').append(num(y)).append(" Td ").append(literal(s)).append(" Tj ET\n");
    }

    private static void rect(StringBuilder p, double x, double y, double w, double h, double r, double g, double b) {
        p.append(num(r)).append(' ').append(num(g)).append(' ').append(num(b)).append(" rg\n");
        p.append(num(x)).append(' ').append(num(y)).append(' ').append(num(w)).append(' ').append(num(h)).append(" re f\n");
    }

    private static void linea(StringBuilder p, double x1, double y1, double x2, double y2) {
        p.append("0.82 0.85 0.89 RG 0.5 w ").append(num(x1)).append(' ').append(num(y1)).append(" m ")
         .append(num(x2)).append(' ').append(num(y2)).append(" l S\n");
    }

    /** Cadena literal PDF en WinAnsi: escapa \ ( ) y escribe los bytes fuera de ASCII como \ooo. */
    private static String literal(String s) {
        byte[] bytes = (s == null ? "" : s).getBytes(WIN_ANSI);
        StringBuilder sb = new StringBuilder("(");
        for (byte bt : bytes) {
            int v = bt & 0xFF;
            if (v == '\\' || v == '(' || v == ')') {
                sb.append('\\').append((char) v);
            } else if (v < 32 || v > 126) {
                sb.append(String.format("\\%03o", v));
            } else {
                sb.append((char) v);
            }
        }
        return sb.append(")").toString();
    }

    private static String num(double d) {
        String s = String.format(Locale.ROOT, "%.2f", d);
        return s.contains(".") ? s.replaceAll("0+$", "").replaceAll("\\.$", "") : s;
    }

    private static double sumar(double[] a) {
        double s = 0;
        for (double v : a) s += v;
        return s;
    }

    // ----------------------------------------------------- estructura del archivo

    private static byte[] armarPdf(List<StringBuilder> paginas) throws IOException {
        // objetos: 1 catálogo, 2 páginas, 3 Helvetica, 4 Helvetica-Bold, luego (página, contenido) por cada página
        int total = paginas.size();
        List<Integer> offsets = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        escribir(out, "%PDF-1.4\n%âãÏÓ\n");

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < total; i++) kids.append(5 + i * 2).append(" 0 R ");

        objeto(out, offsets, 1, "<< /Type /Catalog /Pages 2 0 R >>");
        objeto(out, offsets, 2, "<< /Type /Pages /Kids [" + kids + "] /Count " + total + " >>");
        objeto(out, offsets, 3, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        objeto(out, offsets, 4, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");
        for (int i = 0; i < total; i++) {
            int idPagina = 5 + i * 2, idContenido = idPagina + 1;
            objeto(out, offsets, idPagina, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + (int) ANCHO + " " + (int) ALTO + "] "
                    + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + idContenido + " 0 R >>");
            byte[] contenido = paginas.get(i).toString().getBytes(WIN_ANSI);
            offsets.add(out.size());
            escribir(out, idContenido + " 0 obj\n<< /Length " + contenido.length + " >>\nstream\n");
            out.write(contenido);
            escribir(out, "\nendstream\nendobj\n");
        }

        int inicioXref = out.size();
        int cantidad = offsets.size() + 1;
        StringBuilder xref = new StringBuilder("xref\n0 " + cantidad + "\n0000000000 65535 f \n");
        for (int off : offsets) xref.append(String.format("%010d 00000 n \n", off));
        xref.append("trailer\n<< /Size ").append(cantidad).append(" /Root 1 0 R >>\nstartxref\n")
            .append(inicioXref).append("\n%%EOF\n");
        escribir(out, xref.toString());
        return out.toByteArray();
    }

    private static void objeto(ByteArrayOutputStream out, List<Integer> offsets, int id, String cuerpo) throws IOException {
        offsets.add(out.size());
        escribir(out, id + " 0 obj\n" + cuerpo + "\nendobj\n");
    }

    private static void escribir(ByteArrayOutputStream out, String s) throws IOException {
        out.write(s.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
    }
}
