package com.agrovalleconnect.agrovalle_connect.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agrovalleconnect.agrovalle_connect.dto.MensajeContactoRequest;
import com.agrovalleconnect.agrovalle_connect.dto.MensajeContactoResponse;
import com.agrovalleconnect.agrovalle_connect.service.ContactoService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class ContactoControllerTest {

    private static final String CORREO = "sofia@correo.com";

    @Mock
    private ContactoService contactoService;

    @InjectMocks
    private ContactoController contactoController;

    @Test
    void enviar_delegaConElCorreoAutenticadoYRetorna200() {
        MensajeContactoRequest request = new MensajeContactoRequest(12L, "Me interesan 20 kg");
        Authentication authentication = new UsernamePasswordAuthenticationToken(CORREO, null);
        MensajeContactoResponse esperado =
                new MensajeContactoResponse(7L, 12L, LocalDateTime.of(2026, 11, 20, 15, 30));
        when(contactoService.enviar(CORREO, request)).thenReturn(esperado);

        ResponseEntity<MensajeContactoResponse> respuesta =
                contactoController.enviar(request, authentication);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(esperado, respuesta.getBody());
        verify(contactoService).enviar(CORREO, request);
    }
}
