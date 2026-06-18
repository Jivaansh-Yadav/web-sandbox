package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.SandboxProfile

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SandboxDashboard(
    viewModel: SandboxViewModel,
    modifier: Modifier = Modifier
) {
    val profiles by viewModel.allProfiles.collectAsState()
    val isShowingForm by viewModel.isShowingNewProfileForm.collectAsState()
    val editingProfile by viewModel.editingProfile.collectAsState()
    val activeSandbox by viewModel.activeSandbox.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            activeSandbox != null -> {
                SandboxWebView(
                    profile = activeSandbox!!,
                    onExit = { viewModel.exitSandbox() }
                )
            }
            isShowingForm -> {
                ProfileFormScreen(
                    profile = editingProfile,
                    onSave = { name, url, desktop, ptr, selectors, css, js, block, direct ->
                        viewModel.saveProfile(name, url, desktop, ptr, selectors, css, js, block, direct)
                    },
                    onCancel = { viewModel.cancelEditing() }
                )
            }
            else -> {
                DashboardMainList(
                    profiles = profiles,
                    onLaunch = { viewModel.launchSandbox(it) },
                    onEdit = { viewModel.startEditingProfile(it) },
                    onDelete = { viewModel.deleteProfile(it) },
                    onAddNew = { viewModel.startNewProfile() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardMainList(
    profiles: List<SandboxProfile>,
    onLaunch: (SandboxProfile) -> Unit,
    onEdit: (SandboxProfile) -> Unit,
    onDelete: (SandboxProfile) -> Unit,
    onAddNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var quickLaunchUrl by remember { mutableStateOf("") }
    var showQuickLaunchDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFEF7FF), // Pure High Density light background
        floatingActionButton = {
            if (selectedTab != 2) {
                FloatingActionButton(
                    onClick = onAddNew,
                    containerColor = Color(0xFFEADDFF),
                    contentColor = Color(0xFF21005D),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_sandbox_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Sandbox Instance",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        bottomBar = {
            // High Density M3 Bottom Navigation Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                color = Color(0xFFF3EDF7),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Tab 1: Home
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { selectedTab = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selectedTab == 0) Color(0xFFE8DEF8) else Color.Transparent)
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Default.Home else Icons.Default.Home,
                                    contentDescription = "Home tab",
                                    tint = if (selectedTab == 0) Color(0xFF1D192B) else Color(0xFF49454F),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Home",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) Color(0xFF1D1B20) else Color(0xFF49454F)
                            )
                        }
                    }

                    // Tab 2: Recent List
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { selectedTab = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selectedTab == 1) Color(0xFFE8DEF8) else Color.Transparent)
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = "Recent tab",
                                    tint = if (selectedTab == 1) Color(0xFF1D192B) else Color(0xFF49454F),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Recent",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) Color(0xFF1D1B20) else Color(0xFF49454F)
                            )
                        }
                    }

                    // Tab 3: Settings
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { selectedTab = 2 },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selectedTab == 2) Color(0xFFE8DEF8) else Color.Transparent)
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings tab",
                                    tint = if (selectedTab == 2) Color(0xFF1D192B) else Color(0xFF49454F),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Settings",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 2) Color(0xFF1D1B20) else Color(0xFF49454F)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFEF7FF))
        ) {
            // Simulated Status Bar to mimic top layout in Design HTML
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "9:41",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.8f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.15f))
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.15f))
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.15f))
                    )
                }
            }

            // Beautiful custom header/App bar from design HTML ("NativeShell" with JD branding)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Menu placeholder */ }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Options menu",
                        tint = Color(0xFF1D1B20),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "NativeShell",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF1D1B20),
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { showQuickLaunchDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search URL",
                        tint = Color(0xFF1D1B20)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // JD user icon from mockup
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6750A4)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JD",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Tab rendering switch
            when (selectedTab) {
                0 -> {
                    // Home Screen with the gorgeous search bar and high density wrapping cards grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        // Launcher quick-action bar
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable { showQuickLaunchDialog = true }
                                .padding(vertical = 4.dp),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = Color(0xFF49454F)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Open sandboxed URL...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF49454F)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Headings
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECENT WRAPPERS",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF49454F)
                            )
                            TextButton(
                                onClick = { selectedTab = 1 },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "MANAGE",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6750A4)
                                )
                            }
                        }

                        if (profiles.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No sandboxes configured yet. Add some below.")
                            }
                        } else {
                            // Perfect Grid Cells from layout specification
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .testTag("sandbox_profile_grid"),
                                contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(profiles) { profile ->
                                    val (colorStart, colorEnd) = remember(profile.id) {
                                        val index = profile.id % 4
                                        when (index) {
                                            0 -> Pair(Color(0xFF3B82F6), Color(0xFF1D4ED8)) // Blue
                                            1 -> Pair(Color(0xFF10B981), Color(0xFF0D9488)) // Emerald
                                            2 -> Pair(Color(0xFFFB923C), Color(0xFFEF4444)) // Orange/Red
                                            else -> Pair(Color(0xFFA855F7), Color(0xFF4F46E5)) // Purple
                                        }
                                    }

                                    // Custom beautifully-designed card item matching HTML style
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(145.dp)
                                            .clickable { onLaunch(profile) }
                                            .testTag("grid_card_${profile.id}"),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            // White icon container with shadow element
                                            Box(
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(30.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            Brush.linearGradient(
                                                                colors = listOf(colorStart, colorEnd)
                                                            )
                                                        )
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Text(
                                                text = profile.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF1D1B20),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = profile.url
                                                    .replace("https://", "")
                                                    .replace("http://", "")
                                                    .replace("www.", ""),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF49454F),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Recent wraps detailed list screen
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Text(
                            text = "Comprehensive Wrappers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF49454F),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        if (profiles.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No Sandboxes configured yet.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .testTag("sandbox_profile_list"),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(profiles) { profile ->
                                    SandboxProfileItem(
                                        profile = profile,
                                        onLaunch = { onLaunch(profile) },
                                        onEdit = { onEdit(profile) },
                                        onDelete = { onDelete(profile) }
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Settings Tab - configuration toggles and diagnostics
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "System Diagnostics",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D1B20)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Active sandboxes count:", style = MaterialTheme.typography.bodyMedium)
                                    Text("${profiles.size}", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Internet connectivity status:", style = MaterialTheme.typography.bodyMedium)
                                    Text("Online", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Device Hardware Support:", style = MaterialTheme.typography.bodyMedium)
                                    Text("Enabled", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Escape instructions
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF21005D)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Unspoken Sandbox Escape",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF21005D)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Because sandboxed apps are loaded in 100% full screen mode without menus or address bars, escape is bound to specialized multi-touch triggers:\n\n1. Press and keep 3 fingers on screen simultaneously for 1.3 seconds.\n2. Tap the screen rapidly 5 times in a row.\n\nEther method instantly exits state.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF21005D)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Credits & Support",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Web Sandbox is built using pure Material 3 theme parameters with a High Density card layout to run websites inside native wrappers beautifully.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF49454F)
                        )
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }

    // Quick sandbox launch pop-up dialog
    if (showQuickLaunchDialog) {
        AlertDialog(
            onDismissRequest = { showQuickLaunchDialog = false },
            title = { Text("Direct URL Launcher") },
            text = {
                Column {
                    Text("Type any URL to load in the sandbox wrapper. It stays locked inside.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = quickLaunchUrl,
                        onValueChange = { quickLaunchUrl = it },
                        placeholder = { Text("e.g. news.ycombinator.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (quickLaunchUrl.isNotBlank()) {
                            showQuickLaunchDialog = false
                            val formatted = when {
                                quickLaunchUrl.startsWith("http://") || quickLaunchUrl.startsWith("https://") -> quickLaunchUrl
                                else -> "https://$quickLaunchUrl"
                            }
                            onLaunch(
                                SandboxProfile(
                                    name = quickLaunchUrl,
                                    url = formatted,
                                    blockExternalPages = true
                                )
                            )
                        }
                    }
                ) {
                    Text("Launch Sandbox")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickLaunchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SandboxProfileItem(
    profile: SandboxProfile,
    onLaunch: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onLaunch() }
            .testTag("profile_card_${profile.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = profile.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_button_${profile.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Sandbox Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("delete_button_${profile.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Sandbox Profile",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Attribute Configuration Badges
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (profile.isDirectLaunch) {
                    ModeBadge(
                        text = "Direct Launch",
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        textColor = MaterialTheme.colorScheme.onErrorContainer,
                        icon = Icons.Default.Refresh
                    )
                }
                if (profile.blockExternalPages) {
                    ModeBadge(
                        text = "Containment Sandboxed",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        icon = Icons.Default.Lock
                    )
                } else {
                    ModeBadge(
                        text = "Open Web Navigation",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        icon = Icons.Default.Warning
                    )
                }
                if (profile.isDesktopMode) {
                    ModeBadge(
                        text = "Desktop Screen Override",
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        icon = Icons.Default.Star
                    )
                }
                if (profile.cssHideSelectors.isNotEmpty()) {
                    ModeBadge(
                        text = "${profile.cssHideSelectors.split(",").size} DOM rules hidden",
                        containerColor = Color(0xFFE2D4F7),
                        textColor = Color(0xFF4B1D8C),
                        icon = Icons.Default.Build
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onLaunch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("launch_button_${profile.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enter Canvas Sandbox", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Sandbox?") },
            text = { Text("Are you sure you want to permanently delete '${profile.name}'? All locally customized script injections and CSS codes for this setup will be lost.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ModeBadge(
    text: String,
    containerColor: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileFormScreen(
    profile: SandboxProfile?,
    onSave: (
        name: String,
        url: String,
        isDesktopMode: Boolean,
        isPullToRefreshEnabled: Boolean,
        cssHideSelectors: String,
        customCss: String,
        customJs: String,
        blockExternalPages: Boolean,
        isDirectLaunch: Boolean
    ) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var url by remember { mutableStateOf(profile?.url ?: "") }
    var isDesktopMode by remember { mutableStateOf(profile?.isDesktopMode ?: false) }
    var isPullToRefreshEnabled by remember { mutableStateOf(profile?.isPullToRefreshEnabled ?: false) }
    var cssHideSelectors by remember { mutableStateOf(profile?.cssHideSelectors ?: "") }
    var customCss by remember { mutableStateOf(profile?.customCss ?: "") }
    var customJs by remember { mutableStateOf(profile?.customJs ?: "") }
    var blockExternalPages by remember { mutableStateOf(profile?.blockExternalPages ?: true) }
    var isDirectLaunch by remember { mutableStateOf(profile?.isDirectLaunch ?: false) }

    var nameError by remember { mutableStateOf(false) }
    var urlError by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (profile == null) "New Sandbox Profile" else "Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // General Details Category
            Text(
                text = "Identity & URL Setup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = it.isBlank()
                },
                label = { Text("Display Name") },
                placeholder = { Text("e.g. My Custom App") },
                isError = nameError,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_profile_name"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            if (nameError) {
                Text(
                    text = "Name cannot be empty",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = url,
                onValueChange = {
                    url = it
                    urlError = it.isBlank()
                },
                label = { Text("Target Web URL") },
                placeholder = { Text("e.g. workspace.notion.so") },
                isError = urlError,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_profile_url"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done)
            )
            if (urlError) {
                Text(
                    text = "URL cannot be empty",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Behavior Toggles Category
            Text(
                text = "Native Sandbox Configurations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ConfigToggleRow(
                title = "Direct Launch Mode",
                subtitle = "Bypasses the dashboard list and boots straight into this canvas when the Android app is opened.",
                checked = isDirectLaunch,
                onCheckedChange = { isDirectLaunch = it },
                modifier = Modifier.testTag("toggle_direct_launch")
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            ConfigToggleRow(
                title = "Containment Domain Sandbox",
                subtitle = "Blocks external hyperlinking (e.g. redirects, trackers) off-site. Keep everything locked in the frame.",
                checked = blockExternalPages,
                onCheckedChange = { blockExternalPages = it },
                modifier = Modifier.testTag("toggle_block_external")
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            ConfigToggleRow(
                title = "User Agent - Desktop Web",
                subtitle = "Forces Desktop versions of heavy sites (e.g. Notion, Linear) to bypass restricted mobile screen limitations.",
                checked = isDesktopMode,
                onCheckedChange = { isDesktopMode = it },
                modifier = Modifier.testTag("toggle_desktop_mode")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Advanced Rules styling Category
            Text(
                text = "Advanced UI Injections (Scripts & Styles)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Configure injection overrides here to customize and hide components from the website. Elements are kept hidden dynamically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = cssHideSelectors,
                onValueChange = { cssHideSelectors = it },
                label = { Text("Hide CSS Selectors") },
                placeholder = { Text("e.g. header, .banner, #floating-ad-unit") },
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_hide_selectors"),
                supportingText = {
                    Text("Comma-separated selectors to hide automatically on page frames.")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = customCss,
                onValueChange = { customCss = it },
                label = { Text("Custom CSS Stylesheet") },
                placeholder = { Text("e.g. body { background-color: #000; }\n.sidebar { border: none; }") },
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_custom_css"),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                supportingText = {
                    Text("Custom styles stylesheet to inject into the document head.")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = customJs,
                onValueChange = { customJs = it },
                label = { Text("Custom JavaScript Code") },
                placeholder = { Text("e.g. console.log('Hello Sandbox!');\nif(document.querySelector('.ad')) ...") },
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_custom_js"),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                supportingText = {
                    Text("Arbitrary script package compiled to run after DOM loads complete.")
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Save Actions
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    }
                    if (url.isBlank()) {
                        urlError = true
                    }
                    if (name.isNotBlank() && url.isNotBlank()) {
                        onSave(
                            name,
                            url,
                            isDesktopMode,
                            isPullToRefreshEnabled,
                            cssHideSelectors,
                            customCss,
                            customJs,
                            blockExternalPages,
                            isDirectLaunch
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_saved_profile"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Sandbox Configuration", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Cancel Changes", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ConfigToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
