package com.vidacruz.triage.domain.model;

import com.vidacruz.triage.domain.exception.DominioException;

public record SignosVitales(
        int frecuenciaCardiaca,
        int presionSistolica,
        int presionDiastolica,
        int spo2,
        double temperatura,
        int dolor,
        int gcs
) {
    public SignosVitales {
        validarRango("frecuenciaCardiaca", frecuenciaCardiaca, 20, 250);
        validarRango("presionSistolica", presionSistolica, 60, 260);
        validarRango("presionDiastolica", presionDiastolica, 30, 160);
        validarRango("spo2", spo2, 70, 100);
        validarRango("temperatura", temperatura, 34.0, 42.0);
        validarRango("dolor", dolor, 0, 10);
        validarRango("gcs", gcs, 3, 15);

        // Regla cruzada: diastólica siempre menor que sistólica
        if (presionDiastolica >= presionSistolica) {
            throw new DominioException(
                    "presionDiastolica",
                    "La presión diastólica (" + presionDiastolica +
                            ") debe ser menor que la sistólica (" + presionSistolica + ")"
            );
        }
    }

    private static void validarRango(String campo, int valor, int min, int max) {
        if (valor < min || valor > max) {
            throw new DominioException(campo,
                    "El campo " + campo + " debe estar entre " + min + " y " + max +
                            " (valor recibido: " + valor + ")");
        }
    }

    private static void validarRango(String campo, double valor, double min, double max) {
        if (valor < min || valor > max) {
            throw new DominioException(campo,
                    "El campo " + campo + " debe estar entre " + min + " y " + max +
                            " (valor recibido: " + valor + ")");
        }
    }
}
