package com.br.mesusers.auth;

public record AuthResponseDTO(
        String token,
        String tokenType,
        long expiresIn,
        Long userId,
        boolean isAdmin) {
}