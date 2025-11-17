package com.example.plantdiscoveryjournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.plantdiscoveryjournal.data.local.database.AppDatabase
import com.example.plantdiscoveryjournal.data.repository.AuthRepository
import com.example.plantdiscoveryjournal.data.repository.DiscoveryRepository
import com.example.plantdiscoveryjournal.ui.navigation.AppNavGraph
import com.example.plantdiscoveryjournal.ui.viewmodel.AuthViewModel
import com.example.plantdiscoveryjournal.ui.viewmodel.CaptureViewModel
import com.example.plantdiscoveryjournal.ui.viewmodel.DetailViewModel
import com.example.plantdiscoveryjournal.ui.viewmodel.JournalViewModel
import com.example.plantdiscoveryjournal.ui.theme.PlantDiscoveryTheme

class MainActivity : ComponentActivity() {

    // Repositories
    private lateinit var authRepository: AuthRepository
    private lateinit var discoveryRepository: DiscoveryRepository

    // ViewModels
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialiser les repositories
        val database = AppDatabase.getDatabase(applicationContext)
        authRepository = AuthRepository()
        discoveryRepository = DiscoveryRepository(
            discoveryDao = database.discoveryDao(),
            context = applicationContext
        )

        // Initialiser le AuthViewModel
        authViewModel = AuthViewModel(authRepository)

        setContent {
            PlantDiscoveryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    AppNavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
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