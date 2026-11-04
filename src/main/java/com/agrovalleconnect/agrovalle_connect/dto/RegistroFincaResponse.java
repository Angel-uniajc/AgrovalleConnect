package com.agrovalleconnect.agrovalle_connect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record RegistroFincaResponse(
        Long id,
        String nombre,
        String municipio,
        String vereda,
        @JsonProperty("area_hectareas")
        BigDecimal areaHectareas
) {
}