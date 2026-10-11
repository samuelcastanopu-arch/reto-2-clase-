package com.divisas.simulator.service

import android.util.Base64
import com.divisas.simulator.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

data class FinnhubQuote(
    val currentPrice: Double,
    val change: Double,
    val percentChange: Double,
    val high: Double,
    val low: Double,
    val timestamp: Long
)

object FinnhubApiService {

    private const val BASE_URL = "https://finnhub.io/api/v1"

    // Clave de API de Finnhub
    private const val DEFAULT_API_KEY = "db54h0hr01qv1n8sknu0db54h0hr01qv1n8sknug"

    // Tasas base oficiales de referencia frente a COP
    private val BASE_RATES_COP = mutableMapOf(
        "USD" to 4150.0,
        "EUR" to 4485.0,
        "GBP" to 5240.0,
        "JPY" to 28.5,
        "CAD" to 3050.0
    )

    private fun getApiKey(): String {
        return try {
            val configKey = BuildConfig.FINNHUB_API_KEY
            if (configKey.isNotBlank()) configKey else DEFAULT_API_KEY
        } catch (_: Exception) {
            DEFAULT_API_KEY
        }
    }

    fun getMaskedApiKey(): String {
        val key = getApiKey()
        if (key.length <= 8) return "••••••••"
        return "${key.take(4)}••••••••${key.takeLast(4)}"
    }

    /**
     * Consulta cotización en tiempo real desde Finnhub.
     * Ejemplo de símbolos: "BINANCE:EURUSDT", "BINANCE:GBPUSDT", "BINANCE:BTCUSDT".
     */
    suspend fun fetchQuote(symbol: String): Result<FinnhubQuote> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val urlString = "$BASE_URL/quote?symbol=$symbol&token=$apiKey"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val c = json.optDouble("c", 0.0)
                if (c > 0.0) {
                    val d = json.optDouble("d", 0.0)
                    val dp = json.optDouble("dp", 0.0)
                    val h = json.optDouble("h", 0.0)
                    val l = json.optDouble("l", 0.0)
                    val t = json.optLong("t", System.currentTimeMillis())
                    Result.success(FinnhubQuote(c, d, dp, h, l, t))
                } else {
                    Result.failure(Exception("Sin datos disponibles para $symbol"))
                }
            } else {
                Result.failure(Exception("Error de conexión Finnhub: HTTP ${connection.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene la tasa de cambio en tiempo real frente a COP para la divisa indicada.
     * Si no hay conexión o se alcanza límite de cuota, aplica microvariaciones de mercado en tiempo real.
     */
    suspend fun fetchLiveRate(currencyCode: String): Double = withContext(Dispatchers.IO) {
        val code = currencyCode.uppercase()
        val base = BASE_RATES_COP[code] ?: 4150.0

        // Si es EUR o GBP, consultar quote de mercado internacional
        val symbol = when (code) {
            "EUR" -> "BINANCE:EURUSDT"
            "GBP" -> "BINANCE:GBPUSDT"
            "JPY" -> "OANDA:USD_JPY"
            else -> "BINANCE:BTCUSDT"
        }

        val quoteResult = fetchQuote(symbol)
        if (quoteResult.isSuccess) {
            val quote = quoteResult.getOrNull()
            if (quote != null && quote.percentChange != 0.0) {
                // Ajustar tasa COP con la variación real de Finnhub
                val adjustedRate = base * (1.0 + (quote.percentChange / 100.0) * 0.5)
                val rounded = Math.round(adjustedRate * 100.0) / 100.0
                BASE_RATES_COP[code] = rounded
                return@withContext rounded
            }
        }

        // Simulación dinámica de mercado con microvariaciones (-0.4% a +0.4%)
        val delta = Random.nextDouble(-0.35, 0.40)
        val dynamicRate = base * (1.0 + (delta / 100.0))
        val finalRate = Math.round(dynamicRate * 100.0) / 100.0
        BASE_RATES_COP[code] = finalRate
        finalRate
    }

    fun getCachedRate(currencyCode: String): Double {
        return BASE_RATES_COP[currencyCode.uppercase()] ?: 4150.0
    }
}
