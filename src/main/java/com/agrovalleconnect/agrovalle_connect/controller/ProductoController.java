package com.agrovalleconnect.agrovalle_connect.controller;

import com.agrovalleconnect.agrovalle_connect.dto.OfertaCatalogoResponse;
import com.agrovalleconnect.agrovalle_connect.dto.PublicarCosechaRequest;
import com.agrovalleconnect.agrovalle_connect.dto.PublicarCosechaResponse;
import com.agrovalleconnect.agrovalle_connect.service.CatalogoService;
import com.agrovalleconnect.agrovalle_connect.service.CosechaService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final CosechaService cosechaService;

    private final CatalogoService catalogoService;

    /**
     * HU-04: cualquier usuario autenticado consulta las ofertas disponibles, con filtros
     * opcionales por municipio y categoría -> 200 con un arreglo (vacío si no hay coincidencias).
     */
    @GetMapping
    public ResponseEntity<List<OfertaCatalogoResponse>> consultar(
            @RequestParam(required = false) String municipio,
            @RequestParam(required = false) String categoria) {
        return ResponseEntity.ok(catalogoService.consultar(municipio, categoria));
    }

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
