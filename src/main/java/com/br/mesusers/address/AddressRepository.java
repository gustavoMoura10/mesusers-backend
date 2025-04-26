package com.br.mesusers.address;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
    Page<AddressEntity> findByUserId(Long userId, Pageable pageable);
    List<AddressEntity> findByUserId(Long userId);
    Optional<AddressEntity> findByIdAndUserId(Long id, Long userId);
}