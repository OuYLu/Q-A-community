package com.community.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    /**
     * Secret key used to sign JWTs. Must be at least 32 characters for HS256.
     */
    private String secret;

    /**
     * Expiration time in minutes.
     */
    private long expireMinutes;

    /**
     * Token issuer name.
     */
    private String issuer;
}
