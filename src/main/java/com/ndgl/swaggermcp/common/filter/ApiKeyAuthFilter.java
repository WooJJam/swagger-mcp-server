package com.ndgl.swaggermcp.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String MCP_ENDPOINT = "/mcp";

    private final String validToken;

    public ApiKeyAuthFilter(@Value("${mcp.auth.token}") final String validToken) {
        this.validToken = validToken;
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        // /mcp 경로에만 필터 적용
        return !request.getRequestURI().startsWith(MCP_ENDPOINT);
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("MCP 접근 거부 - Authorization 헤더 없음: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"인증 토큰이 필요합니다\"}");
            return;
        }

        final String token = authHeader.substring(BEARER_PREFIX.length());

        if (!validToken.equals(token)) {
            log.warn("MCP 접근 거부 - 유효하지 않은 토큰: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"유효하지 않은 토큰입니다\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
