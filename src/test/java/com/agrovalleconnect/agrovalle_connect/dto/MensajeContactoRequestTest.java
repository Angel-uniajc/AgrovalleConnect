package com.agrovalleconnect.agrovalle_connect.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

class MensajeContactoRequestTest {

    private static ValidatorFactory fabrica;
    private static Validator validador;
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    @BeforeAll
    static void iniciar() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validador = fabrica.getValidator();
    }

    @AfterAll
    static void cerrar() {
        fabrica.close();
    }

    private Set<String> camposConError(MensajeContactoRequest request) {
        Set<ConstraintViolation<MensajeContactoRequest>> errores = validador.validate(request);
        return errores.stream()
                .map(error -> error.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void requestValido_noTieneViolaciones() {
        assertThat(camposConError(new MensajeContactoRequest(12L, "Me interesa el mango"))).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void mensajeNuloVacioOSoloEspacios_esRechazado(String texto) {
        assertThat(camposConError(new MensajeContactoRequest(12L, texto))).containsExactly("mensaje");
    }

    @Test
    void mensajeDe2000Caracteres_esAceptado() {
        assertThat(camposConError(new MensajeContactoRequest(12L, "a".repeat(2000)))).isEmpty();
    }

    @Test
    void mensajeDe2001Caracteres_esRechazado() {
        assertThat(camposConError(new MensajeContactoRequest(12L, "a".repeat(2001))))
                .containsExactly("mensaje");
    }

    @Test
    void idProductoNulo_esRechazado() {
        assertThat(camposConError(new MensajeContactoRequest(null, "Hola"))).containsExactly("idProducto");
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void idProductoCeroONegativo_esRechazado(long id) {
        assertThat(camposConError(new MensajeContactoRequest(id, "Hola"))).containsExactly("idProducto");
    }

    @Test
    void json_leeIdProductoEnSnakeCaseEIgnoraCamposExtra() {
        String cuerpo = """
                {"id_producto": 12, "mensaje": "Hola",
                 "comprador_id": 99, "fecha_envio": "2020-01-01T00:00:00"}
                """;

        MensajeContactoRequest request = MAPPER.readValue(cuerpo, MensajeContactoRequest.class);

        assertThat(request.idProducto()).isEqualTo(12L);
        assertThat(request.mensaje()).isEqualTo("Hola");
    }
}
