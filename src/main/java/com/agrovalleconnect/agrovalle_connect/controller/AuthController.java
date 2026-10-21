package com.agrovalleconnect.agrovalle_connect.controller;

import com.agrovalleconnect.agrovalle_connect.dto.LoginRequest;
import com.agrovalleconnect.agrovalle_connect.dto.LoginResponse;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroAgricultorRequest;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroAgricultorResponse;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.service.AgricultorRegisterService;
import com.agrovalleconnect.agrovalle_connect.service.AutenticacionService;

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

  public AuthController(AgricultorRegisterService agricultorService,
      AutenticacionService autenticacionService) {
    this.agricultorService = agricultorService;
    this.autenticacionService = autenticacionService;
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

  /**
   * HU-16: inicio de sesión.
   * 200 OK con el token si las credenciales son correctas,
   * 401 si no lo son (ManejadorErrores) y 400 si faltan datos (@Valid).
   */
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(autenticacionService.login(request));
  }
}
