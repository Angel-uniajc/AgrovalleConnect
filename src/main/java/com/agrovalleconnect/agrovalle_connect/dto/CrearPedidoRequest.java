package com.agrovalleconnect.agrovalle_connect.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * HU-12: cuerpo de POST /api/v1/pedidos.
 * La lista items es el "carrito" de esta operación (no hay tabla de carrito).
 * No tiene comprador_id: el comprador sale del token. Cualquier campo extra se ignora.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CrearPedidoRequest(

        @NotEmpty(message = "El pedido debe incluir al menos un producto")
        @Valid
        List<ItemPedidoRequest> items

) {
}
