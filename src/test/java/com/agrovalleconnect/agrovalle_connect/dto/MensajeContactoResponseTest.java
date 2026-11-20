package com.agrovalleconnect.agrovalle_connect.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.MensajeContacto;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class MensajeContactoResponseTest {

    // Mismo motor JSON (Jackson 3) que usa Spring Boot 4 en las respuestas
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    @Test
    void respuesta_soloExponeIdIdProductoYFechaEnvio() {
        MensajeContacto mensaje = MensajeContacto.builder()
                .id(7L)
                .comprador(Comprador.builder().id(3L).documento("12345678").build())
                .producto(Producto.builder().id(12L).tipo("Mango").build())
                .mensaje("Texto privado")
                .fechaEnvio(LocalDateTime.of(2026, 11, 20, 15, 30, 45))
                .build();

        String salida = MAPPER.writeValueAsString(MensajeContactoResponse.desde(mensaje));
        @SuppressWarnings("unchecked")
        Map<String, Object> campos = MAPPER.readValue(salida, Map.class);

        assertThat(campos).containsOnlyKeys("id", "id_producto", "fecha_envio");
        assertThat(campos.get("id")).isEqualTo(7);
        assertThat(campos.get("id_producto")).isEqualTo(12);
        assertThat(campos.get("fecha_envio")).isEqualTo("2026-11-20T15:30:45");
        assertThat(salida).doesNotContain("12345678", "Texto privado", "Mango");
    }
}
