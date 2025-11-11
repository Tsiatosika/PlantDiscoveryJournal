package com.example.plantdiscoveryjournal.ui.screens.capture

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Permission caméra
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val cameraPermissionStatus = cameraPermission.status

    // Launcher pour la caméra
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, photoUri!!))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, photoUri)
                }
                capturedBitmap = bitmap
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur de chargement: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher pour la galerie
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                capturedBitmap = bitmap
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur de chargement: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Gérer les états
    LaunchedEffect(uiState) {
        when (uiState) {
            is CaptureUiState.Success -> {
                onNavigateToDetail((uiState as CaptureUiState.Success).discoveryId)
                viewModel.resetState()
            }
            is CaptureUiState.Error -> {
                Toast.makeText(
                    context,
                    (uiState as CaptureUiState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle Découverte") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is CaptureUiState.Processing -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Aperçu de l'image capturée
                        if (capturedBitmap != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Image(
                                    bitmap = capturedBitmap!!.asImageBitmap(),
                                    contentDescription = "Image capturée",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Boutons d'action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { capturedBitmap = null },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Annuler")
                                }

                                Button(
                                    onClick = { viewModel.processImage(capturedBitmap!!) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Identifier")
                                }
                            }
                        } else {
                            // Boutons de sélection d'image
                            Spacer(modifier = Modifier.weight(1f))

                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )

                            Text(
                                text = "Capturez ou sélectionnez une image",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = {
                                    if (cameraPermissionStatus.isGranted) {
                                        val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                                        photoUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        cameraLauncher.launch(photoUri)
                                    } else {
                                        cameraPermission.launchPermissionRequest()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Prendre une Photo", style = MaterialTheme.typography.titleMedium)
                            }

                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Sélectionner de la Galerie", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}