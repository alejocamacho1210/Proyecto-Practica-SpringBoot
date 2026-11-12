package com.academy.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorWriter errors;

    public RestAuthenticationEntryPoint(SecurityErrorWriter errors) { this.errors = errors; }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        Object reason = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE);
        String message = reason == null ? "Se requiere autenticación para acceder a este recurso" : reason.toString();
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        errors.write(request, response, HttpStatus.UNAUTHORIZED, message);
    }
}