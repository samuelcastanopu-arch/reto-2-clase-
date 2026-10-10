package com.divisas.simulator.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.divisas.simulator.domain.model.UserRole
import com.divisas.simulator.presentation.admin.AdminDashboardScreen
import com.divisas.simulator.presentation.admin.AdminViewModel
import com.divisas.simulator.presentation.auth.AuthViewModel
import com.divisas.simulator.presentation.auth.LoginScreen
import com.divisas.simulator.presentation.auth.RegisterScreen
import com.divisas.simulator.presentation.history.HistoryScreen
import com.divisas.simulator.presentation.history.HistoryViewModel
import com.divisas.simulator.presentation.home.HomeScreen
import com.divisas.simulator.presentation.home.HomeViewModel
import com.divisas.simulator.presentation.operations.OperationsScreen
import com.divisas.simulator.presentation.operations.OperationsViewModel
import com.divisas.simulator.presentation.profile.ProfileScreen
import com.divisas.simulator.presentation.profile.ProfileViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ViewModel compartido o de home para conocer el rol
    val homeViewModel: HomeViewModel = hiltViewModel()
    val homeState by homeViewModel.uiState.collectAsState()
    val isAdmin = homeState.currentUser?.role == UserRole.ADMIN

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Operations.route,
        Screen.History.route,
        Screen.Admin.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    navController = navController,
                    isAdmin = isAdmin
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Autenticación
            composable(Screen.Login.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onLoginSuccess = {
                        homeViewModel.loadData()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Register.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onRegisterSuccess = {
                        homeViewModel.loadData()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Pantallas Principales
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToOperations = { currency, type ->
                        navController.navigate(Screen.Operations.route)
                    },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                )
            }

            composable(Screen.Operations.route) {
                val operationsViewModel: OperationsViewModel = hiltViewModel()
                OperationsScreen(viewModel = operationsViewModel)
            }

            composable(Screen.History.route) {
                val historyViewModel: HistoryViewModel = hiltViewModel()
                HistoryScreen(viewModel = historyViewModel)
            }

            composable(Screen.Admin.route) {
                val adminViewModel: AdminViewModel = hiltViewModel()
                AdminDashboardScreen(viewModel = adminViewModel)
            }

            composable(Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogoutSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
