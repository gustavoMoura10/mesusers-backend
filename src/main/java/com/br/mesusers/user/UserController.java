package com.br.mesusers.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.br.mesusers.auth.AuthService;
import com.br.mesusers.shared.records.PaginatedResponse;
import com.br.mesusers.shared.records.ResponseRecord;

import jakarta.security.auth.message.AuthException;

@RestController
@RequestMapping("api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<ResponseRecord<UserResponseDTO>> create(@RequestBody UserRequestDTO userRecord) {
        UserResponseDTO savedUser = userService.save(userRecord);
        return savedUser != null ? ResponseEntity.ok(ResponseRecord.success("User created successfully", savedUser))
                : ResponseEntity.badRequest().body(ResponseRecord.error(400, "User creation failed"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseRecord<UserResponseDTO>> getById(@PathVariable Long id) {
        UserResponseDTO user = userService.findById(id);
        return ResponseEntity.ok(ResponseRecord.success(user));
    }

    @GetMapping
    public ResponseEntity<ResponseRecord<PaginatedResponse<UserResponseDTO>>> getAll(
            @RequestHeader("Authorization") String token,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        Page<UserResponseDTO> pageResult = userService.findAll(page, pageSize);
        PaginatedResponse<UserResponseDTO> response = new PaginatedResponse<>(
                pageResult.getContent(),
                page,
                (int) pageResult.getTotalElements(),
                pageSize,
                pageResult.getTotalPages());

        return ResponseEntity.ok(ResponseRecord.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseRecord<UserResponseDTO>> update(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String token,
            @RequestBody UserRequestDTO user) throws AuthException {
        UserEntity userEntity = authService.getCurrentUser(token);
        if (!userEntity.getId().equals(id) && !userEntity.getAdmin()) {
            return ResponseEntity.status(403).body(ResponseRecord.error(403, "Forbidden"));
        }
        UserResponseDTO updatedUser = userService.update(id, user);
        return ResponseEntity.ok(ResponseRecord.success("User updated successfully", updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseRecord<UserResponseDTO>> delete(@RequestHeader("Authorization") String token,
            @PathVariable("id") Long id) throws AuthException {
        UserEntity userEntity = authService.getCurrentUser(token);
        if (!userEntity.getId().equals(id) && !userEntity.getAdmin()) {
            return ResponseEntity.status(403).body(ResponseRecord.error(403, "Forbidden"));
        }
        UserResponseDTO deletedUser = userService.delete(id);
        return ResponseEntity.ok(ResponseRecord.success("User deleted successfully", deletedUser));
    }
}