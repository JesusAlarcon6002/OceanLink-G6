package com.oceanlink.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;

/**
 * Filtro que corre ANTES de cualquier Servlet o JSP y deja todo en UTF-8.
 * Así las tildes y la "ñ" de los formularios (POST) y de los textos de las
 * pantallas se leen y se muestran bien, sin tener que repetir
 * request.setCharacterEncoding("UTF-8") en cada Servlet.
 */
@WebFilter(filterName = "EncodingFilter", urlPatterns = "/*")
public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }
}
