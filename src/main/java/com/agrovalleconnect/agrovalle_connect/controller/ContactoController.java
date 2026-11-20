package com.agrovalleconnect.agrovalle_connect.controller;

import com.agrovalleconnect.agrovalle_connect.dto.MensajeContactoRequest;
import com.agrovalleconnect.agrovalle_connect.dto.MensajeContactoResponse;
import com.agrovalleconnect.agrovalle_connect.service.ContactoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contacto")
@RequiredArgsConstructor
public class ContactoController {

    private final ContactoService contactoService;

    /**
     * HU-05: el comprador autenticado deja un mensaje al agricultor de un producto -> 200.
     * El comprador sale siempre del token, nunca del cuerpo. Contrato en
     * docs/contrato-contacto-hu05.md.
     */
    @PostMapping("/mensaje")
    public ResponseEntity<MensajeContactoResponse> enviar(
            @Valid @RequestBody MensajeContactoRequest request, Authentication authentication) {
        return ResponseEntity.ok(contactoService.enviar(authentication.getName(), request));
    }
}
