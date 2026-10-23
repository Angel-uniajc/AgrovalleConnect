package com.agrovalleconnect.agrovalle_connect.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RegistroFincaRequestTest {

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

    private RegistroFincaRequest requestValido() {
        return new RegistroFincaRequest(
                "La Esperanza",
                "Palmira",
                "El Cabuyal",
                new BigDecimal("12.50"));
    }

    @Test
    void requestValido_noTieneViolaciones() {
        Set<ConstraintViolation<RegistroFincaRequest>> errores =
                validador.validate(requestValido());

        assertThat(errores).isEmpty();
    }

    @Test
    void nombreVacio_esRechazado() {
        RegistroFincaRequest request = new RegistroFincaRequest(
                "",
                "Palmira",
                "El Cabuyal",
                new BigDecimal("12.50"));

        Set<ConstraintViolation<RegistroFincaRequest>> errores =
                validador.validate(request);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath())
                .hasToString("nombre");
    }

    @Test
    void municipioVacio_esRechazado() {
        RegistroFincaRequest request = new RegistroFincaRequest(
                "La Esperanza",
                "",
                "El Cabuyal",
                new BigDecimal("12.50"));

        Set<ConstraintViolation<RegistroFincaRequest>> errores =
                validador.validate(request);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath())
                .hasToString("municipio");
    }

    @Test
    void areaNegativa_esRechazada() {
        RegistroFincaRequest request = new RegistroFincaRequest(
                "La Esperanza",
                "Palmira",
                "El Cabuyal",
                new BigDecimal("-5"));

        Set<ConstraintViolation<RegistroFincaRequest>> errores =
                validador.validate(request);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath())
                .hasToString("areaHectareas");
    }

    @Test
    void areaCero_esRechazada() {
        RegistroFincaRequest request = new RegistroFincaRequest(
                "La Esperanza",
                "Palmira",
                "El Cabuyal",
                BigDecimal.ZERO);

        Set<ConstraintViolation<RegistroFincaRequest>> errores =
                validador.validate(request);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath())
                .hasToString("areaHectareas");
    }

    @Test
    void areaConTresDecimales_esRechazada() {
        RegistroFincaRequest request = new RegistroFincaRequest(
                "La Esperanza",
                "Palmira",
                "El Cabuyal",
                new BigDecimal("12.505"));

        Set<ConstraintViolation<RegistroFincaRequest>> errores =
                validador.validate(request);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath())
                .hasToString("areaHectareas");
    }

    @Test
    void nombreMayorA100Caracteres_esRechazado() {
        RegistroFincaRequest request = new RegistroFincaRequest(
                "A".repeat(101),
                "Palmira",
                "El Cabuyal",
                new BigDecimal("12.50"));

        Set<ConstraintViolation<RegistroFincaRequest>> errores =
                validador.validate(request);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath())
                .hasToString("nombre");
    }

    @Test
    void veredaMayorA100Caracteres_esRechazada() {
        RegistroFincaRequest request = new RegistroFincaRequest(
                "La Esperanza",
                "Palmira",
                "A".repeat(101),
                new BigDecimal("12.50"));

        Set<ConstraintViolation<RegistroFincaRequest>> errores =
                validador.validate(request);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath())
                .hasToString("vereda");
    }

    @Test
    void sinVeredaNiArea_esAceptado() {
        RegistroFincaRequest request = new RegistroFincaRequest(
                "La Esperanza",
                "Palmira",
                null,
                null);

        Set<ConstraintViolation<RegistroFincaRequest>> errores =
                validador.validate(request);

        assertThat(errores).isEmpty();
    }
}