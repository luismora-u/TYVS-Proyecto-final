package edu.unisabana.proyecto.delivery.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejo centralizado de errores de la capa REST.
 *
 * <p>Convierte las entradas invalidas en respuestas HTTP 400 (Bad Request),
 * evitando que se propaguen como errores internos HTTP 500.</p>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Entradas invalidas del cliente -> 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
