package com.agrovalleconnect.agrovalle_connect.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PedidoCreadoEventTest {

    @Test
    void agricultores_noSeModificanDesdeFueraDelEvento() {
        List<Long> ids = new ArrayList<>(List.of(9L));
        PedidoCreadoEvent evento = new PedidoCreadoEvent(41L, 7L, ids);
        ids.clear();
        assertThat(evento.agricultoresIds()).containsExactly(9L);
        assertThatThrownBy(() -> evento.agricultoresIds().add(10L))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
