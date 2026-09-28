package com.agrovalleconnect.agrovalle_connect.controller;

import com.agrovalleconnect.agrovalle_connect.dto.RegistroAgricultorRequest;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroAgricultorResponse;
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

  @PostMapping("/register")
  public ResponseEntity<RegistroAgricultorResponse> registrar(
      @Valid @RequestBody RegistroAgricultorRequest request) {

    RegistroAgricultorResponse respuesta = new RegistroAgricultorResponse(
        request.nombre(), "Agricultor registrado correctamente");

    return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
  }
}