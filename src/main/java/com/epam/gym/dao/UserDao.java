package com.epam.gym.dao;

import com.epam.gym.entity.User;
import java.util.Optional;

public interface UserDao {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
