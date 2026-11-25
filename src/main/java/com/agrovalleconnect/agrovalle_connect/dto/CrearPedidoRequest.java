package com.agrovalleconnect.agrovalle_connect.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * HU-12: cuerpo de POST /api/v1/pedidos.
 * La lista items es el "carrito" de esta operación (no hay tabla de carrito).
 * No tiene comprador_id: el comprador sale del token. Cualquier campo extra se ignora.
 * Cada elemento de items es obligatorio: [null] se rechaza con 400.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CrearPedidoRequest(

        @NotEmpty(message = "El pedido debe incluir al menos un producto")
        @Valid
        List<@NotNull(message = "El ítem es obligatorio") ItemPedidoRequest> items

) {
}
