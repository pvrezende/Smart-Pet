package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.ApiPageResponse;
import com.paulo.smartpet.dto.CreateUserRequest;
import com.paulo.smartpet.dto.UserResponse;
import com.paulo.smartpet.entity.User;
import com.paulo.smartpet.entity.UserRole;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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

    public ApiPageResponse<UserResponse> listPaged(
            Boolean active,
            String role,
            String search,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir
    ) {
        String normalizedSearch = normalizeBlank(search);
        UserRole normalizedRole = normalizeRole(role);

        int safePage = page == null ? 0 : page;
        int safeSize = size == null ? 10 : size;

        if (safePage < 0) {
            throw new BusinessException("Página não pode ser negativa");
        }

        if (safeSize < 1 || safeSize > 100) {
            throw new BusinessException("Tamanho da página deve estar entre 1 e 100");
        }

        String safeSortBy = resolveUserSortBy(sortBy);
        Sort.Direction direction = resolveSortDirection(sortDir);

        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, safeSortBy));

        Page<User> result = userRepository.findPageByFilters(active, normalizedRole, normalizedSearch, pageable);

        List<UserResponse> content = result.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new ApiPageResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast(),
                result.isEmpty()
        );
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

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private UserRole normalizeRole(String value) {
        String normalized = normalizeBlank(value);
        if (normalized == null) {
            return null;
        }

        try {
            return UserRole.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Perfil inválido");
        }
    }

    private Sort.Direction resolveSortDirection(String sortDir) {
        return "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private String resolveUserSortBy(String sortBy) {
        String normalized = normalizeBlank(sortBy);
        Set<String> allowed = Set.of("id", "name", "username", "role", "active");

        if (normalized == null) {
            return "name";
        }

        if (!allowed.contains(normalized)) {
            throw new BusinessException("Campo de ordenação inválido para usuários");
        }

        return normalized;
    }
}