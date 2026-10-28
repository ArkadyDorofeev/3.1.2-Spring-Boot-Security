package ru.kata.spring.boot_security.demo.dao;

import org.springframework.stereotype.Repository;
import ru.kata.spring.boot_security.demo.model.User;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class UserDaoImpl implements UserDao {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public List<User> findAll() {
        return entityManager.createQuery("SELECT DISTINCT user FROM User user LEFT JOIN FETCH user.roles", User.class). getResultList();
    }

    @Override
    public User findById(Long id) {
        return entityManager.find(User.class, id);
    }

    @Override
    public void save(User user) {
        entityManager.persist(user);
    }

    @Override
    public void deleteById(Long id) {
        User user = findById(id);

        if (user != null) {
            entityManager.remove(user);
        }
    }

    @Override
    public void update(User user) {
        entityManager.merge(user);
    }

    @Override
    public User findByEmail(String email) {
        try {
            return entityManager
                    .createQuery("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}

