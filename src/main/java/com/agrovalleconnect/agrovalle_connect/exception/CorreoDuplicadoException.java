package com.agrovalleconnect.agrovalle_connect.exception;

public class CorreoDuplicadoException extends RecursoDuplicadoException {

    public CorreoDuplicadoException() {
        super("El correo ya está registrado");
    }
}
