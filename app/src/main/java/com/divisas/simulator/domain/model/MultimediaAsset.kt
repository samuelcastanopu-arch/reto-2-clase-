package com.divisas.simulator.domain.model

data class MultimediaAsset(
    val id: String,
    val title: String,
    val type: String, // "VOUCHER_TRANSACCION", "CERTIFICADO_AUDITORIA", "CARNET_FINANCIERO"
    val transactionId: String,
    val currencyCode: String,
    val amount: Double,
    val qrCodeUrl: String,
    val bannerImageUrl: String,
    val statusText: String,
    val securityHash: String,
    val generatedAt: Long = System.currentTimeMillis()
)
