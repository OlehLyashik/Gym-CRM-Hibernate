package com.epam.gym.service;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.exception.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;

    public AuthService(TraineeDao traineeDao, TrainerDao trainerDao) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
    }

    @Transactional(readOnly = true)
    public boolean matchTraineeCredentials(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        return traineeDao.findByUsername(username)
                .map(t -> password.equals(t.getUser().getPassword()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean matchTrainerCredentials(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        return trainerDao.findByUsername(username)
                .map(t -> password.equals(t.getUser().getPassword()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Trainee authenticateTrainee(String username, String password) {
        log.debug("Authenticating trainee: {}", username);
        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Invalid trainee username or password"));
        if (password == null || !password.equals(trainee.getUser().getPassword())) {
            log.warn("Authentication failed for trainee: {}", username);
            throw new AuthenticationException("Invalid trainee username or password");
        }
        return trainee;
    }

    @Transactional(readOnly = true)
    public Trainer authenticateTrainer(String username, String password) {
        log.debug("Authenticating trainer: {}", username);
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Invalid trainer username or password"));
        if (password == null || !password.equals(trainer.getUser().getPassword())) {
            log.warn("Authentication failed for trainer: {}", username);
            throw new AuthenticationException("Invalid trainer username or password");
        }
        return trainer;
    }
}
