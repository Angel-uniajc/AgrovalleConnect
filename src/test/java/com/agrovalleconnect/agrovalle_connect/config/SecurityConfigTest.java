package com.agrovalleconnect.agrovalle_connect.config;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class SecurityConfigTest {

    private final PasswordEncoder encoder = new SecurityConfig().passwordEncoder();

    @Test 
    void passwordNoSeGuardaEnTextoPlano(){
        String hash = encoder.encode("Clave123@");
        assertNotEquals("Clave123@", hash);
    }

    @Test 
    void matchesValidaPasswordCorrectaEIncorrecta(){
        String hash = encoder.encode("Clave123@");
        assertTrue(encoder.matches("Clave123@", hash));
        assertFalse(encoder.matches("OtraClave3*", hash));
    }
}
