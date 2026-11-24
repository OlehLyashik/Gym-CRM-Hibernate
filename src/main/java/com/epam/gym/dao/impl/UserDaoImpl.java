package com.epam.gym.dao.impl;

import com.epam.gym.dao.UserDao;
import com.epam.gym.entity.User;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {

    private final SessionFactory sessionFactory;

    public UserDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    @Override
    public boolean existsByUsername(String username) {
        Long count = sessionFactory.getCurrentSession()
                .createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                .setParameter("username", username)
                .uniqueResult();
        return count != null && count > 0;
    }
}
