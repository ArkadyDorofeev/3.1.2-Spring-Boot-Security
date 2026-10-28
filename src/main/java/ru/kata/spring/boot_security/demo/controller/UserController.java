package ru.kata.spring.boot_security.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {
    @GetMapping("/admin")
    public String adminPage() {
        return "users";
    }

    @GetMapping({"/admin/new", "/admin/edit"})
    public String legacyPage() {
        return "redirect:/admin";
    }
}
