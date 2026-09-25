package com.devsclinic.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.devsclinic.backend.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Returns a clean JSON 403 (e.g. an Auditor token hitting a write endpoint) instead of the
 * default HTML error page. */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        ApiError error = ApiError.of(HttpStatus.FORBIDDEN.value(), "You don't have permission to perform this action");
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
