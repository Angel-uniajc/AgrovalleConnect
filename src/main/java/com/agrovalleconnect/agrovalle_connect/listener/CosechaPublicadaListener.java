package com.agrovalleconnect.agrovalle_connect.listener;

import com.agrovalleconnect.agrovalle_connect.event.CosechaPublicadaEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CosechaPublicadaListener {

    @EventListener
    public void registrarPublicacion(CosechaPublicadaEvent evento) {
        log.info("Cosecha publicada: productoId={}, tipo={}, fincaId={}, municipio={}",
                evento.productoId(), evento.tipo(), evento.fincaId(), evento.municipio());
    }
}
