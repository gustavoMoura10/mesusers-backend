package com.br.mesusers.address;

import com.br.mesusers.shared.reflection.DTOMapper;
import com.br.mesusers.user.UserEntity;
import com.br.mesusers.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    public AddressResponseDTO create(AddressRequestDTO request) {
        UserEntity user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String cleanedCep = request.cep().replaceAll("[^0-9]", "");
        AddressEntity address = DTOMapper.transform(request, AddressEntity.class);
        address.setUser(user);
        address.setCep(cleanedCep);
        address = addressRepository.save(address);

        return new AddressResponseDTO(address);
    }

    public AddressResponseDTO findById(Long id) {
        AddressEntity address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"));
        return new AddressResponseDTO(address);
    }

    public Page<AddressResponseDTO> findAll(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        return addressRepository.findAll(pageable)
                .map(address -> new AddressResponseDTO(address));
    }

    public long countAll() {
        return addressRepository.count();
    }

    public AddressResponseDTO update(Long id, Long userId, AddressRequestDTO request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        AddressEntity address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));

        String cleanedCep = request.cep().replaceAll("[^0-9]", "");
        address.setCep(cleanedCep);
        address.setStreet(request.street());
        address.setNumber(request.number());
        address.setComplement(request.complement());
        address.setNeighborhood(request.neighborhood());
        address.setCity(request.city());
        address.setState(request.state());
        address.setUser(user);
        AddressEntity updatedAddress = addressRepository.save(address);
        return new AddressResponseDTO(updatedAddress);

    }

    public AddressResponseDTO delete(Long id) {
        AddressEntity address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"));
        addressRepository.delete(address);
        return new AddressResponseDTO(address);
    }
}