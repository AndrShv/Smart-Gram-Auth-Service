package com.example.project.service;


import com.example.project.exceptions.EmailSendingExeption;
import com.example.project.interfaces.EmailService;
import com.example.project.metrics.EmailMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailMetricsService emailMetrics;

    @Override
    public void sendPasswordResetCode(String to, String code) {
        log.info("Отправка письма сброса пароля на: {}", to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password reset");
        message.setText("Ваш код: " + code);

        try {
            log.info("Отправляем письмо на: {}", to);
            mailSender.send(message);
            emailMetrics.sent();
            log.info("Письмо успешно отправлено на: {}", to);
        } catch (Exception e) {
            emailMetrics.failed();
            log.error("Ошибка отправки письма на {}: {}", to, e.getMessage());
            throw new EmailSendingExeption("Email sending failed", e);
        }
    }
}
