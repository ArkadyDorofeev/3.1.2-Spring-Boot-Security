package ru.kata.spring.boot_security.demo.dao;
import org.springframework.stereotype.Repository;
import ru.kata.spring.boot_security.demo.model.Role;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
@Repository
public class RoleDaoImpl implements RoleDao {
    @PersistenceContext
    private EntityManager entityManager;
    @Override
    public List<Role> findAll() {
        return entityManager.createQuery("SELECT r FROM Role r ORDER BY r.id", Role.class).getResultList();
    }
    @Override
    public Role findById(Long id) {
        return entityManager.find(Role.class, id);
    }
}
