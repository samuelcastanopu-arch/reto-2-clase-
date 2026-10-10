import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";

export interface UpdateBaseRateData {
  currencyCode: string;
  newRate: number;
  reason: string;
}

export const updateBaseRate = onCall<UpdateBaseRateData>(
  { enforceAppCheck: false },
  async (request) => {
    // 1. Validar autenticación
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Debe iniciar sesión para realizar esta acción.");
    }

    // 2. Validar rol de Administrador
    const isAdmin = request.auth.token.role === "admin";
    if (!isAdmin) {
      throw new HttpsError("permission-denied", "Acceso denegado: Se requieren permisos de Administrador.");
    }

    const { currencyCode, newRate, reason } = request.data;
    const adminId = request.auth.uid;
    const adminEmail = request.auth.token.email || "";

    if (!currencyCode || !["USD", "EUR"].includes(currencyCode.toUpperCase())) {
      throw new HttpsError("invalid-argument", "Divisa inválida.");
    }

    const parsedNewRate = Number(newRate);
    if (isNaN(parsedNewRate) || parsedNewRate <= 0) {
      throw new HttpsError("invalid-argument", "La nueva tasa debe ser un número estrictamente positivo.");
    }

    if (!reason || reason.trim().length < 5) {
      throw new HttpsError("invalid-argument", "Debe especificar un motivo claro de al menos 5 caracteres.");
    }

    const normCurrency = currencyCode.toUpperCase();
    const db = admin.firestore();

    const result = await db.runTransaction(async (transaction) => {
      const currencyRef = db.collection("currencies").doc(normCurrency);
      const currencyDoc = await transaction.get(currencyRef);

      let previousRate = 0;
      if (currencyDoc.exists) {
        previousRate = Number(currencyDoc.data()?.baseRate) || 0;
      }

      const serverTimestamp = admin.firestore.FieldValue.serverTimestamp();

      // Actualizar tasa vigente en 'currencies'
      transaction.set(
        currencyRef,
        {
          code: normCurrency,
          baseRate: parsedNewRate,
          updatedAt: serverTimestamp,
          updatedByAdminId: adminId,
          updatedByAdminEmail: adminEmail,
          isActive: true,
        },
        { merge: true }
      );

      // Guardar registro histórico inmutable en 'rate_history'
      const historyRef = db.collection("rate_history").doc();
      transaction.set(historyRef, {
        id: historyRef.id,
        currencyCode: normCurrency,
        previousRate,
        newRate: parsedNewRate,
        changedAt: serverTimestamp,
        adminId,
        adminEmail,
        reason: reason.trim(),
      });

      // Guardar log de auditoría en 'audit_logs'
      const auditRef = db.collection("audit_logs").doc();
      transaction.set(auditRef, {
        id: auditRef.id,
        action: "UPDATE_BASE_RATE",
        performedBy: adminId,
        performedByEmail: adminEmail,
        timestamp: serverTimestamp,
        metadata: {
          currencyCode: normCurrency,
          previousRate,
          newRate: parsedNewRate,
          reason: reason.trim(),
        },
      });

      return {
        success: true,
        currencyCode: normCurrency,
        previousRate,
        newRate: parsedNewRate,
      };
    });

    return result;
  }
);
