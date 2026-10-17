package com.divisas.simulator.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.divisas.simulator.domain.model.User
import com.divisas.simulator.domain.model.UserRole
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class UserRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USERS = "registered_users"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
    }

    init {
        initDefaultUsers()
    }

    private fun initDefaultUsers() {
        if (!prefs.contains(KEY_USERS)) {
            val defaultUsers = listOf(
                User(
                    uid = "admin_001",
                    email = "admin@divisas.com",
                    firstName = "Carlos",
                    lastName = "Administrador",
                    role = UserRole.ADMIN
                ),
                User(
                    uid = "user_001",
                    email = "usuario@divisas.com",
                    firstName = "Samuel",
                    lastName = "Operador",
                    role = UserRole.CLIENT
                )
            )
            saveUsersList(defaultUsers)
            // Guardar usuario por defecto activo (Samuel)
            saveCurrentUserId("user_001")
        }
    }

    fun getAllUsers(): List<User> {
        val jsonStr = prefs.getString(KEY_USERS, null) ?: return emptyList()
        val list = mutableListOf<User>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val role = try { UserRole.valueOf(obj.getString("role")) } catch (_: Exception) { UserRole.CLIENT }
                list.add(
                    User(
                        uid = obj.getString("uid"),
                        email = obj.getString("email"),
                        firstName = obj.getString("firstName"),
                        lastName = obj.getString("lastName"),
                        role = role,
                        photoUrl = obj.optString("photoUrl", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun getCurrentUser(): User? {
        val uid = prefs.getString(KEY_CURRENT_USER_ID, null) ?: return null
        return getAllUsers().find { it.uid == uid }
    }

    fun saveCurrentUserId(uid: String?) {
        if (uid != null) {
            prefs.edit().putString(KEY_CURRENT_USER_ID, uid).apply()
        } else {
            prefs.edit().remove(KEY_CURRENT_USER_ID).apply()
        }
    }

    fun login(email: String, password: String): Result<User> {
        val cleanEmail = email.trim().lowercase()
        val users = getAllUsers()
        val found = users.find { it.email.lowercase() == cleanEmail }
        return if (found != null) {
            saveCurrentUserId(found.uid)
            Result.success(found)
        } else {
            Result.failure(IllegalArgumentException("Usuario no encontrado con el correo $cleanEmail"))
        }
    }

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: UserRole
    ): Result<User> {
        val cleanEmail = email.trim().lowercase()
        val users = getAllUsers().toMutableList()
        if (users.any { it.email.lowercase() == cleanEmail }) {
            return Result.failure(IllegalArgumentException("El correo ya se encuentra registrado."))
        }

        val newUser = User(
            uid = "usr_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
            email = cleanEmail,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            role = role,
            createdAt = System.currentTimeMillis()
        )
        users.add(newUser)
        saveUsersList(users)
        saveCurrentUserId(newUser.uid)
        return Result.success(newUser)
    }

    fun switchRoleForTesting(newRole: UserRole): User? {
        val current = getCurrentUser() ?: return null
        val updated = current.copy(role = newRole)
        val users = getAllUsers().toMutableList()
        val index = users.indexOfFirst { it.uid == current.uid }
        if (index != -1) {
            users[index] = updated
            saveUsersList(users)
        }
        return updated
    }

    fun updateUserPhoto(userId: String, photoUrl: String): User? {
        val users = getAllUsers().toMutableList()
        val index = users.indexOfFirst { it.uid == userId }
        if (index != -1) {
            val updated = users[index].copy(photoUrl = photoUrl)
            users[index] = updated
            saveUsersList(users)
            return updated
        }
        return null
    }

    fun logout() {
        saveCurrentUserId(null)
    }

    private fun saveUsersList(users: List<User>) {
        val array = JSONArray()
        for (u in users) {
            val obj = JSONObject().apply {
                put("uid", u.uid)
                put("email", u.email)
                put("firstName", u.firstName)
                put("lastName", u.lastName)
                put("role", u.role.name)
                put("photoUrl", u.photoUrl)
                put("createdAt", u.createdAt)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_USERS, array.toString()).apply()
    }
}
