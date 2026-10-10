package com.divisas.simulator.data.repository

import com.divisas.simulator.core.constants.AppConstants
import com.divisas.simulator.core.result.Resource
import com.divisas.simulator.data.model.RateHistoryDto
import com.divisas.simulator.domain.model.AuditLog
import com.divisas.simulator.domain.model.RateHistory
import com.divisas.simulator.domain.repository.AdminRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.math.BigDecimal
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val functions: FirebaseFunctions,
    private val firestore: FirebaseFirestore
) : AdminRepository {

    override suspend fun updateBaseRate(
        currencyCode: String,
        newRate: BigDecimal,
        reason: String
    ): Resource<Unit> {
        return try {
            val requestData = hashMapOf(
                "currencyCode" to currencyCode.uppercase(),
                "newRate" to newRate.toDouble(),
                "reason" to reason
            )

            functions
                .getHttpsCallable(AppConstants.Functions.UPDATE_BASE_RATE)
                .call(requestData)
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al actualizar tasa base.", e)
        }
    }

    override fun getRateHistoryFlow(currencyCode: String?): Flow<List<RateHistory>> = callbackFlow {
        var query: Query = firestore.collection(AppConstants.Collections.RATE_HISTORY)
            .orderBy("changedAt", Query.Direction.DESCENDING)

        if (!currencyCode.isNullOrBlank()) {
            query = query.whereEqualTo("currencyCode", currencyCode.uppercase())
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(RateHistoryDto::class.java)?.toDomain()
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    override fun getAuditLogsFlow(): Flow<List<AuditLog>> = callbackFlow {
        val listener = firestore.collection(AppConstants.Collections.AUDIT_LOGS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val action = doc.getString("action") ?: ""
                    val performedBy = doc.getString("performedBy") ?: ""
                    val performedByEmail = doc.getString("performedByEmail") ?: ""
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: System.currentTimeMillis()
                    @Suppress("UNCHECKED_CAST")
                    val metadata = (doc.get("metadata") as? Map<String, Any>) ?: emptyMap()

                    AuditLog(
                        id = doc.id,
                        action = action,
                        performedBy = performedBy,
                        performedByEmail = performedByEmail,
                        timestamp = timestamp,
                        details = metadata
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }
}
