package com.agrovalleconnect.agrovalle_connect.exception;

/**
 * HU-05: el producto existe pero no está ACTIVO o no tiene cantidad disponible -> 400.
 */
public class ProductoNoDisponibleException extends RuntimeException {
    public ProductoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
