package com.epam.gym.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CredentialsGeneratorTest {

    private final CredentialsGenerator generator = new CredentialsGenerator();

    @Test
    void generateUsername_shouldReturnFirstLastWhenUnique() {
        String username = generator.generateUsername("Oleh", "Liashyk", u -> false);
        assertEquals("Oleh.Liashyk", username);
    }

    @Test
    void generateUsername_shouldTrimSpaces() {
        String username = generator.generateUsername("  Oleh ", " Liashyk  ", u -> false);
        assertEquals("Oleh.Liashyk", username);
    }

    @Test
    void generateUsername_shouldAppendSerialWhenExists() {
        Set<String> existing = Set.of("Oleh.Liashyk", "Oleh.Liashyk1");
        String username = generator.generateUsername("Oleh", "Liashyk", existing::contains);
        assertEquals("Oleh.Liashyk2", username);
    }

    @Test
    void generatePassword_shouldGenerateTenAlphanumericCharacters() {
        String password = generator.generatePassword();
        assertNotNull(password);
        assertEquals(10, password.length());
        assertTrue(password.matches("[A-Za-z0-9]{10}"));
    }
}
