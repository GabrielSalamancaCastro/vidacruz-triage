package com.vidacruz.triage.infrastructure.adapter.in.web;

import com.vidacruz.triage.domain.exception.DominioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DominioException.class)
    public ResponseEntity<Map<String, Object>> handleDominio(DominioException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", Instant.now().toString(),
                "error", "VALIDACION_DOMINIO",
                "campo", ex.getCampo() != null ? ex.getCampo() : "",
                "mensaje", ex.getMessage()
        ));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(WebExchangeBindException ex) {
        List<Map<String, String>> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "campo", fe.getField(),
                        "mensaje", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : ""
                ))
                .toList();
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", Instant.now().toString(),
                "error", "VALIDACION_REQUEST",
                "errores", errores
        ));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<Map<String, Object>> handleInputInvalido(ServerWebInputException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", Instant.now().toString(),
                "error", "JSON_INVALIDO",
                "mensaje", "El cuerpo de la petición no es JSON válido"
        ));
    }
}
