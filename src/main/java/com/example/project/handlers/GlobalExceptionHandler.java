package com.example.project.handlers;

import com.example.project.exceptions.EmailException;
import com.example.project.exceptions.EmailSendingExeption;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<String> handleEmailException(EmailException ex) {
        return ResponseEntity.status(500).body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleOtherRuntime(RuntimeException ex) {
        return ResponseEntity.status(500).body(ex.getMessage());
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body("Request body is missing or invalid");
    }

    @ExceptionHandler(EmailSendingExeption.class)
    public ResponseEntity<String> handleEmailError(EmailSendingExeption e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}

