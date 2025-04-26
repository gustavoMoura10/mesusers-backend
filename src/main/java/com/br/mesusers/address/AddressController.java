package com.br.mesusers.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.br.mesusers.auth.AuthService;
import com.br.mesusers.shared.records.PaginatedResponse;
import com.br.mesusers.shared.records.ResponseRecord;
import com.br.mesusers.shared.reflection.DTOMapper;
import com.br.mesusers.user.UserEntity;
import com.br.mesusers.viacep.ViaCepResponse;
import com.br.mesusers.viacep.ViaCepService;
import jakarta.security.auth.message.AuthException;

@RestController
@RequestMapping("api/addresses")
public class AddressController {

        @Autowired
        private AddressService addressService;
        @Autowired
        private ViaCepService viaCepService;
        @Autowired
        private AuthService authService;

        @PostMapping
        public ResponseEntity<ResponseRecord<AddressResponseDTO>> create(
                        @RequestHeader("Authorization") String token,
                        @Validated @RequestBody AddressRequestDTO request) throws AuthException {

                UserEntity userEntity = authService.getCurrentUser(token);
                if (request.userId() != null && !request.userId().equals(userEntity.getId())
                                && !userEntity.getAdmin()) {
                        return ResponseEntity.status(403).body(ResponseRecord.error(403, "Forbidden"));
                }
                ViaCepResponse viaCepResponse = viaCepService.getAddressFromCep(request.cep());
                AddressRequestDTO completeRequest = new AddressRequestDTO(
                                viaCepResponse.cep(),
                                request.number(),
                                request.complement(),
                                viaCepResponse.street(),
                                viaCepResponse.neighborhood(),
                                viaCepResponse.city(),
                                viaCepResponse.state(),
                                request.userId());
                AddressResponseDTO address = addressService.create(completeRequest);

                return ResponseEntity.ok(ResponseRecord.success(
                                "Address created successfully",
                                address));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ResponseRecord<AddressResponseDTO>> getById(
                        @RequestHeader("Authorization") String token,
                        @PathVariable Long id) throws AuthException {

                // Long userId = authService.getUserIdFromToken(token);
                AddressResponseDTO address = addressService.findById(id);

                return ResponseEntity.ok(ResponseRecord.success(
                                "Address retrieved successfully",
                                address));
        }

        @GetMapping
        public ResponseEntity<ResponseRecord<PaginatedResponse<AddressResponseDTO>>> getAll(
                        @RequestHeader("Authorization") String token,
                        @RequestParam(name = "page", defaultValue = "1") int page,
                        @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) throws AuthException {
                Page<AddressResponseDTO> pageResult = addressService.findAll(page, pageSize);

                PaginatedResponse<AddressResponseDTO> response = new PaginatedResponse<>(
                                pageResult.getContent(),
                                page,
                                (int) pageResult.getTotalElements(),
                                pageSize,
                                pageResult.getTotalPages());

                return ResponseEntity.ok(ResponseRecord.success(
                                "Addresses retrieved successfully",
                                response));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ResponseRecord<AddressResponseDTO>> update(
                        @RequestHeader("Authorization") String token,
                        @PathVariable("id") Long id,
                        @Validated @RequestBody AddressRequestDTO request) throws AuthException {

                Long userId = authService.getUserIdFromToken(token);
                if (request.userId() != null && !request.userId().equals(userId) && !authService.isAdmin(token)) {
                        return ResponseEntity.status(403).body(ResponseRecord.error(403, "Forbidden"));
                }
                ViaCepResponse viaCepResponse = viaCepService.getAddressFromCep(request.cep());
                AddressRequestDTO completeRequest = new AddressRequestDTO(
                                viaCepResponse.cep(),
                                request.number(),
                                request.complement(),
                                viaCepResponse.street(),
                                viaCepResponse.neighborhood(),
                                viaCepResponse.city(),
                                viaCepResponse.state(),
                                request.userId());

                AddressResponseDTO updatedAddress = addressService.update(id, request.userId(), completeRequest);

                return ResponseEntity.ok(ResponseRecord.success(
                                "Address updated successfully",
                                updatedAddress));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ResponseRecord<Void>> delete(
                        @RequestHeader("Authorization") String token,
                        @PathVariable("id") Long id) throws AuthException {

                Long userId = authService.getUserIdFromToken(token);
                AddressResponseDTO addressResponseDTO = addressService.findById(id);
                if (addressResponseDTO.user().id() != null && !addressResponseDTO.user().id().equals(userId)
                                && !authService.isAdmin(token)) {
                        return ResponseEntity.status(403).body(ResponseRecord.error(403, "Forbidden"));
                }
                addressResponseDTO = addressService.delete(id);

                return ResponseEntity.ok(ResponseRecord.success(
                                "Address deleted successfully",
                                null));
        }
}