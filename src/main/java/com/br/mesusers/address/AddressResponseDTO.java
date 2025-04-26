package com.br.mesusers.address;

import com.br.mesusers.user.UserEntity;
import com.br.mesusers.user.UserResponseDTO;

public record AddressResponseDTO(
        Long id,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String cep,
        UserResponseDTO user) {
    public AddressResponseDTO(AddressEntity addressEntity) {
        this(
                addressEntity.getId(),
                addressEntity.getStreet(),
                addressEntity.getNumber(),
                addressEntity.getComplement(),
                addressEntity.getNeighborhood(),
                addressEntity.getCity(),
                addressEntity.getState(),
                addressEntity.getCep(),
                new UserResponseDTO(
                        addressEntity.getUser().getId(),
                        addressEntity.getUser().getUsername(),
                        addressEntity.getUser().getEmail(),
                        addressEntity.getUser().getAdmin()));
    }

}