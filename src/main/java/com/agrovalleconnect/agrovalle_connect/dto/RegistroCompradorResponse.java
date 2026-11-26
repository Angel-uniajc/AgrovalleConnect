package com.agrovalleconnect.agrovalle_connect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * HU-17: respuesta 201 del registro de comprador.
 * No incluye password, hash, documento ni la entidad JPA.
 */
public record RegistroCompradorResponse(
        String nombre,
        String rol,
        @JsonProperty("tipo_comprador") String tipoComprador,
        String mensaje
) {
}
