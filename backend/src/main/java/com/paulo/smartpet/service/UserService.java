package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.CreateUserRequest;
import com.paulo.smartpet.dto.UserResponse;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.entity.User;
import com.paulo.smartpet.entity.UserRole;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StoreService storeService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            StoreService storeService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.storeService = storeService;
    }

    public List<UserResponse> list(Long storeId) {
        List<User> users = storeId == null
                ? userRepository.findAll()
                : userRepository.findByStoreIdOrderByNameAsc(storeId);

        return users.stream()
                .sorted(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER))
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

        Store store = resolveStoreForRole(request.role(), request.storeId());

        User user = new User();
        user.setId(null);
        user.setName(request.name().trim());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setStore(store);
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
                    admin.setRole(UserRole.SUPER_ADMIN);
                    admin.setStore(null);
                    admin.setActive(true);
                    return userRepository.save(admin);
                });
    }

    private Store resolveStoreForRole(UserRole role, Long storeId) {
        if (role == null) {
            throw new BusinessException("Perfil é obrigatório");
        }

        if (role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN) {
            if (storeId != null) {
                throw new BusinessException("Usuário global não deve ser vinculado a uma loja");
            }
            return null;
        }

        if (storeId == null) {
            throw new BusinessException("Usuário deste perfil deve ser vinculado a uma loja");
        }

        return storeService.getEntityById(storeId);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getRole(),
                user.getStore() != null ? user.getStore().getId() : null,
                user.getStore() != null ? user.getStore().getName() : null,
                user.getActive()
        );
    }

    private String normalizeUsername(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}