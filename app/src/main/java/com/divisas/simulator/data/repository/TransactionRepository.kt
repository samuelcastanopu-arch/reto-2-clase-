package com.divisas.simulator.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.divisas.simulator.domain.model.Operation
import com.divisas.simulator.domain.model.OperationStatus
import com.divisas.simulator.domain.model.OperationType
import com.divisas.simulator.domain.model.Wallet
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.math.abs

class TransactionRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("currency_tx_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TRANSACTIONS = "transactions_history"
        private const val KEY_WALLETS = "user_wallets"
    }

    init {
        initSampleDataIfNeeded()
    }

    private fun initSampleDataIfNeeded() {
        if (!prefs.contains(KEY_TRANSACTIONS)) {
            val sampleTxList = listOf(
                Operation(
                    id = "TX_DEMO_01",
                    userId = "user_001",
                    userEmail = "usuario@divisas.com",
                    userName = "Samuel Operador",
                    operationType = OperationType.COMPRA,
                    currencyCode = "USD",
                    amount = 250.0,
                    rateAtOrder = 4150.0,
                    totalCop = 250.0 * 4150.0,
                    timestamp = System.currentTimeMillis() - 3600000,
                    rateAtTransition = 4180.0,
                    percentageDeviation = 0.72,
                    status = OperationStatus.PENDIENTE_AUDITORIA,
                    auditObservation = "Pendiente de revisión por auditor"
                ),
                Operation(
                    id = "TX_DEMO_02",
                    userId = "user_001",
                    userEmail = "usuario@divisas.com",
                    userName = "Samuel Operador",
                    operationType = OperationType.VENTA,
                    currencyCode = "EUR",
                    amount = 100.0,
                    rateAtOrder = 4450.0,
                    totalCop = 100.0 * 4450.0,
                    timestamp = System.currentTimeMillis() - 7200000,
                    rateAtTransition = 4590.0,
                    percentageDeviation = 3.15,
                    status = OperationStatus.PENDIENTE_AUDITORIA,
                    auditObservation = "Desviación alta detectada al momento de la orden"
                )
            )
            saveTransactions(sampleTxList)
        }
    }

    // ==========================================
    // BILLETERAS PERSISTENTES
    // ==========================================

    fun getWallet(userId: String): Wallet {
        val walletsMap = loadAllWallets()
        return walletsMap[userId] ?: Wallet(userId = userId).also {
            saveWallet(it)
        }
    }

    fun saveWallet(wallet: Wallet) {
        val current = loadAllWallets().toMutableMap()
        current[wallet.userId] = wallet
        saveAllWallets(current)
    }

    private fun loadAllWallets(): Map<String, Wallet> {
        val jsonStr = prefs.getString(KEY_WALLETS, null) ?: return emptyMap()
        val result = mutableMapOf<String, Wallet>()
        try {
            val jsonObject = JSONObject(jsonStr)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val userId = keys.next()
                val walletObj = jsonObject.getJSONObject(userId)
                val copBalance = walletObj.optDouble("copBalance", 25000000.0)
                val balancesObj = walletObj.optJSONObject("balances")
                val balances = mutableMapOf<String, Double>()
                if (balancesObj != null) {
                    val bKeys = balancesObj.keys()
                    while (bKeys.hasNext()) {
                        val cCode = bKeys.next()
                        balances[cCode] = balancesObj.getDouble(cCode)
                    }
                }
                result[userId] = Wallet(userId, copBalance, balances)
            }
        } catch (_: Exception) {}
        return result
    }

    private fun saveAllWallets(wallets: Map<String, Wallet>) {
        val root = JSONObject()
        for ((uId, w) in wallets) {
            val wObj = JSONObject()
            wObj.put("copBalance", w.copBalance)
            val bObj = JSONObject()
            for ((c, amount) in w.balances) {
                bObj.put(c, amount)
            }
            wObj.put("balances", bObj)
            root.put(uId, wObj)
        }
        prefs.edit().putString(KEY_WALLETS, root.toString()).apply()
    }

    // ==========================================
    // GESTIÓN DE TRANSACCIONES Y AUDITORÍA
    // ==========================================

    fun getAllTransactions(): List<Operation> {
        val jsonStr = prefs.getString(KEY_TRANSACTIONS, null) ?: return emptyList()
        val list = mutableListOf<Operation>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val type = try { OperationType.valueOf(obj.getString("operationType")) } catch (_: Exception) { OperationType.COMPRA }
                val status = try { OperationStatus.valueOf(obj.getString("status")) } catch (_: Exception) { OperationStatus.PENDIENTE_AUDITORIA }
                list.add(
                    Operation(
                        id = obj.getString("id"),
                        userId = obj.getString("userId"),
                        userEmail = obj.optString("userEmail", ""),
                        userName = obj.optString("userName", "Usuario"),
                        operationType = type,
                        currencyCode = obj.getString("currencyCode"),
                        amount = obj.getDouble("amount"),
                        rateAtOrder = obj.getDouble("rateAtOrder"),
                        totalCop = obj.getDouble("totalCop"),
                        timestamp = obj.getLong("timestamp"),
                        rateAtTransition = obj.optDouble("rateAtTransition", obj.getDouble("rateAtOrder")),
                        percentageDeviation = obj.optDouble("percentageDeviation", 0.0),
                        status = status,
                        auditObservation = obj.optString("auditObservation", ""),
                        auditedBy = if (obj.isNull("auditedBy")) null else obj.optString("auditedBy"),
                        auditedAt = if (obj.isNull("auditedAt")) null else obj.optLong("auditedAt")
                    )
                )
            }
        } catch (_: Exception) {}
        return list.sortedByDescending { it.timestamp }
    }

    fun getTransactionsForUser(userId: String): List<Operation> {
        return getAllTransactions().filter { it.userId == userId }
    }

    fun createTransaction(
        userId: String,
        userEmail: String,
        userName: String,
        type: OperationType,
        currencyCode: String,
        amount: Double,
        rateAtOrder: Double
    ): Result<Operation> {
        val wallet = getWallet(userId)
        val totalCop = amount * rateAtOrder

        // Validación contable preliminar
        if (type == OperationType.COMPRA) {
            if (wallet.copBalance < totalCop) {
                return Result.failure(IllegalArgumentException("Saldo insuficiente en COP ($ ${wallet.copBalance}) para comprar $amount $currencyCode."))
            }
        } else {
            val foreignBalance = wallet.getForeignBalance(currencyCode)
            if (foreignBalance < amount) {
                return Result.failure(IllegalArgumentException("Saldo insuficiente en $currencyCode ($foreignBalance disponible) para vender."))
            }
        }

        val op = Operation(
            id = "TX_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
            userId = userId,
            userEmail = userEmail,
            userName = userName,
            operationType = type,
            currencyCode = currencyCode.uppercase(),
            amount = amount,
            rateAtOrder = rateAtOrder,
            totalCop = totalCop,
            timestamp = System.currentTimeMillis(),
            rateAtTransition = rateAtOrder,
            percentageDeviation = 0.0,
            status = OperationStatus.PENDIENTE_AUDITORIA,
            auditObservation = "En espera de auditoría oficial"
        )

        val all = getAllTransactions().toMutableList()
        all.add(0, op)
        saveTransactions(all)

        return Result.success(op)
    }

    /**
     * El Administrador audita la compra o venta:
     * Verifica que sea exitosa y que la variación entre la tasa pactada y la tasa
     * al momento de la transición NO SUPERE el 2.5% del valor de la divisa.
     */
    fun auditTransaction(
        transactionId: String,
        adminName: String,
        rateAtTransition: Double,
        approve: Boolean,
        observation: String
    ): Result<Operation> {
        val all = getAllTransactions().toMutableList()
        val index = all.indexOfFirst { it.id == transactionId }
        if (index == -1) {
            return Result.failure(IllegalArgumentException("Transacción no encontrada."))
        }

        val currentTx = all[index]

        // Cálculo de variación porcentual: |rateTransition - rateOrder| / rateOrder * 100
        val deviation = if (currentTx.rateAtOrder > 0) {
            abs(rateAtTransition - currentTx.rateAtOrder) / currentTx.rateAtOrder * 100.0
        } else 0.0
        val formattedDeviation = Math.round(deviation * 100.0) / 100.0

        val newStatus: OperationStatus
        val finalObservation: String

        if (approve) {
            // Regla de auditoría estricta: NO SUPERAR 2.5% al momento de la transición
            if (formattedDeviation > 2.5) {
                return Result.failure(
                    IllegalArgumentException(
                        "REGLA DE AUDITORÍA: La desviación al momento de la transición (${formattedDeviation}%) supera el límite permitido de 2.5%. No puede aprobarse como exitosa."
                    )
                )
            }
            newStatus = OperationStatus.EXITOSA
            finalObservation = if (observation.isBlank()) {
                "Aprobada por auditoría. Desviación en transición ($formattedDeviation% <= 2.5%) cumple normativa oficial."
            } else observation

            // Ejecución contable efectiva en la billetera
            applyWalletMovement(currentTx)
        } else {
            newStatus = OperationStatus.RECHAZADA
            finalObservation = if (observation.isBlank()) {
                "Rechazada por auditoría. Desviación detectada: $formattedDeviation%."
            } else observation
        }

        val auditedOp = currentTx.copy(
            rateAtTransition = rateAtTransition,
            percentageDeviation = formattedDeviation,
            status = newStatus,
            auditObservation = finalObservation,
            auditedBy = adminName,
            auditedAt = System.currentTimeMillis()
        )

        all[index] = auditedOp
        saveTransactions(all)

        return Result.success(auditedOp)
    }

    private fun applyWalletMovement(op: Operation) {
        val wallet = getWallet(op.userId)
        val currentForeignBalances = wallet.balances.toMutableMap()
        val currentForeign = currentForeignBalances[op.currencyCode] ?: 0.0

        val updatedWallet = if (op.operationType == OperationType.COMPRA) {
            // Usuario compra divisa: paga COP, recibe divisa
            currentForeignBalances[op.currencyCode] = currentForeign + op.amount
            wallet.copy(
                copBalance = (wallet.copBalance - op.totalCop).coerceAtLeast(0.0),
                balances = currentForeignBalances
            )
        } else {
            // Usuario vende divisa: entrega divisa, recibe COP
            currentForeignBalances[op.currencyCode] = (currentForeign - op.amount).coerceAtLeast(0.0)
            wallet.copy(
                copBalance = wallet.copBalance + op.totalCop,
                balances = currentForeignBalances
            )
        }

        saveWallet(updatedWallet)
    }

    private fun saveTransactions(transactions: List<Operation>) {
        val array = JSONArray()
        for (tx in transactions) {
            val obj = JSONObject().apply {
                put("id", tx.id)
                put("userId", tx.userId)
                put("userEmail", tx.userEmail)
                put("userName", tx.userName)
                put("operationType", tx.operationType.name)
                put("currencyCode", tx.currencyCode)
                put("amount", tx.amount)
                put("rateAtOrder", tx.rateAtOrder)
                put("totalCop", tx.totalCop)
                put("timestamp", tx.timestamp)
                put("rateAtTransition", tx.rateAtTransition)
                put("percentageDeviation", tx.percentageDeviation)
                put("status", tx.status.name)
                put("auditObservation", tx.auditObservation)
                if (tx.auditedBy != null) put("auditedBy", tx.auditedBy)
                if (tx.auditedAt != null) put("auditedAt", tx.auditedAt)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_TRANSACTIONS, array.toString()).apply()
    }
}
