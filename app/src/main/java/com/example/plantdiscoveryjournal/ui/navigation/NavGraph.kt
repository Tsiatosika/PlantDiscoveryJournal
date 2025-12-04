package com.example.plantdiscoveryjournal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.plantdiscoveryjournal.ui.screens.auth.LoginScreen
import com.example.plantdiscoveryjournal.ui.screens.auth.SignUpScreen
import com.example.plantdiscoveryjournal.ui.screens.capture.CaptureScreen
import com.example.plantdiscoveryjournal.ui.screens.detail.DetailScreen
import com.example.plantdiscoveryjournal.ui.screens.journal.JournalScreen
import com.example.plantdiscoveryjournal.ui.viewmodel.AuthState
import com.example.plantdiscoveryjournal.ui.viewmodel.AuthViewModel
import com.example.plantdiscoveryjournal.ui.viewmodel.CaptureViewModel
import com.example.plantdiscoveryjournal.ui.viewmodel.DetailViewModel
import com.example.plantdiscoveryjournal.ui.viewmodel.JournalViewModel
import com.example.plantdiscoveryjournal.ui.viewmodel.ThemeViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
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
    themeViewModel: ThemeViewModel,
    getJournalViewModel: (String) -> JournalViewModel,
    getCaptureViewModel: (String) -> CaptureViewModel,
    getDetailViewModel: (Long) -> DetailViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    val startDestination = when (authState) {
        is AuthState.Authenticated -> Screen.Journal.route
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.Journal.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.Journal.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

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
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                themeViewModel = themeViewModel
            )
        }

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
