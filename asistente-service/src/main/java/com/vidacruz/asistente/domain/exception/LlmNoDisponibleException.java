package com.vidacruz.asistente.domain.exception;

public class LlmNoDisponibleException extends RuntimeException{
    public LlmNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
