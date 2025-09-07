package org.mentorship.reflectly.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleAuthConfig {

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        // --- CẢNH BÁO BẢO MẬT ---
        // Cấu hình này chỉ kiểm tra chữ ký của Google (Issuer) và thời gian hết hạn (Expiration).
        // Nó KHÔNG kiểm tra token này được cấp cho ứng dụng nào (Audience).
        // KHÔNG SỬ DỤNG TRONG PRODUCTION.

        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                // Không gọi .setAudience() ở đây
                .build();
    }
}