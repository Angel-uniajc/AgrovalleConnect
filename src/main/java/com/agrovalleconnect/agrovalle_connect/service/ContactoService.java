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

/**
 * HU-05: registra la intención de contacto de un comprador sobre un producto.
 * Contrato en docs/contrato-contacto-hu05.md. Solo guarda el mensaje:
 * no cambia el stock, no crea pedidos ni emite eventos.
 */
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
        Comprador comprador = obtenerComprador(correoComprador);
        Producto producto = obtenerProductoDisponible(request.idProducto());

        MensajeContacto mensaje = MensajeContacto.builder()
                .comprador(comprador)
                .producto(producto)
                .mensaje(request.mensaje())
                .build();

        return MensajeContactoResponse.desde(mensajeContactoRepository.save(mensaje));
    }

    private Comprador obtenerComprador(String correo) {
        // mensaje genérico: no revela si el usuario existe o qué le falta
        return compradorRepository.findByUsuarioCorreo(correo)
                .orElseThrow(() -> new AccesoDenegadoException("Acceso denegado"));
    }

    private Producto obtenerProductoDisponible(Long idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        if (!estaDisponible(producto)) {
            throw new ProductoNoDisponibleException("El producto no está disponible para contacto");
        }
        return producto;
    }

    private static boolean estaDisponible(Producto producto) {
        return producto.getEstado() == Estado.ACTIVO
                && producto.getCantidadDisponible() != null
                && producto.getCantidadDisponible().compareTo(BigDecimal.ZERO) > 0;
    }
}
