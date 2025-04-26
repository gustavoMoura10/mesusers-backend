package com.br.mesusers.auth;

import com.br.mesusers.user.UserEntity;
import com.br.mesusers.user.UserRepository;
import com.br.mesusers.user.UserResponseDTO;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.security.auth.message.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecretKey jwtSecretKey;
    private static final long JWT_EXPIRATION_MS = TimeUnit.DAYS.toMillis(1);

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${jwt.secret.key}") String secretKey) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtSecretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String refreshToken(String token) throws AuthException {
        Claims claims = validateAndParseToken(token);
        UserEntity user = userRepository.findByEmail(claims.getSubject())
                .orElseThrow(() -> new AuthException("Usuário não encontrado"));
        return generateJwtToken(user);
    }

    public long getJwtExpirationMs() {
        return JWT_EXPIRATION_MS;
    }

    public UserResponseDTO reauthenticate(String token) throws AuthException {
        Claims claims = validateAndParseToken(token);
        UserEntity user = userRepository.findByEmail(claims.getSubject())
                .orElseThrow(() -> new AuthException("Usuário não encontrado"));

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAdmin());
    }

    public String authenticate(LoginRequestDTO loginRequest) throws AuthException {
        UserEntity user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new AuthException("Usuário não encontrado"));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new AuthException("Credenciais inválidas");
        }

        return generateJwtToken(user);
    }

    public String generateJwtToken(UserEntity user) {
        String roles = user.getAdmin() ? "ADMIN,USER" : "USER";

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
                .signWith(jwtSecretKey, Jwts.SIG.HS256)
                .compact();
    }

    public Long getCurrentUserId(String token) throws AuthException {
        return getUserIdFromToken(token);
    }

    public UserEntity getCurrentUser(String token) throws AuthException {
        String email = validateAndParseToken(token).getSubject();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("Usuário não encontrado"));
    }

    public boolean isAdmin(String token) throws AuthException {

        Claims claims = validateAndParseToken(token);
        return claims.get("roles", String.class).contains("ADMIN");
    }

    public Long getUserIdFromToken(String token) throws AuthException {
        Claims claims = validateAndParseToken(token);
        return claims.get("userId", Long.class);
    }

    public Claims validateAndParseToken(String token) throws AuthException {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token.isEmpty()) {
            throw new AuthException("Token inválido");
        }
        try {
            return Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new AuthException("Token expirado", ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AuthException("Token inválido", ex);
        }
    }

    public Claims validateToken(String token) throws AuthException {
        return validateAndParseToken(token);
    }
}