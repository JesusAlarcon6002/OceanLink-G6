package com.oceanlink.util;

import com.oceanlink.beans.Reporte;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Genera un archivo Excel (.xlsx) a partir de un {@link Reporte} sin librerías externas:
 * un .xlsx es un zip con XML (formato Office Open XML). Incluye título, encabezado con color,
 * filtros automáticos, fila de encabezado inmovilizada y anchos de columna ajustados.
 */
public final class ExportadorXlsx {

    public static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // números "simples" (sin ceros a la izquierda, para no romper etiquetas ni teléfonos)
    private static final Pattern NUMERO = Pattern.compile("-?(0|[1-9]\\d{0,8})(\\.\\d+)?");

    private ExportadorXlsx() { }

    public static byte[] generar(Reporte reporte, String detalleFiltros) throws IOException {
        List<String> cols = reporte.getColumnas();
        List<List<String>> filas = reporte.getFilas();
        int nCols = Math.max(cols.size(), 1);
        final int filaEncabezado = 4;

        StringBuilder sheet = new StringBuilder(4096);
        sheet.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
             .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        int ultimaFila = filaEncabezado + filas.size();
        sheet.append("<dimension ref=\"A1:").append(letra(nCols - 1)).append(Math.max(ultimaFila, filaEncabezado)).append("\"/>");
        sheet.append("<sheetViews><sheetView workbookViewId=\"0\" showGridLines=\"0\">")
             .append("<pane ySplit=\"").append(filaEncabezado).append("\" topLeftCell=\"A").append(filaEncabezado + 1)
             .append("\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>");
        sheet.append("<sheetFormatPr defaultRowHeight=\"15\"/>");

        // anchos de columna según el contenido (con tope)
        sheet.append("<cols>");
        for (int c = 0; c < nCols; c++) {
            int max = c < cols.size() ? largo(cols.get(c)) : 8;
            for (List<String> f : filas) {
                if (c < f.size()) max = Math.max(max, largo(f.get(c)));
            }
            double ancho = Math.min(Math.max(max + 3, 10), 60);
            sheet.append("<col min=\"").append(c + 1).append("\" max=\"").append(c + 1)
                 .append("\" width=\"").append(ancho).append("\" customWidth=\"1\"/>");
        }
        sheet.append("</cols><sheetData>");

        // fila 1: título; fila 2: fecha y filtros
        sheet.append("<row r=\"1\" ht=\"24\" customHeight=\"1\">")
             .append(celdaTexto("A1", reporte.getTitulo(), 1)).append("</row>");
        String generado = "Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                + "  |  Registros: " + filas.size();
        sheet.append("<row r=\"2\">").append(celdaTexto("A2", generado, 2)).append("</row>");
        if (detalleFiltros != null && !detalleFiltros.isEmpty()) {
            sheet.append("<row r=\"3\">").append(celdaTexto("A3", "Filtros: " + detalleFiltros, 2)).append("</row>");
        }

        // encabezado
        sheet.append("<row r=\"").append(filaEncabezado).append("\" ht=\"20\" customHeight=\"1\">");
        for (int c = 0; c < cols.size(); c++) {
            sheet.append(celdaTexto(letra(c) + filaEncabezado, cols.get(c), 3));
        }
        sheet.append("</row>");

        // datos (filas alternadas con fondo suave)
        for (int i = 0; i < filas.size(); i++) {
            int r = filaEncabezado + 1 + i;
            int estiloTexto = (i % 2 == 0) ? 4 : 5;
            int estiloNum = (i % 2 == 0) ? 6 : 7;
            sheet.append("<row r=\"").append(r).append("\">");
            List<String> f = filas.get(i);
            for (int c = 0; c < nCols; c++) {
                String v = c < f.size() && f.get(c) != null ? f.get(c) : "";
                String ref = letra(c) + r;
                if (NUMERO.matcher(v).matches()) {
                    sheet.append("<c r=\"").append(ref).append("\" s=\"").append(estiloNum).append("\"><v>")
                         .append(v).append("</v></c>");
                } else {
                    sheet.append(celdaTexto(ref, v, estiloTexto));
                }
            }
            sheet.append("</row>");
        }
        sheet.append("</sheetData>");
        if (!cols.isEmpty()) {
            sheet.append("<autoFilter ref=\"A").append(filaEncabezado).append(":").append(letra(cols.size() - 1))
                 .append(Math.max(ultimaFila, filaEncabezado)).append("\"/>");
        }
        sheet.append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.75\" bottom=\"0.75\" header=\"0.3\" footer=\"0.3\"/>");
        sheet.append("<pageSetup orientation=\"landscape\" fitToHeight=\"0\"/>");
        sheet.append("</worksheet>");

        String nombreHoja = nombreHoja(reporte.getTitulo());
        String workbook = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"" + xml(nombreHoja) + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
                + "<definedNames><definedName name=\"_xlnm._FilterDatabase\" localSheetId=\"0\" hidden=\"1\">'"
                + xml(nombreHoja).replace("'", "''") + "'!$A$" + filaEncabezado + ":$" + letra(nCols - 1) + "$"
                + Math.max(ultimaFila, filaEncabezado) + "</definedName></definedNames>"
                + "</workbook>";

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bos)) {
            agregar(zip, "[Content_Types].xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                    + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                    + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                    + "</Types>");
            agregar(zip, "_rels/.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>");
            agregar(zip, "xl/workbook.xml", workbook);
            agregar(zip, "xl/_rels/workbook.xml.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                    + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                    + "</Relationships>");
            agregar(zip, "xl/styles.xml", estilos());
            agregar(zip, "xl/worksheets/sheet1.xml", sheet.toString());
        }
        return bos.toByteArray();
    }

    /**
     * Estilos (índice de cellXfs): 0 normal, 1 título, 2 subtítulo gris, 3 encabezado (azul, blanco, negrita),
     * 4/5 texto fila par/impar, 6/7 número fila par/impar.
     */
    private static String estilos() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
            + "<fonts count=\"5\">"
            + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
            + "<font><b/><sz val=\"15\"/><color rgb=\"FF1E3A5F\"/><name val=\"Calibri\"/></font>"
            + "<font><i/><sz val=\"10\"/><color rgb=\"FF64748B\"/><name val=\"Calibri\"/></font>"
            + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>"
            + "<font><sz val=\"11\"/><color rgb=\"FF1F2937\"/><name val=\"Calibri\"/></font>"
            + "</fonts>"
            + "<fills count=\"4\">"
            + "<fill><patternFill patternType=\"none\"/></fill>"
            + "<fill><patternFill patternType=\"gray125\"/></fill>"
            + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF1E3A5F\"/><bgColor indexed=\"64\"/></patternFill></fill>"
            + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFEFF4FA\"/><bgColor indexed=\"64\"/></patternFill></fill>"
            + "</fills>"
            + "<borders count=\"2\">"
            + "<border><left/><right/><top/><bottom/><diagonal/></border>"
            + "<border><left style=\"thin\"><color rgb=\"FFD0D9E4\"/></left><right style=\"thin\"><color rgb=\"FFD0D9E4\"/></right>"
            + "<top style=\"thin\"><color rgb=\"FFD0D9E4\"/></top><bottom style=\"thin\"><color rgb=\"FFD0D9E4\"/></bottom><diagonal/></border>"
            + "</borders>"
            + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
            + "<cellXfs count=\"8\">"
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
            + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
            + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
            + "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment vertical=\"center\"/></xf>"
            + "<xf numFmtId=\"0\" fontId=\"4\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment vertical=\"top\" wrapText=\"1\"/></xf>"
            + "<xf numFmtId=\"0\" fontId=\"4\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment vertical=\"top\" wrapText=\"1\"/></xf>"
            + "<xf numFmtId=\"0\" fontId=\"4\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"right\" vertical=\"top\"/></xf>"
            + "<xf numFmtId=\"0\" fontId=\"4\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"right\" vertical=\"top\"/></xf>"
            + "</cellXfs>"
            + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
            + "</styleSheet>";
    }

    private static String celdaTexto(String ref, String texto, int estilo) {
        String t = xml(texto == null ? "" : texto);
        return "<c r=\"" + ref + "\" s=\"" + estilo + "\" t=\"inlineStr\"><is><t xml:space=\"preserve\">" + t + "</t></is></c>";
    }

    private static void agregar(ZipOutputStream zip, String nombre, String contenido) throws IOException {
        zip.putNextEntry(new ZipEntry(nombre));
        zip.write(contenido.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    /** Letra de columna: 0 -> A, 25 -> Z, 26 -> AA. */
    static String letra(int indice) {
        StringBuilder sb = new StringBuilder();
        int n = indice;
        do {
            sb.insert(0, (char) ('A' + n % 26));
            n = n / 26 - 1;
        } while (n >= 0);
        return sb.toString();
    }

    private static int largo(String s) {
        if (s == null) return 0;
        int max = 0;
        for (String linea : s.split("\n")) max = Math.max(max, linea.length());
        return max;
    }

    /** Nombre de hoja válido para Excel: máx. 31 caracteres y sin []:*?/\ */
    private static String nombreHoja(String titulo) {
        String n = (titulo == null || titulo.trim().isEmpty() ? "Reporte" : titulo).replaceAll("[\\[\\]:*?/\\\\]", " ").trim();
        return n.length() > 31 ? n.substring(0, 31) : n;
    }

    /** Escapa para XML y elimina caracteres de control que Excel no acepta. */
    private static String xml(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 0x20 && c != '\t' && c != '\n' && c != '\r') continue;
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}
