package engine.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    @Test
    void testBCryptPasswordEncoder() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        assertNotNull(encoder);

        String password = "password123";
        String encoded = encoder.encode(password);

        assertNotNull(encoded);
        assertNotEquals(password, encoded);
        assertTrue(encoder.matches(password, encoded));
        assertFalse(encoder.matches("wrong", encoded));
    }

    @Test
    void testSecurityConfigCreation() {
        SecurityConfig config = new SecurityConfig();

        assertNotNull(config);

        BCryptPasswordEncoder encoder = config.passwordEncoder();

        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);

        String test = "test";
        String encoded = encoder.encode(test);
        assertTrue(encoder.matches(test, encoded));
    }
}