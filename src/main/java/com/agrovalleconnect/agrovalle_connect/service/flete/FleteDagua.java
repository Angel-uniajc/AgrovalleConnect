package com.agrovalleconnect.agrovalle_connect.service.flete;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class FleteDagua implements EstrategiaFlete {

    private static final BigDecimal TARIFA = new BigDecimal("12000.00");

    @Override
    public String municipio() {
        return "Dagua";
    }

    @Override
    public BigDecimal tarifa() {
        return TARIFA;
    }
}
