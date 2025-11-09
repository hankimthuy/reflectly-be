package org.mentorship.reflectly.security;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class CookieJwtDecoder implements JwtDecoder {

    @Override
    public Jwt decode(String token) throws JwtException {
        if (token == null || token.isEmpty()) {
            throw new JwtException("Token is null or empty");
        }

        try {
            JWT jwt = SignedJWT.parse(token);
            JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();

            Map<String, Object> headers = extractHeaders(jwt);
            Map<String, Object> claims = extractClaims(claimsSet);

            Instant issuedAt = claimsSet.getIssueTime() != null 
                ? claimsSet.getIssueTime().toInstant() 
                : Instant.now();
            Instant expiresAt = claimsSet.getExpirationTime() != null 
                ? claimsSet.getExpirationTime().toInstant() 
                : Instant.now().plusSeconds(3600);

            return new Jwt(
                token,
                issuedAt,
                expiresAt,
                headers,
                claims
            );
        } catch (ParseException e) {
            throw new JwtException("Failed to parse JWT token", e);
        }
    }

    private Map<String, Object> extractHeaders(JWT jwt) {
        Map<String, Object> headers = new HashMap<>();
        
        if (jwt.getHeader() != null) {
            if (jwt.getHeader().getAlgorithm() != null) {
                headers.put("alg", jwt.getHeader().getAlgorithm().getName());
            }
            Map<String, Object> headerParams = jwt.getHeader().toJSONObject();
            if (headerParams.containsKey("kid")) {
                headers.put("kid", headerParams.get("kid"));
            }
        }
        
        return headers;
    }

    private Map<String, Object> extractClaims(JWTClaimsSet claimsSet) {
        Map<String, Object> claims = new HashMap<>();
        
        if (claimsSet.getSubject() != null) {
            claims.put("sub", claimsSet.getSubject());
        }
        if (claimsSet.getIssuer() != null) {
            claims.put("iss", claimsSet.getIssuer());
        }
        if (claimsSet.getAudience() != null && !claimsSet.getAudience().isEmpty()) {
            claims.put("aud", claimsSet.getAudience().get(0));
        }
        if (claimsSet.getExpirationTime() != null) {
            claims.put("exp", claimsSet.getExpirationTime().getTime() / 1000);
        }
        if (claimsSet.getIssueTime() != null) {
            claims.put("iat", claimsSet.getIssueTime().getTime() / 1000);
        }
        
        return claims;
    }
}
