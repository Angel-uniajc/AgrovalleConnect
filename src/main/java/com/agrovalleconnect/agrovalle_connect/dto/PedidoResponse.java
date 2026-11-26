package com.agrovalleconnect.agrovalle_connect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * HU-12: respuesta 201 de POST /api/v1/pedidos.
 * No expone la entidad JPA ni datos privados del comprador.
 * total = subtotal + costo_flete.
 */
public record PedidoResponse(
        Long id,
        String estado,
        List<ItemPedidoResponse> items,
        BigDecimal subtotal,
        @JsonProperty("costo_flete") BigDecimal costoFlete,
        BigDecimal total
) {
}
