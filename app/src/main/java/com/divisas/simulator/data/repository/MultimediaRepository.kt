package com.divisas.simulator.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.divisas.simulator.domain.model.MultimediaAsset
import org.json.JSONArray
import org.json.JSONObject

class MultimediaRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("multimedia_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ASSETS = "generated_multimedia_assets"
    }

    init {
        initDefaultSample()
    }

    private fun initDefaultSample() {
        if (!prefs.contains(KEY_ASSETS)) {
            val sample = listOf(
                MultimediaAsset(
                    id = "MED_SAMPLE_01",
                    title = "Voucher Digital de Transacción Financiera",
                    type = "VOUCHER_TRANSACCION",
                    transactionId = "TX_DEMO_01",
                    currencyCode = "USD",
                    amount = 250.0,
                    qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&color=1A237E&data=DIVISAS_OFICIAL_TX_DEMO_01",
                    bannerImageUrl = "https://images.unsplash.com/photo-1580519542036-c47de6196ba5?w=800&auto=format&fit=crop",
                    statusText = "Generado con éxito por el Servicio Multimedia de Android",
                    securityHash = "SEC-A1B2C3D4",
                    generatedAt = System.currentTimeMillis() - 1800000
                )
            )
            saveAssetsList(sample)
        }
    }

    fun getAllAssets(): List<MultimediaAsset> {
        val jsonStr = prefs.getString(KEY_ASSETS, null) ?: return emptyList()
        val list = mutableListOf<MultimediaAsset>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    MultimediaAsset(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        type = obj.getString("type"),
                        transactionId = obj.getString("transactionId"),
                        currencyCode = obj.getString("currencyCode"),
                        amount = obj.getDouble("amount"),
                        qrCodeUrl = obj.getString("qrCodeUrl"),
                        bannerImageUrl = obj.getString("bannerImageUrl"),
                        statusText = obj.getString("statusText"),
                        securityHash = obj.getString("securityHash"),
                        generatedAt = obj.getLong("generatedAt")
                    )
                )
            }
        } catch (_: Exception) {}
        return list.sortedByDescending { it.generatedAt }
    }

    fun saveAsset(asset: MultimediaAsset) {
        val current = getAllAssets().toMutableList()
        current.add(0, asset)
        saveAssetsList(current)
    }

    private fun saveAssetsList(list: List<MultimediaAsset>) {
        val array = JSONArray()
        for (a in list) {
            val obj = JSONObject().apply {
                put("id", a.id)
                put("title", a.title)
                put("type", a.type)
                put("transactionId", a.transactionId)
                put("currencyCode", a.currencyCode)
                put("amount", a.amount)
                put("qrCodeUrl", a.qrCodeUrl)
                put("bannerImageUrl", a.bannerImageUrl)
                put("statusText", a.statusText)
                put("securityHash", a.securityHash)
                put("generatedAt", a.generatedAt)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_ASSETS, array.toString()).apply()
    }
}
