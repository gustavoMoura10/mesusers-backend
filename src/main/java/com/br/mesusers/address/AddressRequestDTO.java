package com.br.mesusers.address;

import com.br.mesusers.user.UserRequestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) para representar os dados de requisição de
 * endereço
 */
public record AddressRequestDTO(
        @NotBlank(message = "CEP é obrigatório") @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos") String cep,

        @NotBlank(message = "Número é obrigatório") String number,

        String complement,

        @Size(max = 100, message = "Logradouro deve ter no máximo 100 caracteres") String street,

        @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres") String neighborhood,

        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres") String city,

        @Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres") String state,
        Long userId) {
}