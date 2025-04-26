package com.br.mesusers.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRequestDTO userRequestDTO;
    private UserEntity userEntity;
    private UserResponseDTO userResponseDTO;

   
    private String encryptPassword(String rawPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(rawPassword);
    }

    @BeforeEach
    void setUp() {
        userRequestDTO = new UserRequestDTO(
                null,
                "João da Silva",
                "joao@gmail.com",
                "senha123",
                false);

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("João da Silva");
        userEntity.setEmail("joao@gmail.com");
        userEntity.setPassword("senha123");
        userEntity.setAdmin(false);

        userResponseDTO = new UserResponseDTO(
                1L,
                "João da Silva",
                "joao@gmail.com",
                false);
    }

    @Test
    void save_ShouldEncryptPasswordAndSaveUser() {
        when(this.encryptPassword("senha123")).thenReturn("senha123");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        UserResponseDTO result = userService.save(userRequestDTO);
        assertNotNull(result);
        assertEquals(userResponseDTO.id(), result.id());
        assertEquals(userResponseDTO.username(), result.username());
        assertEquals(userResponseDTO.email(), result.email());
        assertEquals(userResponseDTO.admin(), result.admin());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void update_ShouldUpdateExistingUser() {
        UserRequestDTO updateDTO = new UserRequestDTO(
                "Alex Bala da Silva",
                "alexbaladasilva@gmail.com",
                "novaSenha@098",
                true);

        UserEntity existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setUsername("John Doe");
        existingUser.setEmail("john@example.com");
        existingUser.setPassword("oldEncryptedPassword");
        existingUser.setAdmin(false);

        UserEntity updatedUser = new UserEntity();
        updatedUser.setId(1L);
        updatedUser.setUsername("Alex Bala da Silva");
        updatedUser.setEmail("alexbaladasilva@gmail.com");
        updatedUser.setPassword("newEncryptedPassword");
        updatedUser.setAdmin(true);

        when(passwordEncoder.encode("novaSenha@098")).thenReturn("newEncryptedPassword");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(updatedUser);

        UserResponseDTO result = userService.update(1L, updateDTO);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Alex Bala da Silva", result.username());
        assertEquals("alexbaladasilva@gmail.com", result.email());
        assertTrue(result.admin());

        verify(passwordEncoder).encode("novaSenha@098");
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void update_ShouldReturnNull_WhenUserNotFound() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        UserResponseDTO result = userService.update(1L, userRequestDTO);
        assertNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_ShouldHandlePartialUpdates() {
        UserRequestDTO partialUpdateDTO = new UserRequestDTO(null, null, "novaSenha@098", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncryptedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        UserResponseDTO result = userService.update(1L, partialUpdateDTO);
        assertNotNull(result);
        assertEquals(userEntity.getUsername(), userEntity.getUsername());
        assertEquals(userEntity.getEmail(), userEntity.getEmail());
        verify(userRepository).save(userEntity);
    }

    @Test
    void delete_ShouldDeleteUser_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        UserResponseDTO result = userService.delete(1L);
        assertNotNull(result);
        verify(userRepository).delete(userEntity);
    }

    @Test
    void delete_ShouldReturnNull_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        UserResponseDTO result = userService.delete(1L);
        assertNull(result);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void findAll_ShouldReturnPaginatedUsers() {
        Page<UserEntity> userPage = new PageImpl<>(Collections.singletonList(userEntity), PageRequest.of(0, 10), 1);
        when(userRepository.findAll(PageRequest.of(0, 10))).thenReturn(userPage);
        Page<UserResponseDTO> result = userService.findAll(1, 10);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(PageRequest.of(0, 10));
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        when(userRepository.findByEmail("lorembaladasilva@gmail.com")).thenReturn(Optional.of(userEntity));
        UserResponseDTO result = userService.findByEmail("lorembaladasilva@gmail.com");
        assertNotNull(result);
        assertEquals(userResponseDTO.email(), result.email());
    }

    @Test
    void findByEmail_ShouldReturnNull_WhenEmailDoesNotExist() {
        when(userRepository.findByEmail("emailmaluco@gmail.com")).thenReturn(Optional.empty());
        UserResponseDTO result = userService.findByEmail("emailmaluco@gmail.com");
        assertNull(result);
    }
}