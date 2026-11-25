package com.agrovalleconnect.agrovalle_connect.service.flete;

import com.agrovalleconnect.agrovalle_connect.exception.MunicipioSinTarifaException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CalculadoraFlete {

    private final Map<String, EstrategiaFlete> estrategiasPorMunicipio;

    public CalculadoraFlete(List<EstrategiaFlete> estrategias) {
        this.estrategiasPorMunicipio = estrategias.stream()
                .collect(Collectors.toMap(EstrategiaFlete::municipio, Function.identity()));
    }

    public BigDecimal calcular(String municipio) {
        EstrategiaFlete estrategia = estrategiasPorMunicipio.get(municipio);
        if (estrategia == null) {
            throw new MunicipioSinTarifaException();
        }
        return estrategia.tarifa();
    }

    public BigDecimal calcularTotal(Collection<String> municipios) {
        return municipios.stream()
                .distinct()
                .map(this::calcular)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
