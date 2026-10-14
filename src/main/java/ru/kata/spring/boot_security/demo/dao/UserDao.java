package ru.kata.spring.boot_security.demo.dao;

import ru.kata.spring.boot_security.demo.model.User;


import java.util.List;

public interface UserDao {

    User findByEmail(String email);

    List<User> findAll();

    User findById(Long id);

    void save(User user);

    public void deleteById(Long id);

    void update(User user);
}

