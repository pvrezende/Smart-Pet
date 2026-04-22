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

        String path = request.getRequestURI();

        if (isPublicPath(request, path)) {
            filterChain.doFilter(request, response);
            return;
        }

        saasAccessControlService.validateCurrentUserOperationalAccess();
        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(HttpServletRequest request, String path) {
        return path.startsWith("/api/auth/")
                || path.startsWith("/h2-console")
                || ("/api/saas-plans/catalog".equals(path) && "GET".equalsIgnoreCase(request.getMethod()))
                || ("/api/webhooks/asaas".equals(path) && "POST".equalsIgnoreCase(request.getMethod()));
    }
}