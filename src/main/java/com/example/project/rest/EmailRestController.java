package com.example.project.rest;


import com.example.project.dto.SendCodeRequest;
import com.example.project.interfaces.EmailService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailRestController {

    private final EmailService emailService;


    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@Valid @RequestBody SendCodeRequest request) {
        log.info("REST: запрос отправки кода на {}", request.getTo());

        emailService.sendPasswordResetCode(request.getTo(), request.getCode());

        return ResponseEntity.ok("Письмо отправлено на " + request.getTo());
    }
}
