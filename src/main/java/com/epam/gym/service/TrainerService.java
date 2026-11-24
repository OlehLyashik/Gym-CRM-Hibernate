package com.epam.gym.service;

import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.dao.UserDao;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.exception.EntityNotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.util.CredentialsGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final UserDao userDao;
    private final AuthService authService;
    private final CredentialsGenerator credentialsGenerator;

    public TrainerService(
            TrainerDao trainerDao,
            TrainingTypeDao trainingTypeDao,
            UserDao userDao,
            AuthService authService,
            CredentialsGenerator credentialsGenerator
    ) {
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
        this.userDao = userDao;
        this.authService = authService;
        this.credentialsGenerator = credentialsGenerator;
    }

    @Transactional
    public Trainer createProfile(String firstName, String lastName, Long specializationId) {
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
        if (specializationId == null) {
            throw new ValidationException("Specialization ID is required");
        }

        TrainingType specialization = trainingTypeDao.findById(specializationId)
                .orElseThrow(() -> new EntityNotFoundException("TrainingType not found with id: " + specializationId));

        String username = credentialsGenerator.generateUsername(firstName, lastName, userDao::existsByUsername);
        String password = credentialsGenerator.generatePassword();

        User user = new User(firstName.trim(), lastName.trim(), username, password, true);
        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);

        Trainer saved = trainerDao.save(trainer);
        log.info("Created Trainer profile with username: {}", username);
        return saved;
    }

    @Transactional(readOnly = true)
    public Trainer getByUsername(String authUsername, String authPassword, String targetUsername) {
        authService.authenticateTrainer(authUsername, authPassword);
        return trainerDao.findByUsername(targetUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + targetUsername));
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        Trainer trainer = authService.authenticateTrainer(username, oldPassword);
        if (newPassword == null || newPassword.isBlank()) {
            throw new ValidationException("New password is required");
        }
        trainer.getUser().setPassword(newPassword);
        trainerDao.update(trainer);
        log.info("Updated password for trainer: {}", username);
    }

    @Transactional
    public Trainer updateProfile(
            String authUsername,
            String authPassword,
            String firstName,
            String lastName,
            Long specializationId,
            Boolean isActive
    ) {
        Trainer trainer = authService.authenticateTrainer(authUsername, authPassword);
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
        if (specializationId == null) {
            throw new ValidationException("Specialization ID is required");
        }
        if (isActive == null) {
            throw new ValidationException("isActive is required");
        }

        TrainingType specialization = trainingTypeDao.findById(specializationId)
                .orElseThrow(() -> new EntityNotFoundException("TrainingType not found with id: " + specializationId));

        User user = trainer.getUser();
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setIsActive(isActive);
        trainer.setSpecialization(specialization);

        Trainer updated = trainerDao.update(trainer);
        log.info("Updated Trainer profile: {}", authUsername);
        return updated;
    }

    @Transactional
    public void activateOrDeactivate(String authUsername, String authPassword, boolean active) {
        Trainer trainer = authService.authenticateTrainer(authUsername, authPassword);
        Boolean current = trainer.getUser().getIsActive();
        if (Objects.equals(current, active)) {
            throw new ValidationException("Trainer is already in state isActive=" + active);
        }
        trainer.getUser().setIsActive(active);
        trainerDao.update(trainer);
        log.info("Changed trainer {} isActive status to {}", authUsername, active);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getNotAssignedToTrainee(String traineeUsername, String traineePassword) {
        authService.authenticateTrainee(traineeUsername, traineePassword);
        return trainerDao.findNotAssignedToTrainee(traineeUsername);
    }

    private void validateName(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }
}
