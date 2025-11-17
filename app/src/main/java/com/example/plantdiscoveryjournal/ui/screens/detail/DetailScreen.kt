package com.example.plantdiscoveryjournal.ui.screens.detail

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.plantdiscoveryjournal.domain.model.Discovery
import com.example.plantdiscoveryjournal.ui.theme.*
import com.example.plantdiscoveryjournal.ui.viewmodel.DetailUiState
import com.example.plantdiscoveryjournal.ui.viewmodel.DetailViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Gérer l'état de suppression
    LaunchedEffect(uiState) {
        if (uiState is DetailUiState.Deleted) {
            Toast.makeText(context, "Discovery deleted", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Discovery Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextBlack)
                    }
                },
                actions = {
                    if (uiState is DetailUiState.Success) {
                        IconButton(onClick = { /* Share */ }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share",
                                tint = TextBlack
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = TextBlack
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite,
                    titleContentColor = TextBlack
                )
            )
        },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryGreen
                    )
                }
                is DetailUiState.Success -> {
                    DiscoveryDetailContent(
                        discovery = state.discovery,
                        onDeleteClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is DetailUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = ErrorRed
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            fontSize = 16.sp,
                            color = ErrorRed
                        )
                    }
                }
                is DetailUiState.Deleted -> {
                    // Géré par LaunchedEffect
                }
            }
        }
    }

    // Dialogue de confirmation de suppression
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed)
            },
            title = {
                Text(
                    "Delete this discovery?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    "This action is irreversible. The discovery will be permanently deleted.",
                    color = TextGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDiscovery()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = TextBlack
                    )
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun DiscoveryDetailContent(
    discovery: Discovery,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        // Image principale avec coins arrondis
        AsyncImage(
            model = File(discovery.imageLocalPath),
            contentDescription = discovery.name,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nom de la plante
            Text(
                text = discovery.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )

            // Date de découverte
            Text(
                text = "Discovered: ${discovery.getFormattedDate()}",
                fontSize = 13.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Section "Did You Know"
            Text(
                text = discovery.aiFact,
                fontSize = 14.sp,
                color = TextDarkGray,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bouton Delete Entry
            Button(
                onClick = onDeleteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Delete Entry",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BackgroundWhite
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}