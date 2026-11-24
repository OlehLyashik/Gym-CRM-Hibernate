package com.epam.gym.entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "trainers")
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "specialization_id", referencedColumnName = "id", nullable = false)
    private TrainingType specialization;

    @OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Training> trainings = new ArrayList<>();

    @ManyToMany(mappedBy = "trainers")
    private Set<Trainee> trainees = new HashSet<>();

    public Trainer() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TrainingType getSpecialization() { return specialization; }
    public void setSpecialization(TrainingType specialization) { this.specialization = specialization; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Training> getTrainings() { return trainings; }
    public void setTrainings(List<Training> trainings) { this.trainings = trainings; }

    public Set<Trainee> getTrainees() { return trainees; }
    public void setTrainees(Set<Trainee> trainees) { this.trainees = trainees; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trainer other)) return false;
        if (user != null && user.getUsername() != null && other.user != null && other.user.getUsername() != null) {
            return Objects.equals(user.getUsername(), other.user.getUsername());
        }
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        if (user != null && user.getUsername() != null) {
            return Objects.hash(user.getUsername());
        }
        return id != null ? Objects.hash(id) : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "Trainer{id=" + id + ", specialization=" + specialization + ", user=" + user + "}";
    }
}
