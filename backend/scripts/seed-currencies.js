const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

async function seedCurrencies() {
  console.log("Inicializando divisas base en Cloud Firestore...");

  const currencies = [
    {
      code: "USD",
      name: "Dólar Estadounidense",
      symbol: "$",
      isActive: true,
      baseRate: 4000.0,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedByAdminId: "system_bootstrap",
      updatedByAdminEmail: "admin@divisas.com",
    },
    {
      code: "EUR",
      name: "Euro",
      symbol: "€",
      isActive: true,
      baseRate: 4350.0,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedByAdminId: "system_bootstrap",
      updatedByAdminEmail: "admin@divisas.com",
    },
  ];

  for (const curr of currencies) {
    await db.collection("currencies").doc(curr.code).set(curr, { merge: true });
    console.log(`Divisa ${curr.code} configurada con tasa base: $ ${curr.baseRate} COP`);
  }

  console.log("Inicialización de divisas completada con éxito.");
}

seedCurrencies().catch(console.error);
