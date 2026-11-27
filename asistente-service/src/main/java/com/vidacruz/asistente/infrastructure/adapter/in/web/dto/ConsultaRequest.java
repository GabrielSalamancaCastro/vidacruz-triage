package com.vidacruz.asistente.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsultaRequest(
        @NotBlank(message = "La pregunta es obligatoria")
        String pregunta
) {
}
