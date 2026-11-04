package com.agrovalleconnect.agrovalle_connect.controller;

import com.agrovalleconnect.agrovalle_connect.dto.RegistroFincaRequest;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroFincaResponse;
import com.agrovalleconnect.agrovalle_connect.service.FincaService;
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
@RequestMapping("/api/v1/fincas")
@RequiredArgsConstructor
public class FincaController {

    private final FincaService fincaService;

    @PostMapping
    public ResponseEntity<RegistroFincaResponse> registrar(
            @Valid @RequestBody RegistroFincaRequest request, Authentication authentication) {
        RegistroFincaResponse respuesta = fincaService.registrar(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}