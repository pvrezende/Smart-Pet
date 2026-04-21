package com.paulo.smartpet.security;

import com.paulo.smartpet.service.SaasAccessControlService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SaasAccessFilter extends OncePerRequestFilter {

    private final SaasAccessControlService saasAccessControlService;

    public SaasAccessFilter(SaasAccessControlService saasAccessControlService) {
        this.saasAccessControlService = saasAccessControlService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (shouldSkip(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        saasAccessControlService.validateCurrentUserOperationalAccess();
        filterChain.doFilter(request, response);
    }

    private boolean shouldSkip(String uri) {
        return uri.startsWith("/api/auth/")
                || uri.startsWith("/api/saas-plans/catalog")
                || uri.startsWith("/h2-console")
                || uri.startsWith("/error")
                || uri.startsWith("/actuator");
    }
}