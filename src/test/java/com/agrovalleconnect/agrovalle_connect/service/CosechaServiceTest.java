package com.agrovalleconnect.agrovalle_connect.service;

import com.agrovalleconnect.agrovalle_connect.dto.PublicarCosechaRequest;
import com.agrovalleconnect.agrovalle_connect.dto.PublicarCosechaResponse;
import com.agrovalleconnect.agrovalle_connect.exception.AccesoDenegadoException;
import com.agrovalleconnect.agrovalle_connect.exception.CategoriaInvalidaException;
import com.agrovalleconnect.agrovalle_connect.exception.RecursoNoEncontradoException;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.model.Producto.Estado;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.CategoriaRepository;
import com.agrovalleconnect.agrovalle_connect.repository.FincaRepository;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CosechaServiceTest {

    private static final String CORREO = "ana@correo.com";

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private FincaRepository fincaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CosechaService cosechaService;

    @Test
    void publicar_conDatosValidos_guardaProductoActivoYRetornaResponse() {
        when(fincaRepository.findById(1L)).thenReturn(Optional.of(fincaDe(CORREO)));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(new Categoria()));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        PublicarCosechaResponse response = cosechaService.publicar(CORREO, request(1L, 2L));

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());

        assertEquals(Estado.ACTIVO, captor.getValue().getEstado());
        assertEquals("Café pergamino", captor.getValue().getTipo());
        assertEquals(0, new BigDecimal("25").compareTo(captor.getValue().getCantidadDisponible()));
        assertEquals(10L, response.id());
    }

    @Test
    void publicar_conFincaInexistente_lanzaRecursoNoEncontrado() {
        when(fincaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> cosechaService.publicar(CORREO, request(99L, 2L)));

        verify(productoRepository, never()).save(any());
        verifyNoInteractions(categoriaRepository);
    }

    @Test
    void publicar_conFincaDeOtroAgricultor_lanzaAccesoDenegado() {
        when(fincaRepository.findById(1L)).thenReturn(Optional.of(fincaDe("otro@correo.com")));

        assertThrows(AccesoDenegadoException.class,
                () -> cosechaService.publicar(CORREO, request(1L, 2L)));

        verify(productoRepository, never()).save(any());
        verifyNoInteractions(categoriaRepository);
    }

    @Test
    void publicar_conCategoriaInexistente_lanzaCategoriaInvalida() {
        when(fincaRepository.findById(1L)).thenReturn(Optional.of(fincaDe(CORREO)));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(CategoriaInvalidaException.class,
                () -> cosechaService.publicar(CORREO, request(1L, 2L)));

        verify(productoRepository, never()).save(any());
    }

    private Finca fincaDe(String correo) {
        Usuario usuario = Usuario.builder().correo(correo).build();
        Agricultor agricultor = Agricultor.builder().usuario(usuario).build();
        return Finca.builder().id(1L).agricultor(agricultor).build();
    }

    private PublicarCosechaRequest request(Long fincaId, Long categoriaId) {
        return new PublicarCosechaRequest(fincaId, categoriaId, "Café pergamino",
                new BigDecimal("25"), new BigDecimal("18500"), LocalDate.now());
    }
}