package com.seekglobal.authapi.service;

import com.seekglobal.authapi.model.User;
import com.seekglobal.authapi.repository.UserRepository;
import com.seekglobal.authapi.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnTokenWhenLoginIsSuccessful() {
        String username = "test_user";
        String password = "password123";
        String encodedPassword = "encodedPassword";
        String jwtToken = "mocked-jwt-token";

        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setPassword(encodedPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(mockUser)).thenReturn(jwtToken);

        String result = authService.login(username, password);

        assertEquals(jwtToken, result);
        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).matches(password, encodedPassword);
        verify(jwtUtil, times(1)).generateToken(mockUser);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        String username = "non_existent_user";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.login(username, password));

        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        String username = "test_user";
        String password = "wrong_password";
        String encodedPassword = "encodedPassword";

        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setPassword(encodedPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(username, password));

        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).matches(password, encodedPassword);
        verify(jwtUtil, never()).generateToken(any());
    }

}
