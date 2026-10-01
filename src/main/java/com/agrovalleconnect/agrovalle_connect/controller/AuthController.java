package com.agrovalleconnect.agrovalle_connect.controller;

import com.agrovalleconnect.agrovalle_connect.dto.RegistroAgricultorRequest;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroAgricultorResponse;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.service.AgricultorRegisterService;

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

  public AuthController(AgricultorRegisterService agricultorService) {
    this.agricultorService = agricultorService;
  }

  @PostMapping("/register")
  public ResponseEntity<RegistroAgricultorResponse> registrar(
      @Valid @RequestBody RegistroAgricultorRequest request) {

        Agricultor agricultor = agricultorService.registrar(request);

    RegistroAgricultorResponse respuesta = new RegistroAgricultorResponse(
        agricultor.getUsuario().getNombre(),
      "Agricultor registrado exitosamente");

    return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
  }
}