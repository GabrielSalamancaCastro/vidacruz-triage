package com.vidacruz.triage.domain.model;

import java.time.Instant;
import java.util.Map;

public record ReporteHora(
        Instant hora,
        long totalRegistrados,
        Map<String, Long> porNivel,
        double esperaPromedioMin,
        double esperaP95Min
) {
}
