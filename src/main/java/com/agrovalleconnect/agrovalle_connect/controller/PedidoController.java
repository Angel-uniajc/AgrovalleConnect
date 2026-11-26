package com.agrovalleconnect.agrovalle_connect.controller;

import com.agrovalleconnect.agrovalle_connect.dto.CrearPedidoRequest;
import com.agrovalleconnect.agrovalle_connect.dto.PedidoResponse;
import com.agrovalleconnect.agrovalle_connect.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HU-12: entrada HTTP de pedidos; la identidad del comprador procede del JWT. */
@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponse> crear(
            @Valid @RequestBody CrearPedidoRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.crear(authentication.getName(), request));
    }
}
