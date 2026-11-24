package com.epam.gym.service;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.exception.EntityNotFoundException;
import com.epam.gym.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingDao trainingDao;
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final AuthService authService;

    public TrainingService(
            TrainingDao trainingDao,
            TraineeDao traineeDao,
            TrainerDao trainerDao,
            TrainingTypeDao trainingTypeDao,
            AuthService authService
    ) {
        this.trainingDao = trainingDao;
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
        this.authService = authService;
    }

    @Transactional
    public Training addTraining(
            String authUsername,
            String authPassword,
            boolean isTrainerAuth,
            String traineeUsername,
            String trainerUsername,
            String trainingName,
            Long trainingTypeId,
            LocalDate trainingDate,
            Integer trainingDuration
    ) {
        if (isTrainerAuth) {
            authService.authenticateTrainer(authUsername, authPassword);
        } else {
            authService.authenticateTrainee(authUsername, authPassword);
        }

        if (traineeUsername == null || traineeUsername.isBlank()
                || trainerUsername == null || trainerUsername.isBlank()
                || trainingName == null || trainingName.isBlank()
                || trainingTypeId == null
                || trainingDate == null
                || trainingDuration == null || trainingDuration <= 0) {
            throw new ValidationException("All training fields are required and duration must be positive");
        }

        Trainee trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + traineeUsername));
        Trainer trainer = trainerDao.findByUsername(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerUsername));
        TrainingType trainingType = trainingTypeDao.findById(trainingTypeId)
                .orElseThrow(() -> new EntityNotFoundException("TrainingType not found with id: " + trainingTypeId));

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(trainingName.trim());
        training.setTrainingType(trainingType);
        training.setTrainingDate(trainingDate);
        training.setTrainingDuration(trainingDuration);

        trainee.getTrainers().add(trainer);

        Training saved = trainingDao.save(training);
        log.info("Added Training '{}' between trainee {} and trainer {}", trainingName, traineeUsername, trainerUsername);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(
            String traineeUsername,
            String traineePassword,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingTypeName
    ) {
        authService.authenticateTrainee(traineeUsername, traineePassword);
        return trainingDao.findTraineeTrainingsByCriteria(traineeUsername, fromDate, toDate, trainerName, trainingTypeName);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(
            String trainerUsername,
            String trainerPassword,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName
    ) {
        authService.authenticateTrainer(trainerUsername, trainerPassword);
        return trainingDao.findTrainerTrainingsByCriteria(trainerUsername, fromDate, toDate, traineeName);
    }
}
