package org.mentorship.reflectly.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class GoogleAuthConfig {

    @Value("${spring.security.oauth2.resource-server.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance()).build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    }
}