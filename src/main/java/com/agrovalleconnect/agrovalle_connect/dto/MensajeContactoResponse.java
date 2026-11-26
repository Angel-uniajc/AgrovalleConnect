package com.agrovalleconnect.agrovalle_connect.dto;

import com.agrovalleconnect.agrovalle_connect.model.MensajeContacto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * HU-05: respuesta 200 del contacto. Solo id, producto y fecha:
 * no expone la entidad JPA ni datos del comprador o del agricultor.
 */
public record MensajeContactoResponse(
        Long id,
        @JsonProperty("id_producto") Long idProducto,
        @JsonProperty("fecha_envio") LocalDateTime fechaEnvio
) {

    public static MensajeContactoResponse desde(MensajeContacto mensaje) {
        return new MensajeContactoResponse(
                mensaje.getId(),
                mensaje.getProducto().getId(),
                mensaje.getFechaEnvio());
    }
}
