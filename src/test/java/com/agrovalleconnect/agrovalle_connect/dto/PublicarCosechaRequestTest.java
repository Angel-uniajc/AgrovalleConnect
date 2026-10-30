package com.agrovalleconnect.agrovalle_connect.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PublicarCosechaRequestTest {

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

    private PublicarCosechaRequest request(String tipo, BigDecimal cantidad,
                                           BigDecimal precio, LocalDate fecha) {
        return new PublicarCosechaRequest(1L, 2L, tipo, cantidad, precio, fecha);
    }

    private PublicarCosechaRequest requestValido() {
        return request("Café pergamino", new BigDecimal("50.00"),
                new BigDecimal("18500.00"), LocalDate.now().plusDays(1));
    }

    private Set<String> camposConError(PublicarCosechaRequest request) {
        Set<ConstraintViolation<PublicarCosechaRequest>> errores = validador.validate(request);
        return errores.stream()
                .map(error -> error.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void requestValido_noTieneViolaciones() {
        assertThat(camposConError(requestValido())).isEmpty();
    }

    @Test
    void fechaDeAyer_esRechazada() {
        PublicarCosechaRequest request = request("Café pergamino", new BigDecimal("50.00"),
                new BigDecimal("18500.00"), LocalDate.now().minusDays(1));

        assertThat(camposConError(request)).containsExactly("fechaCosecha");
    }

    @Test
    void fechaDeHoy_esAceptada() {
        PublicarCosechaRequest request = request("Café pergamino", new BigDecimal("50.00"),
                new BigDecimal("18500.00"), LocalDate.now());

        assertThat(camposConError(request)).isEmpty();
    }

    @Test
    void cantidadCero_esRechazada() {
        PublicarCosechaRequest request = request("Café pergamino", BigDecimal.ZERO,
                new BigDecimal("18500.00"), LocalDate.now());

        assertThat(camposConError(request)).containsExactly("cantidad");
    }

    @Test
    void precioNegativo_esRechazado() {
        PublicarCosechaRequest request = request("Café pergamino", new BigDecimal("50.00"),
                new BigDecimal("-100.00"), LocalDate.now());

        assertThat(camposConError(request)).containsExactly("precioUnitario");
    }

    @Test
    void camposNulos_sonRechazados() {
        PublicarCosechaRequest request = new PublicarCosechaRequest(null, null, null, null, null, null);

        assertThat(camposConError(request)).containsExactlyInAnyOrder(
                "fincaId", "categoriaId", "tipo", "cantidad", "precioUnitario", "fechaCosecha");
    }

    @Test
    void tipoDeMasDe80Caracteres_esRechazado() {
        PublicarCosechaRequest request = request("a".repeat(81), new BigDecimal("50.00"),
                new BigDecimal("18500.00"), LocalDate.now());

        assertThat(camposConError(request)).containsExactly("tipo");
    }

    @Test
    void precioConTresDecimales_esRechazado() {
        PublicarCosechaRequest request = request("Café pergamino", new BigDecimal("50.00"),
                new BigDecimal("18500.123"), LocalDate.now());

        assertThat(camposConError(request)).containsExactly("precioUnitario");
    }
}