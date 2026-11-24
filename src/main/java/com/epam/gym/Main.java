package com.epam.gym;

import com.epam.gym.config.HibernateConfig;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.facade.GymFacade;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(HibernateConfig.class)) {
            GymFacade facade = context.getBean(GymFacade.class);

            Trainer trainer = facade.createTrainer("John", "Doe", 1L);
            System.out.println("Created Trainer -> username: " + trainer.getUser().getUsername()
                    + ", password: " + trainer.getUser().getPassword());

            Trainee trainee = facade.createTrainee("Oleh", "Liashyk", LocalDate.of(1992, 1, 1), "Varash");
            System.out.println("Created Trainee -> username: " + trainee.getUser().getUsername()
                    + ", password: " + trainee.getUser().getPassword());

            Training training = facade.addTraining(
                    trainee.getUser().getUsername(),
                    trainee.getUser().getPassword(),
                    false,
                    trainee.getUser().getUsername(),
                    trainer.getUser().getUsername(),
                    "First Workout",
                    1L,
                    LocalDate.now(),
                    60
            );
            System.out.println("Created Training ID: " + training.getId());
        }
    }
}