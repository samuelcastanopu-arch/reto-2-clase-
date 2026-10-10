package com.divisas.simulator.domain.model

enum class UserRole {
    CLIENT,
    ADMIN
}

enum class AccountStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED
}

data class User(
    val uid: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val documentType: String? = null,
    val documentNumber: String? = null,
    val phoneNumber: String,
    val birthDate: Long? = null,
    val address: String? = null,
    val photoUrl: String? = null,
    val photoPublicId: String? = null,
    val role: UserRole = UserRole.CLIENT,
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = "$firstName $lastName".trim()

    val isAdmin: Boolean
        get() = role == UserRole.ADMIN
}
