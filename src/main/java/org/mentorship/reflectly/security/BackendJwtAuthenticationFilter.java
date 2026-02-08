package org.mentorship.reflectly.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.dto.ErrorResponseDto;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Filter that validates backend-issued JWTs on protected routes.
 * Runs before Spring's oauth2ResourceServer filter so that backend JWTs
 * are handled here, and only unrecognized tokens fall through to the
 * Google ID token flow (for backward compatibility during migration).
 */
@Component
@RequiredArgsConstructor
public class BackendJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);

            // Try to parse as a backend JWT first
            try {
                Claims claims = jwtService.parseToken(token);
                String userId = claims.getSubject();

                Optional<UserEntity> userOpt = userRepository.findById(Long.parseLong(userId));
                if (userOpt.isEmpty()) {
                    sendUnauthorizedResponse(response, "User not found");
                    return;
                }

                UserEntity user = userOpt.get();
                GoogleAuthenticationToken authentication = new GoogleAuthenticationToken(user, token, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Skip the rest of the security filter chain's JWT processing
                // since we've already authenticated
                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                // Not a valid backend JWT — let it fall through to the
                // existing Google ID token oauth2ResourceServer flow
                // for backward compatibility during migration
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        ErrorResponseDto error = ErrorResponseDto.builder().message(message).build();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
