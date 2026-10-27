package com.devAssist.backend.security;

import com.devAssist.backend.dto.AuthRequest;
import com.devAssist.backend.dto.AuthResponse;
import com.devAssist.backend.entity.User;
import com.devAssist.backend.repository.UserRepository;
import com.devAssist.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtService jwt = mock(JwtService.class);
    private final AuthService service = new AuthService(users, encoder, jwt);

    @Test void registrationStoresHashAndReturnsBearerToken() {
        when(users.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(jwt.generate("user@example.com")).thenReturn("signed-token");
        when(jwt.expirationSeconds()).thenReturn(3600L);
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = service.register(new AuthRequest(" User@Example.com ", "password123"));

        assertEquals("user@example.com", response.email());
        assertEquals("signed-token", response.token());
        verify(users).save(argThat(user -> user.getPasswordHash() != null
                && !user.getPasswordHash().equals("password123")
                && encoder.matches("password123", user.getPasswordHash())));
    }

    @Test void duplicateEmailIsRejected() {
        when(users.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.register(new AuthRequest("user@example.com", "password123")));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(users, never()).save(any());
    }

    @Test void loginRejectsInvalidPassword() {
        when(users.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(new User("user@example.com", encoder.encode("correct-password"))));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.login(new AuthRequest("user@example.com", "wrong-password")));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
