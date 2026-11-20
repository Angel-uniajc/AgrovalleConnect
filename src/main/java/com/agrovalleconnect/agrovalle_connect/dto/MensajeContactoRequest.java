package com.agrovalleconnect.agrovalle_connect.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * HU-05: cuerpo de POST /api/v1/contacto/mensaje.
 * No tiene comprador_id ni fecha_envio: el comprador sale del token
 * y la fecha la pone el servidor. Cualquier campo extra se ignora.
 *  * Los tipos se leen sin conversiones: id_producto debe ser un entero JSON
 *  * y mensaje un texto JSON (ver LecturaJsonEstricta).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MensajeContactoRequest(

        @NotNull(message = "El producto es obligatorio")
        @Positive(message = "El producto debe ser un número mayor que cero")
        @JsonProperty("id_producto")
        @JsonDeserialize(using = LecturaJsonEstricta.EnteroEstricto.class)
        Long idProducto,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = MensajeContactoRequest.MENSAJE_MAX,
                message = "El mensaje no puede superar 2000 caracteres")
        @JsonDeserialize(using = LecturaJsonEstricta.TextoEstricto.class)
        String mensaje

) {
    public static final int MENSAJE_MAX = 2000;
}
