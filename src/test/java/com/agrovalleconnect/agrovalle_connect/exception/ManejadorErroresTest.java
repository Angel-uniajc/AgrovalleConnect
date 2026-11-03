package com.agrovalleconnect.agrovalle_connect.exception;

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
}
