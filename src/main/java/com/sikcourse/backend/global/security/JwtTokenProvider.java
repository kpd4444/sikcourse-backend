package com.sikcourse.backend.global.security;

import com.sikcourse.backend.domain.user.entity.UserRole;
import com.sikcourse.backend.global.config.JwtProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
public class JwtTokenProvider {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final JwtProperties properties;
    private final Base64.Encoder base64UrlEncoder = Base64.getUrlEncoder().withoutPadding();
    private final Base64.Decoder base64UrlDecoder = Base64.getUrlDecoder();

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
    }

    public String createAccessToken(Long userId, String email, UserRole role) {
        long now = Instant.now().getEpochSecond();
        long expiration = now + properties.accessTokenExpirationMillis() / 1000;

        String header = base64UrlEncode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64UrlEncode("""
                {"sub":"%d","email":"%s","role":"%s","iat":%d,"exp":%d}
                """.formatted(userId, escapeJson(email), role.name(), now, expiration).trim());

        return header + "." + payload + "." + sign(header + "." + payload);
    }

    public AuthUser parseAccessToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);
        if (!constantTimeEquals(expectedSignature, parts[2])) {
            throw new IllegalArgumentException("Invalid JWT signature");
        }

        String payload = new String(base64UrlDecoder.decode(parts[1]), StandardCharsets.UTF_8);
        long expiration = Long.parseLong(readJsonStringOrNumber(payload, "exp"));
        if (Instant.now().getEpochSecond() >= expiration) {
            throw new IllegalArgumentException("Expired JWT");
        }

        Long userId = Long.valueOf(readJsonStringOrNumber(payload, "sub"));
        String email = readJsonStringOrNumber(payload, "email");
        UserRole role = UserRole.valueOf(readJsonStringOrNumber(payload, "role"));
        return new AuthUser(userId, email, role);
    }

    private String base64UrlEncode(String value) {
        return base64UrlEncoder.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return base64UrlEncoder.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign JWT", exception);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        return MessageDigestSupport.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String readJsonStringOrNumber(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start < 0) {
            throw new IllegalArgumentException("Missing JWT claim: " + key);
        }

        int valueStart = start + pattern.length();
        if (json.charAt(valueStart) == '"') {
            int textStart = valueStart + 1;
            int textEnd = json.indexOf('"', textStart);
            return json.substring(textStart, textEnd);
        }

        int valueEnd = valueStart;
        while (valueEnd < json.length() && Character.isDigit(json.charAt(valueEnd))) {
            valueEnd++;
        }
        return json.substring(valueStart, valueEnd);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
