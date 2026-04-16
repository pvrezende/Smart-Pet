package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.CreateUserRequest;
import com.paulo.smartpet.dto.UserResponse;
import com.paulo.smartpet.entity.User;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> list() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        return toResponse(user);
    }

    public UserResponse create(CreateUserRequest request) {
        String username = normalizeUsername(request.username());

        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("Já existe usuário cadastrado com esse username");
        }

        User user = new User();
        user.setId(null);
        user.setName(request.name().trim());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setActive(true);

        return toResponse(userRepository.save(user));
    }

    public void deactivate(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        user.setActive(false);
        userRepository.save(user);
    }

    public User ensureDefaultAdminExists() {
        String defaultUsername = "admin";

        return userRepository.findByUsername(defaultUsername)
                .orElseGet(() -> {
                    User admin = new User();
                    admin.setName("Administrador");
                    admin.setUsername(defaultUsername);
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRole(com.paulo.smartpet.entity.UserRole.ADMIN);
                    admin.setActive(true);
                    return userRepository.save(admin);
                });
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getRole(),
                user.getActive()
        );
    }

    private String normalizeUsername(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}