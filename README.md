# Divisas Simulator 🪙

Aplicación Android nativa desarrollada en **Kotlin** y **Jetpack Compose** con arquitectura **MVVM / Clean Architecture**, backend en **Cloud Functions for Firebase (TypeScript)**, base de datos en **Cloud Firestore**, autenticación mediante **Firebase Authentication** y gestión de fotografías en **Cloudinary**.

---

## 📌 Características Principales

* **Simulación de Compra y Venta**: Entorno de simulación contable para USD y EUR frente a COP sin movimientos financieros reales.
* **Regla Central del 2,5 %**: Validación matemática estricta tanto en cliente (asistente visual interactivo) como en el servidor (Cloud Function atómica). Ninguna operación puede diferir en más del 2,5 % de la tasa base vigente.
* **Control de Acceso Basado en Roles (RBAC)**:
  * **Usuario Tradicional (Cliente)**: Consulta tasas, simula operaciones, revisa su propio historial y motivos de rechazo, actualiza su foto de perfil.
  * **Administrador**: Modifica manualmente las tasas base oficiales, consulta el histórico inmutable de cambios y supervisa los logs de auditoría de seguridad.
* **Auditoría e Inmutabilidad**: Las operaciones rechazadas también quedan registradas en el servidor con su motivo exacto de rechazo.
* **Prevención de Duplicados**: Control de idempotencia con UUID v4 en cada solicitud de simulación.
* **Seguridad de Secretos**: Ningún secreto de Cloudinary ni credenciales administrativas existen en el APK de Android. La subida de fotos utiliza firmas criptográficas efímeras (HMAC-SHA1).

---

## 🏗 Arquitectura del Sistema

El proyecto sigue los principios de **Clean Architecture**, **MVVM** y **Unidirectional Data Flow (UDF)**:

```
divisas/
├── android/                         # Proyecto Android Studio
│   ├── app/
│   │   ├── src/main/java/com/divisas/simulator/
│   │   │   ├── core/                # Utilidades, formateadores (COP/USD/EUR), MathUtil (BigDecimal)
│   │   │   ├── data/                # DTOs, mappers y repositorios (Firebase + Cloudinary)
│   │   │   ├── domain/              # Modelos, interfaces y casos de uso (regla 2.5%)
│   │   │   ├── presentation/        # Jetpack Compose M3, ViewModels, componentes y navegación
│   │   │   └── di/                  # Módulos de inyección de dependencias con Hilt
│   │   └── src/test/                # Pruebas unitarias de JUnit para la regla del 2.5%
│   └── gradle/libs.versions.toml    # Version Catalog centralizado
└── backend/                         # Backend Firebase
    ├── firestore.rules              # Reglas de seguridad con mínimo privilegio
    ├── firestore.indexes.json       # Índices compuestos para consultas ordenadas
    ├── firebase.json                # Configuración de emuladores locales
    ├── scripts/                     # Scripts de bootstrap (admin) y semilla de divisas
    └── functions/                   # Cloud Functions en TypeScript (Node.js 20 LTS)
        ├── src/operations/          # simulateOperation (idempotencia y regla 2.5%)
        ├── src/rates/               # updateBaseRate (histórico y auditoría)
        ├── src/cloudinary/          # getCloudinaryUploadSignature (firma HMAC)
        └── test/                    # Pruebas unitarias de la regla del 2.5% en Node.js
```

---

## 🚀 Guía de Puesta en Marcha

### Requisitos Previos
1. **Android Studio** (Hedgehog, Koala o Ladybug con JDK 17 configurado).
2. **Node.js** (v18 o superior) y **npm**.
3. **Firebase CLI**: `npm install -g firebase-tools`.

---

### Paso 1: Configurar el Backend y Emuladores

1. Instalar dependencias del backend:
   ```bash
   cd backend/functions
   npm install
   npm run build
   ```

2. Ejecutar las pruebas unitarias de la regla del 2,5 %:
   ```bash
   node test/rule25.test.js
   ```

3. Iniciar la suite de emuladores locales de Firebase (opcional para desarrollo sin costo):
   ```bash
   cd ..
   firebase emulators:start
   ```
   * *Auth Emulator:* `http://localhost:9099`
   * *Firestore Emulator:* `http://localhost:8080`
   * *Functions Emulator:* `http://localhost:5001`
   * *Emulator UI:* `http://localhost:4000`

4. Poblar las divisas base iniciales (USD = 4.000 COP, EUR = 4.350 COP):
   ```bash
   node scripts/seed-currencies.js
   ```

---

### Paso 2: Configurar y Ejecutar la App Android

1. Abre la carpeta `android/` en **Android Studio**.
2. Espera a que **Gradle** sincronice las dependencias del catálogo `libs.versions.toml`.
3. Para conectar con Firebase productivo:
   * Coloca tu archivo `google-services.json` descargado de Firebase Console en `android/app/google-services.json`.
4. Si deseas utilizar los emuladores locales durante el desarrollo:
   * En `android/app/build.gradle.kts`, mantén `USE_FIREBASE_EMULATOR = true` en la variante debug.
5. Selecciona un dispositivo virtual (AVD con Android 8.0 / API 26 o superior) y presiona **Run (`Shift + F10`)**.

---

### Paso 3: Flujo de Pruebas Recomendado

1. **Registro de Usuario**: Abre la app y crea una cuenta como cliente.
2. **Simular Compra Aprobada**:
   * Selecciona USD (Tasa base: $ 4.000 COP).
   * Ingresa cantidad: `100`.
   * Ingresa tasa aplicada: `4050` (Desviación: +1,25 % $\rightarrow$ En rango).
   * Presiona "Procesar Simulación" $\rightarrow$ La operación se aprueba por un total de `$ 405.000 COP`.
3. **Simular Venta Rechazada por Regla del 2,5 %**:
   * Selecciona USD.
   * Ingresa tasa aplicada: `4150` (Desviación: +3,75 % $\rightarrow$ Fuera de rango).
   * El indicador visual advierte el rechazo.
   * Al procesar, el servidor rechaza la operación y la registra en el historial con el motivo explícito.
4. **Asignar Rol de Administrador**:
   * Ejecuta: `ADMIN_BOOTSTRAP_EMAIL="tu_correo@ejemplo.com" node backend/scripts/bootstrap-admin.js`.
   * Cierra sesión y vuelve a ingresar. Verás habilitada la pestaña **Admin** en la barra inferior.
5. **Modificar Tasa Base Oficial**:
   * Desde la pestaña Admin, cambia la tasa de USD a $ 4.100 COP justificando el motivo.
   * Regresa al Inicio y comprueba que la tasa base vigente se actualizó reactivamente.
