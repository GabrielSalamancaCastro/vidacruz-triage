package com.vidacruz.triage.domain.service;

import com.vidacruz.triage.domain.model.NivelTriaje;
import com.vidacruz.triage.domain.model.QuejaAdministrativa;
import com.vidacruz.triage.domain.model.SignosVitales;

public class ClasificadorTriaje {
    public NivelTriaje clasificar(SignosVitales signos, String quejaPrincipal) {
        if (esCritico(signos)) {
            return NivelTriaje.CRITICO;
        }
        if (esUrgente(signos)) {
            return NivelTriaje.URGENTE;
        }
        if (esUrgente2(signos)) {
            return NivelTriaje.URGENTE_2;
        }

        return QuejaAdministrativa.esAdministrativa(quejaPrincipal)
                ? NivelTriaje.NO_URGENTE_2
                : NivelTriaje.NO_URGENTE;
    }

    /** Nivel 1: FC < 40 o > 120, SpO2 < 90, P.Sist < 90, GCS <= 8. */
    private boolean esCritico(SignosVitales sv) {
        return sv.frecuenciaCardiaca() < 40
                || sv.frecuenciaCardiaca() > 120
                || sv.spo2() < 90
                || sv.presionSistolica() < 90
                || sv.gcs() <= 8;
    }

    /** Nivel 2: 90<=SpO2<=93, 90<=P.Sist<=100, Temp>=38.5, 9<=GCS<=13. */
    private boolean esUrgente(SignosVitales sv) {
        return (sv.spo2() >= 90 && sv.spo2() <= 93)
                || (sv.presionSistolica() >= 90 && sv.presionSistolica() <= 100)
                || sv.temperatura() >= 38.5
                || (sv.gcs() >= 9 && sv.gcs() <= 13);
    }

    /** Nivel 3: 90<=FC<=120, 38.0<=Temp<38.5, Dolor>=7. */
    private boolean esUrgente2(SignosVitales sv) {
        return (sv.frecuenciaCardiaca() >= 90 && sv.frecuenciaCardiaca() <= 120)
                || (sv.temperatura() >= 38.0 && sv.temperatura() < 38.5)
                || sv.dolor() >= 7;
    }
}
