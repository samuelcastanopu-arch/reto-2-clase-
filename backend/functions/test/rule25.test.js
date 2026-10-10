const assert = require("assert");

function validateRule25(baseRate, appliedRate) {
  if (baseRate <= 0 || appliedRate <= 0) {
    return { isValid: false, reason: "Tasa debe ser mayor a cero" };
  }
  const diff = Math.abs(appliedRate - baseRate);
  const percentageDeviation = Number(((diff / baseRate) * 100).toFixed(6));
  const isValid = percentageDeviation <= 2.50000001;
  return {
    isValid,
    percentageDeviation,
    reason: isValid ? null : `Desviación de ${percentageDeviation}% excede el 2.5%`
  };
}

function runTests() {
  console.log("=== Ejecutando pruebas unitarias de la Regla del 2.5% (Backend) ===");

  const baseRate = 4000.0;

  // 1. Tasa idéntica (0%)
  const testExact = validateRule25(baseRate, 4000.0);
  assert.strictEqual(testExact.isValid, true, "Tasa exacta debe ser aprobada");
  assert.strictEqual(testExact.percentageDeviation, 0);

  // 2. Límite superior exacto (2.50%)
  const testUpperExact = validateRule25(baseRate, 4100.0);
  assert.strictEqual(testUpperExact.isValid, true, "4100.0 debe ser aprobada (2.50%)");

  // 3. Caso borde inferior al límite (2.49%)
  const testUpperInside = validateRule25(baseRate, 4099.6);
  assert.strictEqual(testUpperInside.isValid, true, "4099.6 debe ser aprobada (2.49%)");

  // 4. Caso borde que supera el límite (2.51%)
  const testUpperOver = validateRule25(baseRate, 4100.4);
  assert.strictEqual(testUpperOver.isValid, false, "4100.4 debe ser rechazada (2.51%)");

  // 5. Límite inferior exacto (-2.50%)
  const testLowerExact = validateRule25(baseRate, 3900.0);
  assert.strictEqual(testLowerExact.isValid, true, "3900.0 debe ser aprobada (-2.50%)");

  // 6. Caso borde inferior dentro del margen (-2.49%)
  const testLowerInside = validateRule25(baseRate, 3900.4);
  assert.strictEqual(testLowerInside.isValid, true, "3900.4 debe ser aprobada (-2.49%)");

  // 7. Caso borde inferior fuera del margen (-2.51%)
  const testLowerOver = validateRule25(baseRate, 3899.6);
  assert.strictEqual(testLowerOver.isValid, false, "3899.6 debe ser rechazada (-2.51%)");

  // 8. Tasa negativa
  const testNegative = validateRule25(baseRate, -100);
  assert.strictEqual(testNegative.isValid, false, "Tasa negativa debe ser rechazada");

  // 9. Tasa cero
  const testZero = validateRule25(baseRate, 0);
  assert.strictEqual(testZero.isValid, false, "Tasa cero debe ser rechazada");

  console.log("✔ Todas las pruebas unitarias de la Regla del 2.5% PASARON exitosamente.");
}

runTests();
