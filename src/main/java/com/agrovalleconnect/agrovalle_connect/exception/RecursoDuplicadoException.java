package com.agrovalleconnect.agrovalle_connect.exception;

/**
 * Clase base de todos los errores "este recurso ya existe".
 * El manejador de errores la usa para responder 409.
 */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
