package com.br.mesusers.auth;

import com.br.mesusers.user.UserEntity;
import com.br.mesusers.user.UserRepository;
import com.br.mesusers.user.UserResponseDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.security.auth.message.AuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    private final String secretKey = "1234567890123456789012345678901234567890123456789012345678901234";
    private SecretKey jwtSecretKey;
    private UserEntity userEntity;
    private LoginRequestDTO loginRequestDTO;

    @BeforeEach
    void setUp() {
        jwtSecretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        // Criando a instância manualmente com a secretKey
        authService = new AuthService(userRepository, passwordEncoder, secretKey);

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("João da Silva");
        userEntity.setEmail("joao@gmail.com");
        userEntity.setPassword("encodedPassword");
        userEntity.setAdmin(false);

        loginRequestDTO = new LoginRequestDTO("joao@gmail.com", "senha1234");
    }

    @Test
    void authenticate_ShouldReturnToken_WhenCredentialsAreValid() throws AuthException {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act
        String token = authService.authenticate(loginRequestDTO);

        // Assert
        assertNotNull(token);
        verify(userRepository).findByEmail("joao@gmail.com");
        verify(passwordEncoder).matches("senha1234", "encodedPassword");
    }

    @Test
    void authenticate_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.authenticate(loginRequestDTO);
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(userRepository).findByEmail("joao@gmail.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void authenticate_ShouldThrowException_WhenPasswordIsInvalid() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.authenticate(loginRequestDTO);
        });

        assertEquals("Credenciais inválidas", exception.getMessage());
        verify(passwordEncoder).matches("senha1234", "encodedPassword");
    }

    @Test
    void refreshToken_ShouldReturnNewToken_WhenTokenIsValid() throws AuthException {
        // Arrange
        String validToken = generateValidToken();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));

        // Act
        String newToken = authService.refreshToken(validToken);

        // Assert
        assertNotNull(newToken);
        verify(userRepository).findByEmail("joao@gmail.com");
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenIsInvalid() {
        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.refreshToken("invalidToken");
        });

        assertTrue(exception.getMessage().contains("Token inválido"));
    }

    @Test
    void validateToken_ShouldReturnClaims_WhenTokenIsValid() throws AuthException {
        // Arrange
        String validToken = generateValidToken();

        // Act
        Claims claims = authService.validateToken(validToken);

        // Assert
        assertNotNull(claims);
        assertEquals("joao@gmail.com", claims.getSubject());
    }

    @Test
    void validateToken_ShouldThrowException_WhenTokenIsExpired() {
        // Arrange
        String expiredToken = Jwts.builder()
                .subject("joao@gmail.com")
                .issuedAt(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)))
                .expiration(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)))
                .signWith(jwtSecretKey, Jwts.SIG.HS256)
                .compact();

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.validateToken(expiredToken);
        });

        assertEquals("Token expirado", exception.getMessage());
    }

    @Test
    void reauthenticate_ShouldReturnUserResponse_WhenTokenIsValid() throws AuthException {
        // Arrange
        String validToken = generateValidToken();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));

        // Act
        UserResponseDTO response = authService.reauthenticate(validToken);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("joao@gmail.com", response.email());
        assertFalse(response.admin());
    }

    @Test
    void getUserIdFromToken_ShouldReturnUserId_WhenTokenIsValid() throws AuthException {
        // Arrange
        String validToken = generateValidToken();

        // Act
        Long userId = authService.getUserIdFromToken(validToken);

        // Assert
        assertEquals(1L, userId);
    }

    @Test
    void isAdmin_ShouldReturnFalse_WhenUserIsNotAdmin() throws AuthException {
        // Arrange
        String validToken = generateValidToken();

        // Act
        boolean isAdmin = authService.isAdmin(validToken);

        // Assert
        assertFalse(isAdmin);
    }

    @Test
    void isAdmin_ShouldReturnTrue_WhenUserIsAdmin() throws AuthException {
        // Arrange
        userEntity.setAdmin(true);
        String adminToken = Jwts.builder()
                .subject("joao@gmail.com")
                .claim("userId", 1L)
                .claim("roles", "ADMIN,USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)))
                .signWith(jwtSecretKey, Jwts.SIG.HS256)
                .compact();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));

        // Act
        boolean isAdmin = authService.isAdmin(adminToken);

        // Assert
        assertTrue(isAdmin);
    }

    @Test
    void getCurrentUser_ShouldReturnUser_WhenTokenIsValid() throws AuthException {
        // Arrange
        String validToken = generateValidToken();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));

        // Act
        UserEntity user = authService.getCurrentUser(validToken);

        // Assert
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("João da Silva", user.getUsername());
    }

    @Test
    void validateAndParseToken_ShouldHandleBearerToken() throws AuthException {
        // Arrange
        String bearerToken = "Bearer " + generateValidToken();

        // Act
        Claims claims = authService.validateAndParseToken(bearerToken);

        // Assert
        assertNotNull(claims);
        assertEquals("joao@gmail.com", claims.getSubject());
    }

    private String generateValidToken() {
        return Jwts.builder()
                .subject("joao@gmail.com")
                .claim("userId", 1L)
                .claim("roles", "USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)))
                .signWith(jwtSecretKey, Jwts.SIG.HS256)
                .compact();
    }
}