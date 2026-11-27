#!/usr/bin/env bash
# Smoke test: registra E1 y consulta el asistente.
# Uso: ./smoke-test.sh

set -e

TRIAGE_URL=${TRIAGE_URL:-http://localhost:8081}
ASISTENTE_URL=${ASISTENTE_URL:-http://localhost:8082}
API_KEY=${API_KEY_HEADER:-vidacruz-dev-api-key}

echo "==> Health triage..."
curl -sf "$TRIAGE_URL/actuator/health" | head -c 200; echo

echo "==> Health asistente..."
curl -sf "$ASISTENTE_URL/actuator/health" | head -c 200; echo

echo "==> Registrando E1 (debe ser CRITICO)..."
curl -s -X POST "$TRIAGE_URL/triage/pacientes" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{
    "nombreCompleto": "Paciente E1",
    "edad": 45,
    "frecuenciaCardiaca": 130,
    "presionSistolica": 180,
    "presionDiastolica": 110,
    "spo2": 97,
    "temperatura": 36.8,
    "dolor": 8,
    "gcs": 15,
    "quejaPrincipal": "Dolor torácico"
  }' | tee /tmp/e1.json; echo

echo "==> Consulta asistente: crisis hipertensiva..."
curl -s -X POST "$ASISTENTE_URL/asistente/consulta" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{"pregunta": "¿Cuáles son las medidas iniciales ante una crisis hipertensiva?"}' | tee /tmp/consulta.json; echo

echo "==> Estado urgencias..."
curl -s "$ASISTENTE_URL/asistente/estado-urgencias" \
  -H "X-API-Key: $API_KEY" | tee /tmp/estado.json; echo

echo "==> OK"