package com.epam.gym.dao;

import com.epam.gym.entity.Training;
import java.time.LocalDate;
import java.util.List;

public interface TrainingDao {
    Training save(Training training);
    List<Training> findTraineeTrainingsByCriteria(String traineeUsername, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName);
    List<Training> findTrainerTrainingsByCriteria(String trainerUsername, LocalDate fromDate, LocalDate toDate, String traineeName);
}
