package com.curso.mercat.security;

import com.curso.mercat.model.Role;
import com.curso.mercat.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    // Clave HMAC-SHA256 válida codificada en Base64 (mínimo 256 bits)
    private static final String TEST_SECRET_BASE64 = "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RpbmctMTIzNDU2Nzg5MDEyMw==";
    private static final long TEST_EXPIRATION_MS = 3600000L; // 1 hora

    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET_BASE64);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION_MS);

        testUser = new User("tester@mercat.com", "password", Role.ROLE_USER);
    }

    @Test
    @DisplayName("generateToken y extractUsername: Genera un token JWT y extrae el email como subject")
    void generateToken_And_ExtractUsername_ShouldMatchUserEmail() {
        String token = jwtService.generateToken(testUser);

        assertThat(token).isNotBlank();
        String extractedEmail = jwtService.extractUsername(token);
        assertThat(extractedEmail).isEqualTo("tester@mercat.com");
    }

    @Test
    @DisplayName("generateToken con extraClaims: Incluye claims personalizados en el payload")
    void generateToken_WithExtraClaims_ShouldIncludeCustomClaims() {
        Map<String, Object> claims = Map.of("role", "ROLE_ADMIN", "customId", 42);

        String token = jwtService.generateToken(claims, testUser);

        assertThat(token).isNotBlank();
        String roleClaim = jwtService.extractClaim(token, c -> c.get("role", String.class));
        Integer customIdClaim = jwtService.extractClaim(token, c -> c.get("customId", Integer.class));

        assertThat(roleClaim).isEqualTo("ROLE_ADMIN");
        assertThat(customIdClaim).isEqualTo(42);
    }

    @Test
    @DisplayName("isTokenValid: Retorna true si el token corresponde al usuario y no ha expirado")
    void isTokenValid_MatchingUser_ShouldReturnTrue() {
        String token = jwtService.generateToken(testUser);

        boolean isValid = jwtService.isTokenValid(token, testUser);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid: Retorna false si el token pertenece a otro usuario")
    void isTokenValid_DifferentUser_ShouldReturnFalse() {
        String token = jwtService.generateToken(testUser);
        User anotherUser = new User("other@mercat.com", "password", Role.ROLE_USER);

        boolean isValid = jwtService.isTokenValid(token, anotherUser);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("isTokenValid: Lanza ExpiredJwtException si el token ha expirado")
    void isTokenValid_ExpiredToken_ShouldThrowExpiredJwtException() {
        // Configuramos expiración en el pasado
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -5000L);
        String expiredToken = jwtService.generateToken(testUser);

        assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, testUser))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
