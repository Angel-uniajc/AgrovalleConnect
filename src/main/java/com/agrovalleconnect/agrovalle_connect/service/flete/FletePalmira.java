package com.agrovalleconnect.agrovalle_connect.service.flete;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class FletePalmira implements EstrategiaFlete {

    @Override
    public String municipio() {
        return "Palmira";
    }

    @Override
    public BigDecimal tarifa() {
        return BigDecimal.ZERO;
    }
}
