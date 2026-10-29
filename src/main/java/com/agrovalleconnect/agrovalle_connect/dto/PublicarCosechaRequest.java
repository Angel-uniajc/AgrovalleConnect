package com.agrovalleconnect.agrovalle_connect.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PublicarCosechaRequest(
        Long fincaId,
        Long categoriaId,
        String tipo,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        LocalDate fechaCosecha
) {
}