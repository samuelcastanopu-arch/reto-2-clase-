package com.divisas.simulator.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    data object Auth : Screen("auth", "Sesión")
    data object Trading : Screen("trading", "Compra/Venta")
    data object AdminAudit : Screen("admin_audit", "Auditoría")
    data object Multimedia : Screen("multimedia", "Multimedia")
}
