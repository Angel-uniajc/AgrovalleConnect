package com.agrovalleconnect.agrovalle_connect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OfertaCatalogoResponse(
        Long id,
        String tipo,
        @JsonProperty("cantidad_disponible")
        BigDecimal cantidadDisponible,
        @JsonProperty("precio_unitario")
        BigDecimal precioUnitario,
        @JsonProperty("fecha_cosecha")
        LocalDate fechaCosecha,
        String categoria,
        @JsonProperty("finca_id")
        Long fincaId,
        String municipio
) {
}
