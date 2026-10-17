package com.divisas.simulator.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
        containerColor = Color(0xFF090D16),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentUser?.photoUrl?.isNotBlank() == true) {
                            AsyncImage(
                                model = currentUser?.photoUrl,
                                contentDescription = "Foto de cliente",
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "🪙 DIVISAS PRO",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp,
                            color = Color(0xFFF8FAFC)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (currentUser != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentUser?.isAdmin == true) Color(0xFF7C3AED) else Color(0xFF0284C7)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (currentUser?.isAdmin == true) "ADMIN" else "USUARIO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (currentUser != null) {
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
                                contentDescription = "Alternar Rol",
                                tint = Color(0xFF38BDF8)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0D131F)
                ),
                modifier = Modifier.border(width = 0.5.dp, color = Color(0xFF1E293B))
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0D131F),
                tonalElevation = 0.dp,
                modifier = Modifier.border(width = 0.5.dp, color = Color(0xFF1E293B))
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
                    label = { Text("1. Sesión", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF38BDF8),
                        selectedTextColor = Color(0xFF38BDF8),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color(0xFF16253B)
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
                    label = { Text("2. Compra/Venta", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF10B981),
                        selectedTextColor = Color(0xFF10B981),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color(0xFF063327)
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
                    label = { Text("3. Auditoría", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFA78BFA),
                        selectedTextColor = Color(0xFFA78BFA),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color(0xFF2E1065)
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
                    label = { Text("4. Multimedia", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFF59E0B),
                        selectedTextColor = Color(0xFFF59E0B),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color(0xFF332005)
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
