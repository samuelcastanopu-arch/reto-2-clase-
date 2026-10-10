import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";

export interface SetRoleData {
  targetEmail: string;
  role: "client" | "admin";
}

export const assignUserRole = onCall<SetRoleData>(
  { enforceAppCheck: false },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Debe estar autenticado.");
    }

    const isCurrentAdmin = request.auth.token.role === "admin";
    const callerEmail = request.auth.token.email || "";
    const bootstrapEmail = process.env.ADMIN_BOOTSTRAP_EMAIL || "admin@divisas.com";

    // Permitir si quien llama es admin O si es el correo de bootstrap inicial
    if (!isCurrentAdmin && callerEmail.toLowerCase() !== bootstrapEmail.toLowerCase()) {
      throw new HttpsError("permission-denied", "Solo administradores pueden cambiar roles.");
    }

    const { targetEmail, role } = request.data;
    if (!targetEmail || !["client", "admin"].includes(role)) {
      throw new HttpsError("invalid-argument", "Parámetros inválidos.");
    }

    const user = await admin.auth().getUserByEmail(targetEmail);
    await admin.auth().setCustomUserClaims(user.uid, { role });

    await admin.firestore().collection("users").doc(user.uid).set(
      {
        role,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    return {
      success: true,
      email: targetEmail,
      assignedRole: role,
    };
  }
);
