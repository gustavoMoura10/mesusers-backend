package com.br.mesusers.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.mesusers.shared.records.ResponseRecord;
import com.br.mesusers.user.UserResponseDTO;

import jakarta.security.auth.message.AuthException;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseRecord<AuthResponseDTO>> login(
            @RequestBody LoginRequestDTO loginRequest) {
        try {
            String token = authService.authenticate(loginRequest);
            Long userId = authService.getUserIdFromToken(token);
            boolean isAdmin = authService.isAdmin(token);

            AuthResponseDTO response = new AuthResponseDTO(
                    token,
                    "Bearer",
                    authService.getJwtExpirationMs(),
                    userId,
                    isAdmin);

            return ResponseEntity.ok(
                    ResponseRecord.success("Login realizado com sucesso", response));
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseRecord.error(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<ResponseRecord<UserResponseDTO>> validateToken(
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "").trim();
            authService.validateToken(token);
            UserResponseDTO user = authService.reauthenticate(token);
            return ResponseEntity.ok(ResponseRecord.success(user));
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseRecord.error(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseRecord<AuthResponseDTO>> refreshToken(
            @RequestHeader("Authorization") String authorization) {
        try {
            String oldToken = authorization.replace("Bearer ", "").trim();
            String newToken = authService.refreshToken(oldToken);

            AuthResponseDTO response = new AuthResponseDTO(
                    newToken,
                    "Bearer",
                    authService.getJwtExpirationMs(),
                    authService.getUserIdFromToken(newToken),
                    authService.isAdmin(newToken));

            return ResponseEntity.ok(
                    ResponseRecord.success("Token atualizado com sucesso", response));
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseRecord.error(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
        }
    }
}