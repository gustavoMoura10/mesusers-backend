package com.br.mesusers.user;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.br.mesusers.shared.reflection.DTOMapper;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private String encryptPassword(String rawPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(rawPassword);
    }

    public UserResponseDTO save(UserRequestDTO userDTO) {
        String encryptedPassword = encryptPassword(userDTO.password());
        userDTO = new UserRequestDTO(null, userDTO.username(), userDTO.email(), encryptedPassword, false);
        UserEntity user = DTOMapper.transform(userDTO, UserEntity.class);
        user = userRepository.save(user);
        return DTOMapper.transform(user, UserResponseDTO.class);
    }

    public UserResponseDTO findById(Long id) {
        Optional<UserEntity> user = userRepository.findById(id);
        return user.map(u -> DTOMapper.transform(u, UserResponseDTO.class)).orElse(null);
    }

    public UserResponseDTO update(Long id, UserRequestDTO userDTO) {
        Optional<UserEntity> existingUserOpt = userRepository.findById(id);

        if (existingUserOpt.isEmpty()) {
            return null;
        }

        UserEntity existingUser = existingUserOpt.get();
        if (userDTO.username() != null) {
            existingUser.setUsername(userDTO.username());
        }
        if (userDTO.email() != null) {
            existingUser.setEmail(userDTO.email());
        }
        if (userDTO.password() != null && !userDTO.password().isEmpty()) {
            existingUser.setPassword(encryptPassword(userDTO.password()));
        }
        if (userDTO.admin() != null) {
            existingUser.setAdmin(userDTO.admin());
        }
        UserEntity updated = userRepository.save(existingUser);
        return DTOMapper.transform(updated, UserResponseDTO.class);
    }

    public UserResponseDTO delete(Long id) {
        Optional<UserEntity> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.delete(user.get());
            return DTOMapper.transform(user.get(), UserResponseDTO.class);
        }
        return null;
    }

    public Page<UserResponseDTO> findAll(int page, int pageSize) {
        Page<UserEntity> users = userRepository.findAll(PageRequest.of(page - 1, pageSize));
        return users.map(user -> DTOMapper.transform(user, UserResponseDTO.class));
    }

    public UserResponseDTO findByEmail(String email) {
        Optional<UserEntity> user = userRepository.findByEmail(email);
        return user.map(u -> DTOMapper.transform(u, UserResponseDTO.class)).orElse(null);
    }
}
