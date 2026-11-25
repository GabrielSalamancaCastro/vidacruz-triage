package com.vidacruz.triage.domain.service;

import com.vidacruz.triage.domain.model.NivelTriaje;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SlaCalculatorTest {
    private final SlaCalculator calculadora = new SlaCalculator();

    @Test
    void critico_tiene_sla_de_5_minutos() {
        assertThat(calculadora.calcularSlaMinutos(NivelTriaje.CRITICO)).isEqualTo(5);
    }

    @Test
    void urgente_tiene_sla_de_10_minutos() {
        assertThat(calculadora.calcularSlaMinutos(NivelTriaje.URGENTE)).isEqualTo(10);
    }

    @Test
    void urgente_2_tiene_sla_de_30_minutos() {
        assertThat(calculadora.calcularSlaMinutos(NivelTriaje.URGENTE_2)).isEqualTo(30);
    }

    @Test
    void no_urgente_tiene_sla_de_60_minutos() {
        assertThat(calculadora.calcularSlaMinutos(NivelTriaje.NO_URGENTE)).isEqualTo(60);
    }

    @Test
    void no_urgente_2_tiene_sla_de_120_minutos() {
        assertThat(calculadora.calcularSlaMinutos(NivelTriaje.NO_URGENTE_2)).isEqualTo(120);
    }

    @Test
    void nivel_nulo_lanza_excepcion() {
        assertThatThrownBy(() -> calculadora.calcularSlaMinutos(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
