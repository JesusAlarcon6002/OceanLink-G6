package com.oceanlink.util;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.net.IDN;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Validación de correos en tres capas (sin librerías externas):
 *   1) FORMATO: una dirección bien escrita (usuario@dominio.tld, sin espacios ni puntos dobles).
 *   2) DOMINIO REAL: el dominio debe existir en internet y poder recibir correo, o sea, tener registro MX
 *      (o, como indica el estándar, un registro A). Así se rechaza "asjdiasdijdas@nsdaiodiasj.com".
 *   3) Dominios desechables (mailinator, etc.) se rechazan.
 * Esto NO prueba que exista ese buzón en particular: para eso está VerificacionCorreo (código enviado al correo).
 */
public final class ValidadorCorreo {

    /** Resultado de validar: si es válido, si se pudo consultar el DNS y el mensaje para mostrar. */
    public static final class Resultado {
        public final boolean valido;
        public final boolean dominioComprobado;
        public final String mensaje;

        Resultado(boolean valido, boolean dominioComprobado, String mensaje) {
            this.valido = valido;
            this.dominioComprobado = dominioComprobado;
            this.mensaje = mensaje;
        }
    }

    // parte local: letras, números y . _ % + - (sin empezar/terminar en punto ni dobles puntos)
    private static final Pattern LOCAL = Pattern.compile("^[A-Za-z0-9_%+\\-]+(\\.[A-Za-z0-9_%+\\-]+)*$");
    // dominio: etiquetas de letras/números/guiones separadas por punto, TLD de al menos 2 letras
    private static final Pattern ETIQUETA = Pattern.compile("^[A-Za-z0-9]([A-Za-z0-9\\-]{0,61}[A-Za-z0-9])?$");
    private static final Pattern TLD = Pattern.compile("^[A-Za-z]{2,24}$");

    private static final Set<String> DESECHABLES = new HashSet<>(Arrays.asList(
            "mailinator.com", "guerrillamail.com", "guerrillamail.net", "10minutemail.com", "tempmail.com",
            "temp-mail.org", "yopmail.com", "trashmail.com", "sharklasers.com", "getnada.com", "maildrop.cc",
            "throwawaymail.com", "dispostable.com", "fakeinbox.com", "mintemail.com"));

    // resultado del DNS por dominio, para no consultar a cada rato (10 min)
    private static final long VIGENCIA_MS = 10 * 60 * 1000L;
    private static final Map<String, Object[]> CACHE = new ConcurrentHashMap<>();   // dominio -> {Long hora, Boolean existe}

    private ValidadorCorreo() {
    }

    /** Solo el formato (rápido, sin red). */
    public static boolean sintaxisValida(String correo) {
        return mensajeSintaxis(correo) == null;
    }

    /** Formato + dominio real + no desechable. */
    public static Resultado validar(String correo) {
        return validar(correo, null);
    }

    /** Igual que validar(correo), pero además el dominio debe estar en la lista (si la lista no es null ni vacía). */
    public static Resultado validar(String correo, Set<String> dominiosPermitidos) {
        String error = mensajeSintaxis(correo);
        if (error != null) {
            return new Resultado(false, false, error);
        }
        String dominio = dominioDe(correo);
        if (dominiosPermitidos != null && !dominiosPermitidos.isEmpty() && !dominiosPermitidos.contains(dominio)) {
            return new Resultado(false, false, "Solo se aceptan correos de estos dominios: @"
                    + String.join(", @", dominiosPermitidos) + ".");
        }
        if (DESECHABLES.contains(dominio)) {
            return new Resultado(false, false, "No se aceptan correos temporales o desechables (" + dominio + ").");
        }
        return comprobarDominio(dominio);
    }

    // ------------------------------------------------------------------ formato

    private static String mensajeSintaxis(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return "Escribe un correo.";
        }
        String c = correo.trim();
        if (c.length() > 254) {
            return "El correo es demasiado largo.";
        }
        int arroba = c.indexOf('@');
        if (arroba <= 0 || arroba != c.lastIndexOf('@') || arroba == c.length() - 1) {
            return "El correo debe tener el formato usuario@dominio.com.";
        }
        String local = c.substring(0, arroba);
        String dominio = c.substring(arroba + 1);
        if (local.length() > 64 || !LOCAL.matcher(local).matches()) {
            return "La parte antes de la @ tiene caracteres no permitidos.";
        }
        String ascii;
        try {
            ascii = IDN.toASCII(dominio);
        } catch (IllegalArgumentException e) {
            return "El dominio del correo no es válido.";
        }
        String[] etiquetas = ascii.split("\\.", -1);
        if (etiquetas.length < 2) {
            return "El dominio del correo no es válido (falta algo como .com).";
        }
        for (String e : etiquetas) {
            if (!ETIQUETA.matcher(e).matches()) {
                return "El dominio del correo no es válido.";
            }
        }
        if (!TLD.matcher(etiquetas[etiquetas.length - 1]).matches() && !etiquetas[etiquetas.length - 1].startsWith("xn--")) {
            return "El dominio del correo no es válido.";
        }
        return null;
    }

    private static String dominioDe(String correo) {
        String c = correo.trim();
        String dominio = c.substring(c.indexOf('@') + 1).toLowerCase(Locale.ROOT);
        try {
            return IDN.toASCII(dominio);
        } catch (IllegalArgumentException e) {
            return dominio;
        }
    }

    // ------------------------------------------------------------------ dominio (DNS)

    private static Resultado comprobarDominio(String dominio) {
        Object[] previo = CACHE.get(dominio);
        if (previo != null && System.currentTimeMillis() - (Long) previo[0] < VIGENCIA_MS) {
            return resultado((Boolean) previo[1], dominio);
        }
        try {
            boolean existe = dominioRecibeCorreo(dominio);
            if (!existe && !dnsConfiable()) {
                // Si ni siquiera un dominio que sí existe (gmail.com) se resuelve, el DNS de esta red no sirve
                // (firewall, proxy...): no se rechaza a nadie por eso.
                System.err.println("[Correo] El DNS no resuelve dominios conocidos: solo se valida el formato de " + dominio);
                return new Resultado(true, false, "No se pudo comprobar el dominio (el DNS de esta red no responde). Solo se validó el formato.");
            }
            CACHE.put(dominio, new Object[]{System.currentTimeMillis(), existe});
            return resultado(existe, dominio);
        } catch (NamingException e) {
            // No se pudo consultar el DNS (sin internet, tiempo agotado...): no se bloquea al usuario por eso.
            System.err.println("[Correo] No se pudo comprobar el dominio " + dominio + ": " + e.getMessage());
            return new Resultado(true, false, "No se pudo comprobar el dominio (¿sin internet?). Solo se validó el formato.");
        }
    }

    private static volatile Object[] controlDns;   // {Long hora, Boolean funciona}

    /** Comprueba (con caché de 10 min) que el DNS resuelve dominios que sí existen. */
    private static boolean dnsConfiable() {
        Object[] c = controlDns;
        if (c != null && System.currentTimeMillis() - (Long) c[0] < VIGENCIA_MS) {
            return (Boolean) c[1];
        }
        boolean funciona = false;
        for (String control : new String[]{"gmail.com", "outlook.com"}) {
            try {
                if (dominioRecibeCorreo(control)) {
                    funciona = true;
                    break;
                }
            } catch (NamingException ignorado) {
                // se prueba con el siguiente
            }
        }
        controlDns = new Object[]{System.currentTimeMillis(), funciona};
        return funciona;
    }

    private static Resultado resultado(boolean existe, String dominio) {
        return existe
                ? new Resultado(true, true, "Dominio válido.")
                : new Resultado(false, true, "El dominio \"" + dominio + "\" no existe o no recibe correos. Revisa que esté bien escrito.");
    }

    /**
     * true si el dominio tiene registro MX (o A/AAAA como alternativa); false si no existe.
     * @throws NamingException si el DNS no respondió (no es lo mismo que "no existe")
     */
    static boolean dominioRecibeCorreo(String dominio) throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        env.put("com.sun.jndi.dns.timeout.initial", "3000");
        env.put("com.sun.jndi.dns.timeout.retries", "2");
        DirContext ctx = new InitialDirContext(env);
        try {
            Attributes attrs;
            try {
                attrs = ctx.getAttributes(dominio, new String[]{"MX", "A", "AAAA"});
            } catch (NameNotFoundException e) {
                return false;   // el dominio no existe (NXDOMAIN)
            }
            Attribute mx = attrs.get("MX");
            if (mx != null && mx.size() > 0) {
                // "0 ." = MX nulo (RFC 7505): el dominio declara que NO recibe correo
                boolean algunoValido = false;
                for (int i = 0; i < mx.size(); i++) {
                    String v = String.valueOf(mx.get(i)).trim();
                    if (!v.endsWith(" .") && !v.equals(".")) algunoValido = true;
                }
                return algunoValido;
            }
            Attribute a = attrs.get("A");
            Attribute aaaa = attrs.get("AAAA");
            return (a != null && a.size() > 0) || (aaaa != null && aaaa.size() > 0);
        } finally {
            try {
                ctx.close();
            } catch (NamingException ignorado) {
                // nada que hacer
            }
        }
    }
}
