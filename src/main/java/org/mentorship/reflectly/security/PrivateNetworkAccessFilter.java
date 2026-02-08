package org.mentorship.reflectly.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Handles Chrome/Safari Private Network Access (PNA) preflight requests.
 * Mobile browsers send an extra header on CORS preflight:
 *   Access-Control-Request-Private-Network: true
 * The server must respond with:
 *   Access-Control-Allow-Private-Network: true
 * Without this, mobile browsers block the request with a Network Error.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PrivateNetworkAccessFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String pnaHeader = request.getHeader("Access-Control-Request-Private-Network");

        if ("true".equalsIgnoreCase(pnaHeader)) {
            response.setHeader("Access-Control-Allow-Private-Network", "true");
        }

        // For OPTIONS preflight with PNA, return 200 immediately
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) && "true".equalsIgnoreCase(pnaHeader)) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
