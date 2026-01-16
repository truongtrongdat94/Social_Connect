package com.connect.social_connect.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import com.connect.social_connect.domain.response.ResLoginDTO;
import com.nimbusds.jose.util.Base64;

@Service
public class AuthService {

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

    @Value("${app.jwt.base64-secret}")
    private String jwtKey;

    @Value("${app.jwt.access-token-validity-in-seconds}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    private final JwtEncoder jwtEncoder;

    public AuthService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    // Create access token
    public String createAccessToken(String email, ResLoginDTO dto) {
        Instant now = Instant.now();
        Instant validity = now.plus(accessTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim("user", dto.getUser())
                .build();

        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(
                JwsHeader.with(JWT_ALGORITHM).build(),
                claims);

        return jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
    }

    // Create refresh token
    public String createRefreshToken(String email, ResLoginDTO dto) {
        Instant now = Instant.now();
        Instant validity = now.plus(refreshTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim("user", dto.getUser())
                .build();

        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(
                JwsHeader.with(JWT_ALGORITHM).build(),
                claims);

        return jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
    }

    // Validate and decode refresh token
    public Jwt checkValidRefreshToken(String token) {
        try {
            SecretKey secretKey = getSecretKey();
            NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                    .macAlgorithm(JWT_ALGORITHM)
                    .build();
            return jwtDecoder.decode(token);
        } catch (JwtException e) {
            throw new JwtException("Refresh token không hợp lệ: " + e.getMessage());
        }
    }

    // Get secret key from base64 encoded string
    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }

    // Get access token expiration
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    // Get refresh token expiration
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
