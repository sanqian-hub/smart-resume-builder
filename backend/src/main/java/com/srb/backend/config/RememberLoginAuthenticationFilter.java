package com.srb.backend.config;

import com.srb.backend.common.SessionUtils;
import com.srb.backend.service.RememberLoginTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RememberLoginAuthenticationFilter extends OncePerRequestFilter {

    private final RememberLoginTokenService rememberLoginTokenService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SessionUtils.getLoginUserIdIfPresent(request) == null) {
            rememberLoginTokenService.restoreSessionIfPossible(request, response);
        }
        filterChain.doFilter(request, response);
    }
}
