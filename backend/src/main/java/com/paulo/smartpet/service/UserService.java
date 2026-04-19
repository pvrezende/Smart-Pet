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
    private final AuthenticatedUserService authenticatedUserService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            StoreService storeService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.storeService = storeService;
        this.authenticatedUserService = authenticatedUserService;
    }

    public List<UserResponse> list(Long storeId) {
        User currentUser = authenticatedUserService.getCurrentUser();

        if (authenticatedUserService.isGlobalAdmin(currentUser)) {
            List<User> users = storeId == null
                    ? userRepository.findAll()
                    : userRepository.findByStoreIdOrderByNameAsc(storeId);

            return users.stream()
                    .sorted(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER))
                    .map(this::toResponse)
                    .toList();
        }

        if (authenticatedUserService.isStoreAdmin(currentUser)) {
            Long currentStoreId = authenticatedUserService.getRequiredStoreId(currentUser);

            if (storeId != null && !storeId.equals(currentStoreId)) {
                throw new BusinessException("Você não pode listar usuários de outra loja");
            }

            return userRepository.findByStoreIdOrderByNameAsc(currentStoreId)
                    .stream()
                    .sorted(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER))
                    .map(this::toResponse)
                    .toList();
        }

        throw new BusinessException("Você não tem permissão para listar usuários");
    }

    public UserResponse getById(Long id) {
        User currentUser = authenticatedUserService.getCurrentUser();
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (authenticatedUserService.isGlobalAdmin(currentUser)) {
            return toResponse(targetUser);
        }

        if (authenticatedUserService.isStoreAdmin(currentUser)) {
            Long currentStoreId = authenticatedUserService.getRequiredStoreId(currentUser);
            Long targetStoreId = targetUser.getStore() != null ? targetUser.getStore().getId() : null;

            if (targetStoreId == null || !targetStoreId.equals(currentStoreId)) {
                throw new BusinessException("Você não pode acessar usuário de outra loja");
            }

            return toResponse(targetUser);
        }

        throw new BusinessException("Você não tem permissão para visualizar usuários");
    }

    public UserResponse create(CreateUserRequest request) {
        User currentUser = authenticatedUserService.getCurrentUser();
        String username = normalizeUsername(request.username());

        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("Já existe usuário cadastrado com esse username");
        }

        validateUserCreationPermission(currentUser, request);

        Store store = resolveStoreForRole(request.role(), request.storeId(), currentUser);

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
        User currentUser = authenticatedUserService.getCurrentUser();
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (authenticatedUserService.isGlobalAdmin(currentUser)) {
            targetUser.setActive(false);
            userRepository.save(targetUser);
            return;
        }

        if (authenticatedUserService.isStoreAdmin(currentUser)) {
            Long currentStoreId = authenticatedUserService.getRequiredStoreId(currentUser);
            Long targetStoreId = targetUser.getStore() != null ? targetUser.getStore().getId() : null;

            if (targetStoreId == null || !targetStoreId.equals(currentStoreId)) {
                throw new BusinessException("Você não pode desativar usuário de outra loja");
            }

            if (targetUser.getRole() != UserRole.ATTENDANT) {
                throw new BusinessException("Admin da loja só pode desativar atendentes da própria loja");
            }

            targetUser.setActive(false);
            userRepository.save(targetUser);
            return;
        }

        throw new BusinessException("Você não tem permissão para desativar usuários");
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

    private void validateUserCreationPermission(User currentUser, CreateUserRequest request) {
        if (authenticatedUserService.isGlobalAdmin(currentUser)) {
            return;
        }

        if (authenticatedUserService.isStoreAdmin(currentUser)) {
            if (request.role() != UserRole.ATTENDANT) {
                throw new BusinessException("Admin da loja só pode criar usuários do tipo ATTENDANT");
            }

            Long currentStoreId = authenticatedUserService.getRequiredStoreId(currentUser);

            if (request.storeId() == null || !request.storeId().equals(currentStoreId)) {
                throw new BusinessException("Admin da loja só pode criar usuários para a própria loja");
            }

            return;
        }

        throw new BusinessException("Você não tem permissão para criar usuários");
    }

    private Store resolveStoreForRole(UserRole role, Long storeId, User currentUser) {
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

        if (authenticatedUserService.isStoreAdmin(currentUser)) {
            Long currentStoreId = authenticatedUserService.getRequiredStoreId(currentUser);
            if (!storeId.equals(currentStoreId)) {
                throw new BusinessException("Admin da loja só pode operar na própria loja");
            }
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