package org.mentorship.reflectly.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.dto.ErrorResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cheap in-memory rate limiting to blunt scripted abuse while this app has no CAPTCHA/paid tier
 * of its own. Two independent buckets, both reset on app restart/scale (acceptable at the current
 * single-instance scale — revisit with a shared store like Redis if that changes):
 * <ul>
 *   <li>Per-IP on {@code /api/auth/**} — the only unauthenticated surface, so IP is the only key
 *   available; blunts credential-stuffing / signup hammering.</li>
 *   <li>Per-user on {@code POST /api/conversations/**} — the endpoints that call Gemini; blunts a
 *   single (even legitimate) account hammering the Coach.</li>
 * </ul>
 * Must run after authentication has been resolved (see SecurityConfig filter ordering) so the
 * per-user bucket can key off the authenticated user rather than always falling back to IP.
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Value("${app.rate-limit.auth-requests-per-minute:10}")
    private int authRequestsPerMinute;

    @Value("${app.rate-limit.conversation-requests-per-minute:10}")
    private int conversationRequestsPerMinute;

    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> conversationBuckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !isRateLimited(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Bucket bucket = isAuthRoute(request)
                ? authBuckets.computeIfAbsent(clientIp(request), key -> newBucket(authRequestsPerMinute))
                : conversationBuckets.computeIfAbsent(conversationRateLimitKey(request),
                        key -> newBucket(conversationRequestsPerMinute));

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            sendTooManyRequests(response);
        }
    }

    private boolean isRateLimited(HttpServletRequest request) {
        return isAuthRoute(request) || isConversationWrite(request);
    }

    private boolean isAuthRoute(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/auth/");
    }

    private boolean isConversationWrite(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/conversations")
                && "POST".equalsIgnoreCase(request.getMethod());
    }

    private String conversationRateLimitKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof GoogleAuthenticationToken googleAuth && googleAuth.getUser() != null) {
            return "user:" + googleAuth.getUser().getId();
        }
        // Shouldn't normally happen (this route requires auth), but fall back to IP rather than
        // letting an unauthenticated edge case bypass rate limiting entirely.
        return "ip:" + clientIp(request);
    }

    private Bucket newBucket(int requestsPerMinute) {
        Bandwidth limit = Bandwidth.classic(requestsPerMinute, Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendTooManyRequests(HttpServletResponse response) throws IOException {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .message("Bạn đang thao tác quá nhanh, vui lòng thử lại sau ít phút.")
                .build();
        response.setStatus(429); // HttpServletResponse has no SC_TOO_MANY_REQUESTS constant
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
