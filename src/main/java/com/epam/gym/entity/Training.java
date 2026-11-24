package com.epam.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "trainings")
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainee_id", referencedColumnName = "id", nullable = false)
    private Trainee trainee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainer_id", referencedColumnName = "id", nullable = false)
    private Trainer trainer;

    @Column(name = "training_name", nullable = false)
    private String trainingName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "training_type_id", referencedColumnName = "id", nullable = false)
    private TrainingType trainingType;

    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @Column(name = "training_duration", nullable = false)
    private Integer trainingDuration;

    public Training() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Trainee getTrainee() { return trainee; }
    public void setTrainee(Trainee trainee) { this.trainee = trainee; }

    public Trainer getTrainer() { return trainer; }
    public void setTrainer(Trainer trainer) { this.trainer = trainer; }

    public String getTrainingName() { return trainingName; }
    public void setTrainingName(String trainingName) { this.trainingName = trainingName; }

    public TrainingType getTrainingType() { return trainingType; }
    public void setTrainingType(TrainingType trainingType) { this.trainingType = trainingType; }

    public LocalDate getTrainingDate() { return trainingDate; }
    public void setTrainingDate(LocalDate trainingDate) { this.trainingDate = trainingDate; }

    public Integer getTrainingDuration() { return trainingDuration; }
    public void setTrainingDuration(Integer trainingDuration) { this.trainingDuration = trainingDuration; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Training training)) return false;
        return id != null && Objects.equals(id, training.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "Training{id=" + id + ", trainingName='" + trainingName + "', trainingDate=" + trainingDate + ", trainingDuration=" + trainingDuration + "}";
    }
}
