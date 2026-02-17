package com.example.project.rest;




import com.example.project.dto.UserLoginDTO;
import com.example.project.dto.UserRegisterDTO;
import com.example.project.dto.UserResponseDTO;
import com.example.project.entity.User;
import com.example.project.repository.UserRepository;
import com.example.project.service.AuthServiceImpl;
import com.example.project.service.custom.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthRestController {

    private final AuthServiceImpl authService;
    private final UserRepository userRepository;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserRegisterDTO dto) {
        try {
            authService.registerUser(dto);
            return ResponseEntity.ok("Пользователь успешно зарегистрирован");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginDTO dto) {
        try {
            UserResponseDTO response = authService.loginUser(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String userId = (String) authentication.getPrincipal();

        User user = userRepository.findById(UUID.fromString(userId))
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        UserResponseDTO response = UserResponseDTO.builder()
                .id(String.valueOf(user.getId()))
                .email(user.getEmail())
                .role(String.valueOf(user.getRole()))
                .build();

        return ResponseEntity.ok(response);
    }


}


