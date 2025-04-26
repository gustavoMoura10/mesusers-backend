package com.br.mesusers.auth;

import com.br.mesusers.shared.records.ResponseRecord;
import com.br.mesusers.user.UserResponseDTO;
import jakarta.security.auth.message.AuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private LoginRequestDTO loginRequest;
    private AuthResponseDTO authResponse;
    private UserResponseDTO userResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequestDTO("user@example.com", "password123");
        
        authResponse = new AuthResponseDTO(
            "token123",
            "Bearer",
            3600000L,
            1L,
            false
        );
        
        userResponse = new UserResponseDTO(1L, "User Name", "user@example.com", false);
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() throws AuthException {
        // Arrange
        when(authService.authenticate(any(LoginRequestDTO.class))).thenReturn("token123");
        when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
        when(authService.isAdmin(anyString())).thenReturn(false);
        when(authService.getJwtExpirationMs()).thenReturn(3600000L);

        // Act
        ResponseEntity<ResponseRecord<AuthResponseDTO>> response = 
            authController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
        assertEquals("Login realizado com sucesso", response.getBody().message());
        assertEquals("token123", response.getBody().data().token());
        
        verify(authService).authenticate(loginRequest);
        verify(authService).getUserIdFromToken("token123");
        verify(authService).isAdmin("token123");
        verify(authService).getJwtExpirationMs();
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() throws AuthException {
        // Arrange
        when(authService.authenticate(any(LoginRequestDTO.class)))
            .thenThrow(new AuthException("Credenciais inválidas"));

        // Act
        ResponseEntity<ResponseRecord<AuthResponseDTO>> response = 
            authController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().statusCode());
        assertEquals("Credenciais inválidas", response.getBody().message());
        
        verify(authService).authenticate(loginRequest);
    }

    @Test
    void validateToken_ShouldReturnUser_WhenTokenIsValid() throws AuthException {
        // Arrange
        String validToken = "Bearer validToken123";
        when(authService.reauthenticate(anyString())).thenReturn(userResponse);

        // Act
        ResponseEntity<ResponseRecord<UserResponseDTO>> response = 
            authController.validateToken(validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
        assertEquals(userResponse, response.getBody().data());
        
        verify(authService).validateToken("validToken123");
        verify(authService).reauthenticate("validToken123");
    }

    @Test
    void validateToken_ShouldReturnUnauthorized_WhenTokenIsInvalid() throws AuthException {
        // Arrange
        String invalidToken = "Bearer invalidToken";
        when(authService.validateToken(anyString()))
            .thenThrow(new AuthException("Token inválido"));

        // Act
        ResponseEntity<ResponseRecord<UserResponseDTO>> response = 
            authController.validateToken(invalidToken);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().statusCode());
        assertEquals("Token inválido", response.getBody().message());
        
        verify(authService).validateToken("invalidToken");
        verify(authService, never()).reauthenticate(anyString());
    }

    @Test
    void refreshToken_ShouldReturnNewToken_WhenOldTokenIsValid() throws AuthException {
        // Arrange
        String oldToken = "Bearer oldToken123";
        when(authService.refreshToken(anyString())).thenReturn("newToken123");
        when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
        when(authService.isAdmin(anyString())).thenReturn(false);
        when(authService.getJwtExpirationMs()).thenReturn(3600000L);

        // Act
        ResponseEntity<ResponseRecord<AuthResponseDTO>> response = 
            authController.refreshToken(oldToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
        assertEquals("Token atualizado com sucesso", response.getBody().message());
        assertEquals("newToken123", response.getBody().data().token());
        
        verify(authService).refreshToken("oldToken123");
        verify(authService).getUserIdFromToken("newToken123");
        verify(authService).isAdmin("newToken123");
        verify(authService).getJwtExpirationMs();
    }

    @Test
    void refreshToken_ShouldReturnUnauthorized_WhenOldTokenIsInvalid() throws AuthException {
        // Arrange
        String invalidToken = "Bearer invalidToken";
        when(authService.refreshToken(anyString()))
            .thenThrow(new AuthException("Token expirado ou inválido"));

        // Act
        ResponseEntity<ResponseRecord<AuthResponseDTO>> response = 
            authController.refreshToken(invalidToken);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().statusCode());
        assertEquals("Token expirado ou inválido", response.getBody().message());
        
        verify(authService).refreshToken("invalidToken");
    }

    @Test
    void validateToken_ShouldHandleMalformedAuthorizationHeader() {
        // Arrange
        String malformedToken = "InvalidTokenWithoutBearer";

        // Act
        ResponseEntity<ResponseRecord<UserResponseDTO>> response = 
            authController.validateToken(malformedToken);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().message().contains("Token malformado"));
    }
}