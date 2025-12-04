package com.example.plantdiscoveryjournal.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.plantdiscoveryjournal.R
import com.example.plantdiscoveryjournal.domain.model.Discovery
import com.example.plantdiscoveryjournal.ui.theme.PrimaryGreen
import com.example.plantdiscoveryjournal.ui.theme.TextGray
import com.example.plantdiscoveryjournal.ui.viewmodel.JournalSortOption
import com.example.plantdiscoveryjournal.ui.viewmodel.JournalViewModel
import com.example.plantdiscoveryjournal.ui.viewmodel.ThemeViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    onNavigateToCapture: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onSignOut: () -> Unit,
    themeViewModel: ThemeViewModel
) {
    val discoveries by viewModel.discoveries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()

    val isDark by themeViewModel.isDarkTheme.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Rechercher une plante...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = colorScheme.primary
                            ),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = colorScheme.primary
                                )
                            }
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = colorScheme.primary.copy(alpha = 0.08f)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Mes",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = "découvertes",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = { themeViewModel.toggleTheme() }) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Changer de thème",
                                tint = colorScheme.primary
                            )
                        }

                        if (isSearchActive) {
                            IconButton(onClick = { viewModel.clearSearch() }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Fermer la recherche",
                                    tint = colorScheme.onSurface
                                )
                            }
                        } else {
                            IconButton(onClick = { viewModel.toggleSearch() }) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Rechercher",
                                    tint = colorScheme.primary
                                )
                            }

                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    Icons.Default.Sort,
                                    contentDescription = "Trier",
                                    tint = colorScheme.primary
                                )
                            }

                            IconButton(onClick = { showSignOutDialog = true }) {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = "Déconnexion",
                                    tint = TextGray
                                )
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Plus récentes",
                                    fontWeight =
                                        if (sortOption == JournalSortOption.MOST_RECENT)
                                            FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                viewModel.setSortOption(JournalSortOption.MOST_RECENT)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "A → Z",
                                    fontWeight =
                                        if (sortOption == JournalSortOption.NAME_ASC)
                                            FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                viewModel.setSortOption(JournalSortOption.NAME_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Z → A",
                                    fontWeight =
                                        if (sortOption == JournalSortOption.NAME_DESC)
                                            FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                viewModel.setSortOption(JournalSortOption.NAME_DESC)
                                showSortMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface
                ),
                modifier = Modifier.shadow(2.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCapture,
                containerColor = PrimaryGreen,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(8.dp, CircleShape)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Nouvelle découverte",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryGreen,
                        strokeWidth = 3.dp
                    )
                }

                discoveries.isEmpty() -> {
                    if (isSearchActive && searchQuery.isNotBlank()) {
                        NoResultsView(modifier = Modifier.align(Alignment.Center))
                    } else {
                        EmptyStateView(modifier = Modifier.align(Alignment.Center))
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${discoveries.size} plante${if (discoveries.size > 1) "s" else ""} découverte${if (discoveries.size > 1) "s" else ""}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextGray
                                )

                                Surface(
                                    color = PrimaryGreen.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.logo),
                                            contentDescription = null,
                                            tint = PrimaryGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Journal",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = PrimaryGreen
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val categories = listOf("Toutes", "Fleur", "Arbre", "Insecte", "Autre")

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                categories.forEach { cat ->
                                    FilterChip(
                                        selected = (categoryFilter ?: "Toutes") == cat,
                                        onClick = { viewModel.setCategoryFilter(cat) },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                        }

                        items(
                            items = discoveries,
                            key = { it.id }
                        ) { discovery ->
                            DiscoveryCard(
                                discovery = discovery,
                                onClick = { onNavigateToDetail(discovery.id) }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }

            error?.let { errorMessage ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.error,
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK", color = Color.White)
                        }
                    }
                ) {
                    Text(errorMessage, color = Color.White)
                }
            }

            if (showSignOutDialog) {
                AlertDialog(
                    onDismissRequest = { showSignOutDialog = false },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.logo),
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(40.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Déconnexion",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    text = {
                        Text(
                            text = "Êtes-vous sûr de vouloir vous déconnecter de votre compte ?",
                            fontSize = 16.sp,
                            color = TextGray
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSignOutDialog = false
                                onSignOut()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Se déconnecter",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showSignOutDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Annuler",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = colorScheme.surface
                )
            }
        }
    }
}

@Composable
fun DiscoveryCard(
    discovery: Discovery,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                AsyncImage(
                    model = File(discovery.imageLocalPath),
                    contentDescription = discovery.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                ),
                                startY = 0f,
                                endY = 600f
                            )
                        )
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    color = PrimaryGreen,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Identifiée",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = discovery.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = PrimaryGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = discovery.category,
                            fontSize = 11.sp,
                            color = PrimaryGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = discovery.getFormattedDate(),
                        fontSize = 13.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 1.dp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Voir les détails",
                        fontSize = 13.sp,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = PrimaryGreen.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Aucune découverte",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Commencez votre aventure botanique en découvrant votre première plante.",
            fontSize = 15.sp,
            color = TextGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun NoResultsView(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = TextGray.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = TextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Aucun résultat",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Essayez une autre recherche ou découvrez de nouvelles plantes.",
            fontSize = 14.sp,
            color = TextGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}
