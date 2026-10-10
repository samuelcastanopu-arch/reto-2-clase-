package com.divisas.simulator.data.repository

import com.divisas.simulator.core.constants.AppConstants
import com.divisas.simulator.core.result.Resource
import com.divisas.simulator.data.model.CurrencyDto
import com.divisas.simulator.domain.model.Currency
import com.divisas.simulator.domain.repository.CurrencyRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CurrencyRepository {

    override fun getCurrenciesFlow(): Flow<List<Currency>> = callbackFlow {
        val listener = firestore.collection(AppConstants.Collections.CURRENCIES)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CurrencyDto::class.java)?.toDomain()
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getCurrencyByCode(code: String): Resource<Currency> {
        return try {
            val doc = firestore.collection(AppConstants.Collections.CURRENCIES)
                .doc(code.uppercase())
                .get()
                .await()

            val dto = doc.toObject(CurrencyDto::class.java)
            if (dto != null) {
                Resource.Success(dto.toDomain())
            } else {
                Resource.Error("Divisa no encontrada.")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al obtener divisa.", e)
        }
    }
}
