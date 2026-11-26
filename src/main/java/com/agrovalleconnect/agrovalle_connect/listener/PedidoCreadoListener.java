package com.agrovalleconnect.agrovalle_connect.listener;

import com.agrovalleconnect.agrovalle_connect.event.PedidoCreadoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Registra el aviso; no envía correo ni confirma el commit. */
@Component
@Slf4j
public class PedidoCreadoListener {

    @EventListener
    public void registrarPedido(PedidoCreadoEvent evento) {
        for (Long agricultorId : evento.agricultoresIds()) {
            log.info("Aviso de pedido creado: pedidoId={}, compradorId={}, agricultorId={}",
                    evento.pedidoId(), evento.compradorId(), agricultorId);
        }
    }
}
