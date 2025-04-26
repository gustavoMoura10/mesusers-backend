package com.br.mesusers.user;

import com.br.mesusers.auth.AuthService;
import com.br.mesusers.shared.records.PaginatedResponse;
import com.br.mesusers.shared.records.ResponseRecord;


import jakarta.security.auth.message.AuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserController userController;

    private UserRequestDTO userRequestDTO;
    private UserResponseDTO userResponseDTO;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        userRequestDTO = new UserRequestDTO(null, "João da Silva", "joao@gmail.com", "password123", false);
        userResponseDTO = new UserResponseDTO(1L, "João da Silva", "joao@gmail.com", false);
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setAdmin(false);
    }

    @Test
    void createUser_ShouldReturnSuccessResponse_WhenUserIsCreated() {
        when(userService.save(any(UserRequestDTO.class))).thenReturn(userResponseDTO);

        ResponseEntity<ResponseRecord<UserResponseDTO>> response = userController.create(userRequestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
        assertEquals("User created successfully", response.getBody().message());
        assertEquals(userResponseDTO, response.getBody().data());
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenUserCreationFails() {
        when(userService.save(any(UserRequestDTO.class))).thenReturn(null);

        ResponseEntity<ResponseRecord<UserResponseDTO>> response = userController.create(userRequestDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals(400, response.getBody().statusCode());
        assertEquals("User creation failed", response.getBody().message());
    }

    @Test
    void getUserById_ShouldReturnUser_WhenIdExists() {
        when(userService.findById(anyLong())).thenReturn(userResponseDTO);

        ResponseEntity<ResponseRecord<UserResponseDTO>> response = userController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
        assertEquals(userResponseDTO, response.getBody().data());
    }

    @Test
    void getAllUsers_ShouldReturnPaginatedResponse() {
        List<UserResponseDTO> users = Collections.singletonList(userResponseDTO);
        Page<UserResponseDTO> page = new PageImpl<>(users, PageRequest.of(0, 10), 1);
        
        when(userService.findAll(anyInt(), anyInt())).thenReturn(page);

        ResponseEntity<ResponseRecord<PaginatedResponse<UserResponseDTO>>> response = 
            userController.getAll("token", 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
        assertEquals(1, response.getBody().data().items().size());
        assertEquals(1, response.getBody().data().items());
        assertEquals(1, response.getBody().data().totalPages());
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenAuthorized() throws AuthException {
        when(authService.getCurrentUser(anyString())).thenReturn(userEntity);
        when(userService.update(anyLong(), any(UserRequestDTO.class))).thenReturn(userResponseDTO);

        ResponseEntity<ResponseRecord<UserResponseDTO>> response = 
            userController.update(1L, "token", userRequestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
        assertEquals("User updated successfully", response.getBody().message());
        assertEquals(userResponseDTO, response.getBody().data());
    }

    @Test
    void updateUser_ShouldReturnForbidden_WhenNotAuthorized() throws AuthException {
        UserEntity anotherUser = new UserEntity();
        anotherUser.setId(2L);
        anotherUser.setAdmin(false);
        
        when(authService.getCurrentUser(anyString())).thenReturn(anotherUser);

        ResponseEntity<ResponseRecord<UserResponseDTO>> response = 
            userController.update(1L, "token", userRequestDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().statusCode());
        assertEquals("Forbidden", response.getBody().message());
    }

    @Test
    void updateUser_ShouldAllowAdminToUpdateAnyUser() throws AuthException {
        UserEntity adminUser = new UserEntity();
        adminUser.setId(2L);
        adminUser.setAdmin(true);
        
        when(authService.getCurrentUser(anyString())).thenReturn(adminUser);
        when(userService.update(anyLong(), any(UserRequestDTO.class))).thenReturn(userResponseDTO);

        ResponseEntity<ResponseRecord<UserResponseDTO>> response = 
            userController.update(1L, "token", userRequestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
    }

    @Test
    void deleteUser_ShouldReturnSuccess_WhenAuthorized() throws AuthException {
        when(authService.getCurrentUser(anyString())).thenReturn(userEntity);
        when(userService.delete(anyLong())).thenReturn(userResponseDTO);

        ResponseEntity<ResponseRecord<UserResponseDTO>> response = 
            userController.delete("token", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
        assertEquals("User deleted successfully", response.getBody().message());
        assertEquals(userResponseDTO, response.getBody().data());
    }

    @Test
    void deleteUser_ShouldReturnForbidden_WhenNotAuthorized() throws AuthException {
        UserEntity anotherUser = new UserEntity();
        anotherUser.setId(2L);
        anotherUser.setAdmin(false);
        
        when(authService.getCurrentUser(anyString())).thenReturn(anotherUser);

        ResponseEntity<ResponseRecord<UserResponseDTO>> response = 
            userController.delete("token", 1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().statusCode());
        assertEquals("Forbidden", response.getBody().message());
    }

    @Test
    void deleteUser_ShouldAllowAdminToDeleteAnyUser() throws AuthException {
        UserEntity adminUser = new UserEntity();
        adminUser.setId(2L);
        adminUser.setAdmin(true);
        
        when(authService.getCurrentUser(anyString())).thenReturn(adminUser);
        when(userService.delete(anyLong())).thenReturn(userResponseDTO);

        ResponseEntity<ResponseRecord<UserResponseDTO>> response = 
            userController.delete("token", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
    }
}