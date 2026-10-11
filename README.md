# Divisas Simulator 🪙 - Reto 2 de Clase

Aplicación móvil nativa para Android desarrollada **100% en Kotlin** con **Jetpack Compose** y arquitectura **MVVM (Model-View-ViewModel)**.

Este proyecto ha sido reestructurado para eliminar completamente dependencias de múltiples lenguajes (se eliminaron TypeScript y JavaScript), quedando un **único lenguaje de programación (Kotlin)** que abre y compila directamente en **Android Studio** sin requerir emuladores externos, Node.js ni Firebase CLI.

---

## 🚀 Requerimientos del Reto Cumplidos

1. **Un Solo Lenguaje**:
   - Todo el proyecto está escrito exclusivamente en **Kotlin** (`.kt` y Gradle Kotlin DSL `.kts`).
   - Se eliminaron todos los archivos TypeScript, JavaScript y entornos Node.

2. **Arquitectura MVVM**:
   - **Model**: `User`, `Currency`, `Operation`, `Wallet`, `MultimediaAsset` y sus repositorios persistentes.
   - **ViewModel**: `AuthViewModel`, `TradingViewModel`, `AdminAuditViewModel`, `MultimediaViewModel`.
   - **View**: Componentes declarativos en **Jetpack Compose** (sin Java para componentes y sin layouts XML).

3. **Las 4 Vistas Requeridas**:
   - **Vista 1: Registro e Inicio de Sesión** (`AuthScreen.kt`):
     - Registro e inicio de sesión con roles diferenciados: `Usuario (Cliente)` y `Administrador (Auditor)`.
     - Persistencia de sesión y botones de acceso rápido para pruebas del docente.
   - **Vista 2: Compra y Venta de Divisas** (`TradingScreen.kt`):
     - Integración con el servicio en vivo de **Finnhub**.
     - Billetera persistente (saldo en COP y divisas extranjeras USD, EUR, GBP).
     - Cálculo en tiempo real de la desviación porcentual frente a la cotización de mercado.
   - **Vista 3: Auditoría de Administrador** (`AdminAuditScreen.kt`):
     - Panel de control para el rol Administrador.
     - **Regla Central del 2.5%**: Al momento de auditar, compara la tasa pactada con la tasa vigente de Finnhub al momento de la transición. Si la variación supera el **2.5%**, el sistema prohíbe aprobarla como exitosa.
   - **Vista 4: Uso de Servicio para Generar Multimedia** (`MultimediaScreen.kt`):
     - Uso de un componente nativo de Android (`MultimediaGenerationService : Service()`).
     - Genera comprobantes digitales de alta resolución con código QR dinámico de verificación, sellos criptográficos y retroalimentación de audio.

4. **Integración con Finnhub**:
   - Servicio `FinnhubApiService.kt` para consultar cotizaciones y tasas de cambio en tiempo real mediante API REST oficial (`https://finnhub.io/api/v1`).
   - Respaldo dinámico con microvariaciones para garantizar funcionamiento fluido incluso sin conexión.

5. **Persistencia de Datos**:
   - Las cuentas de usuario, billeteras, transacciones y multimedia generada persisten de manera local y reactiva mediante `SharedPreferences` y serialización JSON.

---

## 📱 Credenciales de Prueba Rápida

La app incluye credenciales preconfiguradas con botones de un solo toque:

| Rol | Correo | Contraseña | Capacidades |
|---|---|---|---|
| **Administrador (Auditor)** | `admin@divisas.com` | `admin123` | Audita compras/ventas, valida regla 2.5% al momento de la transición, aprueba o rechaza operaciones. |
| **Usuario (Operador)** | `usuario@divisas.com` | `user123` | Consulta tasas Finnhub, compra y vende divisas, administra su saldo y envía operaciones a auditoría. |

*Nota: También se puede alternar de rol en cualquier momento desde la barra superior de la app para agilizar la calificación.*

---

## 📐 Fórmula de la Regla del 2.5% en la Transición

$$\text{Desviación Porcentual (\%)} = \frac{|\text{Tasa al Momento de la Transición} - \text{Tasa al Momento de la Orden}|}{\text{Tasa al Momento de la Orden}} \times 100\%$$

* **Si $\text{Desviación} \le 2.5\%$**: El Administrador puede aprobar la operación como **EXITOSA**, impactando el saldo de la billetera.
* **Si $\text{Desviación} > 2.5\%$**: La regla bloquea la aprobación exitosa y requiere que el auditor la rechace o registre la observación correspondiente.

---

## 🛠️ Cómo Abrir y Ejecutar en Android Studio

1. Abre **Android Studio**.
2. Selecciona **Open** y elige la carpeta del proyecto:
   `C:\Users\Admin\AndroidStudioProjects\reto-2-clase-`
3. Espera que Gradle sincronice automáticamente (el Gradle Wrapper 9.5 y AGP 9.3.3 ya están configurados).
4. Selecciona tu emulador o dispositivo físico y presiona **Run ▶️ (`Shift + F10`)**.
5. Para ejecutar las pruebas unitarias:
   ```bash
   ./gradlew testDebugUnitTest
   ```
