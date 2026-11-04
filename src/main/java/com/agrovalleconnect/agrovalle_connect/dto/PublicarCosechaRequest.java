package com.agrovalleconnect.agrovalle_connect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PublicarCosechaRequest(

        @NotNull(message = "La finca es obligatoria")
        @JsonProperty("finca_id")
        Long fincaId,

        @NotNull(message = "La categoría es obligatoria")
        @JsonProperty("categoria_id")
        Long categoriaId,

        @NotBlank(message = "El tipo de producto es obligatorio")
        @Size(max = 80, message = "El tipo de producto no puede superar 80 caracteres")
        String tipo,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        @Digits(integer = 10, fraction = 2, message = "La cantidad admite máximo 10 enteros y 2 decimales")
        BigDecimal cantidad,

        @NotNull(message = "El precio unitario es obligatorio")
        @Positive(message = "El precio unitario debe ser mayor que cero")
        @Digits(integer = 10, fraction = 2, message = "El precio unitario admite máximo 10 enteros y 2 decimales")
        @JsonProperty("precio_unitario")
        BigDecimal precioUnitario,

        @NotNull(message = "La fecha de cosecha es obligatoria")
        @FutureOrPresent(message = "La fecha de cosecha no puede ser anterior a hoy")
        @JsonProperty("fecha_cosecha")
        LocalDate fechaCosecha

) {
}