package com.agrovalleconnect.agrovalle_connect.exception;

import java.math.BigDecimal;

public class StockInsuficienteException extends RuntimeException {

    private final Long idProducto;

    private final BigDecimal cantidadSolicitada;

    private final BigDecimal stockDisponible;

    public StockInsuficienteException(Long idProducto, BigDecimal cantidadSolicitada, BigDecimal stockDisponible) {
        super("Stock insuficiente");
        this.idProducto = idProducto;
        this.cantidadSolicitada = cantidadSolicitada;
        this.stockDisponible = stockDisponible;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public BigDecimal getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public BigDecimal getStockDisponible() {
        return stockDisponible;
    }
}
