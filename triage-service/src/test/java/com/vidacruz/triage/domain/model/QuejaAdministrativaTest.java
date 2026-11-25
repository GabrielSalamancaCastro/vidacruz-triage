package com.vidacruz.triage.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class QuejaAdministrativaTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "documentos",
            "recolección de resultados",
            "renovación de recetario",
            "vacunación",
            "seguimiento"
    })
    void las_cinco_quejas_del_enunciado_son_administrativas(String queja) {
        assertThat(QuejaAdministrativa.esAdministrativa(queja)).isTrue();
    }

    @Test
    void es_case_insensitive() {
        assertThat(QuejaAdministrativa.esAdministrativa("DOCUMENTOS")).isTrue();
        assertThat(QuejaAdministrativa.esAdministrativa("Renovación De Recetario")).isTrue();
    }

    @Test
    void tolera_espacios_en_los_extremos() {
        assertThat(QuejaAdministrativa.esAdministrativa("  vacunación  ")).isTrue();
    }

    @Test
    void null_no_es_administrativa() {
        assertThat(QuejaAdministrativa.esAdministrativa(null)).isFalse();
    }

    @Test
    void texto_vacio_no_es_administrativa() {
        assertThat(QuejaAdministrativa.esAdministrativa("")).isFalse();
        assertThat(QuejaAdministrativa.esAdministrativa("   ")).isFalse();
    }

    @Test
    void queja_clinica_no_es_administrativa() {
        assertThat(QuejaAdministrativa.esAdministrativa("Dolor torácico")).isFalse();
        assertThat(QuejaAdministrativa.esAdministrativa("Fiebre")).isFalse();
    }

    @Test
    void coincidencia_parcial_no_cuenta() {
        assertThat(QuejaAdministrativa.esAdministrativa("documentos pendientes")).isFalse();
        assertThat(QuejaAdministrativa.esAdministrativa("necesito vacunación")).isFalse();
    }
}
