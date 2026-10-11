package com.divisas.simulator.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.divisas.simulator.domain.model.UserRole
import com.divisas.simulator.presentation.screens.admin.AdminAuditScreen
import com.divisas.simulator.presentation.screens.auth.AuthScreen
import com.divisas.simulator.presentation.screens.multimedia.MultimediaScreen
import com.divisas.simulator.presentation.screens.trading.TradingScreen
import com.divisas.simulator.presentation.viewmodel.AdminAuditViewModel
import com.divisas.simulator.presentation.viewmodel.AuthViewModel
import com.divisas.simulator.presentation.viewmodel.MultimediaViewModel
import com.divisas.simulator.presentation.viewmodel.TradingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    tradingViewModel: TradingViewModel = viewModel(),
    adminAuditViewModel: AdminAuditViewModel = viewModel(),
    multimediaViewModel: MultimediaViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Auth.route

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🪙 Divisas Simulator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (currentUser != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentUser?.isAdmin == true) Color(0xFF8B5CF6) else Color(0xFF38BDF8)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (currentUser?.isAdmin == true) "ADMIN" else "CLIENTE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (currentUser != null) {
                        // Botón de alternancia rápida de rol para facilitar la evaluación del docente
                        IconButton(onClick = {
                            val nextRole = if (currentUser?.isAdmin == true) UserRole.CLIENT else UserRole.ADMIN
                            authViewModel.switchRoleForDemo(nextRole)
                            if (nextRole == UserRole.ADMIN) {
                                navController.navigate(Screen.AdminAudit.route)
                            } else {
                                navController.navigate(Screen.Trading.route)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Cambiar Rol",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1E3A8A)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // Vista 1: Registro y Sesión
                NavigationBarItem(
                    selected = currentRoute == Screen.Auth.route,
                    onClick = {
                        if (currentRoute != Screen.Auth.route) {
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Sesión") },
                    label = { Text("1. Sesión", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E3A8A),
                        selectedTextColor = Color(0xFF1E3A8A),
                        indicatorColor = Color(0xFFE0E7FF)
                    )
                )

                // Vista 2: Compra y Venta (Finnhub)
                NavigationBarItem(
                    selected = currentRoute == Screen.Trading.route,
                    onClick = {
                        if (currentRoute != Screen.Trading.route) {
                            navController.navigate(Screen.Trading.route)
                        }
                    },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Compra/Venta") },
                    label = { Text("2. Compra/Venta", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E3A8A),
                        selectedTextColor = Color(0xFF1E3A8A),
                        indicatorColor = Color(0xFFE0E7FF)
                    )
                )

                // Vista 3: Auditoría de Administrador (Regla 2.5%)
                NavigationBarItem(
                    selected = currentRoute == Screen.AdminAudit.route,
                    onClick = {
                        if (currentRoute != Screen.AdminAudit.route) {
                            navController.navigate(Screen.AdminAudit.route)
                        }
                    },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Auditoría") },
                    label = { Text("3. Auditoría", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF673AB7),
                        selectedTextColor = Color(0xFF673AB7),
                        indicatorColor = Color(0xFFEDE7F6)
                    )
                )

                // Vista 4: Uso de servicio para generar multimedia
                NavigationBarItem(
                    selected = currentRoute == Screen.Multimedia.route,
                    onClick = {
                        if (currentRoute != Screen.Multimedia.route) {
                            navController.navigate(Screen.Multimedia.route)
                        }
                    },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Multimedia") },
                    label = { Text("4. Multimedia", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F172A),
                        selectedTextColor = Color(0xFF0F172A),
                        indicatorColor = Color(0xFFE2E8F0)
                    )
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Auth.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // VISTA 1: Registro e Inicio de Sesión
            composable(Screen.Auth.route) {
                AuthScreen(
                    authViewModel = authViewModel,
                    onNavigateToTrading = { navController.navigate(Screen.Trading.route) },
                    onNavigateToAdmin = { navController.navigate(Screen.AdminAudit.route) }
                )
            }

            // VISTA 2: Compra y Venta de Divisas (Finnhub)
            composable(Screen.Trading.route) {
                TradingScreen(
                    currentUser = currentUser,
                    tradingViewModel = tradingViewModel
                )
            }

            // VISTA 3: Auditoría de Administrador (Regla 2.5%)
            composable(Screen.AdminAudit.route) {
                AdminAuditScreen(
                    currentUser = currentUser,
                    adminAuditViewModel = adminAuditViewModel
                )
            }

            // VISTA 4: Uso de servicio para generar multimedia
            composable(Screen.Multimedia.route) {
                MultimediaScreen(
                    multimediaViewModel = multimediaViewModel
                )
            }
        }
    }
}
