package com.agrovalleconnect.agrovalle_connect.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class PedidoResponseTest {

    // Mismo motor JSON (Jackson 3) que usa Spring Boot 4 en las respuestas
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    @Test
    @SuppressWarnings("unchecked")
    void respuesta_usaSnakeCaseYSoloLosCamposDelContrato() {
        ItemPedidoResponse item = new ItemPedidoResponse(12L, new BigDecimal("2.00"), new BigDecimal("3200.00"));
        PedidoResponse pedido = new PedidoResponse(41L, "PENDIENTE_PAGO", List.of(item),
                new BigDecimal("6400.00"), new BigDecimal("10000.00"), new BigDecimal("16400.00"));

        String salida = MAPPER.writeValueAsString(pedido);
        Map<String, Object> campos = MAPPER.readValue(salida, Map.class);

        assertThat(campos).containsOnlyKeys("id", "estado", "items", "subtotal", "costo_flete", "total");
        assertThat(campos.get("estado")).isEqualTo("PENDIENTE_PAGO");

        List<Map<String, Object>> items = (List<Map<String, Object>>) campos.get("items");
        assertThat(items).hasSize(1);
        assertThat(items.get(0)).containsOnlyKeys("id_producto", "cantidad", "precio_unitario");
        assertThat(items.get(0).get("id_producto")).isEqualTo(12);
    }
}
