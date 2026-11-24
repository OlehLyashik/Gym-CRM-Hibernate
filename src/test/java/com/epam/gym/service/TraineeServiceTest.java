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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private UserDao userDao;

    @Mock
    private AuthService authService;

    @Mock
    private CredentialsGenerator credentialsGenerator;

    @InjectMocks
    private TraineeService traineeService;

    private Trainee buildTrainee(String username, String password, boolean active) {
        Trainee t = new Trainee();
        t.setUser(new User("Oleh", "Liashyk", username, password, active));
        return t;
    }

    @Test
    void createProfile_shouldSaveTraineeWithGeneratedCredentials() {
        when(credentialsGenerator.generateUsername(eq("Oleh"), eq("Liashyk"), any())).thenReturn("Oleh.Liashyk");
        when(credentialsGenerator.generatePassword()).thenReturn("Pass123456");
        when(traineeDao.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee created = traineeService.createProfile("Oleh", "Liashyk", LocalDate.of(1992, 1, 1), "Varash");

        assertEquals("Oleh.Liashyk", created.getUser().getUsername());
        assertEquals("Pass123456", created.getUser().getPassword());
        assertTrue(created.getUser().getIsActive());
        assertEquals("Varash", created.getAddress());
        verify(traineeDao).save(any(Trainee.class));
    }

    @Test
    void createProfile_shouldThrowWhenRequiredNameMissing() {
        assertThrows(ValidationException.class, () -> traineeService.createProfile(" ", "Liashyk", null, null));
        assertThrows(ValidationException.class, () -> traineeService.createProfile("Oleh", null, null, null));
        verifyNoInteractions(traineeDao);
    }

    @Test
    void getByUsername_shouldReturnTraineeWhenAuthenticated() {
        Trainee target = buildTrainee("Target.User", "123", true);
        when(traineeDao.findByUsername("Target.User")).thenReturn(Optional.of(target));

        Trainee result = traineeService.getByUsername("Oleh.Liashyk", "pass", "Target.User");

        verify(authService).authenticateTrainee("Oleh.Liashyk", "pass");
        assertSame(target, result);
    }

    @Test
    void getByUsername_shouldThrowWhenTargetNotFound() {
        when(traineeDao.findByUsername("Missing.User")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> traineeService.getByUsername("Oleh.Liashyk", "pass", "Missing.User"));
    }

    @Test
    void changePassword_shouldUpdatePassword() {
        Trainee trainee = buildTrainee("Oleh.Liashyk", "oldPass", true);
        when(authService.authenticateTrainee("Oleh.Liashyk", "oldPass")).thenReturn(trainee);

        traineeService.changePassword("Oleh.Liashyk", "oldPass", "newPass123");

        assertEquals("newPass123", trainee.getUser().getPassword());
        verify(traineeDao).update(trainee);
    }

    @Test
    void changePassword_shouldThrowWhenNewPasswordBlank() {
        Trainee trainee = buildTrainee("Oleh.Liashyk", "oldPass", true);
        when(authService.authenticateTrainee("Oleh.Liashyk", "oldPass")).thenReturn(trainee);

        assertThrows(ValidationException.class,
                () -> traineeService.changePassword("Oleh.Liashyk", "oldPass", "  "));
    }

    @Test
    void updateProfile_shouldUpdateFields() {
        Trainee trainee = buildTrainee("Oleh.Liashyk", "pass", true);
        when(authService.authenticateTrainee("Oleh.Liashyk", "pass")).thenReturn(trainee);
        when(traineeDao.update(trainee)).thenReturn(trainee);

        Trainee updated = traineeService.updateProfile(
                "Oleh.Liashyk", "pass", "NewFirst", "NewLast",
                LocalDate.of(1995, 5, 5), "Kyiv", false
        );

        assertEquals("NewFirst", updated.getUser().getFirstName());
        assertEquals("NewLast", updated.getUser().getLastName());
        assertEquals("Kyiv", updated.getAddress());
        assertFalse(updated.getUser().getIsActive());
    }

    @Test
    void activateOrDeactivate_shouldToggleStatusAndRejectSameStatus() {
        Trainee trainee = buildTrainee("Oleh.Liashyk", "pass", true);
        when(authService.authenticateTrainee("Oleh.Liashyk", "pass")).thenReturn(trainee);

        assertThrows(ValidationException.class,
                () -> traineeService.activateOrDeactivate("Oleh.Liashyk", "pass", true));

        traineeService.activateOrDeactivate("Oleh.Liashyk", "pass", false);
        assertFalse(trainee.getUser().getIsActive());
        verify(traineeDao).update(trainee);
    }

    @Test
    void deleteByUsername_shouldCallDaoDelete() {
        Trainee trainee = buildTrainee("Oleh.Liashyk", "pass", true);
        when(authService.authenticateTrainee("Oleh.Liashyk", "pass")).thenReturn(trainee);

        traineeService.deleteByUsername("Oleh.Liashyk", "pass");

        verify(traineeDao).delete(trainee);
    }

    @Test
    void updateTrainersList_shouldUpdateWhenAllTrainersExist() {
        Trainee trainee = buildTrainee("Oleh.Liashyk", "pass", true);
        when(authService.authenticateTrainee("Oleh.Liashyk", "pass")).thenReturn(trainee);

        Trainer tr1 = new Trainer();
        tr1.setUser(new User("A", "B", "A.B", "1", true));
        when(trainerDao.findByUsernames(Set.of("A.B"))).thenReturn(List.of(tr1));
        when(traineeDao.update(trainee)).thenReturn(trainee);

        Trainee result = traineeService.updateTrainersList("Oleh.Liashyk", "pass", Set.of("A.B"));
        assertEquals(1, result.getTrainers().size());
    }

    @Test
    void updateTrainersList_shouldThrowWhenAnyTrainerMissing() {
        Trainee trainee = buildTrainee("Oleh.Liashyk", "pass", true);
        when(authService.authenticateTrainee("Oleh.Liashyk", "pass")).thenReturn(trainee);

        Trainer tr1 = new Trainer();
        tr1.setUser(new User("A", "B", "A.B", "1", true));
        when(trainerDao.findByUsernames(Set.of("A.B", "Missing.Trainer"))).thenReturn(List.of(tr1));

        assertThrows(EntityNotFoundException.class,
                () -> traineeService.updateTrainersList("Oleh.Liashyk", "pass", Set.of("A.B", "Missing.Trainer")));
    }
}
