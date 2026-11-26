package com.agrovalleconnect.agrovalle_connect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * HU-12: una línea del pedido (un producto y la cantidad que se quiere comprar).
 * id_producto debe ser un entero JSON (ver LecturaJsonEstricta).
 */
public record ItemPedidoRequest(

        @NotNull(message = "El producto es obligatorio")
        @Positive(message = "El producto debe ser un número mayor que cero")
        @JsonProperty("id_producto")
        @JsonDeserialize(using = LecturaJsonEstricta.EnteroEstricto.class)
        Long idProducto,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        @Digits(integer = 10, fraction = 2, message = "La cantidad admite máximo 10 enteros y 2 decimales")
        BigDecimal cantidad

) {
}
