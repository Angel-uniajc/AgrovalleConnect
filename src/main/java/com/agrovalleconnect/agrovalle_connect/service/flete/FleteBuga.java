package com.agrovalleconnect.agrovalle_connect.service.flete;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class FleteBuga implements EstrategiaFlete {

    @Override
    public String municipio() {
        return "Buga";
    }

    @Override
    public BigDecimal tarifa() {
        return BigDecimal.ZERO;
    }
}
