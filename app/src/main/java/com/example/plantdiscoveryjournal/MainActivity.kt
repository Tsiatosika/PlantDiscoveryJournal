package com.example.plantdiscoveryjournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.plantdiscoveryjournal.data.local.database.AppDatabase
import com.example.plantdiscoveryjournal.data.repository.AuthRepository
import com.example.plantdiscoveryjournal.data.repository.DiscoveryRepository
import com.example.plantdiscoveryjournal.ui.navigation.AppNavGraph
import com.example.plantdiscoveryjournal.ui.theme.PlantDiscoveryJournalTheme
import com.example.plantdiscoveryjournal.ui.viewmodel.AuthViewModel
import com.example.plantdiscoveryjournal.ui.viewmodel.CaptureViewModel
import com.example.plantdiscoveryjournal.ui.viewmodel.DetailViewModel
import com.example.plantdiscoveryjournal.ui.viewmodel.JournalViewModel
import com.example.plantdiscoveryjournal.ui.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var discoveryRepository: DiscoveryRepository

    private lateinit var authViewModel: AuthViewModel
    private lateinit var themeViewModel: ThemeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        authRepository = AuthRepository()
        discoveryRepository = DiscoveryRepository(
            discoveryDao = database.discoveryDao(),
            context = applicationContext
        )

        authViewModel = AuthViewModel(authRepository)
        themeViewModel = ThemeViewModel()

        setContent {
            PlantDiscoveryJournalTheme(
                darkTheme = themeViewModel.isDarkTheme.collectAsState().value
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    AppNavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        themeViewModel = themeViewModel,
                        getJournalViewModel = { userId ->
                            JournalViewModel(discoveryRepository, userId)
                        },
                        getCaptureViewModel = { userId ->
                            CaptureViewModel(discoveryRepository, userId)
                        },
                        getDetailViewModel = { discoveryId ->
                            DetailViewModel(discoveryRepository, discoveryId)
                        }
                    )
                }
            }
        }
    }
}
