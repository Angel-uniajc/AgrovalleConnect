package com.agrovalleconnect.agrovalle_connect.exception;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManejadorErroresTest {

    private final ManejadorErrores manejador = new ManejadorErrores();

    @Test
    void recursoNoEncontrado_retorna404ConMensaje() {
        ResponseEntity<String> respuesta =
                manejador.recursoNoEncontrado(new RecursoNoEncontradoException("Finca no encontrada"));

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Finca no encontrada", respuesta.getBody());
    }

    @Test
    void accesoDenegado_retorna403ConMensaje() {
        ResponseEntity<String> respuesta =
                manejador.accesoDenegado(new AccesoDenegadoException("La finca no pertenece al agricultor"));

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
        assertEquals("La finca no pertenece al agricultor", respuesta.getBody());
    }

    @Test
    void categoriaInvalida_retorna400ConMensaje() {
        ResponseEntity<String> respuesta =
                manejador.categoriaInvalida(new CategoriaInvalidaException("Categoría inválida"));

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("Categoría inválida", respuesta.getBody());
    }

    @Test
    void stockInsuficiente_retorna400ConLosCuatroCamposDelContrato() {
        ResponseEntity<Map<String, Object>> respuesta = manejador.stockInsuficiente(
                new StockInsuficienteException(12L, new BigDecimal("16.00"), new BigDecimal("10.00")));

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("Stock insuficiente", respuesta.getBody().get("error"));
        assertEquals(12L, respuesta.getBody().get("id_producto"));
        assertEquals(new BigDecimal("16.00"), respuesta.getBody().get("cantidad_solicitada"));
        assertEquals(new BigDecimal("10.00"), respuesta.getBody().get("stock_disponible"));
    }

    @Test
    void stockInsuficiente_conservaElOrdenDeLasClavesDelContrato() {
        ResponseEntity<Map<String, Object>> respuesta = manejador.stockInsuficiente(
                new StockInsuficienteException(12L, new BigDecimal("16.00"), new BigDecimal("10.00")));

        List<String> claves = new ArrayList<>(respuesta.getBody().keySet());

        assertEquals(List.of("error", "id_producto", "cantidad_solicitada", "stock_disponible"), claves);
    }

    @Test
    void productoAgotado_retorna400ConErrorEIdProducto() {
        ResponseEntity<Map<String, Object>> respuesta =
                manejador.productoAgotado(new ProductoAgotadoException(12L));

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("Producto agotado", respuesta.getBody().get("error"));
        assertEquals(12L, respuesta.getBody().get("id_producto"));
    }

    @Test
    void productoAgotado_noIncluyeCamposDeStock() {
        ResponseEntity<Map<String, Object>> respuesta =
                manejador.productoAgotado(new ProductoAgotadoException(12L));

        assertEquals(List.of("error", "id_producto"), new ArrayList<>(respuesta.getBody().keySet()));
    }
}
