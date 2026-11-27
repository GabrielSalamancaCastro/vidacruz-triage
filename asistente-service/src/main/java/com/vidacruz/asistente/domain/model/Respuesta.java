package com.vidacruz.asistente.domain.model;

import java.util.List;

public record Respuesta(
        String respuesta,
        List<Fuente> fuentes
) {
    public static final String MENSAJE_SIN_PROTOCOLO =
            "No existe un protocolo disponible en el corpus oficial para responder a esta pregunta.";

    public Respuesta {
        if (respuesta == null || respuesta.isBlank()) {
            throw new IllegalArgumentException("respuesta es obligatoria");
        }
        if (fuentes == null) {
            throw new IllegalArgumentException("fuentes no puede ser null (usar List.of())");
        }
    }

    public static Respuesta sinProtocolo() {
        return new Respuesta(MENSAJE_SIN_PROTOCOLO, List.of());
    }

    public boolean tieneFuentes() {
        return !fuentes.isEmpty();
    }
}
