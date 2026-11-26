package com.vidacruz.triage.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarPacienteRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre completo debe tener entre 3 y 100 caracteres")
        String nombreCompleto,

        @NotNull(message = "La edad es obligatoria")
        @Min(value = 0, message = "La edad debe ser mayor o igual a 0")
        @Max(value = 120, message = "La edad debe ser menor o igual a 120")
        Integer edad,

        @NotNull(message = "La frecuencia cardíaca es obligatoria")
        @Min(value = 20, message = "La frecuencia cardíaca debe ser mayor o igual a 20 bpm")
        @Max(value = 250, message = "La frecuencia cardíaca debe ser menor o igual a 250 bpm")
        Integer frecuenciaCardiaca,

        @NotNull(message = "La presión sistólica es obligatoria")
        @Min(value = 60, message = "La presión sistólica debe ser mayor o igual a 60 mmHg")
        @Max(value = 260, message = "La presión sistólica debe ser menor o igual a 260 mmHg")
        Integer presionSistolica,

        @NotNull(message = "La presión diastólica es obligatoria")
        @Min(value = 30, message = "La presión diastólica debe ser mayor o igual a 30 mmHg")
        @Max(value = 160, message = "La presión diastólica debe ser menor o igual a 160 mmHg")
        Integer presionDiastolica,

        @NotNull(message = "El SpO2 es obligatorio")
        @Min(value = 70, message = "El SpO2 debe ser mayor o igual a 70 %")
        @Max(value = 100, message = "El SpO2 debe ser menor o igual a 100 %")
        Integer spo2,

        @NotNull(message = "La temperatura es obligatoria")
        @Min(value = 34, message = "La temperatura debe ser mayor o igual a 34.0 °C")
        @Max(value = 42, message = "La temperatura debe ser menor o igual a 42.0 °C")
        Double temperatura,

        @NotNull(message = "El dolor es obligatorio")
        @Min(value = 0, message = "El dolor debe ser mayor o igual a 0")
        @Max(value = 10, message = "El dolor debe ser menor o igual a 10")
        Integer dolor,

        @NotNull(message = "El GCS es obligatorio")
        @Min(value = 3, message = "El GCS debe ser mayor o igual a 3")
        @Max(value = 15, message = "El GCS debe ser menor o igual a 15")
        Integer gcs,

        @NotBlank(message = "La queja principal es obligatoria")
        @Size(max = 500, message = "La queja principal no debe exceder 500 caracteres")
        String quejaPrincipal
) {
}
