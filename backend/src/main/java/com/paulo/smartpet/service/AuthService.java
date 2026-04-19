package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.AuthMeResponse;
import com.paulo.smartpet.dto.LoginRequest;
import com.paulo.smartpet.dto.LoginResponse;
import com.paulo.smartpet.entity.User;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.UserRepository;
import com.paulo.smartpet.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim().toLowerCase();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuário ou senha inválidos"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BusinessException("Usuário inativo");
        }

        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPassword());

        if (!passwordMatches) {
            throw new BusinessException("Usuário ou senha inválidos");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getRole(),
                user.getStore() != null ? user.getStore().getId() : null,
                user.getStore() != null ? user.getStore().getName() : null,
                user.getActive(),
                token,
                "Login realizado com sucesso"
        );
    }

    public AuthMeResponse me(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));

        return new AuthMeResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getRole(),
                user.getStore() != null ? user.getStore().getId() : null,
                user.getStore() != null ? user.getStore().getName() : null,
                user.getActive()
        );
    }
}