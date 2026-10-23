package ru.kata.spring.boot_security.demo.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;

public interface UserService extends UserDetailsService {

    List<User> findAll();

    User findById(Long id);

    public void deleteById(Long id);

    void createUser(String name, String lastName, Integer age,
                    String email, String password, List<Long> roleIds);

    void updateUser(Long id, String name, String lastName, Integer age,
                    String email, String password, List<Long> roleIds);


}
