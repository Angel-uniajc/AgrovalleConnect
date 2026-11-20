package com.agrovalleconnect.agrovalle_connect.controller;

import com.agrovalleconnect.agrovalle_connect.dto.LoginRequest;
import com.agrovalleconnect.agrovalle_connect.dto.LoginResponse;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroAgricultorRequest;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroCompradorRequest;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroRequest;
import com.agrovalleconnect.agrovalle_connect.service.AgricultorRegisterService;
import com.agrovalleconnect.agrovalle_connect.service.AutenticacionService;
import com.agrovalleconnect.agrovalle_connect.service.CompradorRegisterService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AgricultorRegisterService agricultorService;

    private final AutenticacionService autenticacionService;

    private final CompradorRegisterService compradorService;

    public AuthController(AgricultorRegisterService agricultorService,
                          AutenticacionService autenticacionService,
                          CompradorRegisterService compradorService) {
        this.agricultorService = agricultorService;
        this.autenticacionService = autenticacionService;
        this.compradorService = compradorService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(switch (request) {
            case RegistroAgricultorRequest agricultor -> agricultorService.registrar(agricultor);
            case RegistroCompradorRequest comprador -> compradorService.registrar(comprador);
        });
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(autenticacionService.login(request));
    }
}
