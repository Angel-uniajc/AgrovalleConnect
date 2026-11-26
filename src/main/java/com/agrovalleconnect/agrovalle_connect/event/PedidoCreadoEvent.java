package com.agrovalleconnect.agrovalle_connect.event;

import java.util.List;

/** Evento inmutable sin entidades JPA. La emisión es síncrona y previa al commit. */
public record PedidoCreadoEvent(Long pedidoId, Long compradorId, List<Long> agricultoresIds) {

    public PedidoCreadoEvent {
        agricultoresIds = List.copyOf(agricultoresIds);
    }
}
