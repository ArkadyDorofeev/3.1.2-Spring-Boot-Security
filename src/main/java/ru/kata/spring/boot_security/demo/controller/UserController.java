package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import java.util.List;
import ru.kata.spring.boot_security.demo.service.UserService;


@Controller
@RequestMapping("/admin")
public class UserController {
    private final UserService userService;

    private final RoleService roleService;

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @ModelAttribute("currentUser")
    public User currentUser(@AuthenticationPrincipal User user) {
        return user;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("roles", roleService.findAll());
        return "users";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("roles", roleService.findAll());
        return "user-create";
    }

    @PostMapping("/add")
    public String addUser(
            @RequestParam("name") String name,
            @RequestParam("lastName") String lastName,
            @RequestParam("age") Integer age,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
            Model model
    ) {
        try {
            userService.createUser(name, lastName, age, email, password, roleIds);
            return "redirect:/admin";
        } catch (IllegalArgumentException e) {
            model.addAttribute("roles", roleService.findAll());
            model.addAttribute("error", e.getMessage());
            return "user-create";
        }
    }

    @GetMapping("/edit")
    public String showEditForm(
            @RequestParam("id") Long id,
            Model model
    ) {

        User user = userService.findById(id);
        if (user == null) {
            return "redirect:/admin";
        }
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.findAll());
        return "user-edit";
    }

    @PostMapping("/update")
    public String updateUser(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("lastName") String lastName,
            @RequestParam("age") Integer age,
            @RequestParam("email") String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
            Model model
    ) {
        try {
            userService.updateUser(
                    id, name, lastName, age, email, password, roleIds
            );
            return "redirect:/admin";
        } catch (IllegalArgumentException e) {
            User user = userService.findById(id);

            if (user == null) {
                return "redirect:/admin";
            }

            model.addAttribute("user", user);
            model.addAttribute("roles", roleService.findAll());
            model.addAttribute("error", e.getMessage());
            return "user-edit";
        }
    }

    @PostMapping("/delete")
    public String deleteUser(
            @RequestParam("id") Long id
    ) {
        userService.deleteById(id);
        return "redirect:/admin";
    }
}
