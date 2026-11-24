# Gym CRM System — Hibernate Persistence Layer

Persistence layer implementation for the **Gym CRM System** task using **Spring Framework**, **Hibernate ORM**, **MySQL**, and **HikariCP**, covered with **JUnit 5 + Mockito** unit tests.

## Domain Model & Architecture

* **Entities (`com.epam.gym.entity`)**:
  * `User` (`users`) — base user credentials (`username`, `password`), name (`firstName`, `lastName`), and status (`isActive`).
  * `Trainee` (`trainees`) — `1..1` relation with `User`, `M..N` relation with `Trainer` (`trainee_trainer`), and `1..N` cascade removal relation with `Training`.
  * `Trainer` (`trainers`) — `1..1` relation with `User`, `M..1` relation with `TrainingType` (`specialization`), and `M..N` relation with `Trainee`.
  * `Training` (`trainings`) — training sessions linked via foreign keys to `Trainee`, `Trainer`, and `TrainingType`, with date (`LocalDate`) and duration (`Integer`).
  * `TrainingType` (`training_types`) — immutable reference table (`@Immutable`) containing predefined training types.
* **Persistence & Business Layer**:
  * **DAOs (`com.epam.gym.dao`)**: `TraineeDao`, `TrainerDao`, `TrainingDao`, `TrainingTypeDao`, `UserDao` and their Hibernate `SessionFactory` implementations.
  * **Services (`com.epam.gym.service`)**: `AuthService`, `TraineeService`, `TrainerService`, `TrainingService` with credential generation, authentication, required-field validation, non-idempotent activation/deactivation, trainee hard deletion with cascading trainings, criteria-based training queries, transaction management (`@Transactional`), and SLF4J/Logback logging.
  * **Facade (`com.epam.gym.facade.GymFacade`)**: unified entry point for all 18 operations.

## Database Setup & Initialization

1. Configure MySQL connection settings in `src/main/resources/application.properties` (`db.url`, `db.username`, `db.password`).
2. Initialize the Spring/Hibernate context (`HibernateConfig`) so Hibernate creates the schema tables (`hibernate.hbm2ddl.auto=update`).
3. Run `src/main/resources/init-training-types.sql` to populate initial `training_types` (`Fitness`, `Yoga`, `Zumba`, `Stretching`, `Resistance`).

## Running Unit Tests

```bash
mvn test
