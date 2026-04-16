package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.LoginRequest;
import com.paulo.smartpet.dto.LoginResponse;
import com.paulo.smartpet.entity.User;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getRole(),
                user.getActive(),
                "Login realizado com sucesso"
        );
    }
}