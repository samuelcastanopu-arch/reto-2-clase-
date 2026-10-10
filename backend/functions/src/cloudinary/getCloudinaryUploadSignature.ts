import * as crypto from "crypto";
import { onCall, HttpsError } from "firebase-functions/v2/https";

export interface SignatureRequestData {
  folder?: string;
}

export const getCloudinaryUploadSignature = onCall<SignatureRequestData>(
  { enforceAppCheck: false },
  async (request) => {
    // 1. Validar autenticación
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Debe iniciar sesión para subir fotografías.");
    }

    const userId = request.auth.uid;
    const folder = request.data.folder || "profile_photos";
    const timestamp = Math.round(new Date().getTime() / 1000);
    const publicId = `user_${userId}`;

    // Obtener credenciales de variables de entorno seguras
    const cloudName = process.env.CLOUDINARY_CLOUD_NAME || "demo";
    const apiKey = process.env.CLOUDINARY_API_KEY || "demo_key";
    const apiSecret = process.env.CLOUDINARY_API_SECRET || "demo_secret";

    // En Cloudinary, los parámetros a firmar se ordenan alfabéticamente:
    // folder, overwrite, public_id, timestamp
    const paramsToSign = `folder=${folder}&overwrite=true&public_id=${publicId}&timestamp=${timestamp}`;

    // Generar firma SHA-1 con apiSecret
    const signature = crypto
      .createHash("sha1")
      .update(paramsToSign + apiSecret)
      .digest("hex");

    return {
      signature,
      timestamp,
      apiKey,
      cloudName,
      folder,
      publicId,
    };
  }
);
