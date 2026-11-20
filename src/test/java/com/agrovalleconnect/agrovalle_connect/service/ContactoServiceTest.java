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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactoServiceTest {

    private static final String CORREO = "comprador@correo.com";
    private static final Long ID_PRODUCTO = 12L;
    private static final String TEXTO = "Me interesan 20 kg de mango";

    @Mock
    private CompradorRepository compradorRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private MensajeContactoRepository mensajeContactoRepository;

    @InjectMocks
    private ContactoService contactoService;

    @Test
    void enviar_conProductoActivoConStock_guardaMensajeYRetornaResponse() {
        Comprador comprador = Comprador.builder().id(5L).build();
        Producto producto = producto(Estado.ACTIVO, "20");
        LocalDateTime fechaServidor = LocalDateTime.of(2026, 11, 20, 15, 30);
        when(compradorRepository.findByUsuarioCorreo(CORREO)).thenReturn(Optional.of(comprador));
        when(productoRepository.findById(ID_PRODUCTO)).thenReturn(Optional.of(producto));
        when(mensajeContactoRepository.save(any(MensajeContacto.class))).thenAnswer(inv -> {
            MensajeContacto m = inv.getArgument(0);
            m.setId(7L);
            m.setFechaEnvio(fechaServidor); // simula @CreationTimestamp
            return m;
        });

        MensajeContactoResponse response = contactoService.enviar(CORREO, request());

        ArgumentCaptor<MensajeContacto> captor = ArgumentCaptor.forClass(MensajeContacto.class);
        verify(mensajeContactoRepository).save(captor.capture());
        MensajeContacto guardado = captor.getValue();
        assertSame(comprador, guardado.getComprador());
        assertSame(producto, guardado.getProducto());
        assertEquals(TEXTO, guardado.getMensaje());

        assertEquals(new MensajeContactoResponse(7L, ID_PRODUCTO, fechaServidor), response);
        // no toca el stock ni guarda el producto
        assertEquals(0, new BigDecimal("20").compareTo(producto.getCantidadDisponible()));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void enviar_noAsignaLaFechaDesdeElCliente() {
        when(compradorRepository.findByUsuarioCorreo(CORREO))
                .thenReturn(Optional.of(Comprador.builder().id(5L).build()));
        when(productoRepository.findById(ID_PRODUCTO))
                .thenReturn(Optional.of(producto(Estado.ACTIVO, "1")));
        when(mensajeContactoRepository.save(any(MensajeContacto.class))).thenAnswer(inv -> inv.getArgument(0));

        contactoService.enviar(CORREO, request());

        ArgumentCaptor<MensajeContacto> captor = ArgumentCaptor.forClass(MensajeContacto.class);
        verify(mensajeContactoRepository).save(captor.capture());
        // la fecha la pone la base/Hibernate al insertar, no el servicio ni el JSON
        assertNull(captor.getValue().getFechaEnvio());
    }

    @Test
    void enviar_conCompradorSinPerfil_lanzaAccesoDenegadoSinGuardar() {
        when(compradorRepository.findByUsuarioCorreo(CORREO)).thenReturn(Optional.empty());

        AccesoDenegadoException ex = assertThrows(AccesoDenegadoException.class,
                () -> contactoService.enviar(CORREO, request()));

        assertEquals("Acceso denegado", ex.getMessage());
        verifyNoInteractions(productoRepository, mensajeContactoRepository);
    }

    @Test
    void enviar_conProductoInexistente_lanzaRecursoNoEncontradoSinGuardar() {
        when(compradorRepository.findByUsuarioCorreo(CORREO))
                .thenReturn(Optional.of(Comprador.builder().id(5L).build()));
        when(productoRepository.findById(ID_PRODUCTO)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> contactoService.enviar(CORREO, request()));

        verifyNoInteractions(mensajeContactoRepository);
    }

    @Test
    void enviar_conProductoInactivo_lanzaProductoNoDisponibleSinGuardar() {
        verificarProductoNoDisponible(producto(Estado.INACTIVO, "20"));
    }

    @Test
    void enviar_conProductoAgotado_lanzaProductoNoDisponibleSinGuardar() {
        verificarProductoNoDisponible(producto(Estado.AGOTADO, "20"));
    }

    @Test
    void enviar_conStockCero_lanzaProductoNoDisponibleSinGuardar() {
        verificarProductoNoDisponible(producto(Estado.ACTIVO, "0.00"));
    }

    private void verificarProductoNoDisponible(Producto producto) {
        when(compradorRepository.findByUsuarioCorreo(CORREO))
                .thenReturn(Optional.of(Comprador.builder().id(5L).build()));
        when(productoRepository.findById(ID_PRODUCTO)).thenReturn(Optional.of(producto));

        assertThrows(ProductoNoDisponibleException.class,
                () -> contactoService.enviar(CORREO, request()));

        verifyNoInteractions(mensajeContactoRepository);
        verify(productoRepository).findById(ID_PRODUCTO);
        verifyNoMoreInteractions(productoRepository);
    }

    private static MensajeContactoRequest request() {
        return new MensajeContactoRequest(ID_PRODUCTO, TEXTO);
    }

    private static Producto producto(Estado estado, String cantidad) {
        return Producto.builder()
                .id(ID_PRODUCTO)
                .tipo("Mango")
                .estado(estado)
                .cantidadDisponible(new BigDecimal(cantidad))
                .precioUnitario(new BigDecimal("3000"))
                .build();
    }
}
