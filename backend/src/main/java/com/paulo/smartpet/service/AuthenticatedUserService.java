package com.paulo.smartpet.service;

import com.paulo.smartpet.entity.User;
import com.paulo.smartpet.entity.UserRole;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {

    private final UserRepository userRepository;

    public AuthenticatedUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new BusinessException("Usuário autenticado não encontrado");
        }

        String username = authentication.getName().trim().toLowerCase();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));
    }

    public boolean isGlobalAdmin(User user) {
        return user.getRole() == UserRole.SUPER_ADMIN || user.getRole() == UserRole.ADMIN;
    }

    public boolean isStoreAdmin(User user) {
        return user.getRole() == UserRole.ADMIN_STORE;
    }

    public boolean isAttendant(User user) {
        return user.getRole() == UserRole.ATTENDANT;
    }

    public Long getRequiredStoreId(User user) {
        if (user.getStore() == null || user.getStore().getId() == null) {
            throw new BusinessException("Usuário não possui loja vinculada");
        }
        return user.getStore().getId();
    }
}