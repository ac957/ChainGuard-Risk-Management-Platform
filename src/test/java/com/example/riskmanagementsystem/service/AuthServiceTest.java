package com.example.riskmanagementsystem.service;

import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.model.UserProfile;
import com.example.riskmanagementsystem.repo.UserProfileRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthServiceTest {

    @Mock private UserRepository userRepo;
    @Mock private UserProfileRepository profileRepo;
    @Mock private PasswordEncoder encoder;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        when(encoder.encode(anyString()))
                .thenReturn("hashedpassword");
        when(userRepo.existsByEmail(anyString()))
                .thenReturn(false);
        when(userRepo.save(any(User.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(profileRepo.save(any(UserProfile.class)))
                .thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void register_shouldThrowWhenFullNameIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.register(
                        null, "test@test.com", "Password1!"));
    }

    @Test
    void register_shouldThrowWhenEmailIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.register(
                        "John Smith", null, "Password1!"));
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyRegistered() {
        when(userRepo.existsByEmail("existing@test.com"))
                .thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> authService.register(
                        "John Smith",
                        "existing@test.com",
                        "Password1!"));
    }

    @Test
    void register_shouldThrowWhenPasswordIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.register(
                        "John Smith", "test@test.com", null));
    }

    @Test
    void register_shouldThrowWhenPasswordHasNoUppercase() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.register(
                        "John Smith",
                        "test@test.com",
                        "password1!"));
    }

    @Test
    void register_shouldThrowWhenPasswordHasNoSpecialChar() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.register(
                        "John Smith",
                        "test@test.com",
                        "Password1"));
    }

    @Test
    void register_shouldThrowWhenPasswordTooShort() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.register(
                        "John Smith",
                        "test@test.com",
                        "Pa1!"));
    }

    @Test
    void register_shouldPassWithValidInputs() {
        assertDoesNotThrow(() ->
                authService.register(
                        "John Smith",
                        "test@test.com",
                        "Password1!"));
    }

    @Test
    void register_shouldEncodePasswordBeforeSaving() {
        authService.register(
                "John Smith", "test@test.com", "Password1!");
        verify(encoder, times(1)).encode("Password1!");
    }

    @Test
    void register_shouldNormaliseEmailToLowercase() {
        authService.register(
                "John Smith", "TEST@TEST.COM", "Password1!");
        verify(userRepo).existsByEmail("test@test.com");
    }
}