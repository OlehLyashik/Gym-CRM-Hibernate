package com.epam.gym.dao.impl;

import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.entity.TrainingType;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDaoImpl implements TrainingTypeDao {

    private final SessionFactory sessionFactory;

    public TrainingTypeDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<TrainingType> findById(Long id) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(TrainingType.class, id));
    }

    @Override
    public Optional<TrainingType> findByName(String name) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM TrainingType tt WHERE tt.trainingTypeName = :name", TrainingType.class)
                .setParameter("name", name)
                .uniqueResultOptional();
    }

    @Override
    public List<TrainingType> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM TrainingType", TrainingType.class)
                .list();
    }
}
