package com.agrovalleconnect.agrovalle_connect.service.flete;

import java.math.BigDecimal;

public abstract class FletePorMunicipio implements EstrategiaFlete {

    private final String municipio;

    private final BigDecimal tarifa;

    protected FletePorMunicipio(String municipio, String tarifa) {
        this.municipio = municipio;
        this.tarifa = new BigDecimal(tarifa);
    }

    @Override
    public String municipio() {
        return municipio;
    }

    @Override
    public BigDecimal tarifa() {
        return tarifa;
    }
}
