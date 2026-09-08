package ir.danialchoopan.tunecraftmusicplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity
import ir.danialchoopan.tunecraftmusicplayer.data.repository.MusicRepository
import ir.danialchoopan.tunecraftmusicplayer.service.PlayerState
import ir.danialchoopan.tunecraftmusicplayer.service.TuneCraftMediaService
import ir.danialchoopan.tunecraftmusicplayer.ui.components.MiniPlayer
import ir.danialchoopan.tunecraftmusicplayer.ui.components.NowPlayingSheet
import ir.danialchoopan.tunecraftmusicplayer.ui.components.TagEditDialog
import ir.danialchoopan.tunecraftmusicplayer.ui.components.AddToPlaylistDialog
import ir.danialchoopan.tunecraftmusicplayer.ui.components.AudioTrimmerDialog
import ir.danialchoopan.tunecraftmusicplayer.ui.navigation.Screen
import ir.danialchoopan.tunecraftmusicplayer.ui.screens.*
import ir.danialchoopan.tunecraftmusicplayer.ui.theme.TuneCraftTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as TuneCraftApplication
        val repository = app.musicRepository
        val preferences = app.preferencesRepository

        // راه‌اندازی سرویس پخش رسانه (MediaSessionService)
        // برای سرویس‌های Media3 استفاده از startService کافی است چون خود فریم‌ورک هنگام شروع پخش، سرویس را به Foreground ارتقا می‌دهد
        // این تغییر مانع بروز خطای RemoteServiceException$ForegroundServiceDidNotStartInTimeException در اندروید‌های جدید می‌شود
        try {
            val serviceIntent = Intent(this, TuneCraftMediaService::class.java)
            startService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        handleIncomingAudioIntent(intent)

        setContent {
            val themeMode by preferences.themeMode.collectAsState(initial = "DARK")
            val language by preferences.language.collectAsState(initial = "EN")
            val fontScale by preferences.fontScale.collectAsState(initial = 1.0f)
            val isPersian = language == "FA"

            val audioPermission = remember {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                } else {
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }
            }

            var hasAudioPermission by remember {
                val audioPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                val audioGranted = ContextCompat.checkSelfPermission(this@MainActivity, audioPerm) == PackageManager.PERMISSION_GRANTED
                val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                } else true

                mutableStateOf(audioGranted && notifGranted)
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { perms ->
                val audioGranted = perms[Manifest.permission.READ_MEDIA_AUDIO] == true ||
                        perms[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    perms[Manifest.permission.POST_NOTIFICATIONS] == true
                } else true

                hasAudioPermission = audioGranted && notifGranted
                if (audioGranted) {
                    lifecycleScope.launch {
                        repository.scanLocalMedia()
                    }
                    Toast.makeText(
                        this@MainActivity,
                        if (isPersian) "دسترسی‌ها تایید شد. در حال اسکن موزیک‌ها..." else "Permissions granted. Scanning audio files...",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        if (isPersian) "اجازه دسترسی به فایل‌ها داده نشد" else "Storage permission denied",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            val importBackupLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val json = this@MainActivity.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
                                val success = repository.importLibraryBackupJson(json)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        if (success) "Backup restored successfully" else "Failed to restore backup",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                val audioPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                val audioGranted = ContextCompat.checkSelfPermission(this@MainActivity, audioPerm) == PackageManager.PERMISSION_GRANTED
                val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                } else true

                if (audioGranted) {
                    repository.scanLocalMedia()
                }
                if (!audioGranted || !notifGranted) {
                    permissionLauncher.launch(audioPermission)
                }
            }

            val layoutDirection = if (isPersian) LayoutDirection.Rtl else LayoutDirection.Ltr
            val currentDensity = LocalDensity.current

            CompositionLocalProvider(
                LocalLayoutDirection provides layoutDirection,
                LocalDensity provides Density(density = currentDensity.density, fontScale = fontScale)
            ) {
                TuneCraftTheme(themeMode = themeMode) {
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val coroutineScope = rememberCoroutineScope()

                    val allSongs by repository.allSongs.collectAsStateWithLifecycle(initialValue = emptyList())
                    val favoriteSongs by repository.favoriteSongs.collectAsStateWithLifecycle(initialValue = emptyList())
                    val playlists by repository.allPlaylists.collectAsStateWithLifecycle(initialValue = emptyList())
                    val history by repository.playbackHistory.collectAsStateWithLifecycle(initialValue = emptyList())
                    val recentlyPlayed by repository.recentlyPlayed.collectAsStateWithLifecycle(initialValue = emptyList())
                    val mostPlayed by repository.mostPlayed.collectAsStateWithLifecycle(initialValue = emptyList())
                    val recentlyAdded by repository.recentlyAdded.collectAsStateWithLifecycle(initialValue = emptyList())
                    val longestSongs by repository.longestSongs.collectAsStateWithLifecycle(initialValue = emptyList())
                    val shortestSongs by repository.shortestSongs.collectAsStateWithLifecycle(initialValue = emptyList())
                    val customPresets by repository.equalizerPresets.collectAsStateWithLifecycle(initialValue = emptyList())
                    val isScanning by repository.isScanning.collectAsStateWithLifecycle(initialValue = false)
                    // Detect screen orientation and width for adaptive Tablet & Car Head Unit layouts
                    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

                    val playerState by TuneCraftMediaService.playerState.collectAsStateWithLifecycle(initialValue = PlayerState())

                    var showNowPlayingSheet by remember { mutableStateOf(false) }
                    var songToEditTags by remember { mutableStateOf<SongEntity?>(null) }
                    var songToAddToPlaylist by remember { mutableStateOf<SongEntity?>(null) }
                    var songToTrim by remember { mutableStateOf<SongEntity?>(null) }

                    val mediaService = TuneCraftMediaService.instance

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val mainTabs = remember { listOf(Screen.Home, Screen.Library, Screen.Playlists) }
                    val pagerState = rememberPagerState(initialPage = 0) { mainTabs.size }

                    /**
                     * Adaptive Container Layout:
                     * - Portrait (Phones): Floating Bottom Navigation Bar + Bottom MiniPlayer
                     * - Landscape (Tablets / Car Head Units): Side NavigationRail + Main Content + Docked MiniPlayer
                     */
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                modifier = Modifier.width(310.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp)
                                ) {
                                    // Header
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "TuneCraft Player",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                                )
                                                Text(
                                                    text = if (isPersian) "موزیک پلیر پیشرفته" else "Professional Music Player",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Main Navigation Drawer Items
                                    val drawerScreens = listOf(
                                        Screen.Home to Icons.Default.Home,
                                        Screen.Library to Icons.Default.LibraryMusic,
                                        Screen.Playlists to Icons.Default.QueueMusic,
                                        Screen.Equalizer to Icons.Default.Equalizer,
                                        Screen.CarMode to Icons.Default.DirectionsCar,
                                        Screen.Search to Icons.Default.Search,
                                        Screen.Statistics to Icons.Default.BarChart,
                                        Screen.Settings to Icons.Default.Settings,
                                        Screen.About to Icons.Default.Info
                                    )

                                    drawerScreens.forEach { (screen, icon) ->
                                        val isSelected = when (screen) {
                                            Screen.Home, Screen.Library, Screen.Playlists -> {
                                                currentRoute == Screen.Home.route && mainTabs[pagerState.currentPage] == screen
                                            }
                                            else -> currentRoute == screen.route
                                        }

                                        NavigationDrawerItem(
                                            label = { Text(if (isPersian) screen.titleFa else screen.titleEn) },
                                            icon = { Icon(imageVector = icon, contentDescription = null) },
                                            selected = isSelected,
                                            onClick = {
                                                coroutineScope.launch { drawerState.close() }
                                                val mainTabIndex = mainTabs.indexOf(screen)
                                                if (mainTabIndex != -1) {
                                                    if (currentRoute != Screen.Home.route) {
                                                        navController.navigate(Screen.Home.route) {
                                                            popUpTo(Screen.Home.route) { saveState = true }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                    coroutineScope.launch { pagerState.animateScrollToPage(mainTabIndex) }
                                                } else if (currentRoute != screen.route) {
                                                    navController.navigate(screen.route) {
                                                        popUpTo(Screen.Home.route) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            },
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Side NavigationRail for Landscape (Tablets & Car Head Units)
                            if (isLandscape && currentRoute != Screen.CarMode.route) {
                                NavigationRail(
                                    modifier = Modifier.fillMaxHeight(),
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                                    header = {
                                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                                        }
                                    }
                                ) {
                                    val railItems = listOf(
                                        Screen.Home to Icons.Default.Home,
                                        Screen.Library to Icons.Default.LibraryMusic,
                                        Screen.Playlists to Icons.Default.QueueMusic,
                                        Screen.CarMode to Icons.Default.DirectionsCar,
                                        Screen.Equalizer to Icons.Default.Equalizer,
                                        Screen.Search to Icons.Default.Search,
                                        Screen.Settings to Icons.Default.Settings
                                    )
                                    railItems.forEach { (screen, icon) ->
                                        val isSelected = when (screen) {
                                            Screen.Home -> currentRoute == Screen.Home.route && pagerState.currentPage == 0
                                            Screen.Library -> currentRoute == Screen.Home.route && pagerState.currentPage == 1
                                            Screen.Playlists -> currentRoute == Screen.Home.route && pagerState.currentPage == 2
                                            else -> currentRoute == screen.route
                                        }
                                        NavigationRailItem(
                                            selected = isSelected,
                                            onClick = {
                                                val mainTabIndex = mainTabs.indexOf(screen)
                                                if (mainTabIndex != -1) {
                                                    if (currentRoute != Screen.Home.route) {
                                                        navController.navigate(Screen.Home.route) {
                                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                    coroutineScope.launch { pagerState.animateScrollToPage(mainTabIndex) }
                                                } else if (currentRoute != screen.route) {
                                                    navController.navigate(screen.route) {
                                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            },
                                            icon = { Icon(imageVector = icon, contentDescription = screen.titleEn) },
                                            label = { Text(if (isPersian) screen.titleFa else screen.titleEn) }
                                        )
                                    }
                                }
                            }

                            Scaffold(
                                modifier = Modifier.weight(1f),
                                topBar = {
                                    if (currentRoute != Screen.CarMode.route &&
                                        currentRoute != Screen.AllSongsDetail.route &&
                                        currentRoute != Screen.FavoritesDetail.route
                                    ) {
                                        TopAppBar(
                                            title = {
                                                val screenTitle = when {
                                                    currentRoute == Screen.Home.route -> {
                                                        val activeTab = mainTabs.getOrElse(pagerState.currentPage) { Screen.Home }
                                                        if (isPersian) activeTab.titleFa else activeTab.titleEn
                                                    }
                                                    currentRoute == Screen.Search.route -> if (isPersian) "جستجو" else "Search"
                                                    currentRoute == Screen.Statistics.route -> if (isPersian) "آمار شنیداری" else "Statistics"
                                                    currentRoute == Screen.Settings.route -> if (isPersian) "تنظیمات" else "Settings"
                                                     currentRoute == Screen.Equalizer.route -> if (isPersian) "اکولایزر و افکت صوتی" else "Equalizer & Sound FX"
                                                    currentRoute == Screen.About.route -> if (isPersian) "درباره ما" else "About Us"
                                                    else -> "TuneCraft"
                                                }
                                                Text(text = screenTitle, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                            },
                                            navigationIcon = {
                                                if (!isLandscape) {
                                                    IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                                        Icon(
                                                            imageVector = Icons.Default.Menu,
                                                            contentDescription = "Open Drawer"
                                                        )
                                                    }
                                                }
                                            },
                                            actions = {
                                                if (currentRoute != Screen.Search.route) {
                                                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                                                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                                                    }
                                                }
                                            }
                                        )
                                    }
                                },
                                bottomBar = {
                                    if (currentRoute != Screen.CarMode.route) {
                                        Column(
                                            modifier = Modifier.navigationBarsPadding()
                                        ) {
                                            if (playerState.currentSong != null) {
                                                MiniPlayer(
                                                    playerState = playerState,
                                                    onPlayPause = { mediaService?.playPause() },
                                                    onNext = { mediaService?.next() },
                                                    onPrevious = { mediaService?.previous() },
                                                    onClick = { showNowPlayingSheet = true },
                                                    isPersian = isPersian
                                                )
                                            }

                                            // Modern floating bottom navigation bar for portrait mode
                                            if (!isLandscape) {
                                                Surface(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                                    shape = RoundedCornerShape(28.dp),
                                                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                                                    tonalElevation = 8.dp,
                                                    shadowElevation = 12.dp
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                                    ) {
                                                        data class NavItem(val screen: Screen, val icon: androidx.compose.ui.graphics.vector.ImageVector)
                                                        val bottomNavItems = listOf(
                                                            NavItem(Screen.Home, Icons.Default.Home),
                                                            NavItem(Screen.Library, Icons.Default.LibraryMusic),
                                                            NavItem(Screen.Playlists, Icons.Default.QueueMusic)
                                                        )

                                                        bottomNavItems.forEachIndexed { index, item ->
                                                            val isSelected = currentRoute == Screen.Home.route && pagerState.currentPage == index

                                                            // Animate colors smoothly on selection change
                                                            val bgColor by animateColorAsState(
                                                                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                                                                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                                                                label = "navBg"
                                                            )
                                                            val iconColor by animateColorAsState(
                                                                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                                                                label = "navIcon"
                                                            )

                                                            Surface(
                                                                onClick = {
                                                                    if (currentRoute != Screen.Home.route) {
                                                                        navController.navigate(Screen.Home.route) {
                                                                            popUpTo(navController.graph.findStartDestination().id) {
                                                                                saveState = true
                                                                            }
                                                                            launchSingleTop = true
                                                                            restoreState = true
                                                                        }
                                                                    }
                                                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                                                },
                                                                shape = RoundedCornerShape(22.dp),
                                                                color = bgColor,
                                                                modifier = Modifier.padding(horizontal = 2.dp)
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.padding(
                                                                        horizontal = if (isSelected) 20.dp else 16.dp,
                                                                        vertical = 12.dp
                                                                    ),
                                                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.Center
                                                                ) {
                                                                    Icon(
                                                                        imageVector = item.icon,
                                                                        contentDescription = item.screen.titleEn,
                                                                        tint = iconColor,
                                                                        modifier = Modifier.size(22.dp)
                                                                    )
                                                                    if (isSelected) {
                                                                        Spacer(modifier = Modifier.width(8.dp))
                                                                        Text(
                                                                            text = if (isPersian) item.screen.titleFa else item.screen.titleEn,
                                                                            style = MaterialTheme.typography.labelMedium,
                                                                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            ) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Home.route,
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable(Screen.Home.route) {
                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier.fillMaxSize()
                                    ) { page ->
                                        when (page) {
                                            0 -> HomeScreen(
                                                allSongs = allSongs,
                                                recentlyPlayed = recentlyPlayed,
                                                favoriteSongs = favoriteSongs,
                                                recentlyAdded = recentlyAdded,
                                                isScanning = isScanning,
                                                isPersian = isPersian,
                                                hasAudioPermission = hasAudioPermission,
                                                onRequestPermission = { permissionLauncher.launch(audioPermission) },
                                                onRescanMedia = {
                                                    lifecycleScope.launch {
                                                        repository.scanLocalMedia()
                                                        Toast.makeText(this@MainActivity, if (isPersian) "اسکن مجدد انجام شد" else "Media Rescanned", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                onSongClick = { list, idx -> mediaService?.playSongs(list, idx) },
                                                onNavigateToFavorites = { navController.navigate(Screen.FavoritesDetail.route) },
                                                onNavigateToAllSongs = { navController.navigate(Screen.AllSongsDetail.route) },
                                                onNavigateToLibrary = {
                                                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                                },
                                                onNavigateToStats = { navController.navigate(Screen.Statistics.route) }
                                            )
                                            1 -> LibraryScreen(
                                                allSongs = allSongs,
                                                playlists = playlists,
                                                isPersian = isPersian,
                                                hasAudioPermission = hasAudioPermission,
                                                isScanning = isScanning,
                                                onRequestPermission = { permissionLauncher.launch(audioPermission) },
                                                onSongClick = { list, idx -> mediaService?.playSongs(list, idx) },
                                                onRescanMedia = {
                                                    lifecycleScope.launch {
                                                        repository.scanLocalMedia()
                                                        Toast.makeText(this@MainActivity, if (isPersian) "اسکن مجدد انجام شد" else "Media Rescanned", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                onAddToPlaylist = { pId, sId ->
                                                    lifecycleScope.launch {
                                                        repository.addSongToPlaylist(pId, sId)
                                                        Toast.makeText(this@MainActivity, if (isPersian) "به لیست پخش اضافه شد" else "Added to Playlist", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                onCreatePlaylistAndAdd = { name, sId ->
                                                    lifecycleScope.launch {
                                                        val newId = repository.createPlaylist(name)
                                                        repository.addSongToPlaylist(newId, sId)
                                                        Toast.makeText(this@MainActivity, if (isPersian) "لیست پخش ایجاد و آهنگ اضافه شد" else "Playlist created and song added", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                onToggleFavorite = { song ->
                                                    lifecycleScope.launch {
                                                        val newFav = !song.isFavorite
                                                        TuneCraftMediaService.updateSongFavoriteStatus(song.id, newFav)
                                                        repository.toggleFavorite(song.id, newFav)
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            if (newFav) (if (isPersian) "به علاقه‌مندی‌ها اضافه شد" else "Added to Favorites")
                                                            else (if (isPersian) "از علاقه‌مندی‌ها حذف شد" else "Removed from Favorites"),
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            )
                                            2 -> PlaylistsScreen(
                                                playlists = playlists,
                                                allSongs = allSongs,
                                                recentlyPlayed = recentlyPlayed,
                                                mostPlayed = mostPlayed,
                                                recentlyAdded = recentlyAdded,
                                                isPersian = isPersian,
                                                onCreatePlaylist = { name ->
                                                    lifecycleScope.launch {
                                                         repository.createPlaylist(name)
                                                         Toast.makeText(this@MainActivity, if (isPersian) "لیست پخش ایجاد شد" else "Playlist created", Toast.LENGTH_SHORT).show()
                                                     }
                                                },
                                                onDeletePlaylist = { id ->
                                                    lifecycleScope.launch {
                                                         repository.deletePlaylist(id)
                                                         Toast.makeText(this@MainActivity, if (isPersian) "لیست پخش حذف شد" else "Playlist deleted", Toast.LENGTH_SHORT).show()
                                                     }
                                                },
                                                onRenamePlaylist = { id, name ->
                                                    lifecycleScope.launch {
                                                         repository.renamePlaylist(id, name)
                                                         Toast.makeText(this@MainActivity, if (isPersian) "نام لیست پخش تغییر یافت" else "Playlist renamed", Toast.LENGTH_SHORT).show()
                                                     }
                                                },
                                                onRemoveSongFromPlaylist = { pId, sId ->
                                                    lifecycleScope.launch {
                                                         repository.removeSongFromPlaylist(pId, sId)
                                                         Toast.makeText(this@MainActivity, if (isPersian) "آهنگ از لیست پخش حذف شد" else "Song removed from playlist", Toast.LENGTH_SHORT).show()
                                                     }
                                                },
                                                getSongsForPlaylistFlow = { id -> repository.getSongsForPlaylist(id) },
                                                onPlaySongs = { list, idx -> mediaService?.playSongs(list, idx) }
                                            )
                                        }
                                    }
                                }
                                composable(Screen.Search.route) {
                                    SearchScreen(
                                        allSongs = allSongs,
                                        isPersian = isPersian,
                                        onSongClick = { list, idx -> mediaService?.playSongs(list, idx) }
                                    )
                                }
                                composable(Screen.Statistics.route) {
                                    StatisticsScreen(
                                        history = history,
                                        allSongs = allSongs,
                                        isPersian = isPersian
                                    )
                                }
                                composable(Screen.Settings.route) {
                                    SettingsScreen(
                                        preferencesRepository = preferences,
                                        isPersian = isPersian,
                                        onExportBackup = {
                                            lifecycleScope.launch(Dispatchers.IO) {
                                                val json = repository.exportLibraryBackupJson()
                                                val fileName = "TuneCraft_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"
                                                val file = java.io.File(this@MainActivity.getExternalFilesDir(null), fileName)
                                                file.writeText(json)
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Backup saved to: $fileName",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        },
                                        onImportBackup = {
                                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                                addCategory(Intent.CATEGORY_OPENABLE)
                                                type = "application/json"
                                            }
                                            try {
                                                importBackupLauncher.launch(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "File picker not available",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    )
                                }
                                composable(Screen.About.route) {
                                    AboutScreen(isPersian = isPersian)
                                }
                                composable(Screen.FavoritesDetail.route) {
                                    SongListDetailScreen(
                                        title = if (isPersian) "علاقه‌مندی‌ها" else "Favorites",
                                        songs = favoriteSongs,
                                        playlists = playlists,
                                        isPersian = isPersian,
                                        onBack = { navController.popBackStack() },
                                        onSongClick = { list, idx -> mediaService?.playSongs(list, idx) },
                                        onToggleFavorite = { song ->
                                            lifecycleScope.launch {
                                                val newFav = !song.isFavorite
                                                TuneCraftMediaService.updateSongFavoriteStatus(song.id, newFav)
                                                repository.toggleFavorite(song.id, newFav)
                                            }
                                         },
                                        onAddToPlaylist = { pId, sId ->
                                            lifecycleScope.launch {
                                                repository.addSongToPlaylist(pId, sId)
                                                Toast.makeText(this@MainActivity, if (isPersian) "به لیست پخش اضافه شد" else "Added to Playlist", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onCreatePlaylistAndAdd = { name, sId ->
                                            lifecycleScope.launch {
                                                val newId = repository.createPlaylist(name)
                                                repository.addSongToPlaylist(newId, sId)
                                                Toast.makeText(this@MainActivity, if (isPersian) "لیست پخش ایجاد و آهنگ اضافه شد" else "Playlist created and song added", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                                composable(Screen.AllSongsDetail.route) {
                                    SongListDetailScreen(
                                        title = if (isPersian) "همه آهنگ‌ها" else "All Songs",
                                        songs = allSongs,
                                        playlists = playlists,
                                        isPersian = isPersian,
                                        onBack = { navController.popBackStack() },
                                        onSongClick = { list, idx -> mediaService?.playSongs(list, idx) },
                                        onToggleFavorite = { song ->
                                            lifecycleScope.launch {
                                                val newFav = !song.isFavorite
                                                TuneCraftMediaService.updateSongFavoriteStatus(song.id, newFav)
                                                repository.toggleFavorite(song.id, newFav)
                                            }
                                        },
                                        onAddToPlaylist = { pId, sId ->
                                            lifecycleScope.launch {
                                                repository.addSongToPlaylist(pId, sId)
                                                Toast.makeText(this@MainActivity, if (isPersian) "به لیست پخش اضافه شد" else "Added to Playlist", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onCreatePlaylistAndAdd = { name, sId ->
                                            lifecycleScope.launch {
                                                val newId = repository.createPlaylist(name)
                                                repository.addSongToPlaylist(newId, sId)
                                                Toast.makeText(this@MainActivity, if (isPersian) "لیست پخش ایجاد و آهنگ اضافه شد" else "Playlist created and song added", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                                composable(Screen.Equalizer.route) {
                                    EqualizerScreen(
                                        audioFxManager = mediaService?.getAudioFxManager(),
                                        musicRepository = repository,
                                        isPersian = isPersian,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                                composable(Screen.CarMode.route) {
                                    CarModeScreen(
                                        playerState = playerState,
                                        audioFxManager = mediaService?.getAudioFxManager(),
                                        allSongs = allSongs,
                                        favoriteSongs = favoriteSongs,
                                        recentlyPlayed = recentlyPlayed,
                                        isPersian = isPersian,
                                        onExitCarMode = { navController.popBackStack() },
                                        onPlayPause = { mediaService?.playPause() },
                                        onNext = { mediaService?.next() },
                                        onPrevious = { mediaService?.previous() },
                                        onSeekTo = { mediaService?.seekTo(it) },
                                        onPlaySongs = { list, idx -> mediaService?.playSongs(list, idx) }
                                    )
                                }
                            }
                        }
                    }

                    if (showNowPlayingSheet) {
                        NowPlayingSheet(
                            playerState = playerState,
                            audioFxManager = mediaService?.getAudioFxManager(),
                            isPersian = isPersian,
                            onDismiss = { showNowPlayingSheet = false },
                            onPlayPause = { mediaService?.playPause() },
                            onNext = { mediaService?.next() },
                            onPrevious = { mediaService?.previous() },
                            onSeekTo = { mediaService?.seekTo(it) },
                            onToggleFavorite = { song ->
                                lifecycleScope.launch {
                                    val newFav = !song.isFavorite
                                    TuneCraftMediaService.updateSongFavoriteStatus(song.id, newFav)
                                    repository.toggleFavorite(song.id, newFav)
                                    Toast.makeText(
                                        this@MainActivity,
                                        if (newFav) (if (isPersian) "به علاقه‌مندی‌ها اضافه شد" else "Added to Favorites")
                                        else (if (isPersian) "از علاقه‌مندی‌ها حذف شد" else "Removed from Favorites"),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onToggleShuffle = { mediaService?.setShuffleMode(!playerState.shuffleMode) },
                            onToggleRepeat = { mediaService?.toggleRepeatMode() },
                            onSetSleepTimer = { mins -> mediaService?.startSleepTimer(mins) },
                            onEditTags = { songToEditTags = it },
                            onAddToPlaylistClick = { songToAddToPlaylist = it },
                            onTrimAudioClick = { songToTrim = it },
                            customPresets = customPresets,
                            onSaveCustomPreset = { name, bandLevels, bassBoost, virtualizer, balance ->
                                lifecycleScope.launch {
                                    repository.saveEqualizerPreset(name, bandLevels, bassBoost, virtualizer, balance)
                                }
                            },
                            onDeleteCustomPreset = { preset ->
                                lifecycleScope.launch {
                                    repository.deleteEqualizerPreset(preset)
                                }
                            }
                        )
                    }

                    songToAddToPlaylist?.let { song ->
                        AddToPlaylistDialog(
                            song = song,
                            playlists = playlists,
                            isPersian = isPersian,
                            onDismiss = { songToAddToPlaylist = null },
                            onAddToPlaylist = { pId, sId ->
                                lifecycleScope.launch { repository.addSongToPlaylist(pId, sId) }
                            },
                            onCreatePlaylistAndAdd = { name, sId ->
                                lifecycleScope.launch {
                                    val newId = repository.createPlaylist(name)
                                    repository.addSongToPlaylist(newId, sId)
                                }
                            }
                        )
                    }

                    songToEditTags?.let { song ->
                        TagEditDialog(
                            song = song,
                            isPersian = isPersian,
                            onDismiss = { songToEditTags = null },
                            onSave = { updatedSong ->
                                lifecycleScope.launch {
                                    repository.updateSongMetadata(updatedSong)
                                    songToEditTags = null
                                    Toast.makeText(this@MainActivity, "Metadata Saved!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    songToTrim?.let { song ->
                        AudioTrimmerDialog(
                            song = song,
                            isPersian = isPersian,
                            onDismiss = { songToTrim = null }
                        )
                    }
                }
            }
        }
    }
}

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingAudioIntent(intent)
    }

    private fun handleIncomingAudioIntent(intent: Intent?) {
        val action = intent?.action
        val dataUri: Uri? = if (action == Intent.ACTION_VIEW) {
            intent.data
        } else if (action == Intent.ACTION_SEND) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
            }
        } else null

        if (dataUri != null) {
            lifecycleScope.launch {
                try {
                    val repository = (application as TuneCraftApplication).musicRepository
                    val song = repository.resolveSongFromUri(dataUri)
                    TuneCraftMediaService.instance?.playSongs(listOf(song), 0)
                    Toast.makeText(
                        this@MainActivity,
                        "Playing: ${song.title}",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
