package com.example.project.rest;


import com.example.project.dto.UserLoginDTO;
import com.example.project.dto.UserRegisterDTO;
import com.example.project.dto.UserResponseDTO;
import com.example.project.entity.User;
import com.example.project.enums.Role;
import com.example.project.filters.JwtFilter;
import com.example.project.metrics.ApiMetricsService;
import com.example.project.repository.UserRepository;
import com.example.project.service.AuthServiceImpl;
import com.example.project.service.custom.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = AuthRestController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class))
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthServiceImpl authService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ApiMetricsService apiMetrics;

    // ============================
    // REGISTER
    // ============================

    @Test
    void register_success() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("Andrey");
        dto.setEmail("andrey678a@gmail.com");
        dto.setPassword("password123");

        doNothing().when(authService).registerUser(any(UserRegisterDTO.class));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Пользователь успешно зарегистрирован"));

        verify(authService).registerUser(any(UserRegisterDTO.class));
    }

    @Test
    void register_error() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("Andrey");
        dto.setEmail("andrey678a@gmail.com");
        dto.setPassword("password123");

        doThrow(new RuntimeException("User exists"))
                .when(authService).registerUser(any());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User exists"));
    }

    // ============================
    // LOGIN
    // ============================

    @Test
    void login_success() throws Exception {
        UserLoginDTO dto = new UserLoginDTO(
                "andrey@test.com",
                "password123"
        );

        UserResponseDTO response = UserResponseDTO.builder()
                .id("123")
                .username("andrey")
                .email("andrey@test.com")
                .role(Role.USER.name())
                .token("jwt-token")
                .build();

        when(authService.loginUser(any(UserLoginDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("andrey@test.com"))
                .andExpect(jsonPath("$.username").value("andrey"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_unauthorized() throws Exception {
        UserLoginDTO dto = new UserLoginDTO(
                "andrey@test.com",
                "wrong-password"
        );

        when(authService.loginUser(any()))
                .thenThrow(new RuntimeException("Неверный пароль"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Неверный пароль"));
    }


    // ============================
    // REGISTER
    // ============================

    @Test
    void register_missingUsername_shouldReturn400() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setEmail("test@test.com");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingEmail_shouldReturn400() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("TestUser");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingPassword_shouldReturn400() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("TestUser");
        dto.setEmail("test@test.com");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ============================
    // LOGIN
    // ============================

    @Test
    void login_missingEmail_shouldReturn400() throws Exception {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_missingPassword_shouldReturn400() throws Exception {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setEmail("test@test.com");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_nullBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    // ============================
    // /me endpoint
    // ============================

    @Test
    void me_shouldReturnUserDetails() throws Exception {
        User user = new User();
        user.setId(UUID.fromString("76d87ded-3e92-490b-a750-8d07e3b6be6d"));
        user.setEmail("test@test.com");
        user.setRole(Role.USER);

        when(userRepository.findById(UUID.fromString("76d87ded-3e92-490b-a750-8d07e3b6be6d")))
                .thenReturn(Optional.of(user));

        Authentication auth = new TestingAuthenticationToken(
                "76d87ded-3e92-490b-a750-8d07e3b6be6d",
                null,
                List.of(new SimpleGrantedAuthority("USER"))
        );
        auth.setAuthenticated(true);

        mockMvc.perform(get("/api/auth/me")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("76d87ded-3e92-490b-a750-8d07e3b6be6d"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }


    @Test
    void me_withAnonymousUser_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // ============================
    // ERROR HANDLING
    // ============================

    @Test
    void register_runtimeException_shouldReturnBadRequest() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("user");
        dto.setEmail("user@test.com");
        dto.setPassword("password");

        doThrow(new RuntimeException("Custom error")).when(authService).registerUser(any());

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Custom error"));
    }

    @Test
    void login_runtimeException_shouldReturn401() throws Exception {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setEmail("user@test.com");
        dto.setPassword("password");

        when(authService.loginUser(any())).thenThrow(new RuntimeException("Login failed"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Login failed"));
    }
}

