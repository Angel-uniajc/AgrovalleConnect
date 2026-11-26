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
import tools.jackson.databind.json.JsonMapper;

class RegistroRequestDeserializacionTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
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

    @Test
    void jsonDeHu01SinRol_seLeeComoAgricultorValido() {
        String json = """
                {"nombre": "Pedro Gómez", "ubicacion_valle": "Cali", "cedula": "12345678",
                 "correo": "pedro@gmail.com", "password": "Segura@2026"}
                """;

        RegistroRequest request = MAPPER.readValue(json, RegistroRequest.class);

        assertThat(request).isInstanceOf(RegistroAgricultorRequest.class);
        assertThat(request.rol()).isNull();
        assertThat(validador.validate(request)).isEmpty();
    }

    @Test
    void rolAgricultor_seLeeComoAgricultorValido() {
        String json = """
                {"nombre": "Pedro Gómez", "ubicacion_valle": "Cali", "cedula": "12345678",
                 "correo": "pedro@gmail.com", "password": "Segura@2026", "rol": "AGRICULTOR"}
                """;

        RegistroRequest request = MAPPER.readValue(json, RegistroRequest.class);

        assertThat(request).isInstanceOf(RegistroAgricultorRequest.class);
        assertThat(validador.validate(request)).isEmpty();
    }

    @Test
    void rolComprador_seLeeComoComprador() {
        String json = """
                {"nombre": "Restaurante La Ceiba", "correo": "compras@laceiba.co",
                 "password": "Segura@2026", "rol": "COMPRADOR",
                 "tipo_comprador": "restaurante", "documento": "900123456"}
                """;

        RegistroRequest request = MAPPER.readValue(json, RegistroRequest.class);

        assertThat(request).isInstanceOf(RegistroCompradorRequest.class);
        RegistroCompradorRequest comprador = (RegistroCompradorRequest) request;
        assertThat(comprador.tipoComprador()).isEqualTo("restaurante");
        assertThat(comprador.documento()).isEqualTo("900123456");
        assertThat(validador.validate(request)).isEmpty();
    }

    @Test
    void rolDesconocido_noSeAceptaComoAgricultor() {
        String json = """
                {"nombre": "Intruso", "ubicacion_valle": "Cali", "cedula": "12345678",
                 "correo": "intruso@gmail.com", "password": "Segura@2026", "rol": "ADMIN"}
                """;

        RegistroRequest request = MAPPER.readValue(json, RegistroRequest.class);
        Set<ConstraintViolation<RegistroRequest>> errores = validador.validate(request);

        // Jackson usa defaultImpl (agricultor), pero la validación lo frena
        assertThat(errores).hasSize(1);
        assertThat(errores.iterator().next().getPropertyPath()).hasToString("rol");
    }

    @Test
    void respuestaComprador_noExponeCredenciales() {
        RegistroCompradorResponse respuesta = new RegistroCompradorResponse(
                "Restaurante La Ceiba", "COMPRADOR", "RESTAURANTE",
                "Comprador registrado exitosamente");

        String json = MAPPER.writeValueAsString(respuesta);

        assertThat(json).contains("\"tipo_comprador\":\"RESTAURANTE\"");
        assertThat(json).doesNotContain("password", "hash", "documento", "id");
    }
}
