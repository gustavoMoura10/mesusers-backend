package com.br.mesusers.user;

public record UserResponseDTO(
        Long id,
        String username,
        String email,
        Boolean admin) {
    public UserResponseDTO(UserEntity userEntity) {
        this(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                userEntity.getAdmin());

    }



}