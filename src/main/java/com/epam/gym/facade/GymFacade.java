package com.epam.gym.facade;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.service.AuthService;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final AuthService authService;

    public GymFacade(
            TraineeService traineeService,
            TrainerService trainerService,
            TrainingService trainingService,
            AuthService authService
    ) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.authService = authService;
    }

    public Trainer createTrainer(String firstName, String lastName, Long specializationId) {
        return trainerService.createProfile(firstName, lastName, specializationId);
    }

    public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        return traineeService.createProfile(firstName, lastName, dateOfBirth, address);
    }

    public boolean matchTraineeCredentials(String username, String password) {
        return authService.matchTraineeCredentials(username, password);
    }

    public boolean matchTrainerCredentials(String username, String password) {
        return authService.matchTrainerCredentials(username, password);
    }

    public Trainer getTrainerByUsername(String authUsername, String authPassword, String targetUsername) {
        return trainerService.getByUsername(authUsername, authPassword, targetUsername);
    }

    public Trainee getTraineeByUsername(String authUsername, String authPassword, String targetUsername) {
        return traineeService.getByUsername(authUsername, authPassword, targetUsername);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        traineeService.changePassword(username, oldPassword, newPassword);
    }

    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        trainerService.changePassword(username, oldPassword, newPassword);
    }

    public Trainer updateTrainer(String authUsername, String authPassword, String firstName, String lastName, Long specializationId, Boolean isActive) {
        return trainerService.updateProfile(authUsername, authPassword, firstName, lastName, specializationId, isActive);
    }

    public Trainee updateTrainee(String authUsername, String authPassword, String firstName, String lastName, LocalDate dateOfBirth, String address, Boolean isActive) {
        return traineeService.updateProfile(authUsername, authPassword, firstName, lastName, dateOfBirth, address, isActive);
    }

    public void setTraineeActiveStatus(String authUsername, String authPassword, boolean active) {
        traineeService.activateOrDeactivate(authUsername, authPassword, active);
    }

    public void setTrainerActiveStatus(String authUsername, String authPassword, boolean active) {
        trainerService.activateOrDeactivate(authUsername, authPassword, active);
    }

    public void deleteTrainee(String authUsername, String authPassword) {
        traineeService.deleteByUsername(authUsername, authPassword);
    }

    public List<Training> getTraineeTrainings(String traineeUsername, String traineePassword, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {
        return trainingService.getTraineeTrainings(traineeUsername, traineePassword, fromDate, toDate, trainerName, trainingTypeName);
    }

    public List<Training> getTrainerTrainings(String trainerUsername, String trainerPassword, LocalDate fromDate, LocalDate toDate, String traineeName) {
        return trainingService.getTrainerTrainings(trainerUsername, trainerPassword, fromDate, toDate, traineeName);
    }

    public Training addTraining(String authUsername, String authPassword, boolean isTrainerAuth, String traineeUsername, String trainerUsername, String trainingName, Long trainingTypeId, LocalDate trainingDate, Integer trainingDuration) {
        return trainingService.addTraining(authUsername, authPassword, isTrainerAuth, traineeUsername, trainerUsername, trainingName, trainingTypeId, trainingDate, trainingDuration);
    }

    public List<Trainer> getUnassignedTrainersForTrainee(String traineeUsername, String traineePassword) {
        return trainerService.getNotAssignedToTrainee(traineeUsername, traineePassword);
    }

    public Trainee updateTraineeTrainers(String authUsername, String authPassword, Set<String> trainerUsernames) {
        return traineeService.updateTrainersList(authUsername, authPassword, trainerUsernames);
    }
}
