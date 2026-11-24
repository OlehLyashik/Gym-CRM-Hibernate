package com.epam.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "trainees")
public class Trainee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address")
    private String address;

    @OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "trainee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Training> trainings = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id")
    )
    private Set<Trainer> trainers = new HashSet<>();

    public Trainee() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Training> getTrainings() { return trainings; }
    public void setTrainings(List<Training> trainings) { this.trainings = trainings; }

    public Set<Trainer> getTrainers() { return trainers; }
    public void setTrainers(Set<Trainer> trainers) { this.trainers = trainers; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trainee other)) return false;
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
        return "Trainee{id=" + id + ", dateOfBirth=" + dateOfBirth + ", address='" + address + "', user=" + user + "}";
    }
}
