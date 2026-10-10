import * as admin from "firebase-admin";

// Inicializar Firebase Admin SDK una sola vez en el runtime
if (!admin.apps.length) {
  admin.initializeApp();
}

export { simulateOperation } from "./operations/simulateOperation";
export { updateBaseRate } from "./rates/updateBaseRate";
export { getCloudinaryUploadSignature } from "./cloudinary/getCloudinaryUploadSignature";
export { assignUserRole } from "./users/assignUserRole";
