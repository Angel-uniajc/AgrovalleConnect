package com.agrovalleconnect.agrovalle_connect.exception;

public class ProductoAgotadoException extends RuntimeException {

    private final Long idProducto;

    public ProductoAgotadoException(Long idProducto) {
        super("Producto agotado");
        this.idProducto = idProducto;
    }

    public Long getIdProducto() {
        return idProducto;
    }
}
