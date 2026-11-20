package com.agrovalleconnect.agrovalle_connect.controller;

import com.agrovalleconnect.agrovalle_connect.dto.OfertaCatalogoResponse;
import com.agrovalleconnect.agrovalle_connect.dto.PublicarCosechaRequest;
import com.agrovalleconnect.agrovalle_connect.dto.PublicarCosechaResponse;
import com.agrovalleconnect.agrovalle_connect.service.CatalogoService;
import com.agrovalleconnect.agrovalle_connect.service.CosechaService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    private static final String CORREO = "ana@correo.com";

    @Mock
    private CosechaService cosechaService;

    @Mock
    private CatalogoService catalogoService;

    @InjectMocks
    private ProductoController productoController;

    @Test
    void publicar_delegaConElCorreoAutenticadoYRetorna201ConId() {
        PublicarCosechaRequest request = new PublicarCosechaRequest(
                1L, 2L, "Tomate chonto", new BigDecimal("100.00"),
                new BigDecimal("2500.00"), LocalDate.now());
        Authentication authentication = new UsernamePasswordAuthenticationToken(CORREO, null);
        when(cosechaService.publicar(CORREO, request)).thenReturn(new PublicarCosechaResponse(10L));

        ResponseEntity<PublicarCosechaResponse> respuesta =
                productoController.publicar(request, authentication);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertEquals(10L, respuesta.getBody().id());
        verify(cosechaService).publicar(CORREO, request);
    }

    @Test
    void consultar_delegaLosFiltrosAlServicioYRetorna200ConLasOfertas() {
        List<OfertaCatalogoResponse> ofertas = List.of(new OfertaCatalogoResponse(
                12L, "Mango", new BigDecimal("25.50"), new BigDecimal("3200.00"),
                LocalDate.of(2026, 11, 20), "Frutas", 4L, "Dagua"));
        when(catalogoService.consultar("Dagua", "Frutas")).thenReturn(ofertas);

        ResponseEntity<List<OfertaCatalogoResponse>> respuesta =
                productoController.consultar("Dagua", "Frutas");

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(ofertas, respuesta.getBody());
        verify(catalogoService).consultar("Dagua", "Frutas");
    }
}
