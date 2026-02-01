package com.example.project.controllers;


import com.example.project.dto.ForgotPasswordDTO;
import com.example.project.dto.ResetPasswordDTO;
import com.example.project.dto.UserRegisterDTO;
import com.example.project.interfaces.ResetPassword;
import com.example.project.interfaces.SendResetToken;
import com.example.project.service.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthServiceImpl authService;
    private final SendResetToken sendResetToken;
    private final ResetPassword resetPassword;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserRegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute("user") @Valid UserRegisterDTO dto,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            return "register";
        }

        try {
            authService.registerUser(dto);
            return "redirect:/auth/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("forgot", new ForgotPasswordDTO());
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendResetToken(
            @ModelAttribute("forgot") @Valid ForgotPasswordDTO dto,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            return "forgot-password";
        }

        sendResetToken.sendResetToken(dto.getEmail());
        return "redirect:/auth/reset-password?sent";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(Model model) {
        model.addAttribute("reset", new ResetPasswordDTO());
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @ModelAttribute("reset") @Valid ResetPasswordDTO dto,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            return "reset-password";
        }

        try {
            resetPassword.resetPassword(dto.getToken(), dto.getNewPassword());
            return "redirect:/auth/login?resetSuccess";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "reset-password";
        }
    }
}

