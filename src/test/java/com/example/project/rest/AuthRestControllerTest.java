package com.example.project.rest;


import com.example.project.dto.UserLoginDTO;
import com.example.project.dto.UserRegisterDTO;
import com.example.project.dto.UserResponseDTO;
import com.example.project.enums.Role;
import com.example.project.filters.JwtFilter;
import com.example.project.service.AuthServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
}

