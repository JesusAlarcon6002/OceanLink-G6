package com.oceanlink.filters;

import com.oceanlink.beans.UsuarioSesion;
import com.oceanlink.util.Permisos;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Guardia de TODO el sitio (autenticación + autorización por rol), corre antes de cualquier Servlet o JSP.
 *   1. Recursos estáticos (/assets/), el login, recuperar contraseña y solicitud de registro: pasan sin sesión.
 *   2. "/" e index.jsp: redirigen al inicio (o al login si no hay sesión).
 *   3. Cualquier .jsp pedido directo por URL: no se puede (las vistas solo se abren a través de un Servlet).
 *   4. Sin sesión: redirige al login.
 *   5. Con sesión: se revisa en Permisos si el rol puede usar ese Servlet con ese método/action; si no, 403.
 * Además manda cabeceras no-store para que el botón "atrás" no muestre páginas guardadas tras cerrar sesión.
 *
 * Seguridad extra (aplica a TODAS las peticiones, antes de cualquier otra cosa):
 *   - Cabeceras: X-Frame-Options (anti clickjacking), X-Content-Type-Options, Referrer-Policy, Permissions-Policy.
 *   - Anti-CSRF: todo POST/PUT/DELETE debe venir de una página de este mismo sitio (cabecera Origin o Referer
 *     con el mismo host que la petición). Si viene de otro sitio se responde 403. Si el navegador no manda
 *     ninguna de las dos cabeceras se deja pasar (los navegadores actuales siempre mandan Origin en los POST).
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = "/*")
public class AuthFilter implements Filter {

    public static final String ATRIBUTO_USUARIO = "usuario";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        // Este filtro lee parámetros (action) antes que EncodingFilter: fijamos UTF-8 primero para no dañar tildes ni "ñ"
        req.setCharacterEncoding("UTF-8");
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Cabeceras de seguridad (también para /assets/)
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        // Anti-CSRF: los métodos que modifican datos solo se aceptan desde este mismo sitio
        if (!esMetodoSeguro(request.getMethod())
                && !origenValido(request.getHeader("Origin"), request.getHeader("Referer"), request.getHeader("Host"))) {
            denegar(request, response);
            return;
        }

        String ctx = request.getContextPath();
        String ruta = request.getRequestURI().substring(ctx.length());
        // Quitamos ";jsessionid=..." por si viene en la URL
        int pv = ruta.indexOf(';');
        if (pv >= 0) ruta = ruta.substring(0, pv);

        // 1. estáticos y login
        if (ruta.startsWith("/assets/")) {
            chain.doFilter(req, res);
            return;
        }
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // Públicas (sin sesión): login, recuperar contraseña y solicitud de registro
        if (ruta.equals("/LoginServlet") || ruta.equals("/RecuperarServlet") || ruta.equals("/RegistrarseServlet")) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession sesion = request.getSession(false);
        UsuarioSesion usuario = sesion == null ? null : (UsuarioSesion) sesion.getAttribute(ATRIBUTO_USUARIO);

        // 2. raíz del sitio
        if (ruta.equals("/") || ruta.isEmpty() || ruta.equals("/index.jsp")) {
            response.sendRedirect(ctx + (usuario == null ? "/LoginServlet" : "/InicioServlet"));
            return;
        }

        // 4. sin sesión
        if (usuario == null) {
            response.sendRedirect(ctx + "/LoginServlet");
            return;
        }

        // 3. JSP pedido directo
        if (ruta.endsWith(".jsp") || ruta.startsWith("/WEB-INF/")) {
            denegar(request, response);
            return;
        }

        // Servlets comunes a todos los roles autenticados
        if (ruta.equals("/InicioServlet") || ruta.equals("/LogoutServlet") || ruta.equals("/BandejaServlet")) {
            chain.doFilter(req, res);
            return;
        }

        // 5. autorización por rol
        if (Permisos.permite(usuario.getRol(), ruta, request.getMethod(), request.getParameter("action"))) {
            chain.doFilter(req, res);
        } else {
            denegar(request, response);
        }
    }

    private static boolean esMetodoSeguro(String metodo) {
        return "GET".equalsIgnoreCase(metodo) || "HEAD".equalsIgnoreCase(metodo) || "OPTIONS".equalsIgnoreCase(metodo);
    }

    /**
     * ¿La petición viene de una página de este mismo sitio? Compara el host:puerto de Origin (o, si no
     * viene, de Referer) con el de la cabecera Host. Sin ninguna de las dos cabeceras se acepta.
     * Origin "null" (formularios de páginas aisladas) se rechaza.
     */
    public static boolean origenValido(String origin, String referer, String host) {
        String declarado = origin != null && !origin.trim().isEmpty() ? origin.trim()
                : (referer != null && !referer.trim().isEmpty() ? referer.trim() : null);
        if (declarado == null) return true;
        if (host == null || host.trim().isEmpty()) return false;
        try {
            String autoridad = new java.net.URI(declarado).getAuthority();
            return autoridad != null && autoridad.equalsIgnoreCase(host.trim());
        } catch (java.net.URISyntaxException e) {
            return false;
        }
    }

    private void denegar(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        request.getRequestDispatcher("/WEB-INF/vistas/accesoDenegado.jsp").forward(request, response);
    }
}
