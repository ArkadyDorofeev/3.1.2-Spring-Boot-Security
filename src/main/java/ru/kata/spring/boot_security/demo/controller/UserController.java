package ru.kata.spring.boot_security.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.service.RoleService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
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
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
            Model model
    ) {
        if (password.isBlank() || roleIds == null || roleIds.isEmpty()) {
            model.addAttribute("roles", roleService.findAll());
            model.addAttribute("error", "Укажите пароль и выберите хотя бы одну роль.");
            return "user-create";
        }
        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleService.findById(roleId);
            if (role == null) {
                model.addAttribute("roles", roleService.findAll());
                model.addAttribute("error", "Выбрана несуществующая роль.");
                return "user-create";
            }
            roles.add(role);
        }
        User user = new User(name, email);
        user.setPassword(password);
        user.setRoles(roles);
        userService.save(user);
        return "redirect:/admin";
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
        return "user-edit";
    }

    @PostMapping("/update")
    public String updateUser(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("email") String email
    ) {
        User user = userService.findById(id);
        if (user == null) {
            return "redirect:/admin";
        }
        user.setName(name);
        user.setEmail(email);
        userService.update(user);
        return "redirect:/admin";
    }

    @PostMapping("/delete")
    public String deleteUser(
            @RequestParam("id") Long id
    ) {
        userService.deleteById(id);
        return "redirect:/admin";
    }
}
