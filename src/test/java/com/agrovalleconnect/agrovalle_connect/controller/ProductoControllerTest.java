package com.agrovalleconnect.agrovalle_connect.controller;

import com.agrovalleconnect.agrovalle_connect.dto.PublicarCosechaRequest;
import com.agrovalleconnect.agrovalle_connect.dto.PublicarCosechaResponse;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    private static final String CORREO = "ana@correo.com";

    @Mock
    private CosechaService cosechaService;

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
}
