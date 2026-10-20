package com.agrovalleconnect.agrovalle_connect.exception;

public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Credenciales inválidas");
    }
}