package com.divisas.simulator.domain.model

enum class UserRole {
    CLIENT, // Usuario que compra y vende divisas
    ADMIN   // Administrador que audita transacciones
}

data class User(
    val uid: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole = UserRole.CLIENT,
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = "$firstName $lastName".trim()

    val isAdmin: Boolean
        get() = role == UserRole.ADMIN
}
