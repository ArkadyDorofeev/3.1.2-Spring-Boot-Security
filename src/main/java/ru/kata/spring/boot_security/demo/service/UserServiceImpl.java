package ru.kata.spring.boot_security.demo.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.dao.UserDao;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserServiceImpl(UserDao userDao,
                           PasswordEncoder passwordEncoder,
                           RoleService roleService) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        User user = userDao.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        User user = userDao.findById(id);

        if (user != null) {
            org.hibernate.Hibernate.initialize(user.getRoles());
        }
        return user;
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        userDao.deleteById(id);
    }

    @Transactional
    @Override
    public void createUser(String name, String lastName, Integer age,
                           String email, String password, List<Long> roleIds) {

        if (userDao.findByEmail(email) != null) {
            throw new IllegalArgumentException(
                    "Пользователь с такой почтой уже существует."
            );
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Укажите пароль.");
        }

        if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("Выберите роль.");
        }

        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleService.findById(roleId);

            if (role == null) {
                throw new IllegalArgumentException("Выбрана несуществующая роль.");
            }

            roles.add(role);
        }

        User user = new User(name, email);
        user.setLastName(lastName);
        user.setAge(age);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);

        userDao.save(user);
    }

    @Override
    @Transactional
    public void updateUser(Long id, String name, String lastName, Integer age,
                           String email, String password, List<Long> roleIds) {
        User user = userDao.findById(id);

        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден.");
        }
        User existingUser = userDao.findByEmail(email);

        if (existingUser != null && !existingUser.getId().equals(id)) {
            throw new IllegalArgumentException(
                    "Пользователь с такой почтой уже существует."
            );
        }

        if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("Выберите хотя бы одну роль.");
        }

        Set<Role> roles = new HashSet<>();

        for (Long roleId : roleIds) {
            Role role = roleService.findById(roleId);

            if (role == null) {
                throw new IllegalArgumentException("Выбрана несуществующая роль.");
            }

            roles.add(role);
        }

        user.setName(name);
        user.setLastName(lastName);
        user.setAge(age);
        user.setEmail(email);
        user.setRoles(roles);

        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        userDao.update(user);
    }
}
