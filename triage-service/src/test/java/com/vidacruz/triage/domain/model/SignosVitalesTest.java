package com.vidacruz.triage.domain.model;

import com.vidacruz.triage.domain.exception.DominioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SignosVitalesTest {
    private SignosVitales validos() {
        return new SignosVitales(72, 120, 80, 99, 36.5, 2, 15);
    }

    @Test
    void signos_vitales_validos_se_crean_correctamente() {
        assertThatCode(this::validos).doesNotThrowAnyException();
    }

    @Test
    void frecuencia_cardiaca_fuera_de_rango_lanza_excepcion() {
        assertThatThrownBy(() -> new SignosVitales(19, 120, 80, 99, 36.5, 2, 15))
                .isInstanceOf(DominioException.class)
                .hasMessageContaining("frecuenciaCardiaca");
    }

    @Test
    void presion_sistolica_fuera_de_rango_lanza_excepcion() {
        assertThatThrownBy(() -> new SignosVitales(72, 261, 80, 99, 36.5, 2, 15))
                .isInstanceOf(DominioException.class)
                .satisfies(ex -> {
                    DominioException de = (DominioException) ex;
                    assertThat(de.getCampo()).isEqualTo("presionSistolica");
                });
    }

    @Test
    void presion_diastolica_mayor_que_sistolica_lanza_excepcion() {
        assertThatThrownBy(() -> new SignosVitales(72, 100, 110, 99, 36.5, 2, 15))
                .isInstanceOf(DominioException.class)
                .satisfies(ex -> {
                    DominioException de = (DominioException) ex;
                    assertThat(de.getCampo()).isEqualTo("presionDiastolica");
                });
    }

    @Test
    void spo2_fuera_de_rango_lanza_excepcion() {
        assertThatThrownBy(() -> new SignosVitales(72, 120, 80, 69, 36.5, 2, 15))
                .isInstanceOf(DominioException.class)
                .hasMessageContaining("spo2");
    }

    @Test
    void temperatura_fuera_de_rango_lanza_excepcion() {
        assertThatThrownBy(() -> new SignosVitales(72, 120, 80, 99, 42.1, 2, 15))
                .isInstanceOf(DominioException.class)
                .hasMessageContaining("temperatura");
    }

    @Test
    void dolor_fuera_de_rango_lanza_excepcion() {
        assertThatThrownBy(() -> new SignosVitales(72, 120, 80, 99, 36.5, 11, 15))
                .isInstanceOf(DominioException.class)
                .hasMessageContaining("dolor");
    }

    @Test
    void gcs_fuera_de_rango_lanza_excepcion() {
        assertThatThrownBy(() -> new SignosVitales(72, 120, 80, 99, 36.5, 2, 16))
                .isInstanceOf(DominioException.class)
                .hasMessageContaining("gcs");
    }

    @Test
    void valores_en_limites_son_validos() {
        assertThatCode(() -> new SignosVitales(20, 60, 30, 70, 34.0, 0, 3))
                .doesNotThrowAnyException();
        assertThatCode(() -> new SignosVitales(250, 260, 160, 100, 42.0, 10, 15))
                .doesNotThrowAnyException();
    }

    @Test
    void el_campo_de_la_excepcion_es_el_campo_invalido() {
        try {
            new SignosVitales(72, 120, 80, 99, 36.5, 11, 15);
        } catch (DominioException ex) {
            assertThat(ex.getCampo()).isEqualTo("dolor");
        }
    }
}
