package com.vidacruz.asistente.domain.model;

public record Fuente(String protocolo, String seccion) {
    public Fuente {
        if (protocolo == null || protocolo.isBlank()) {
            throw new IllegalArgumentException("protocolo es obligatorio");
        }
        if (seccion == null || seccion.isBlank()) {
            throw new IllegalArgumentException("seccion es obligatoria");
        }
    }
}
