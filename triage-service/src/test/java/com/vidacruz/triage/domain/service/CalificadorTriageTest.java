package com.vidacruz.triage.domain.service;

import com.vidacruz.triage.domain.model.NivelTriaje;
import com.vidacruz.triage.domain.model.SignosVitales;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CalificadorTriageTest {

    private final ClasificadorTriaje clasificador = new ClasificadorTriaje();

    // ============================================================
    // E1–E6 del enunciado (contrato oficial)
    // ============================================================

    @Test
    void e1_dolor_toracico_con_fc_alta_es_critico() {
        // FC=130 (>120 → CRITICO), Dolor=8 (≥7 → URGENTE_2). Gana CRITICO.
        var sv = new SignosVitales(130, 180, 110, 97, 36.8, 8, 15);
        assertThat(clasificador.clasificar(sv, "Dolor torácico"))
                .isEqualTo(NivelTriaje.CRITICO);
    }

    @Test
    void e2_spo2_baja_es_urgente() {
        // SpO2=91 (90≤SpO2≤93 → URGENTE). Nada de nivel 1.
        var sv = new SignosVitales(95, 150, 95, 91, 37.0, 4, 15);
        assertThat(clasificador.clasificar(sv, "Dificultad para respirar"))
                .isEqualTo(NivelTriaje.URGENTE);
    }

    @Test
    void e3_fiebre_moderada_es_urgente_2() {
        // Temp=38.2 (38.0≤T<38.5 → URGENTE_2). Nada de nivel 1 ni 2.
        var sv = new SignosVitales(88, 130, 85, 98, 38.2, 6, 15);
        assertThat(clasificador.clasificar(sv, "Fiebre"))
                .isEqualTo(NivelTriaje.URGENTE_2);
    }

    @Test
    void e4_herida_sin_gravedad_es_no_urgente() {
        // Todo normal → NO_URGENTE (nivel 4). Queja no administrativa.
        var sv = new SignosVitales(72, 120, 80, 99, 36.5, 2, 15);
        assertThat(clasificador.clasificar(sv, "Herida por cortar"))
                .isEqualTo(NivelTriaje.NO_URGENTE);
    }

    @Test
    void e5_renovacion_recetario_es_no_urgente_2() {
        // Todo normal → NO_URGENTE, pero queja administrativa → NO_URGENTE_2.
        var sv = new SignosVitales(70, 115, 75, 99, 36.4, 0, 15);
        assertThat(clasificador.clasificar(sv, "Renovación de recetario"))
                .isEqualTo(NivelTriaje.NO_URGENTE_2);
    }

    @Test
    void e6_inconsciente_es_critico() {
        // FC=35 (<40 → CRITICO), SpO2=85 (<90 → CRITICO), GCS=6 (≤8 → CRITICO).
        var sv = new SignosVitales(35, 80, 50, 85, 35.9, 0, 6);
        assertThat(clasificador.clasificar(sv, "Inconsciente"))
                .isEqualTo(NivelTriaje.CRITICO);
    }

    // ============================================================
    // Tests de límites (los bordes donde se suele fallar)
    // ============================================================

    @Test
    void fc_120_no_es_critico_pero_si_urgente_2() {
        // FC=120 NO es >120, pero sí 90≤FC≤120 → URGENTE_2.
        var sv = new SignosVitales(120, 120, 80, 99, 36.5, 0, 15);
        assertThat(clasificador.clasificar(sv, "Control"))
                .isEqualTo(NivelTriaje.URGENTE_2);
    }

    @Test
    void fc_121_es_critico() {
        var sv = new SignosVitales(121, 120, 80, 99, 36.5, 0, 15);
        assertThat(clasificador.clasificar(sv, "Control"))
                .isEqualTo(NivelTriaje.CRITICO);
    }

    @Test
    void spo2_90_es_urgente() {
        // 90≤SpO2≤93 → URGENTE (no CRITICO, que es <90).
        var sv = new SignosVitales(80, 120, 80, 90, 36.5, 0, 15);
        assertThat(clasificador.clasificar(sv, "Control"))
                .isEqualTo(NivelTriaje.URGENTE);
    }

    @Test
    void spo2_89_es_critico() {
        var sv = new SignosVitales(80, 120, 80, 89, 36.5, 0, 15);
        assertThat(clasificador.clasificar(sv, "Control"))
                .isEqualTo(NivelTriaje.CRITICO);
    }

    @Test
    void gcs_8_es_critico() {
        var sv = new SignosVitales(80, 120, 80, 99, 36.5, 0, 8);
        assertThat(clasificador.clasificar(sv, "Control"))
                .isEqualTo(NivelTriaje.CRITICO);
    }

    @Test
    void gcs_9_es_urgente() {
        // 9≤GCS≤13 → URGENTE.
        var sv = new SignosVitales(80, 120, 80, 99, 36.5, 0, 9);
        assertThat(clasificador.clasificar(sv, "Control"))
                .isEqualTo(NivelTriaje.URGENTE);
    }

    @Test
    void temperatura_38_5_es_urgente() {
        var sv = new SignosVitales(80, 120, 80, 99, 38.5, 0, 15);
        assertThat(clasificador.clasificar(sv, "Control"))
                .isEqualTo(NivelTriaje.URGENTE);
    }

    @Test
    void temperatura_38_0_es_urgente_2() {
        var sv = new SignosVitales(80, 120, 80, 99, 38.0, 0, 15);
        assertThat(clasificador.clasificar(sv, "Control"))
                .isEqualTo(NivelTriaje.URGENTE_2);
    }

    @Test
    void presion_sistolica_90_es_urgente() {
        // 90≤P.Sist≤100 → URGENTE.
        var sv = new SignosVitales(80, 90, 60, 99, 36.5, 0, 15);
        assertThat(clasificador.clasificar(sv, "Control"))
                .isEqualTo(NivelTriaje.URGENTE);
    }

    @Test
    void presion_sistolica_89_es_critico() {
        var sv = new SignosVitales(80, 89, 60, 99, 36.5, 0, 15);
        assertThat(clasificador.clasificar(sv, "Control"))
                .isEqualTo(NivelTriaje.CRITICO);
    }

    // ============================================================
    // "Mayor gravedad gana" (varias condiciones de niveles distintos)
    // ============================================================

    @Test
    void varias_condiciones_gana_la_mas_grave() {
        // FC=130 (CRITICO) + Temp=38.6 (URGENTE) + Dolor=8 (URGENTE_2).
        // Debe ganar CRITICO.
        var sv = new SignosVitales(130, 120, 80, 99, 38.6, 8, 15);
        assertThat(clasificador.clasificar(sv, "Control"))
                .isEqualTo(NivelTriaje.CRITICO);
    }

    // ============================================================
    // Nivel 5: queja administrativa SOLO si es NO_URGENTE
    // ============================================================

    @Test
    void queja_administrativa_con_signos_normales_es_no_urgente_2() {
        var sv = new SignosVitales(72, 120, 80, 99, 36.5, 0, 15);
        assertThat(clasificador.clasificar(sv, "Documentos"))
                .isEqualTo(NivelTriaje.NO_URGENTE_2);
    }

    @Test
    void queja_administrativa_con_signos_graves_NO_es_no_urgente_2() {
        // FC=130 → CRITICO aunque la queja sea administrativa.
        var sv = new SignosVitales(130, 120, 80, 99, 36.5, 0, 15);
        assertThat(clasificador.clasificar(sv, "Documentos"))
                .isEqualTo(NivelTriaje.CRITICO);
    }

    @Test
    void queja_administrativa_es_case_insensitive_y_tolera_espacios() {
        var sv = new SignosVitales(72, 120, 80, 99, 36.5, 0, 15);
        assertThat(clasificador.clasificar(sv, "  RENOVACIÓN DE RECETARIO  "))
                .isEqualTo(NivelTriaje.NO_URGENTE_2);
    }

    @Test
    void todas_las_quejas_administrativas_mapean_a_no_urgente_2() {
        var sv = new SignosVitales(72, 120, 80, 99, 36.5, 0, 15);
        assertThat(clasificador.clasificar(sv, "documentos")).isEqualTo(NivelTriaje.NO_URGENTE_2);
        assertThat(clasificador.clasificar(sv, "recolección de resultados")).isEqualTo(NivelTriaje.NO_URGENTE_2);
        assertThat(clasificador.clasificar(sv, "renovación de recetario")).isEqualTo(NivelTriaje.NO_URGENTE_2);
        assertThat(clasificador.clasificar(sv, "vacunación")).isEqualTo(NivelTriaje.NO_URGENTE_2);
        assertThat(clasificador.clasificar(sv, "seguimiento")).isEqualTo(NivelTriaje.NO_URGENTE_2);
    }
}
