package com.epam.gym.service;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @InjectMocks
    private AuthService authService;

    @Test
    void matchTraineeCredentials_shouldReturnTrueOnlyWhenValid() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User("Oleh", "Liashyk", "Oleh.Liashyk", "secret1234", true));
        when(traineeDao.findByUsername("Oleh.Liashyk")).thenReturn(Optional.of(trainee));

        assertTrue(authService.matchTraineeCredentials("Oleh.Liashyk", "secret1234"));
        assertFalse(authService.matchTraineeCredentials("Oleh.Liashyk", "wrong"));
        assertFalse(authService.matchTraineeCredentials(null, "secret1234"));
        assertFalse(authService.matchTraineeCredentials("Oleh.Liashyk", null));
    }

    @Test
    void matchTrainerCredentials_shouldReturnTrueOnlyWhenValid() {
        Trainer trainer = new Trainer();
        trainer.setUser(new User("John", "Doe", "John.Doe", "pass123456", true));
        when(trainerDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainer));

        assertTrue(authService.matchTrainerCredentials("John.Doe", "pass123456"));
        assertFalse(authService.matchTrainerCredentials("John.Doe", "wrong"));
        assertFalse(authService.matchTrainerCredentials(null, "pass123456"));
    }

    @Test
    void authenticateTrainee_shouldReturnTraineeWhenCredentialsMatch() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User("Oleh", "Liashyk", "Oleh.Liashyk", "secret1234", true));
        when(traineeDao.findByUsername("Oleh.Liashyk")).thenReturn(Optional.of(trainee));

        Trainee result = authService.authenticateTrainee("Oleh.Liashyk", "secret1234");
        assertSame(trainee, result);
    }

    @Test
    void authenticateTrainee_shouldThrowWhenNotFoundOrWrongPassword() {
        when(traineeDao.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(AuthenticationException.class, () -> authService.authenticateTrainee("unknown", "123"));

        Trainee trainee = new Trainee();
        trainee.setUser(new User("Oleh", "Liashyk", "Oleh.Liashyk", "secret1234", true));
        when(traineeDao.findByUsername("Oleh.Liashyk")).thenReturn(Optional.of(trainee));
        assertThrows(AuthenticationException.class, () -> authService.authenticateTrainee("Oleh.Liashyk", "bad"));
    }

    @Test
    void authenticateTrainer_shouldThrowWhenInvalidCredentials() {
        Trainer trainer = new Trainer();
        trainer.setUser(new User("John", "Doe", "John.Doe", "pass123456", true));
        when(trainerDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainer));

        assertSame(trainer, authService.authenticateTrainer("John.Doe", "pass123456"));
        assertThrows(AuthenticationException.class, () -> authService.authenticateTrainer("John.Doe", "wrong"));
    }
}
