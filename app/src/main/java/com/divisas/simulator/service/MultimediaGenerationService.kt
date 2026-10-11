package com.divisas.simulator.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.divisas.simulator.domain.model.MultimediaAsset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.UUID

data class MultimediaServiceState(
    val isRunning: Boolean = false,
    val progress: Int = 0,
    val statusMessage: String = "Servicio multimedia listo",
    val lastAsset: MultimediaAsset? = null
)

class MultimediaGenerationService : Service() {

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + job)

    companion object {
        const val ACTION_GENERATE = "com.divisas.simulator.ACTION_GENERATE"
        const val ACTION_PLAY_SOUND = "com.divisas.simulator.ACTION_PLAY_SOUND"

        const val EXTRA_TX_ID = "extra_tx_id"
        const val EXTRA_CURRENCY = "extra_currency"
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_TYPE = "extra_type"

        private const val CHANNEL_ID = "multimedia_service_channel"
        private const val NOTIFICATION_ID = 4001

        private val _serviceState = MutableStateFlow(MultimediaServiceState())
        val serviceState: StateFlow<MultimediaServiceState> = _serviceState.asStateFlow()

        fun startGeneration(
            context: Context,
            transactionId: String,
            currencyCode: String,
            amount: Double,
            type: String
        ) {
            val intent = Intent(context, MultimediaGenerationService::class.java).apply {
                action = ACTION_GENERATE
                putExtra(EXTRA_TX_ID, transactionId)
                putExtra(EXTRA_CURRENCY, currencyCode)
                putExtra(EXTRA_AMOUNT, amount)
                putExtra(EXTRA_TYPE, type)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun playMultimediaTone() {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 95)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 300)
            } catch (_: Exception) {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_GENERATE -> {
                val txId = intent.getStringExtra(EXTRA_TX_ID) ?: "TX_${System.currentTimeMillis()}"
                val currency = intent.getStringExtra(EXTRA_CURRENCY) ?: "USD"
                val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 100.0)
                val type = intent.getStringExtra(EXTRA_TYPE) ?: "VOUCHER_TRANSACCION"

                startForeground(NOTIFICATION_ID, buildNotification("Iniciando servicio de generación multimedia..."))
                executeMultimediaGeneration(txId, currency, amount, type)
            }
            ACTION_PLAY_SOUND -> {
                playMultimediaTone()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun executeMultimediaGeneration(
        txId: String,
        currency: String,
        amount: Double,
        type: String
    ) {
        serviceScope.launch {
            _serviceState.value = MultimediaServiceState(
                isRunning = true,
                progress = 15,
                statusMessage = "1/4: Conectando con el motor multimedia y verificando datos..."
            )
            updateNotification("1/4: Verificando datos de transacción...")
            delay(600)

            _serviceState.value = _serviceState.value.copy(
                progress = 40,
                statusMessage = "2/4: Generando código QR criptográfico para verificación oficial..."
            )
            updateNotification("2/4: Generando código QR...")
            delay(700)

            // Construir URL del código QR mediante servicio API oficial
            val qrData = "TRANSACCION-DIVISAS:$txId|MONEDA:$currency|MONTO:$amount|HASH:${UUID.randomUUID()}"
            val encodedQrData = try {
                URLEncoder.encode(qrData, "UTF-8")
            } catch (_: Exception) {
                "DIVISAS_VOUCHER_$txId"
            }
            val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&color=1A237E&data=$encodedQrData"

            _serviceState.value = _serviceState.value.copy(
                progress = 75,
                statusMessage = "3/4: Ensamblando comprobante gráfico con sellos de seguridad..."
            )
            updateNotification("3/4: Ensamblando comprobante multimedia...")
            delay(700)

            // Banner multimedia financiero
            val bannerUrl = when (currency.uppercase()) {
                "EUR" -> "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=800&auto=format&fit=crop"
                "GBP" -> "https://images.unsplash.com/photo-1544377193-33dcf4d68fb5?w=800&auto=format&fit=crop"
                else -> "https://images.unsplash.com/photo-1580519542036-c47de6196ba5?w=800&auto=format&fit=crop"
            }

            val hash = "SEC-${UUID.randomUUID().toString().take(8).uppercase()}"
            val asset = MultimediaAsset(
                id = "MED_${System.currentTimeMillis()}",
                title = when (type) {
                    "CERTIFICADO_AUDITORIA" -> "Certificado Oficial de Auditoría 2.5%"
                    "CARNET_FINANCIERO" -> "Carnet Digital de Operador Financiero"
                    else -> "Voucher Digital de Transacción Financiera"
                },
                type = type,
                transactionId = txId,
                currencyCode = currency,
                amount = amount,
                qrCodeUrl = qrUrl,
                bannerImageUrl = bannerUrl,
                statusText = "Generado con éxito por el Servicio Multimedia de Android",
                securityHash = hash,
                generatedAt = System.currentTimeMillis()
            )

            // Reproducir sonido de confirmación multimedia
            playMultimediaTone()

            _serviceState.value = MultimediaServiceState(
                isRunning = false,
                progress = 100,
                statusMessage = "¡Multimedia generada exitosamente!",
                lastAsset = asset
            )
            updateNotification("¡Comprobante multimedia listo!")

            delay(1000)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Servicio de Generación Multimedia",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones durante la generación de comprobantes multimedia"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Generador Multimedia Activo")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
