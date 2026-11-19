package com.agrovalleconnect.agrovalle_connect.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RegistroCompradorRequestTest {

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

    private RegistroCompradorRequest requestCon(String nombre, String correo, String password,
                                                String rol, String tipo, String documento) {
        return new RegistroCompradorRequest(nombre, correo, password, rol, tipo, documento);
    }

    private RegistroCompradorRequest requestValido() {
        return requestCon("Restaurante La Ceiba", "compras@laceiba.co", "Segura@2026",
                "COMPRADOR", "restaurante", "900123456");
    }

    /** Devuelve los nombres de los campos (en Java) que tienen error. */
    private Set<String> camposConError(RegistroCompradorRequest request) {
        return validador.validate(request).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void requestValido_noTieneViolaciones() {
        assertThat(camposConError(requestValido())).isEmpty();
    }

    @Test
    void camposVacios_sonRechazados() {
        RegistroCompradorRequest request = requestCon("", "", "", "COMPRADOR", "", "");

        assertThat(camposConError(request))
                .contains("nombre", "correo", "password", "tipoComprador", "documento");
    }

    @Test
    void nombreMayorA50Caracteres_esRechazado() {
        RegistroCompradorRequest request = requestCon("A".repeat(51), "compras@laceiba.co",
                "Segura@2026", "COMPRADOR", "restaurante", "900123456");

        assertThat(camposConError(request)).containsExactly("nombre");
    }

    @Test
    void correoSinFormato_esRechazado() {
        RegistroCompradorRequest request = requestCon("La Ceiba", "esto-no-es-un-correo",
                "Segura@2026", "COMPRADOR", "restaurante", "900123456");

        assertThat(camposConError(request)).containsExactly("correo");
    }

    @Test
    void correoMayorA50Caracteres_esRechazado() {
        RegistroCompradorRequest request = requestCon("La Ceiba", "a".repeat(45) + "@x.com",
                "Segura@2026", "COMPRADOR", "restaurante", "900123456");

        assertThat(camposConError(request)).containsExactly("correo");
    }

    @ParameterizedTest
    @ValueSource(strings = {"segura2026", "SEGURA@2026", "Segura#2026", "Se@1"})
    void passwordDebil_esRechazada(String password) {
        RegistroCompradorRequest request = requestCon("La Ceiba", "compras@laceiba.co",
                password, "COMPRADOR", "restaurante", "900123456");

        assertThat(camposConError(request)).containsExactly("password");
    }

    @ParameterizedTest
    @ValueSource(strings = {"individual", "COMERCIANTE", "Restaurante"})
    void tipoCompradorPermitido_sinImportarMayusculas_esAceptado(String tipo) {
        RegistroCompradorRequest request = requestCon("La Ceiba", "compras@laceiba.co",
                "Segura@2026", "COMPRADOR", tipo, "900123456");

        assertThat(camposConError(request)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"mayorista", "ADMIN", "restaurante "})
    void tipoCompradorNoPermitido_esRechazado(String tipo) {
        RegistroCompradorRequest request = requestCon("La Ceiba", "compras@laceiba.co",
                "Segura@2026", "COMPRADOR", tipo, "900123456");

        assertThat(camposConError(request)).containsExactly("tipoComprador");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "12345678901", "900123456-7", "abc123456"})
    void documentoInvalido_esRechazado(String documento) {
        RegistroCompradorRequest request = requestCon("La Ceiba", "compras@laceiba.co",
                "Segura@2026", "COMPRADOR", "restaurante", documento);

        assertThat(camposConError(request)).containsExactly("documento");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "1234567890"})
    void documentoEnLosLimites_esAceptado(String documento) {
        RegistroCompradorRequest request = requestCon("La Ceiba", "compras@laceiba.co",
                "Segura@2026", "COMPRADOR", "restaurante", documento);

        assertThat(camposConError(request)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "AGRICULTOR", "comprador"})
    void rolDistintoDeComprador_esRechazado(String rol) {
        RegistroCompradorRequest request = requestCon("La Ceiba", "compras@laceiba.co",
                "Segura@2026", rol, "restaurante", "900123456");

        assertThat(camposConError(request)).containsExactly("rol");
    }

    @Test
    void tipoCompradorNormalizado_devuelveMayusculas() {
        assertThat(requestValido().tipoCompradorNormalizado())
                .isEqualTo(Comprador.TipoComprador.RESTAURANTE);
    }
}
