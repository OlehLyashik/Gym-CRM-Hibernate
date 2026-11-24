package com.epam.gym.dao.impl;

import com.epam.gym.dao.TrainingDao;
import com.epam.gym.entity.Training;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TrainingDaoImpl implements TrainingDao {

    private final SessionFactory sessionFactory;

    public TrainingDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Training save(Training training) {
        sessionFactory.getCurrentSession().persist(training);
        return training;
    }

    @Override
    public List<Training> findTraineeTrainingsByCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingTypeName
    ) {
        StringBuilder hql = new StringBuilder(
                "SELECT tr FROM Training tr " +
                "JOIN FETCH tr.trainee tee " +
                "JOIN FETCH tee.user " +
                "JOIN FETCH tr.trainer tnr " +
                "JOIN FETCH tnr.user " +
                "JOIN FETCH tr.trainingType tt " +
                "WHERE tee.user.username = :traineeUsername"
        );

        Map<String, Object> params = new HashMap<>();
        params.put("traineeUsername", traineeUsername);

        if (fromDate != null) {
            hql.append(" AND tr.trainingDate >= :fromDate");
            params.put("fromDate", fromDate);
        }
        if (toDate != null) {
            hql.append(" AND tr.trainingDate <= :toDate");
            params.put("toDate", toDate);
        }
        if (trainerName != null && !trainerName.isBlank()) {
            hql.append(" AND (LOWER(tnr.user.firstName) LIKE LOWER(:trainerName) " +
                    "OR LOWER(tnr.user.lastName) LIKE LOWER(:trainerName) " +
                    "OR LOWER(CONCAT(tnr.user.firstName, ' ', tnr.user.lastName)) LIKE LOWER(:trainerName) " +
                    "OR LOWER(tnr.user.username) LIKE LOWER(:trainerName))");
            params.put("trainerName", "%" + trainerName.trim() + "%");
        }
        if (trainingTypeName != null && !trainingTypeName.isBlank()) {
            hql.append(" AND LOWER(tt.trainingTypeName) = LOWER(:trainingTypeName)");
            params.put("trainingTypeName", trainingTypeName.trim());
        }

        Query<Training> query = sessionFactory.getCurrentSession().createQuery(hql.toString(), Training.class);
        params.forEach(query::setParameter);
        return query.list();
    }

    @Override
    public List<Training> findTrainerTrainingsByCriteria(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName
    ) {
        StringBuilder hql = new StringBuilder(
                "SELECT tr FROM Training tr " +
                "JOIN FETCH tr.trainee tee " +
                "JOIN FETCH tee.user " +
                "JOIN FETCH tr.trainer tnr " +
                "JOIN FETCH tnr.user " +
                "JOIN FETCH tr.trainingType tt " +
                "WHERE tnr.user.username = :trainerUsername"
        );

        Map<String, Object> params = new HashMap<>();
        params.put("trainerUsername", trainerUsername);

        if (fromDate != null) {
            hql.append(" AND tr.trainingDate >= :fromDate");
            params.put("fromDate", fromDate);
        }
        if (toDate != null) {
            hql.append(" AND tr.trainingDate <= :toDate");
            params.put("toDate", toDate);
        }
        if (traineeName != null && !traineeName.isBlank()) {
            hql.append(" AND (LOWER(tee.user.firstName) LIKE LOWER(:traineeName) " +
                    "OR LOWER(tee.user.lastName) LIKE LOWER(:traineeName) " +
                    "OR LOWER(CONCAT(tee.user.firstName, ' ', tee.user.lastName)) LIKE LOWER(:traineeName) " +
                    "OR LOWER(tee.user.username) LIKE LOWER(:traineeName))");
            params.put("traineeName", "%" + traineeName.trim() + "%");
        }

        Query<Training> query = sessionFactory.getCurrentSession().createQuery(hql.toString(), Training.class);
        params.forEach(query::setParameter);
        return query.list();
    }
}
