package com.agrovalleconnect.agrovalle_connect.exception;

/**
 * HU-17: otro comprador ya usa ese documento -> 409 Conflict.
 */
public class DocumentoDuplicadoException extends RecursoDuplicadoException {

    public DocumentoDuplicadoException() {
        super("El documento ya está registrado");
    }
}
