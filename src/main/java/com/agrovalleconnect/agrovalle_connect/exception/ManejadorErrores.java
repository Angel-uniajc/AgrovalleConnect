package com.agrovalleconnect.agrovalle_connect.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<String> recursoDuplicado(RecursoDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    /**
     * HU-16: email inexistente o contraseña incorrecta -> 401 con mensaje genérico.
     */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<String> credencialesInvalidas(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(MunicipioNoPermitidoException.class)
    public ResponseEntity<String> municipioNoPermitido(MunicipioNoPermitidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * HU-02: la finca indicada no existe -> 404.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<String> recursoNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * HU-02: la finca no pertenece al agricultor autenticado -> 403.
     */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<String> accesoDenegado(AccesoDenegadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    /**
     * HU-02: la categoría indicada no existe -> 400.
     */
    @ExceptionHandler(CategoriaInvalidaException.class)
    public ResponseEntity<String> categoriaInvalida(CategoriaInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Datos de entrada que no cumplen las validaciones del DTO (@Valid) -> 400.
     * Devuelve cada campo con su mensaje, por ejemplo {"email": "must not be blank"}.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> datosInvalidos(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }
}
