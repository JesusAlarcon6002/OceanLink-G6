package com.oceanlink.util;

/**
 * Utilidades pequeñas para los JSP.
 * esc() escapa el texto antes de pintarlo en el HTML, así lo que escriba un
 * usuario (por ejemplo "<script>") se muestra como texto y no se ejecuta (evita XSS).
 */
public final class Html {

    private Html() {
    }

    public static String esc(Object valor) {
        if (valor == null) {
            return "";
        }
        String s = valor.toString();
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
