package com.seekglobal.authapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seekglobal.authapi.dto.AuthRequest;
import com.seekglobal.authapi.handler.GlobalExceptionHandler;
import com.seekglobal.authapi.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest {
    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void shouldReturnTokenWhenLoginIsSuccessful() throws Exception {
        String username = "test_user";
        String password = "password123";
        String jwtToken = "mocked-jwt-token";

        AuthRequest authRequest = new AuthRequest(username, password);

        when(authService.login(username, password)).thenReturn(jwtToken);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(jwtToken));

        verify(authService, times(1)).login(username, password);
    }

    @Test
    @WithMockUser
    void shouldReturnUnauthorizedWhenLoginFails() throws Exception {
        String username = "test_user";
        String password = "wrong_password";

        AuthRequest authRequest = new AuthRequest(username, password);

        when(authService.login(username, password))
                .thenThrow(new BadCredentialsException("Incorrect password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));

        verify(authService, times(1)).login(username, password);
    }
}
