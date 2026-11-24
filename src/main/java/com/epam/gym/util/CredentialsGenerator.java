package com.epam.gym.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.function.Predicate;

@Component
public class CredentialsGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    private final SecureRandom random = new SecureRandom();

    public String generateUsername(String firstName, String lastName, Predicate<String> existsByUsername) {
        String baseUsername = firstName.trim() + "." + lastName.trim();
        String candidate = baseUsername;
        int serial = 1;
        while (existsByUsername.test(candidate)) {
            candidate = baseUsername + serial;
            serial++;
        }
        return candidate;
    }

    public String generatePassword() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
