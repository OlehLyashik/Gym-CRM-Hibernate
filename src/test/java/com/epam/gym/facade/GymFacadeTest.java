package com.epam.gym.facade;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.service.AuthService;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private GymFacade gymFacade;

    @Test
    void shouldDelegateAllOperationsToServices() {
        Trainer trainer = new Trainer();
        Trainee trainee = new Trainee();
        Training training = new Training();

        when(trainerService.createProfile("John", "Doe", 1L)).thenReturn(trainer);
        when(traineeService.createProfile("Oleh", "Liashyk", null, "Varash")).thenReturn(trainee);
        when(authService.matchTraineeCredentials("Oleh.Liashyk", "p1")).thenReturn(true);
        when(authService.matchTrainerCredentials("John.Doe", "p2")).thenReturn(true);
        when(trainerService.getByUsername("John.Doe", "p2", "John.Doe")).thenReturn(trainer);
        when(traineeService.getByUsername("Oleh.Liashyk", "p1", "Oleh.Liashyk")).thenReturn(trainee);
        when(trainerService.updateProfile("John.Doe", "p2", "J", "D", 1L, true)).thenReturn(trainer);
        when(traineeService.updateProfile("Oleh.Liashyk", "p1", "O", "L", null, "Addr", true)).thenReturn(trainee);
        when(trainingService.getTraineeTrainings("Oleh.Liashyk", "p1", null, null, null, null)).thenReturn(List.of(training));
        when(trainingService.getTrainerTrainings("John.Doe", "p2", null, null, null)).thenReturn(List.of(training));
        when(trainingService.addTraining("John.Doe", "p2", true, "Oleh.Liashyk", "John.Doe", "Run", 1L, LocalDate.MIN, 45)).thenReturn(training);
        when(trainerService.getNotAssignedToTrainee("Oleh.Liashyk", "p1")).thenReturn(List.of(trainer));
        when(traineeService.updateTrainersList("Oleh.Liashyk", "p1", Set.of("John.Doe"))).thenReturn(trainee);

        assertSame(trainer, gymFacade.createTrainer("John", "Doe", 1L));
        assertSame(trainee, gymFacade.createTrainee("Oleh", "Liashyk", null, "Varash"));
        assertTrue(gymFacade.matchTraineeCredentials("Oleh.Liashyk", "p1"));
        assertTrue(gymFacade.matchTrainerCredentials("John.Doe", "p2"));
        assertSame(trainer, gymFacade.getTrainerByUsername("John.Doe", "p2", "John.Doe"));
        assertSame(trainee, gymFacade.getTraineeByUsername("Oleh.Liashyk", "p1", "Oleh.Liashyk"));

        gymFacade.changeTraineePassword("Oleh.Liashyk", "p1", "new1");
        verify(traineeService).changePassword("Oleh.Liashyk", "p1", "new1");

        gymFacade.changeTrainerPassword("John.Doe", "p2", "new2");
        verify(trainerService).changePassword("John.Doe", "p2", "new2");

        assertSame(trainer, gymFacade.updateTrainer("John.Doe", "p2", "J", "D", 1L, true));
        assertSame(trainee, gymFacade.updateTrainee("Oleh.Liashyk", "p1", "O", "L", null, "Addr", true));

        gymFacade.setTraineeActiveStatus("Oleh.Liashyk", "p1", false);
        verify(traineeService).activateOrDeactivate("Oleh.Liashyk", "p1", false);

        gymFacade.setTrainerActiveStatus("John.Doe", "p2", false);
        verify(trainerService).activateOrDeactivate("John.Doe", "p2", false);

        gymFacade.deleteTrainee("Oleh.Liashyk", "p1");
        verify(traineeService).deleteByUsername("Oleh.Liashyk", "p1");

        assertEquals(1, gymFacade.getTraineeTrainings("Oleh.Liashyk", "p1", null, null, null, null).size());
        assertEquals(1, gymFacade.getTrainerTrainings("John.Doe", "p2", null, null, null).size());
        assertSame(training, gymFacade.addTraining("John.Doe", "p2", true, "Oleh.Liashyk", "John.Doe", "Run", 1L, LocalDate.MIN, 45));
        assertEquals(1, gymFacade.getUnassignedTrainersForTrainee("Oleh.Liashyk", "p1").size());
        assertSame(trainee, gymFacade.updateTraineeTrainers("Oleh.Liashyk", "p1", Set.of("John.Doe")));
    }
}
