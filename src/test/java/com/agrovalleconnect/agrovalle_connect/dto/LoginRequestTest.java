package com.agrovalleconnect.agrovalle_connect.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LoginRequestTest {

    private static Validator validator;

    @BeforeAll
    static void crearValidador() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void requestValidoNoTieneViolaciones() {
        Set<ConstraintViolation<LoginRequest>> errores =
                validator.validate(new LoginRequest("ana@correo.com", "Clave123"));

        assertTrue(errores.isEmpty());
    }

    @Test
    void emailVacioEsInvalido() {
        Set<ConstraintViolation<LoginRequest>> errores =
                validator.validate(new LoginRequest("  ", "Clave123"));

        assertTrue(hayErrorEn(errores, "email"));
    }

    @Test
    void emailConFormatoIncorrectoEsInvalido() {
        Set<ConstraintViolation<LoginRequest>> errores =
                validator.validate(new LoginRequest("esto-no-es-un-correo", "Clave123"));

        assertTrue(hayErrorEn(errores, "email"));
    }

    @Test
    void passwordVaciaEsInvalida() {
        Set<ConstraintViolation<LoginRequest>> errores =
                validator.validate(new LoginRequest("ana@correo.com", ""));

        assertTrue(hayErrorEn(errores, "password"));
        assertFalse(hayErrorEn(errores, "email"));
    }

    private boolean hayErrorEn(Set<ConstraintViolation<LoginRequest>> errores, String campo) {
        return errores.stream()
                .anyMatch(error -> error.getPropertyPath().toString().equals(campo));
    }
}
