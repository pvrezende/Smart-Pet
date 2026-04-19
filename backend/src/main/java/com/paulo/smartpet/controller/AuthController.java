package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.AuthMeResponse;
import com.paulo.smartpet.dto.LoginRequest;
import com.paulo.smartpet.dto.LoginResponse;
import com.paulo.smartpet.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthMeResponse me(Authentication authentication) {
        return authService.me(authentication.getName());
    }
}