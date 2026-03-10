package com.example.project.interfaces;

public interface EmailService {
    void sendPasswordResetCode(String to, String code);
}
