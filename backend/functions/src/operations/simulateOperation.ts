import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";

export interface SimulateOperationData {
  idempotencyKey: string;
  currencyCode: string;
  operationType: "BUY" | "SELL";
  amount: number;
  appliedRate: number;
}

export const simulateOperation = onCall<SimulateOperationData>(
  { enforceAppCheck: false },
  async (request) => {
    // 1. Validar autenticación
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Debe estar autenticado para realizar operaciones.");
    }

    const userId = request.auth.uid;
    const userEmail = request.auth.token.email || "";
    const { idempotencyKey, currencyCode, operationType, amount, appliedRate } = request.data;

    // Validación básica de campos requeridos
    if (!idempotencyKey || typeof idempotencyKey !== "string") {
      throw new HttpsError("invalid-argument", "El identificador de idempotencia es requerido.");
    }

    if (!currencyCode || !["USD", "EUR"].includes(currencyCode.toUpperCase())) {
      throw new HttpsError("invalid-argument", "Divisa no soportada. Solo se admiten USD y EUR.");
    }

    if (!operationType || !["BUY", "SELL"].includes(operationType)) {
      throw new HttpsError("invalid-argument", "El tipo de operación debe ser BUY o SELL.");
    }

    const parsedAmount = Number(amount);
    const parsedAppliedRate = Number(appliedRate);
    const normCurrency = currencyCode.toUpperCase();

    const db = admin.firestore();

    // 2. Control de Idempotencia: Verificar si esta solicitud ya fue procesada
    const existingOpQuery = await db
      .collection("operations")
      .where("idempotencyKey", "==", idempotencyKey)
      .limit(1)
      .get();

    if (!existingOpQuery.empty) {
      const existingDoc = existingOpQuery.docs[0].data();
      return {
        operationId: existingOpQuery.docs[0].id,
        isIdempotentReplay: true,
        ...existingDoc,
      };
    }

    // 3. Ejecutar transacción para leer la tasa base vigente y registrar la operación atómicamente
    const result = await db.runTransaction(async (transaction) => {
      const currencyRef = db.collection("currencies").doc(normCurrency);
      const currencyDoc = await transaction.get(currencyRef);

      let status: "APPROVED" | "REJECTED" = "REJECTED";
      let rejectionReason: string | null = null;
      let baseRate = 0;
      let percentageDeviation = 0;
      let totalCop = 0;

      if (!currencyDoc.exists) {
        rejectionReason = `La divisa ${normCurrency} no existe en el sistema.`;
      } else {
        const currData = currencyDoc.data()!;
        baseRate = Number(currData.baseRate) || 0;
        const isActive = Boolean(currData.isActive);

        if (!isActive) {
          rejectionReason = `La divisa ${normCurrency} se encuentra actualmente inactiva.`;
        } else if (baseRate <= 0) {
          rejectionReason = `La tasa base para ${normCurrency} no es válida (${baseRate}).`;
        } else if (isNaN(parsedAmount) || parsedAmount <= 0) {
          rejectionReason = `La cantidad de divisas debe ser estrictamente mayor a cero.`;
        } else if (isNaN(parsedAppliedRate) || parsedAppliedRate <= 0) {
          rejectionReason = `La tasa aplicada debe ser estrictamente mayor a cero.`;
        } else {
          // REGLA DEL 2,5 %:
          // Desviación porcentual = (valor absoluto de la tasa aplicada menos la tasa base) / tasa base * 100
          const diff = Math.abs(parsedAppliedRate - baseRate);
          percentageDeviation = Number(((diff / baseRate) * 100).toFixed(6));

          // Tolerancia épsilon para precisión de punto flotante: 2.50000001
          if (percentageDeviation > 2.50000001) {
            status = "REJECTED";
            rejectionReason = `La tasa aplicada ($ ${parsedAppliedRate.toLocaleString("es-CO")}) presenta una desviación del ${percentageDeviation.toFixed(2)}% respecto a la tasa base ($ ${baseRate.toLocaleString("es-CO")}), superando el límite permitido del 2,5%.`;
          } else {
            status = "APPROVED";
            rejectionReason = null;
          }

          // Cálculo del valor total en COP:
          // Cantidad de divisas * Tasa aplicada
          totalCop = Number((parsedAmount * parsedAppliedRate).toFixed(2));
        }
      }

      // Preparar documento para la colección 'operations'
      const operationRef = db.collection("operations").doc();
      const serverTimestamp = admin.firestore.FieldValue.serverTimestamp();

      const operationRecord = {
        id: operationRef.id,
        idempotencyKey,
        userId,
        userEmail,
        operationType,
        currencyCode: normCurrency,
        amount: parsedAmount > 0 ? parsedAmount : 0,
        baseRate,
        appliedRate: parsedAppliedRate > 0 ? parsedAppliedRate : 0,
        percentageDeviation,
        totalCop,
        status,
        rejectionReason,
        createdAt: serverTimestamp,
      };

      transaction.set(operationRef, operationRecord);

      return {
        operationId: operationRef.id,
        status,
        rejectionReason,
        currencyCode: normCurrency,
        operationType,
        amount: parsedAmount,
        baseRate,
        appliedRate: parsedAppliedRate,
        percentageDeviation,
        totalCop,
      };
    });

    return result;
  }
);
