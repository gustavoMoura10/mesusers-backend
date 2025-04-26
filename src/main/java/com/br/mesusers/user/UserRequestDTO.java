package com.br.mesusers.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
        Long id,
        @NotBlank(message = "O nome de usuário é obrigatório") @Size(min = 3, max = 50, message = "O nome de usuário deve ter entre 3 e 50 caracteres") @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "O nome de usuário só pode conter letras, números e underscores") String username,

        @NotBlank(message = "O email é obrigatório") @Email(message = "O email deve ser válido") @Size(max = 100, message = "O email deve ter no máximo 100 caracteres") String email,

        @NotBlank(message = "A senha é obrigatória") @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres") @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$", message = "A senha deve conter pelo menos 1 letra maiúscula, 1 letra minúscula, 1 número e 1 caractere especial") String password,

        @NotNull(message = "O campo admin é obrigatório") Boolean admin) {
    public UserRequestDTO {
        if (username != null) {
            username = username.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }

    public UserRequestDTO(String username, String email, String password, Boolean admin) {
        this(null, username, email, password, admin);
    }

    public UserRequestDTO(String username, String email, String password) {
        this(null, username, email, password, false);
    }

    public UserRequestDTO(String username, String email) {
        this(null, username, email, null, false);
    }
}