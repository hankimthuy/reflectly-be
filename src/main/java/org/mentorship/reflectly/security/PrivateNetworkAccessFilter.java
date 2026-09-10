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

        // Do NOT short-circuit here even for an OPTIONS+PNA preflight: this filter runs before
        // Spring Security's CorsFilter, so returning the response early (as before) skipped it
        // entirely and left the preflight response missing Access-Control-Allow-Origin/
        // Allow-Methods/Allow-Headers — the browser then failed the whole preflight and surfaced
        // it to callers as a generic "Network Error", exactly the bug this filter was meant to
        // avoid. Just add the PNA header above and let the chain continue so CorsFilter still
        // completes the real preflight response.
        filterChain.doFilter(request, response);
    }
}
