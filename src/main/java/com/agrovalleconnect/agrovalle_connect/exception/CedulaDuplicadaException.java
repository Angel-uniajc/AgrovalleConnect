package com.agrovalleconnect.agrovalle_connect.exception;

public class CedulaDuplicadaException extends RecursoDuplicadoException {

    public CedulaDuplicadaException() {
        super("La cédula ya está registrada");
    }
}
