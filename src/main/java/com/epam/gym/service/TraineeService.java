package com.epam.gym.service;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.UserDao;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.User;
import com.epam.gym.exception.EntityNotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.util.CredentialsGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final UserDao userDao;
    private final AuthService authService;
    private final CredentialsGenerator credentialsGenerator;

    public TraineeService(
            TraineeDao traineeDao,
            TrainerDao trainerDao,
            UserDao userDao,
            AuthService authService,
            CredentialsGenerator credentialsGenerator
    ) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.userDao = userDao;
        this.authService = authService;
        this.credentialsGenerator = credentialsGenerator;
    }

    @Transactional
    public Trainee createProfile(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        validateName(firstName, "First name");
        validateName(lastName, "Last name");

        String username = credentialsGenerator.generateUsername(firstName, lastName, userDao::existsByUsername);
        String password = credentialsGenerator.generatePassword();

        User user = new User(firstName.trim(), lastName.trim(), username, password, true);
        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        Trainee saved = traineeDao.save(trainee);
        log.info("Created Trainee profile with username: {}", username);
        return saved;
    }

    @Transactional(readOnly = true)
    public Trainee getByUsername(String authUsername, String authPassword, String targetUsername) {
        authService.authenticateTrainee(authUsername, authPassword);
        return traineeDao.findByUsername(targetUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + targetUsername));
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        Trainee trainee = authService.authenticateTrainee(username, oldPassword);
        if (newPassword == null || newPassword.isBlank()) {
            throw new ValidationException("New password is required");
        }
        trainee.getUser().setPassword(newPassword);
        traineeDao.update(trainee);
        log.info("Updated password for trainee: {}", username);
    }

    @Transactional
    public Trainee updateProfile(
            String authUsername,
            String authPassword,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address,
            Boolean isActive
    ) {
        Trainee trainee = authService.authenticateTrainee(authUsername, authPassword);
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
        if (isActive == null) {
            throw new ValidationException("isActive is required");
        }

        User user = trainee.getUser();
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setIsActive(isActive);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        Trainee updated = traineeDao.update(trainee);
        log.info("Updated Trainee profile: {}", authUsername);
        return updated;
    }

    @Transactional
    public void activateOrDeactivate(String authUsername, String authPassword, boolean active) {
        Trainee trainee = authService.authenticateTrainee(authUsername, authPassword);
        Boolean current = trainee.getUser().getIsActive();
        if (Objects.equals(current, active)) {
            throw new ValidationException("Trainee is already in state isActive=" + active);
        }
        trainee.getUser().setIsActive(active);
        traineeDao.update(trainee);
        log.info("Changed trainee {} isActive status to {}", authUsername, active);
    }

    @Transactional
    public void deleteByUsername(String authUsername, String authPassword) {
        Trainee trainee = authService.authenticateTrainee(authUsername, authPassword);
        traineeDao.delete(trainee);
        log.info("Hard-deleted Trainee profile and cascaded trainings for username: {}", authUsername);
    }

    @Transactional
    public Trainee updateTrainersList(String authUsername, String authPassword, Set<String> trainerUsernames) {
        Trainee trainee = authService.authenticateTrainee(authUsername, authPassword);
        if (trainerUsernames == null) {
            throw new ValidationException("Trainer usernames list cannot be null");
        }

        List<Trainer> trainers = trainerDao.findByUsernames(trainerUsernames);
        if (trainers.size() != trainerUsernames.size()) {
            Set<String> foundUsernames = trainers.stream()
                    .map(t -> t.getUser().getUsername())
                    .collect(Collectors.toSet());
            List<String> missing = trainerUsernames.stream()
                    .filter(u -> !foundUsernames.contains(u))
                    .toList();
            throw new EntityNotFoundException("Trainers not found: " + missing);
        }

        trainee.setTrainers(new HashSet<>(trainers));
        Trainee updated = traineeDao.update(trainee);
        log.info("Updated trainers list for trainee: {}", authUsername);
        return updated;
    }

    private void validateName(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }
}
