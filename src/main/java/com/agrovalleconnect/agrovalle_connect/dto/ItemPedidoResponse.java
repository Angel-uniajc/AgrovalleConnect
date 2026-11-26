package com.agrovalleconnect.agrovalle_connect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * HU-12: una línea del pedido en la respuesta 201.
 * precio_unitario es el precio copiado al momento de la compra.
 */
public record ItemPedidoResponse(
        @JsonProperty("id_producto") Long idProducto,
        BigDecimal cantidad,
        @JsonProperty("precio_unitario") BigDecimal precioUnitario
) {
}
