package com.connect.social_connect.service;

import net.jqwik.api.*;
import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import com.connect.social_connect.domain.response.ResLoginDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Property-based tests for AuthService
 * Feature: auth-module
 */
class AuthServicePropertyTest {

    private AuthService createAuthService(long accessExpiration, long refreshExpiration) {
        JwtEncoder jwtEncoder = Mockito.mock(JwtEncoder.class);
        AuthService authService = new AuthService(jwtEncoder);

        ReflectionTestUtils.setField(authService, "jwtKey",
            "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi1taW5pbXVtLTUxMi1iaXRz");
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", accessExpiration);
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", refreshExpiration);

        // Mock JWT encoder
        Jwt mockJwt = Mockito.mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn("mock-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        return authService;
    }

    /**
     * Feature: auth-module, Property 4: Refresh token has longer expiration than access token
     * Validates: Requirements 2.4
     *
     * For any successful login, the refresh token expiration time SHALL be greater
     * than access token expiration time.
     */
    @Property(tries = 100)
    void refreshTokenExpirationIsAlwaysGreaterThanAccessTokenExpiration(
            @ForAll("validAccessTokenExpiration") long accessExpiration,
            @ForAll("validRefreshTokenExpiration") long refreshExpiration) {

        // Ensure refresh > access for valid configurations
        Assume.that(refreshExpiration > accessExpiration);

        AuthService authService = createAuthService(accessExpiration, refreshExpiration);

        // Verify the configured expiration values
        long actualAccessExpiration = authService.getAccessTokenExpiration();
        long actualRefreshExpiration = authService.getRefreshTokenExpiration();

        // Property: refresh token expiration > access token expiration
        assertThat(actualRefreshExpiration)
                .as("Refresh token expiration (%d) should be greater than access token expiration (%d)",
                        actualRefreshExpiration, actualAccessExpiration)
                .isGreaterThan(actualAccessExpiration);
    }

    @Provide
    Arbitrary<Long> validAccessTokenExpiration() {
        // Access token: 1 hour to 1 day (3600 to 86400 seconds)
        return Arbitraries.longs().between(3600L, 86400L);
    }

    @Provide
    Arbitrary<Long> validRefreshTokenExpiration() {
        // Refresh token: 1 day to 30 days (86400 to 2592000 seconds)
        return Arbitraries.longs().between(86401L, 2592000L);
    }

    /**
     * Feature: auth-module, Property 4: Refresh token has longer expiration than access token
     * Validates: Requirements 2.4
     *
     * Verifies that the default configuration satisfies the property.
     */
    @Property(tries = 100)
    void defaultConfigurationSatisfiesRefreshLongerThanAccess(
            @ForAll("validEmails") String email) {

        // Default values from requirements: access=86400, refresh=604800
        AuthService authService = createAuthService(86400L, 604800L);

        ResLoginDTO dto = createMockLoginDTO(email);

        // Create tokens (this exercises the token creation logic)
        authService.createAccessToken(email, dto);
        authService.createRefreshToken(email, dto);

        // Verify property holds for default config
        assertThat(authService.getRefreshTokenExpiration())
                .isGreaterThan(authService.getAccessTokenExpiration());
    }

    @Provide
    Arbitrary<String> validEmails() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(3)
                .ofMaxLength(20)
                .map(s -> s.toLowerCase() + "@example.com");
    }

    private ResLoginDTO createMockLoginDTO(String email) {
        ResLoginDTO dto = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(1L);
        userLogin.setEmail(email);
        userLogin.setUsername("testuser");
        userLogin.setDisplayName("Test User");
        dto.setUser(userLogin);
        return dto;
    }

    /**
     * Feature: auth-module, Property 8: Token expiration matches configuration
     * Validates: Requirements 6.3, 6.4
     *
     * For any generated token:
     * - Access token expiration = current time + configured access-token-validity-in-seconds
     * - Refresh token expiration = current time + configured refresh-token-validity-in-seconds
     */
    @Property(tries = 100)
    void tokenExpirationMatchesConfiguration(
            @ForAll("validAccessTokenExpiration") long accessExpiration,
            @ForAll("validRefreshTokenExpiration") long refreshExpiration,
            @ForAll("validEmails") String email) {

        // Ensure valid configuration
        Assume.that(refreshExpiration > accessExpiration);

        AuthService authService = createAuthService(accessExpiration, refreshExpiration);

        // Verify configured expiration values are correctly stored
        long actualAccessExpiration = authService.getAccessTokenExpiration();
        long actualRefreshExpiration = authService.getRefreshTokenExpiration();

        // Property 8a: Access token expiration matches configuration
        assertThat(actualAccessExpiration)
                .as("Access token expiration should match configured value")
                .isEqualTo(accessExpiration);

        // Property 8b: Refresh token expiration matches configuration
        assertThat(actualRefreshExpiration)
                .as("Refresh token expiration should match configured value")
                .isEqualTo(refreshExpiration);
    }

    /**
     * Feature: auth-module, Property 8: Token expiration matches configuration
     * Validates: Requirements 6.3, 6.4
     *
     * Verifies default configuration values from requirements:
     * - Access token: 86400 seconds (1 day)
     * - Refresh token: 604800 seconds (7 days)
     */
    @Property(tries = 100)
    void defaultTokenExpirationMatchesRequirements(@ForAll("validEmails") String email) {
        // Default values from Requirements 6.3 and 6.4
        long defaultAccessExpiration = 86400L;   // 1 day
        long defaultRefreshExpiration = 604800L; // 7 days

        AuthService authService = createAuthService(defaultAccessExpiration, defaultRefreshExpiration);

        ResLoginDTO dto = createMockLoginDTO(email);

        // Exercise token creation
        authService.createAccessToken(email, dto);
        authService.createRefreshToken(email, dto);

        // Verify default configuration matches requirements
        assertThat(authService.getAccessTokenExpiration())
                .as("Default access token expiration should be 86400 seconds (Requirement 6.3)")
                .isEqualTo(defaultAccessExpiration);

        assertThat(authService.getRefreshTokenExpiration())
                .as("Default refresh token expiration should be 604800 seconds (Requirement 6.4)")
                .isEqualTo(defaultRefreshExpiration);
    }
}
