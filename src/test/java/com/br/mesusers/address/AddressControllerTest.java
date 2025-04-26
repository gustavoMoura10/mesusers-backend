package com.br.mesusers.address;

import com.br.mesusers.auth.AuthService;
import com.br.mesusers.shared.records.PaginatedResponse;
import com.br.mesusers.shared.records.ResponseRecord;
import com.br.mesusers.user.UserEntity;
import com.br.mesusers.user.UserResponseDTO;
import com.br.mesusers.viacep.ViaCepResponse;
import com.br.mesusers.viacep.ViaCepService;
import jakarta.security.auth.message.AuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

    @Mock
    private AddressService addressService;

    @Mock
    private ViaCepService viaCepService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AddressController addressController;

    private UserEntity userEntity;
    private AddressRequestDTO addressRequestDTO;
    private ViaCepResponse viaCepResponse;
    private AddressResponseDTO addressResponseDTO;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setAdmin(false);

        addressRequestDTO = new AddressRequestDTO(
                "12345678",
                "123",
                "Apto 101",
                null,
                null,
                null,
                null,
                1L);

        viaCepResponse = new ViaCepResponse(
                "12345678",
                "Rua Exemplo",
                "Centro",
                "São Paulo",
                "SP", null);

        addressResponseDTO = new AddressResponseDTO(
                1L,
                "12345678",
                "123",
                "Apto 101",
                "Rua Exemplo",
                "Centro",
                "São Paulo",
                "SP",
                new UserResponseDTO(1L, "Joaozinho", "joao@gmail.com", false));
    }

    @Test
    void create_ShouldReturnAddress_WhenAuthorized() throws AuthException {
        when(authService.getCurrentUser(anyString())).thenReturn(userEntity);
        when(viaCepService.getAddressFromCep(anyString())).thenReturn(viaCepResponse);
        when(addressService.create(any())).thenReturn(addressResponseDTO);

        ResponseEntity<ResponseRecord<AddressResponseDTO>> response = addressController.create("token",
                addressRequestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Address created successfully", response.getBody().message());
        assertEquals(addressResponseDTO, response.getBody().data());
    }

    @Test
    void create_ShouldReturnForbidden_WhenUnauthorized() throws AuthException {
        userEntity.setId(2L);
        when(authService.getCurrentUser(anyString())).thenReturn(userEntity);
        addressRequestDTO = new AddressRequestDTO(
                "12345678", "123", "Apto 101", null, null, null, null, 1L);

        ResponseEntity<ResponseRecord<AddressResponseDTO>> response = addressController.create("token",
                addressRequestDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().statusCode());
    }

    @Test
    void getById_ShouldReturnAddress() throws AuthException {
        when(addressService.findById(anyLong())).thenReturn(addressResponseDTO);

        ResponseEntity<ResponseRecord<AddressResponseDTO>> response = addressController.getById("token", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(addressResponseDTO, response.getBody().data());
    }

    @Test
    void getAll_ShouldReturnPaginatedAddresses() throws AuthException {
        Page<AddressResponseDTO> page = new PageImpl<>(
                Collections.singletonList(addressResponseDTO),
                PageRequest.of(0, 10),
                1);
        when(addressService.findAll(anyInt(), anyInt())).thenReturn(page);

        ResponseEntity<ResponseRecord<PaginatedResponse<AddressResponseDTO>>> response = addressController
                .getAll("token", 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().data().items().size());
    }

    @Test
    void update_ShouldReturnUpdatedAddress_WhenAuthorized() throws AuthException {
        when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
        when(viaCepService.getAddressFromCep(anyString())).thenReturn(viaCepResponse);
        when(addressService.update(anyLong(), anyLong(), any())).thenReturn(addressResponseDTO);

        ResponseEntity<ResponseRecord<AddressResponseDTO>> response = addressController.update("token", 1L,
                addressRequestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(addressResponseDTO, response.getBody().data());
    }

    @Test
    void delete_ShouldReturnSuccess_WhenAuthorized() throws AuthException {
        when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
        when(addressService.findById(anyLong())).thenReturn(addressResponseDTO);
        when(addressService.delete(anyLong())).thenReturn(addressResponseDTO);

        ResponseEntity<ResponseRecord<Void>> response = addressController.delete("token", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void delete_ShouldReturnForbidden_WhenUnauthorized() throws AuthException {
        when(authService.getUserIdFromToken(anyString())).thenReturn(2L);
        when(addressService.findById(anyLong())).thenReturn(addressResponseDTO);

        ResponseEntity<ResponseRecord<Void>> response = addressController.delete("token", 1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}