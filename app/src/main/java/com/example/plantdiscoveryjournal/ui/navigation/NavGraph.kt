package com.example.plantdiscoveryjournal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.plantdiscoveryjournal.ui.screens.auth.AuthScreen
import com.example.plantdiscoveryjournal.ui.screens.auth.AuthState
import com.example.plantdiscoveryjournal.ui.screens.auth.AuthViewModel
import com.example.plantdiscoveryjournal.ui.screens.capture.CaptureScreen
import com.example.plantdiscoveryjournal.ui.screens.capture.CaptureViewModel
import com.example.plantdiscoveryjournal.ui.screens.detail.DetailScreen
import com.example.plantdiscoveryjournal.ui.screens.detail.DetailViewModel
import com.example.plantdiscoveryjournal.ui.screens.journal.JournalScreen
import com.example.plantdiscoveryjournal.ui.screens.journal.JournalViewModel

/**
 * Définitions des routes de navigation
 */
sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Journal : Screen("journal")
    object Capture : Screen("capture")
    object Detail : Screen("detail/{discoveryId}") {
        fun createRoute(discoveryId: Long) = "detail/$discoveryId"
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    getJournalViewModel: (String) -> JournalViewModel,
    getCaptureViewModel: (String) -> CaptureViewModel,
    getDetailViewModel: (Long) -> DetailViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    // Déterminer le point de départ
    val startDestination = when (authState) {
        is AuthState.Authenticated -> Screen.Journal.route
        else -> Screen.Auth.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Écran d'authentification
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.Journal.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // Écran liste du journal
        composable(Screen.Journal.route) {
            val userId = (authState as? AuthState.Authenticated)?.userId ?: return@composable
            val viewModel = getJournalViewModel(userId)

            JournalScreen(
                viewModel = viewModel,
                onNavigateToCapture = {
                    navController.navigate(Screen.Capture.route)
                },
                onNavigateToDetail = { discoveryId ->
                    navController.navigate(Screen.Detail.createRoute(discoveryId))
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Écran de capture
        composable(Screen.Capture.route) {
            val userId = (authState as? AuthState.Authenticated)?.userId ?: return@composable
            val viewModel = getCaptureViewModel(userId)

            CaptureScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDetail = { discoveryId ->
                    navController.navigate(Screen.Detail.createRoute(discoveryId)) {
                        popUpTo(Screen.Journal.route)
                    }
                }
            )
        }

        // Écran détail de découverte
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("discoveryId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val discoveryId = backStackEntry.arguments?.getLong("discoveryId") ?: return@composable
            val viewModel = getDetailViewModel(discoveryId)

            DetailScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}