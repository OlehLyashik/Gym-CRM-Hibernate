package com.epam.gym.service;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void addTraining_shouldSaveTrainingAndLinkTrainerToTrainee() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User("Oleh", "Liashyk", "Oleh.Liashyk", "pass1", true));

        Trainer trainer = new Trainer();
        trainer.setUser(new User("John", "Doe", "John.Doe", "pass2", true));

        TrainingType type = new TrainingType(1L, "Fitness");

        when(traineeDao.findByUsername("Oleh.Liashyk")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findById(1L)).thenReturn(Optional.of(type));
        when(trainingDao.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

        Training saved = trainingService.addTraining(
                "John.Doe", "pass2", true,
                "Oleh.Liashyk", "John.Doe",
                "Morning Cardio", 1L, LocalDate.of(2026, 11, 24), 60
        );

        verify(authService).authenticateTrainer("John.Doe", "pass2");
        assertEquals("Morning Cardio", saved.getTrainingName());
        assertEquals(60, saved.getTrainingDuration());
        assertTrue(trainee.getTrainers().contains(trainer));
        verify(trainingDao).save(any(Training.class));
    }

    @Test
    void addTraining_shouldThrowWhenDurationNotPositive() {
        assertThrows(ValidationException.class, () -> trainingService.addTraining(
                "Oleh.Liashyk", "pass1", false,
                "Oleh.Liashyk", "John.Doe",
                "Workout", 1L, LocalDate.now(), 0
        ));
        verify(authService).authenticateTrainee("Oleh.Liashyk", "pass1");
        verifyNoInteractions(trainingDao);
    }

    @Test
    void getTraineeTrainings_shouldAuthenticateAndDelegateToDao() {
        LocalDate from = LocalDate.of(2026, 11, 1);
        LocalDate to = LocalDate.of(2026, 11, 30);
        when(trainingDao.findTraineeTrainingsByCriteria("Oleh.Liashyk", from, to, "John", "Fitness"))
                .thenReturn(List.of(new Training()));

        List<Training> result = trainingService.getTraineeTrainings(
                "Oleh.Liashyk", "pass1", from, to, "John", "Fitness"
        );

        verify(authService).authenticateTrainee("Oleh.Liashyk", "pass1");
        assertEquals(1, result.size());
    }

    @Test
    void getTrainerTrainings_shouldAuthenticateAndDelegateToDao() {
        LocalDate from = LocalDate.of(2026, 11, 1);
        LocalDate to = LocalDate.of(2026, 11, 30);
        when(trainingDao.findTrainerTrainingsByCriteria("John.Doe", from, to, "Oleh"))
                .thenReturn(List.of(new Training()));

        List<Training> result = trainingService.getTrainerTrainings(
                "John.Doe", "pass2", from, to, "Oleh"
        );

        verify(authService).authenticateTrainer("John.Doe", "pass2");
        assertEquals(1, result.size());
    }
}
