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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private UserDao userDao;

    @Mock
    private AuthService authService;

    @Mock
    private CredentialsGenerator credentialsGenerator;

    @InjectMocks
    private TrainerService trainerService;

    private Trainer buildTrainer(String username, String password, boolean active) {
        Trainer t = new Trainer();
        t.setUser(new User("John", "Doe", username, password, active));
        t.setSpecialization(new TrainingType(1L, "Fitness"));
        return t;
    }

    @Test
    void createProfile_shouldSaveTrainerWithSpecialization() {
        TrainingType fitness = new TrainingType(1L, "Fitness");
        when(trainingTypeDao.findById(1L)).thenReturn(Optional.of(fitness));
        when(credentialsGenerator.generateUsername(eq("John"), eq("Doe"), any())).thenReturn("John.Doe");
        when(credentialsGenerator.generatePassword()).thenReturn("Secret1234");
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer created = trainerService.createProfile("John", "Doe", 1L);

        assertEquals("John.Doe", created.getUser().getUsername());
        assertEquals("Secret1234", created.getUser().getPassword());
        assertEquals(fitness, created.getSpecialization());
        verify(trainerDao).save(any(Trainer.class));
    }

    @Test
    void createProfile_shouldThrowWhenSpecializationNotFound() {
        when(trainingTypeDao.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> trainerService.createProfile("John", "Doe", 99L));
    }

    @Test
    void getByUsername_shouldReturnTrainerWhenAuthenticated() {
        Trainer trainer = buildTrainer("John.Doe", "pass", true);
        when(trainerDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainer));

        Trainer result = trainerService.getByUsername("John.Doe", "pass", "John.Doe");
        verify(authService).authenticateTrainer("John.Doe", "pass");
        assertSame(trainer, result);
    }

    @Test
    void changePassword_shouldUpdateTrainerPassword() {
        Trainer trainer = buildTrainer("John.Doe", "old", true);
        when(authService.authenticateTrainer("John.Doe", "old")).thenReturn(trainer);

        trainerService.changePassword("John.Doe", "old", "newSecret");
        assertEquals("newSecret", trainer.getUser().getPassword());
        verify(trainerDao).update(trainer);
    }

    @Test
    void updateProfile_shouldUpdateTrainerDetails() {
        Trainer trainer = buildTrainer("John.Doe", "pass", true);
        TrainingType yoga = new TrainingType(2L, "Yoga");
        when(authService.authenticateTrainer("John.Doe", "pass")).thenReturn(trainer);
        when(trainingTypeDao.findById(2L)).thenReturn(Optional.of(yoga));
        when(trainerDao.update(trainer)).thenReturn(trainer);

        Trainer updated = trainerService.updateProfile("John.Doe", "pass", "Jane", "Smith", 2L, false);

        assertEquals("Jane", updated.getUser().getFirstName());
        assertEquals("Smith", updated.getUser().getLastName());
        assertEquals(yoga, updated.getSpecialization());
        assertFalse(updated.getUser().getIsActive());
    }

    @Test
    void activateOrDeactivate_shouldRejectSameStateAndToggleDifferentState() {
        Trainer trainer = buildTrainer("John.Doe", "pass", false);
        when(authService.authenticateTrainer("John.Doe", "pass")).thenReturn(trainer);

        assertThrows(ValidationException.class,
                () -> trainerService.activateOrDeactivate("John.Doe", "pass", false));

        trainerService.activateOrDeactivate("John.Doe", "pass", true);
        assertTrue(trainer.getUser().getIsActive());
        verify(trainerDao).update(trainer);
    }

    @Test
    void getNotAssignedToTrainee_shouldAuthenticateTraineeAndReturnTrainers() {
        List<Trainer> unassigned = List.of(buildTrainer("John.Doe", "pass", true));
        when(trainerDao.findNotAssignedToTrainee("Oleh.Liashyk")).thenReturn(unassigned);

        List<Trainer> result = trainerService.getNotAssignedToTrainee("Oleh.Liashyk", "pass");
        verify(authService).authenticateTrainee("Oleh.Liashyk", "pass");
        assertEquals(1, result.size());
    }
}
