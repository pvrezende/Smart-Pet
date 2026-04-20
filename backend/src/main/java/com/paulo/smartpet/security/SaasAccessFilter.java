package com.paulo.smartpet.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paulo.smartpet.dto.ApiErrorResponse;
import com.paulo.smartpet.entity.User;
import com.paulo.smartpet.exception.SaasAccessDeniedException;
import com.paulo.smartpet.repository.UserRepository;
import com.paulo.smartpet.service.StoreAccessGuardService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SaasAccessFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final StoreAccessGuardService storeAccessGuardService;
    private final ObjectMapper objectMapper;

    public SaasAccessFilter(
            UserRepository userRepository,
            StoreAccessGuardService storeAccessGuardService,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.storeAccessGuardService = storeAccessGuardService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/api/auth/")
                || path.startsWith("/h2-console")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = authentication.getName().trim().toLowerCase();

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            storeAccessGuardService.validateOperationalAccess(user);
            filterChain.doFilter(request, response);
        } catch (SaasAccessDeniedException ex) {
            ApiErrorResponse errorResponse = new ApiErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.FORBIDDEN.value(),
                    HttpStatus.FORBIDDEN.getReasonPhrase(),
                    ex.getMessage(),
                    request.getRequestURI(),
                    List.of()
            );

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }
}