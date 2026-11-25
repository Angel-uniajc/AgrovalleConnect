package com.agrovalleconnect.agrovalle_connect.service.flete;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CalculadoraFlete {

    public CalculadoraFlete(List<EstrategiaFlete> estrategias) {
    }

    public BigDecimal calcular(String municipio) {
        return BigDecimal.ZERO;
    }

    public BigDecimal calcularTotal(Collection<String> municipios) {
        return BigDecimal.ZERO;
    }
}
