package com.example.project.service;

import com.example.project.dto.UserLoginDTO;
import com.example.project.dto.UserRegisterDTO;
import com.example.project.dto.UserResponseDTO;
import com.example.project.entity.User;
import com.example.project.enums.Role;
import com.example.project.exceptions.InvalidPasswordException;
import com.example.project.exceptions.UserAlreadyExistsException;
import com.example.project.exceptions.UserNotFoundByEmailException;
import com.example.project.interfaces.Login;
import com.example.project.interfaces.Register;
import com.example.project.metrics.AuthMetricsService;
import com.example.project.metrics.PerformanceMetricsService;
import com.example.project.repository.UserRepository;
import com.example.project.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements Register, Login {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthMetricsService metrics;
    private final PerformanceMetricsService performanceMetrics;

    @Override
    public void registerUser(UserRegisterDTO dto) {
        performanceMetrics.registerTimer().record(() -> {
            log.info("Регистрация пользователя: {}", dto.getEmail());

            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new UserAlreadyExistsException("Пользователь уже существует");
            }
            Role role;
            try {
                role = dto.getRole() != null ? Role.valueOf(dto.getRole().toUpperCase()) : Role.USER;
            } catch (IllegalArgumentException e) {
                log.warn("Неизвестная роль '{}', используем USER", dto.getRole());
                role = Role.USER;
            }

            User user = User.builder()
                    .username(dto.getUsername())
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .role(role)
                    .active(true)
                    .build();

            userRepository.save(user);
            metrics.incrementRegister();

            log.info("Пользователь успешно зарегистрирован {}", dto.getEmail());
        });
    }

    @Override
    public UserResponseDTO loginUser(UserLoginDTO dto) throws Exception {
        return performanceMetrics.loginTimer().recordCallable(() -> {
            log.info("Попытка входа: {}", dto.getEmail());

            User user = userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new UserNotFoundByEmailException("User not found"));

            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                metrics.incrementLoginFailed();
                throw new InvalidPasswordException("Неверный пароль");
            }

            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getId(),
                    List.of(user.getRole())
            );

            metrics.incrementLoginSuccess();

            return UserResponseDTO.builder()
                    .id(String.valueOf(user.getId()))
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .token(token)
                    .build();
        });
    }
}