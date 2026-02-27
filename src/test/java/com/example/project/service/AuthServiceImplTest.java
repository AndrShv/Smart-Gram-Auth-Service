package com.example.project.service;

import com.example.project.dto.UserLoginDTO;
import com.example.project.dto.UserRegisterDTO;
import com.example.project.dto.UserResponseDTO;
import com.example.project.entity.User;
import com.example.project.enums.Role;
import com.example.project.exceptions.InvalidPasswordException;
import com.example.project.exceptions.UserAlreadyExistsException;
import com.example.project.exceptions.UserNotFoundByEmailException;
import com.example.project.repository.UserRepository;
import com.example.project.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("andrey")
                .email("andrey@test.com")
                .password("encoded-password")
                .role(Role.USER)
                .active(true)
                .build();
    }

    // ============================
    // registerUser
    // ============================

    @Test
    void registerUser_success() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("Andrey");
        dto.setEmail("andrey678a@gmail.com");
        dto.setPassword("password123");


        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> authService.registerUser(dto));

        verify(userRepository).existsByEmail(dto.getEmail());
        verify(passwordEncoder).encode(dto.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_emailAlreadyExists() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("Andrey");
        dto.setEmail("andrey678a@gmail.com");
        dto.setPassword("password123");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.registerUser(dto));

        verify(userRepository).existsByEmail(dto.getEmail());
        verify(userRepository, never()).save(any());
    }

    // ============================
    // loginUser
    // ============================

    @Test
    void loginUser_success() {
        UserLoginDTO dto = new UserLoginDTO(
                "andrey@test.com",
                "password123"
        );

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(dto.getPassword(), user.getPassword()))
                .thenReturn(true);

        when(jwtUtil.generateToken(
                user.getEmail(),
                user.getId(),
                List.of(user.getRole())
        )).thenReturn("jwt-token");


        UserResponseDTO response = authService.loginUser(dto);

        assertNotNull(response);
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals("USER", response.getRole());
        assertEquals("jwt-token", response.getToken());

        verify(jwtUtil).generateToken(user.getEmail(), user.getId(), List.of(user.getRole()));
    }

    @Test
    void loginUser_userNotFound() {
        UserLoginDTO dto = new UserLoginDTO(
                "notfound@test.com",
                "password"
        );

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundByEmailException.class,
                () -> authService.loginUser(dto));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }

    @Test
    void loginUser_invalidPassword() {
        UserLoginDTO dto = new UserLoginDTO(
                "andrey@test.com",
                "wrong-password"
        );

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(dto.getPassword(), user.getPassword()))
                .thenReturn(false);

        assertThrows(InvalidPasswordException.class,
                () -> authService.loginUser(dto));

        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }

    // ============================
// registerUser - edge cases
// ============================

    @Test
    void registerUser_withRoleAdmin_success() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("AdminUser");
        dto.setEmail("admin@test.com");
        dto.setPassword("pass123");
        dto.setRole("ADMIN");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> authService.registerUser(dto));

        verify(userRepository).save(argThat(user -> user.getRole() == Role.ADMIN));
    }

    @Test
    void registerUser_withInvalidRole_shouldDefaultToUser() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("TestUser");
        dto.setEmail("test@test.com");
        dto.setPassword("pass123");
        dto.setRole("INVALID_ROLE");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> authService.registerUser(dto));

        verify(userRepository).save(argThat(user -> user.getRole() == Role.USER));
    }

// ============================
// loginUser - additional edge cases
// ============================

    @Test
    void loginUser_nullPassword_shouldThrowInvalidPasswordException() {
        UserLoginDTO dto = new UserLoginDTO(user.getEmail(), null);

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));

        assertThrows(InvalidPasswordException.class,
                () -> authService.loginUser(dto));
    }

    @Test
    void loginUser_nullEmail_shouldThrowUserNotFoundByEmailException() {
        UserLoginDTO dto = new UserLoginDTO(null, "password123");

        assertThrows(UserNotFoundByEmailException.class,
                () -> authService.loginUser(dto));
    }

    @Test
    void loginUser_multipleRoles_tokenGeneratedCorrectly() {
        // создадим юзера с ролями
        user.setRole(Role.ADMIN); // в текущей модели один Role, можно проверить через jwtUtil
        UserLoginDTO dto = new UserLoginDTO(user.getEmail(), "password123");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(
                user.getEmail(),
                user.getId(),
                List.of(user.getRole())
        )).thenReturn("jwt-token-admin");

        UserResponseDTO response = authService.loginUser(dto);

        assertEquals("jwt-token-admin", response.getToken());
        assertEquals("ADMIN", response.getRole());
    }

// ============================
// registerUser edge: empty username/password/email
// ============================

    @Test
    void registerUser_emptyUsername_shouldStillSave() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("");
        dto.setEmail("empty@test.com");
        dto.setPassword("password123");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> authService.registerUser(dto));
    }

    @Test
    void registerUser_emptyPassword_shouldSaveEncodedEmptyPassword() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("EmptyPass");
        dto.setEmail("emptypass@test.com");
        dto.setPassword("");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("")).thenReturn("encoded-empty");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.registerUser(dto);

        verify(userRepository).save(argThat(user -> user.getPassword().equals("encoded-empty")));
    }

}

