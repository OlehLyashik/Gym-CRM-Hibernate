package com.epam.gym.dao.impl;

import com.epam.gym.dao.TrainerDao;
import com.epam.gym.entity.Trainer;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class TrainerDaoImpl implements TrainerDao {

    private final SessionFactory sessionFactory;

    public TrainerDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Trainer save(Trainer trainer) {
        sessionFactory.getCurrentSession().persist(trainer);
        return trainer;
    }

    @Override
    public Trainer update(Trainer trainer) {
        return sessionFactory.getCurrentSession().merge(trainer);
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createQuery("SELECT tr FROM Trainer tr JOIN FETCH tr.user JOIN FETCH tr.specialization WHERE tr.user.username = :username", Trainer.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    @Override
    public List<Trainer> findNotAssignedToTrainee(String traineeUsername) {
        String hql = "SELECT tr FROM Trainer tr " +
                "JOIN FETCH tr.user " +
                "JOIN FETCH tr.specialization " +
                "WHERE tr.user.isActive = true " +
                "AND tr.id NOT IN (" +
                "    SELECT assignedTr.id FROM Trainee t " +
                "    JOIN t.trainers assignedTr " +
                "    WHERE t.user.username = :traineeUsername" +
                ")";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, Trainer.class)
                .setParameter("traineeUsername", traineeUsername)
                .list();
    }

    @Override
    public List<Trainer> findByUsernames(Set<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return Collections.emptyList();
        }
        return sessionFactory.getCurrentSession()
                .createQuery("SELECT tr FROM Trainer tr JOIN FETCH tr.user WHERE tr.user.username IN :usernames", Trainer.class)
                .setParameter("usernames", usernames)
                .list();
    }
}
