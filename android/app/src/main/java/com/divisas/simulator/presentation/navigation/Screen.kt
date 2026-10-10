package com.divisas.simulator.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    data object Login : Screen("login", "Iniciar Sesión")
    data object Register : Screen("register", "Registro")
    data object Home : Screen("home", "Inicio")
    data object Operations : Screen("operations", "Operaciones")
    data object History : Screen("history", "Historial")
    data object Admin : Screen("admin", "Administración")
    data object Profile : Screen("profile", "Mi Perfil")
}
