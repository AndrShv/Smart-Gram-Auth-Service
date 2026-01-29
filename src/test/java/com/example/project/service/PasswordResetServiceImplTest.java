package com.example.project.service;


import com.example.project.entity.User;
import com.example.project.exceptions.EmailSendingExeption;
import com.example.project.exceptions.InvalidTokenException;
import com.example.project.exceptions.UserNotFoundByEmailException;
import com.example.project.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("andrey@test.com")
                .password("old-password")
                .build();
    }

    // ============================
    // sendResetToken
    // ============================

    @Test
    void sendResetToken_success() {
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String token = passwordResetService.sendResetToken(user.getEmail());

        assertNotNull(token);
        assertEquals(6, token.length());
        assertNotNull(user.getResetToken());
        assertNotNull(user.getResetTokenCreatedAt());

        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(userRepository).save(user);
    }

    @Test
    void sendResetToken_userNotFound() {
        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundByEmailException.class,
                () -> passwordResetService.sendResetToken("unknown@test.com"));

        verify(mailSender, never()).send((MimeMessage) any());
    }

    @Test
    void sendResetToken_emailSendingError() {
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        doThrow(new RuntimeException("SMTP error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(EmailSendingExeption.class,
                () -> passwordResetService.sendResetToken(user.getEmail()));
    }

    // ============================
    // resetPassword
    // ============================

    @Test
    void resetPassword_success() {
        user.setResetToken("123456");
        user.setResetTokenCreatedAt(LocalDateTime.now().minusSeconds(30));

        when(userRepository.findByResetToken("123456"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encoded-password");

        passwordResetService.resetPassword("123456", "newPassword");

        assertEquals("encoded-password", user.getPassword());
        assertNull(user.getResetToken());
        assertNull(user.getResetTokenCreatedAt());

        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_invalidToken() {
        when(userRepository.findByResetToken("wrong"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class,
                () -> passwordResetService.resetPassword("wrong", "password"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_tokenExpired() {
        user.setResetToken("123456");
        user.setResetTokenCreatedAt(LocalDateTime.now().minusMinutes(5));

        when(userRepository.findByResetToken("123456"))
                .thenReturn(Optional.of(user));

        assertThrows(InvalidTokenException.class,
                () -> passwordResetService.resetPassword("123456", "password"));

        verify(userRepository, never()).save(any());
    }
}

