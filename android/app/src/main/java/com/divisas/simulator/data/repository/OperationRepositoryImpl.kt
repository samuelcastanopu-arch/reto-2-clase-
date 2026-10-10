package com.divisas.simulator.data.repository

import com.divisas.simulator.core.constants.AppConstants
import com.divisas.simulator.core.result.Resource
import com.divisas.simulator.data.model.OperationDto
import com.divisas.simulator.domain.model.Operation
import com.divisas.simulator.domain.model.OperationStatus
import com.divisas.simulator.domain.model.OperationType
import com.divisas.simulator.domain.repository.OperationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

class OperationRepositoryImpl @Inject constructor(
    private val functions: FirebaseFunctions,
    private val firestore: FirebaseFirestore
) : OperationRepository {

    override suspend fun simulateOperation(
        currencyCode: String,
        operationType: OperationType,
        amount: BigDecimal,
        appliedRate: BigDecimal
    ): Resource<Operation> {
        return try {
            // Generación de UUID v4 para idempotencia estricta
            val idempotencyKey = UUID.randomUUID().toString()

            val requestData = hashMapOf(
                "idempotencyKey" to idempotencyKey,
                "currencyCode" to currencyCode.uppercase(),
                "operationType" to operationType.name,
                "amount" to amount.toDouble(),
                "appliedRate" to appliedRate.toDouble()
            )

            val functionResult = functions
                .getHttpsCallable(AppConstants.Functions.SIMULATE_OPERATION)
                .call(requestData)
                .await()

            @Suppress("UNCHECKED_CAST")
            val data = functionResult.getData() as? Map<String, Any>
                ?: return Resource.Error("Respuesta inesperada del servidor.")

            val opId = data["operationId"] as? String ?: ""
            val statusStr = data["status"] as? String ?: "REJECTED"
            val rejectionReason = data["rejectionReason"] as? String
            val retBaseRate = (data["baseRate"] as? Number)?.toDouble() ?: 0.0
            val retAppliedRate = (data["appliedRate"] as? Number)?.toDouble() ?: appliedRate.toDouble()
            val retDeviation = (data["percentageDeviation"] as? Number)?.toDouble() ?: 0.0
            val retTotalCop = (data["totalCop"] as? Number)?.toDouble() ?: 0.0

            val operation = Operation(
                id = opId,
                idempotencyKey = idempotencyKey,
                userId = "",
                userEmail = "",
                operationType = operationType,
                currencyCode = currencyCode.uppercase(),
                amount = amount,
                baseRate = BigDecimal.valueOf(retBaseRate),
                appliedRate = BigDecimal.valueOf(retAppliedRate),
                percentageDeviation = BigDecimal.valueOf(retDeviation),
                totalCop = BigDecimal.valueOf(retTotalCop),
                status = if (statusStr == "APPROVED") OperationStatus.APPROVED else OperationStatus.REJECTED,
                rejectionReason = rejectionReason,
                createdAt = System.currentTimeMillis()
            )

            Resource.Success(operation)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al simular operación en el servidor.", e)
        }
    }

    override fun getUserOperationsFlow(userId: String): Flow<List<Operation>> = callbackFlow {
        val listener = firestore.collection(AppConstants.Collections.OPERATIONS)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(OperationDto::class.java)?.toDomain()
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override fun getAllOperationsFlow(): Flow<List<Operation>> = callbackFlow {
        val listener = firestore.collection(AppConstants.Collections.OPERATIONS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(OperationDto::class.java)?.toDomain()
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }
}
