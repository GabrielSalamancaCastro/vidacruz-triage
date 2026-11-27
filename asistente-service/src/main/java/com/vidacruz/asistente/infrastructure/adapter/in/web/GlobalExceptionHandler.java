package com.vidacruz.asistente.infrastructure.adapter.in.web;

import com.vidacruz.asistente.domain.exception.LlmNoDisponibleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LlmNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> handleLlmNoDisponible(LlmNoDisponibleException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "timestamp", Instant.now().toString(),
                "error", "ASISTENTE_NO_DISPONIBLE",
                "mensaje", ex.getMessage()
        ));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(WebExchangeBindException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", Instant.now().toString(),
                "error", "VALIDACION_REQUEST",
                "mensaje", "La pregunta es obligatoria"
        ));
    }
}
