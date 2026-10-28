package ru.kata.spring.boot_security.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserRestController {
    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }
        return user;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody UserRequest request) {
        request.validate();
        userService.createUser(request.name, request.lastName, request.age,
                request.email, request.password, request.roleIds);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id, @RequestBody UserRequest request) {
        getUserById(id);
        request.validate();
        userService.updateUser(id, request.name, request.lastName, request.age,
                request.email, request.password, request.roleIds);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        getUserById(id);
        userService.deleteById(id);
    }

    // Separate request object: the entity's password must never appear in JSON responses.
    public static class UserRequest {
        public String name;
        public String lastName;
        public Integer age;
        public String email;
        public String password;
        public List<Long> roleIds;

        public void validate() {
            if (name == null || name.isBlank() || lastName == null || lastName.isBlank()) {
                throw new IllegalArgumentException("Укажите имя и фамилию.");
            }
            if (age == null || age < 0 || age > 150) {
                throw new IllegalArgumentException("Возраст должен быть от 0 до 150.");
            }
            if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new IllegalArgumentException("Укажите корректный email.");
            }
            if (roleIds == null || roleIds.isEmpty() || roleIds.contains(null)) {
                throw new IllegalArgumentException("Выберите хотя бы одну роль.");
            }
            name = name.trim();
            lastName = lastName.trim();
        }
    }
}
