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
import com.example.project.repository.UserRepository;
import com.example.project.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements Register, Login {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @Override
    public void registerUser(UserRegisterDTO dto) {
        log.info("Регистрация пользователя: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        Role role;
        if (dto.getRole() != null) {
            try {
                role = Role.valueOf(dto.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Неизвестная роль: {}, устанавливаем по умолчанию USER", dto.getRole());
                role = Role.USER;
            }
        } else {
            role = Role.USER;
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Пользователь сохранен: {}", savedUser.getEmail());


    }

    @Override
    public UserResponseDTO loginUser(UserLoginDTO dto) {
        log.info("Попытка входа: {}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UserNotFoundByEmailException("Пользователь с таким email не найден."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Неверный пароль.");
        }

        String token = jwtUtil.generateToken(user.getUsername(), List.of(user.getRole()));


        return UserResponseDTO.builder()
                .id(String.valueOf(user.getId()))
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(token)
                .build();
    }


}

