package com.agrovalleconnect.agrovalle_connect.controller;

import com.agrovalleconnect.agrovalle_connect.dto.PublicarCosechaRequest;
import com.agrovalleconnect.agrovalle_connect.dto.PublicarCosechaResponse;
import com.agrovalleconnect.agrovalle_connect.service.CosechaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final CosechaService cosechaService;

    /**
     * HU-02: el agricultor autenticado publica una cosecha en una de sus fincas -> 201 con el ID.
     */
    @PostMapping
    public ResponseEntity<PublicarCosechaResponse> publicar(
            @Valid @RequestBody PublicarCosechaRequest request, Authentication authentication) {
        PublicarCosechaResponse respuesta = cosechaService.publicar(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}
