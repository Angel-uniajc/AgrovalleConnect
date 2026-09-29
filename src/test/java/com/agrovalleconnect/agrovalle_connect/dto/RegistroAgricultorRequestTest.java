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

class RegistroAgricultorRequestTest {

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

    private RegistroAgricultorRequest requestValido() {
        return new RegistroAgricultorRequest(
                "Pedro Gómez",
                "Cali",
                "12345678",
                "delacortez32@gmail.com",
                "LagordaTaborda@123");
    }

    @Test
    void requestValido_noTieneViolaciones() {
        Set<ConstraintViolation<RegistroAgricultorRequest>> errores =
                validador.validate(requestValido());

        assertThat(errores).isEmpty();
    }

    @Test
    void nombreVacio_esRechazado() {
        RegistroAgricultorRequest request = new RegistroAgricultorRequest(
               "",
                "Cali",
                "12345678",
                "delacortez32@gmail.com",
                "LagordaTaborda@123");

        Set<ConstraintViolation<RegistroAgricultorRequest>> errores =
                validador.validate(request);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath())
                .hasToString("nombre");
    }

    @Test
    void nombreMayorA50Caracteres_esRechazado() {
        RegistroAgricultorRequest request = new RegistroAgricultorRequest(
                "A".repeat(51),
                "Cali",
                "12345678",
                "delacortez32@gmail.com",
                "LagordaTaborda@123");

        Set<ConstraintViolation<RegistroAgricultorRequest>> errores =
                validador.validate(request);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath())
                .hasToString("nombre");
    }

    @Test
    void correoVacio_esRechazado() {
        RegistroAgricultorRequest request = new RegistroAgricultorRequest(
                "Pedro Gómez",
                "Cali",
                "12345678",
                "",
                "LagordaTaborda@123");

        Set<ConstraintViolation<RegistroAgricultorRequest>> errores =
                validador.validate(request);

        assertThat(errores).isNotEmpty();
        assertThat(errores.stream()
                .anyMatch(e -> e.getPropertyPath().toString().equals("correo")))
                .isTrue();
    }

    @Test
    void correoSinFormato_esRechazado() {
        RegistroAgricultorRequest request = new RegistroAgricultorRequest(
                "Pedro Gómez",
                "Cali",
                "12345678",
                "esto-no-es-un-correo",
                "LagordaTaborda@123");

        Set<ConstraintViolation<RegistroAgricultorRequest>> errores =
                validador.validate(request);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath())
                .hasToString("correo");
    }

    @Test
    void correoMayorA50Caracteres_esRechazado() {
        RegistroAgricultorRequest request = new RegistroAgricultorRequest(
                "Pedro Gómez",
                "Cali",
                "12345678",
                "a".repeat(45) + "@mail.com",
                "LagordaTaborda@123");

        Set<ConstraintViolation<RegistroAgricultorRequest>> errores =
                validador.validate(request);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath())
                .hasToString("correo");
    }

    @Test
    void passwordCorta_esRechazada() {
        RegistroAgricultorRequest request = new RegistroAgricultorRequest(
                "Pedro Gómez",
                "Cali",
                "12345678",
                "delacortez32@gmail.com",
                "Ta1@");

        Set<ConstraintViolation<RegistroAgricultorRequest>> errores =
                validador.validate(request);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath())
                .hasToString("password");
    }

    @Test
    void passwordDeOchoCaracteres_esAceptada() {
        RegistroAgricultorRequest request = new RegistroAgricultorRequest(
                "Pedro Gómez",
                "Cali",
                "12345678",
                "delacortez32@gmail.com",
                "LagordaTaborda@123");

        assertThat(validador.validate(request)).isEmpty();
    }

    @Test
    void ubicacionMayorA15Caracteres_esRechazada() {
        RegistroAgricultorRequest request = new RegistroAgricultorRequest(
                "Pedro Gómez",
                "A".repeat(16),
                "12345678",
                "delacortez32@gmail.com",
                "LagordaTaborda@123");

        Set<ConstraintViolation<RegistroAgricultorRequest>> errores =
                validador.validate(request);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath())
                .hasToString("ubicacionValle");
    }

    @Test
    void cedulaConLetras_esRechazada() {
        RegistroAgricultorRequest request = new RegistroAgricultorRequest(
                "Pedro Gómez",
                "Cali",
                "12345a78",
                "delacortez32@gmail.com",
                "LagordaTaborda@123");

        Set<ConstraintViolation<RegistroAgricultorRequest>> errores =
                validador.validate(request);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath())
                .hasToString("cedula");
    }

    @Test
    void cedulaDeMenosDe6Digitos_esRechazada() {
        RegistroAgricultorRequest request = new RegistroAgricultorRequest(
                "Pedro Gómez",
                "Cali",
                "12345",
                "delacortez32@gmail.com",
                "LagordaTaborda@123");

        Set<ConstraintViolation<RegistroAgricultorRequest>> errores =
                validador.validate(request);

        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath())
                .hasToString("cedula");
    }
}