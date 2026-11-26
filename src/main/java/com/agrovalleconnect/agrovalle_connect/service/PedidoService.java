package com.agrovalleconnect.agrovalle_connect.service;

import com.agrovalleconnect.agrovalle_connect.dto.CrearPedidoRequest;
import com.agrovalleconnect.agrovalle_connect.dto.PedidoResponse;
import com.agrovalleconnect.agrovalle_connect.repository.CompradorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.PedidoRepository;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;
import com.agrovalleconnect.agrovalle_connect.service.flete.CalculadoraFlete;
import org.springframework.context.ApplicationEventPublisher;

public class PedidoService {

    public PedidoService(CompradorRepository compradores, ProductoRepository productos,
            PedidoRepository pedidos, CalculadoraFlete flete, ApplicationEventPublisher eventos) {
    }

    public PedidoResponse crear(String correo, CrearPedidoRequest request) {
        throw new UnsupportedOperationException("Pendiente de implementación T05-HU12");
    }
}
