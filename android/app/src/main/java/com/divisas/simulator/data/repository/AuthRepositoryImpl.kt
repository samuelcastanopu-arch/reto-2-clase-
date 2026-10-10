package com.divisas.simulator.data.repository

import com.divisas.simulator.core.constants.AppConstants
import com.divisas.simulator.core.result.Resource
import com.divisas.simulator.data.model.UserDto
import com.divisas.simulator.domain.model.AccountStatus
import com.divisas.simulator.domain.model.User
import com.divisas.simulator.domain.model.UserRole
import com.divisas.simulator.domain.repository.AuthRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, pass: String): Resource<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user ?: return Resource.Error("Usuario no encontrado.")

            // Forzar actualización del token para obtener Custom Claims frescos (role)
            val tokenResult = firebaseUser.getIdToken(true).await()
            val roleClaim = tokenResult.claims["role"] as? String ?: "client"

            val docSnapshot = firestore.collection(AppConstants.Collections.USERS)
                .doc(firebaseUser.uid)
                .get()
                .await()

            val user = if (docSnapshot.exists()) {
                val dto = docSnapshot.toObject(UserDto::class.java)
                dto?.copy(role = roleClaim)?.toDomain() ?: createDefaultUser(firebaseUser.uid, email, roleClaim)
            } else {
                createDefaultUser(firebaseUser.uid, email, roleClaim)
            }

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al iniciar sesión.", e)
        }
    }

    override suspend fun register(
        email: String,
        pass: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Resource<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user ?: return Resource.Error("No se pudo registrar el usuario.")

            val newUserDto = UserDto(
                uid = firebaseUser.uid,
                email = email,
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phone,
                role = "client",
                accountStatus = "ACTIVE",
                createdAt = Timestamp.now()
            )

            firestore.collection(AppConstants.Collections.USERS)
                .doc(firebaseUser.uid)
                .set(newUserDto)
                .await()

            Resource.Success(newUserDto.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al registrar el usuario.", e)
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            auth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al cerrar sesión.", e)
        }
    }

    override fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                val listenerRegistration = firestore.collection(AppConstants.Collections.USERS)
                    .doc(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(null)
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            val dto = snapshot.toObject(UserDto::class.java)
                            trySend(dto?.toDomain())
                        } else {
                            trySend(createDefaultUser(firebaseUser.uid, firebaseUser.email ?: "", "client"))
                        }
                    }
            }
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return try {
            val snapshot = firestore.collection(AppConstants.Collections.USERS)
                .doc(firebaseUser.uid)
                .get()
                .await()
            snapshot.toObject(UserDto::class.java)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateProfile(
        firstName: String,
        lastName: String,
        phone: String,
        address: String?,
        documentType: String?,
        documentNumber: String?
    ): Resource<Unit> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Usuario no autenticado.")
        return try {
            val updates = mutableMapOf<String, Any?>(
                "firstName" to firstName,
                "lastName" to lastName,
                "phoneNumber" to phone,
                "address" to address,
                "documentType" to documentType,
                "documentNumber" to documentNumber,
                "updatedAt" to Timestamp.now()
            )
            firestore.collection(AppConstants.Collections.USERS)
                .doc(uid)
                .update(updates)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al actualizar perfil.", e)
        }
    }

    private fun createDefaultUser(uid: String, email: String, role: String): User {
        return User(
            uid = uid,
            email = email,
            firstName = "Usuario",
            lastName = "",
            phoneNumber = "",
            role = if (role.lowercase() == "admin") UserRole.ADMIN else UserRole.CLIENT,
            accountStatus = AccountStatus.ACTIVE
        )
    }
}
