package com.example.iudigitalradio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.iudigitalradio.navigation.RadioNavHost
import com.example.iudigitalradio.navigation.RouteDiscover
import com.example.iudigitalradio.navigation.RouteFavorites
import com.example.iudigitalradio.navigation.RouteProfile
import com.example.iudigitalradio.navigation.RouteSearch
import com.example.iudigitalradio.navigation.RouteWorldMap
import com.example.iudigitalradio.presentation.player.PlayerViewModel
import com.example.iudigitalradio.ui.components.UserAvatarButton
import com.example.iudigitalradio.ui.theme.CyanBright
import com.example.iudigitalradio.ui.theme.IUDigitalRadioTheme
import com.example.iudigitalradio.ui.theme.VioletElectric
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import dagger.hilt.android.AndroidEntryPoint

/**
 * RF-01: CERO XMLs de layout — todo el UI está en @Composable.
 * CONSTRAINT: enableEdgeToEdge() ANTES de setContent {}
 * CONSTRAINT: Scaffold consume innerPadding — sin solapamiento con barras del sistema.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // CONSTRAINT CRÍTICO: enableEdgeToEdge() ANTES de super.onCreate() y setContent {}
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val playerViewModel: PlayerViewModel = hiltViewModel()
            val uiState by playerViewModel.uiState.collectAsStateWithLifecycle()

            IUDigitalRadioTheme(darkMode = uiState.isDarkMode) {
                RadioApp(playerViewModel = playerViewModel, uiState = uiState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioApp(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    uiState: com.example.iudigitalradio.presentation.player.RadioUiState = playerViewModel.uiState.collectAsStateWithLifecycle().value
) {
    val navController  = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    // RF-04: rememberSaveable preserva el estado del menú en rotaciones
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    if (showSettingsDialog) {
        SettingsDialog(
            isVibrationEnabled = uiState.isVibrationEnabled,
            onVibrationToggle = playerViewModel::setVibrationEnabled,
            onDismiss = { showSettingsDialog = false }
        )
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        // CONSTRAINT: Scaffold con innerPadding — sin solapamiento
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text       = "IU Digital Radio",
                        color      = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    // RF-02 + RF-03: Avatar circular con captura de cámara
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                    UserAvatarButton(
                            userPhoto        = uiState.userPhoto,
                            onPhotoTaken     = playerViewModel::setUserPhoto,
                            snackbarHostState = snackbarHostState,
                            onNavigateToProfile = { navController.navigate(RouteProfile) }
                        )
                    }
                },
                actions = {
                    // Menú de 3 líneas (hamburger)
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector        = Icons.Filled.Menu,
                                contentDescription = "Menú",
                                tint               = CyanBright
                            )
                        }
                        DropdownMenu(
                            expanded         = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier         = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text    = { Text("Mapa Mundial", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = { Icon(Icons.Filled.Map, null, tint = CyanBright) },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate(RouteWorldMap)
                                }
                            )
                            DropdownMenuItem(
                                text    = { Text("Buscar Emisoras", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = { Icon(Icons.Filled.Search, null, tint = CyanBright) },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate(RouteSearch)
                                }
                            )
                            DropdownMenuItem(
                                text    = { Text("Mis Favoritas", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = { Icon(Icons.Filled.Favorite, null, tint = VioletElectric) },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate(RouteFavorites)
                                }
                            )
                            DropdownMenuItem(
                                text    = { Text("Ajustes", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = { Icon(Icons.Filled.Settings, null, tint = CyanBright) },
                                onClick = {
                                    menuExpanded = false
                                    showSettingsDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },

        bottomBar = {
            RadioBottomNavigation(
                navController = navController,
                isDarkMode = uiState.isDarkMode
            )
        }
    ) { innerPadding ->
        // CONSTRAINT: innerPadding consumido — ningún elemento se solapa con las barras del sistema
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── Halos de fondo atmosféricos (Aetheric Lumina Layer 0) globales ────────────
            Canvas(modifier = Modifier.fillMaxSize()) {
                val alphaMult = if (uiState.isDarkMode) 1f else 0.4f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanBright.copy(alpha = 0.08f * alphaMult), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.15f),
                        radius = size.width * 0.7f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(VioletElectric.copy(alpha = 0.05f * alphaMult), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.6f),
                        radius = size.width * 0.5f
                    )
                )
            }

            RadioNavHost(
                navController   = navController,
                playerViewModel = playerViewModel
            )
        }
    }
}

@Composable
fun SettingsDialog(
    isVibrationEnabled: Boolean,
    onVibrationToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Ajustes", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vibrar al sintonizar", color = MaterialTheme.colorScheme.onSurface)
                Switch(
                    checked = isVibrationEnabled,
                    onCheckedChange = onVibrationToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.background,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Aceptar", color = VioletElectric)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

/**
 * Bottom Navigation Bar adaptativa con soporte completo de modo claro y oscuro.
 */
@Composable
fun RadioBottomNavigation(
    navController: NavHostController,
    isDarkMode: Boolean = true
) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStack?.destination

    data class NavItem(val route: Any, val icon: ImageVector, val label: String)

    val items = listOf(
        NavItem(RouteDiscover,  Icons.Filled.Radio,    "Descubrir"),
        NavItem(RouteWorldMap,  Icons.Filled.Map,      "Mapa"),
        NavItem(RouteFavorites, Icons.Filled.Favorite, "Favoritas"),
        NavItem(RouteSearch,    Icons.Filled.Search,   "Buscar"),
        NavItem(RouteProfile,   Icons.Filled.Person,   "Perfil")
    )

    val navBg = if (isDarkMode) Color(0xB2131318) else MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    val navBorder = if (isDarkMode) {
        Brush.horizontalGradient(listOf(Color(0x14FFFFFF), CyanBright.copy(0.15f), Color(0x14FFFFFF)))
    } else {
        Brush.horizontalGradient(listOf(Color(0x14000000), MaterialTheme.colorScheme.primary.copy(0.25f), Color(0x14000000)))
    }

    NavigationBar(
        containerColor = navBg,
        tonalElevation = 0.dp,
        modifier = Modifier.border(
            width = 0.5.dp,
            brush = navBorder,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        items.forEach { item ->
            val isSelected = currentDestination?.route?.contains(
                item.route::class.simpleName ?: ""
            ) == true

            NavigationBarItem(
                icon    = { Icon(item.icon, contentDescription = item.label) },
                label   = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    selectedTextColor   = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    indicatorColor      = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}