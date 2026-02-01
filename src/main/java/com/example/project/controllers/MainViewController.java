package com.example.project.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class MainViewController {


    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }


    @GetMapping("/")
    public String homePage() {
        return "redirect:/dashboard";
    }


    @GetMapping("/profile")
    public String profilePage() {
        return "profile";
    }

    @GetMapping("/settings")
    public String settingsPage() {
        return "settings";
    }
}