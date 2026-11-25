package com.agrovalleconnect.agrovalle_connect.service.flete;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class FleteBuga implements EstrategiaFlete {

    private static final BigDecimal TARIFA = new BigDecimal("15000.00");

    @Override
    public String municipio() {
        return "Buga";
    }

    @Override
    public BigDecimal tarifa() {
        return TARIFA;
    }
}
