package com.agrovalleconnect.agrovalle_connect.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AgricultorRegisterDTOTest {

    private static ValidatorFactory fabrica;
    private static Validator validador;

    @BeforeAll
    static void iniciar() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validador = fabrica.getValidator();
    }

    @AfterAll
    static void cerrar() {
        fabrica.close();
    }

    private AgricultorRegisterDTO dtoValido() {
        AgricultorRegisterDTO dto = new AgricultorRegisterDTO();
        dto.setNombre("Pedro");
        dto.setCorreo("pedro@mail.com");
        dto.setPassword("Secreta123");
        dto.setUbicacionValle("Dagua");
        dto.setCedula("1234567890");
        return dto;
    }

    @Test
    void dtoValido_noTieneViolaciones() {
        Set<ConstraintViolation<AgricultorRegisterDTO>> errores = validador.validate(dtoValido());

        assertThat(errores).isEmpty();
    }

    @Test
    void nombreVacio_esRechazado() {
        AgricultorRegisterDTO dto = dtoValido();
        dto.setNombre("");

        Set<ConstraintViolation<AgricultorRegisterDTO>> errores = validador.validate(dto);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath()).hasToString("nombre");
    }

    @Test
    void correoSinFormato_esRechazado() {
        AgricultorRegisterDTO dto = dtoValido();
        dto.setCorreo("esto-no-es-un-correo");

        Set<ConstraintViolation<AgricultorRegisterDTO>> errores = validador.validate(dto);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath()).hasToString("correo");
    }

    @Test
    void passwordCorta_esRechazada() {
        AgricultorRegisterDTO dto = dtoValido();
        dto.setPassword("1234567");   // 7 caracteres: uno menos del mínimo

        Set<ConstraintViolation<AgricultorRegisterDTO>> errores = validador.validate(dto);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath()).hasToString("password");
    }

    @Test
    void passwordDeOchoCaracteres_esAceptada() {
        AgricultorRegisterDTO dto = dtoValido();
        dto.setPassword("12345678");  // justo en el límite

        assertThat(validador.validate(dto)).isEmpty();
    }
}
