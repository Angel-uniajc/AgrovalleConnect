package com.agrovalleconnect.agrovalle_connect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record RegistroFincaRequest(

        @NotBlank(message = "El nombre de la finca es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @NotBlank(message = "El municipio es obligatorio")
        @Size(max = 80, message = "El municipio no puede superar 80 caracteres")
        String municipio,

        @Size(max = 100, message = "La vereda no puede superar 100 caracteres")
        String vereda,

        @Positive(message = "El área debe ser mayor que cero")
        @Digits(integer = 8, fraction = 2, message = "El área admite máximo 8 enteros y 2 decimales")
        @JsonProperty("area_hectareas")
        BigDecimal areaHectareas

) {
}
