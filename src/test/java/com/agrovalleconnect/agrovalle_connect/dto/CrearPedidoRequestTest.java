package com.agrovalleconnect.agrovalle_connect.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class CrearPedidoRequestTest {

    // Mismo motor JSON (Jackson 3) que usa Spring Boot 4
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

    private ItemPedidoRequest item(Long idProducto, String cantidad) {
        return new ItemPedidoRequest(idProducto, cantidad == null ? null : new BigDecimal(cantidad));
    }

    private CrearPedidoRequest pedido(ItemPedidoRequest... items) {
        return new CrearPedidoRequest(List.of(items));
    }

    private Set<ConstraintViolation<CrearPedidoRequest>> errores(CrearPedidoRequest request) {
        return validador.validate(request);
    }

    private Set<String> camposConError(CrearPedidoRequest request) {
        return errores(request).stream()
                .map(error -> error.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private Set<String> mensajes(CrearPedidoRequest request) {
        return errores(request).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    @Test
    void pedidoValido_noTieneViolaciones() {
        CrearPedidoRequest request = pedido(item(12L, "2.00"), item(18L, "1.50"));

        assertThat(camposConError(request)).isEmpty();
    }

    @Test
    void listaVacia_fallaSoloEnItems() {
        CrearPedidoRequest request = new CrearPedidoRequest(List.of());

        assertThat(camposConError(request)).containsExactly("items");
        assertThat(mensajes(request)).containsExactly("El pedido debe incluir al menos un producto");
    }

    @Test
    void listaNula_fallaSoloEnItems() {
        assertThat(camposConError(new CrearPedidoRequest(null))).containsExactly("items");
    }

    @Test
    void itemSinProducto_fallaSoloEnIdProducto() {
        CrearPedidoRequest request = pedido(item(null, "2.00"));

        assertThat(camposConError(request)).containsExactly("items[0].idProducto");
        assertThat(mensajes(request)).containsExactly("El producto es obligatorio");
    }

    @Test
    void productoCeroONegativo_fallaSoloEnIdProducto() {
        assertThat(camposConError(pedido(item(0L, "2.00")))).containsExactly("items[0].idProducto");
        assertThat(camposConError(pedido(item(-5L, "2.00")))).containsExactly("items[0].idProducto");
    }

    @Test
    void cantidadCero_fallaSoloEnCantidad() {
        CrearPedidoRequest request = pedido(item(12L, "0"));

        assertThat(camposConError(request)).containsExactly("items[0].cantidad");
        assertThat(mensajes(request)).containsExactly("La cantidad debe ser mayor que cero");
    }

    @Test
    void cantidadNegativa_fallaSoloEnCantidad() {
        assertThat(camposConError(pedido(item(12L, "-1.50")))).containsExactly("items[0].cantidad");
    }

    @Test
    void cantidadNula_fallaSoloEnCantidad() {
        CrearPedidoRequest request = pedido(item(12L, null));

        assertThat(camposConError(request)).containsExactly("items[0].cantidad");
        assertThat(mensajes(request)).containsExactly("La cantidad es obligatoria");
    }

    @Test
    void cantidadConTresDecimales_fallaSoloEnCantidad() {
        assertThat(camposConError(pedido(item(12L, "1.555")))).containsExactly("items[0].cantidad");
    }

    @Test
    void errorEnSegundoItem_indicaSuPosicion() {
        CrearPedidoRequest request = pedido(item(12L, "2.00"), item(18L, "0"));

        assertThat(camposConError(request)).containsExactly("items[1].cantidad");
    }

    @Test
    void json_leeIdProductoEnSnakeCaseEIgnoraCompradorId() {
        String json = """
                {"comprador_id": 99, "items": [{"id_producto": 12, "cantidad": 2.00}]}
                """;

        CrearPedidoRequest request = MAPPER.readValue(json, CrearPedidoRequest.class);

        assertThat(request.items()).hasSize(1);
        assertThat(request.items().get(0).idProducto()).isEqualTo(12L);
        assertThat(request.items().get(0).cantidad()).isEqualByComparingTo("2.00");
    }
}
