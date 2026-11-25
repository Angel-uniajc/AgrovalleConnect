package com.agrovalleconnect.agrovalle_connect.exception;

public class MunicipioSinTarifaException extends RuntimeException {

    public MunicipioSinTarifaException() {
        super("Municipio sin tarifa de flete");
    }
}
