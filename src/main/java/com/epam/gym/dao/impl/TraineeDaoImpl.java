package com.epam.gym.dao.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.entity.Trainee;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TraineeDaoImpl implements TraineeDao {

    private final SessionFactory sessionFactory;

    public TraineeDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Trainee save(Trainee trainee) {
        sessionFactory.getCurrentSession().persist(trainee);
        return trainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        return sessionFactory.getCurrentSession().merge(trainee);
    }

    @Override
    public void delete(Trainee trainee) {
        Session session = sessionFactory.getCurrentSession();
        Trainee managed = session.contains(trainee) ? trainee : session.merge(trainee);
        session.remove(managed);
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createQuery("SELECT DISTINCT t FROM Trainee t JOIN FETCH t.user LEFT JOIN FETCH t.trainers WHERE t.user.username = :username", Trainee.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }
}
