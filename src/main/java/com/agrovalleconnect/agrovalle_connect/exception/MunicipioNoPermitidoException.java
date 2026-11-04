package com.agrovalleconnect.agrovalle_connect.exception;

public class MunicipioNoPermitidoException extends RuntimeException {

    public MunicipioNoPermitidoException(String municipio) {
        super("El municipio no está permitido: " + municipio);
    }
}
