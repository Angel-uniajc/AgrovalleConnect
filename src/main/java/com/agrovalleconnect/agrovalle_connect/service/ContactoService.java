package com.agrovalleconnect.agrovalle_connect.service;

import com.agrovalleconnect.agrovalle_connect.dto.MensajeContactoRequest;
import com.agrovalleconnect.agrovalle_connect.dto.MensajeContactoResponse;
import com.agrovalleconnect.agrovalle_connect.exception.AccesoDenegadoException;
import com.agrovalleconnect.agrovalle_connect.exception.ProductoNoDisponibleException;
import com.agrovalleconnect.agrovalle_connect.exception.RecursoNoEncontradoException;
import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.MensajeContacto;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.model.Producto.Estado;
import com.agrovalleconnect.agrovalle_connect.repository.CompradorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.MensajeContactoRepository;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactoService {

    private final CompradorRepository compradorRepository;

    private final ProductoRepository productoRepository;

    private final MensajeContactoRepository mensajeContactoRepository;

    public ContactoService(CompradorRepository compradorRepository,
                           ProductoRepository productoRepository,
                           MensajeContactoRepository mensajeContactoRepository) {
        this.compradorRepository = compradorRepository;
        this.productoRepository = productoRepository;
        this.mensajeContactoRepository = mensajeContactoRepository;
    }

    @Transactional
    public MensajeContactoResponse enviar(String correoComprador, MensajeContactoRequest request) {
        Comprador comprador = compradorRepository.findByUsuarioCorreo(correoComprador)
                .orElseThrow(() -> new AccesoDenegadoException("Acceso denegado"));

        Producto producto = productoRepository.findById(request.idProducto())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        if (producto.getEstado() != Estado.ACTIVO
                || producto.getCantidadDisponible().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductoNoDisponibleException("El producto no está disponible para contacto");
        }

        MensajeContacto mensaje = MensajeContacto.builder()
                .comprador(comprador)
                .producto(producto)
                .mensaje(request.mensaje())
                .build();

        return MensajeContactoResponse.desde(mensajeContactoRepository.save(mensaje));
    }
}
