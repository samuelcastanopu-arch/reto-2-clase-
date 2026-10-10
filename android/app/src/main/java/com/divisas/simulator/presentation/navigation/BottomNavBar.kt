package com.divisas.simulator.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.divisas.simulator.presentation.theme.EmeraldPrimary

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val adminOnly: Boolean = false
)

@Composable
fun BottomNavBar(
    navController: NavController,
    isAdmin: Boolean
) {
    val items = listOf(
        BottomNavItem("Inicio", Screen.Home.route, Icons.Default.Home),
        BottomNavItem("Operaciones", Screen.Operations.route, Icons.Default.SwapHoriz),
        BottomNavItem("Historial", Screen.History.route, Icons.Default.History),
        BottomNavItem("Admin", Screen.Admin.route, Icons.Default.AdminPanelSettings, adminOnly = true),
        BottomNavItem("Perfil", Screen.Profile.route, Icons.Default.Person)
    ).filter { !it.adminOnly || isAdmin }

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = EmeraldPrimary.copy(alpha = 0.15f),
                    selectedIconColor = EmeraldPrimary,
                    selectedTextColor = EmeraldPrimary
                )
            )
        }
    }
}
