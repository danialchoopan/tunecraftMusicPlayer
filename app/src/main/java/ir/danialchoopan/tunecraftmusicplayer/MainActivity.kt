package ir.danialchoopan.tunecraftmusicplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity
import ir.danialchoopan.tunecraftmusicplayer.service.PlayerState
import ir.danialchoopan.tunecraftmusicplayer.service.TuneCraftMediaService
import ir.danialchoopan.tunecraftmusicplayer.ui.components.MiniPlayer
import ir.danialchoopan.tunecraftmusicplayer.ui.components.NowPlayingSheet
import ir.danialchoopan.tunecraftmusicplayer.ui.components.TagEditDialog
import ir.danialchoopan.tunecraftmusicplayer.ui.navigation.Screen
import ir.danialchoopan.tunecraftmusicplayer.ui.screens.*
import ir.danialchoopan.tunecraftmusicplayer.ui.theme.TuneCraftTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as TuneCraftApplication
        val repository = app.musicRepository
        val preferences = app.preferencesRepository

        // Start Media Service
        try {
            val serviceIntent = Intent(this, TuneCraftMediaService::class.java)
            startService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            val themeMode by preferences.themeMode.collectAsState(initial = "DARK")
            val language by preferences.language.collectAsState(initial = "EN")
            val fontScale by preferences.fontScale.collectAsState(initial = 1.0f)
            val isPersian = language == "FA"

            val permissionsToRequest = remember {
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

            val audioPermission = permissionsToRequest

            var hasAudioPermission by remember {
                val audioPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                mutableStateOf(
                    ContextCompat.checkSelfPermission(this@MainActivity, audioPerm) == PackageManager.PERMISSION_GRANTED
                )
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { perms ->
                val audioGranted = perms[Manifest.permission.READ_MEDIA_AUDIO] == true ||
                        perms[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                hasAudioPermission = audioGranted
                if (audioGranted) {
                    lifecycleScope.launch {
                        repository.scanLocalMedia()
                    }
                    Toast.makeText(
                        this@MainActivity,
                        if (isPersian) "دسترسی تایید شد. در حال اسکن موزیک‌ها..." else "Permission granted. Scanning audio files...",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        if (isPersian) "اجازه دسترسی داده نشد" else "Permissions denied",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            LaunchedEffect(hasAudioPermission) {
                if (hasAudioPermission) {
                    repository.scanLocalMedia()
                } else {
                    permissionLauncher.launch(permissionsToRequest)
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

                    val playerState by TuneCraftMediaService.playerState.collectAsStateWithLifecycle(initialValue = PlayerState())

                    var showNowPlayingSheet by remember { mutableStateOf(false) }
                    var songToEditTags by remember { mutableStateOf<SongEntity?>(null) }

                    val mediaService = TuneCraftMediaService.instance

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val mainTabs = remember { listOf(Screen.Home, Screen.Library, Screen.Playlists) }
                    val pagerState = rememberPagerState(initialPage = 0) { mainTabs.size }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                modifier = Modifier.width(310.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
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

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                    // Accessibility & Personalization
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Accessibility,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isPersian) "دسترسی‌پذیری و متن" else "Accessibility & Text",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = if (isPersian) "اندازه قلم و متون:" else "Font Size Scale:",
                                        style = MaterialTheme.typography.labelSmall
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            1.0f to (if (isPersian) "عادی" else "Normal"),
                                            1.15f to (if (isPersian) "بزرگ" else "Large"),
                                            1.3f to (if (isPersian) "خیلی بزرگ" else "Huge")
                                        ).forEach { (scale, label) ->
                                            FilterChip(
                                                selected = fontScale == scale,
                                                onClick = {
                                                    coroutineScope.launch { preferences.setFontScale(scale) }
                                                },
                                                label = { Text(label) }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isPersian) "زبان برنامه (Language)" else "Language",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        TextButton(onClick = {
                                            coroutineScope.launch { preferences.setLanguage(if (isPersian) "EN" else "FA") }
                                        }) {
                                            Text(if (isPersian) "English" else "فارسی")
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        Scaffold(
                            topBar = {
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
                                            currentRoute == Screen.About.route -> if (isPersian) "درباره ما" else "About Us"
                                            else -> "TuneCraft"
                                        }
                                        Text(text = screenTitle, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = "Open Drawer"
                                            )
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
                            },
                            bottomBar = {
                                Column {
                                    if (playerState.currentSong != null) {
                                        MiniPlayer(
                                            playerState = playerState,
                                            onPlayPause = { mediaService?.playPause() },
                                            onNext = { mediaService?.next() },
                                            onClick = { showNowPlayingSheet = true }
                                        )
                                    }

                                    // Core 3-Item Floating Bottom Navigation Bar with smooth Swipeable Pager Integration
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        shape = RoundedCornerShape(28.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                                        tonalElevation = 8.dp,
                                        shadowElevation = 10.dp
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 6.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                        ) {
                                            val bottomNavItems = listOf(
                                                Screen.Home to Icons.Default.Home,
                                                Screen.Library to Icons.Default.LibraryMusic,
                                                Screen.Playlists to Icons.Default.QueueMusic
                                            )

                                            bottomNavItems.forEachIndexed { index, (screen, icon) ->
                                                val isSelected = currentRoute == Screen.Home.route && pagerState.currentPage == index

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
                                                        coroutineScope.launch {
                                                            pagerState.animateScrollToPage(index)
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                                                    modifier = Modifier.padding(horizontal = 2.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = icon,
                                                            contentDescription = screen.titleEn,
                                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                        if (isSelected) {
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = if (isPersian) screen.titleFa else screen.titleEn,
                                                                style = MaterialTheme.typography.labelMedium,
                                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
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
                                                onNavigateToLibrary = {
                                                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                                },
                                                onNavigateToStats = { navController.navigate(Screen.Statistics.route) }
                                            )
                                            1 -> LibraryScreen(
                                                allSongs = allSongs,
                                                isPersian = isPersian,
                                                hasAudioPermission = hasAudioPermission,
                                                onRequestPermission = { permissionLauncher.launch(audioPermission) },
                                                onSongClick = { list, idx -> mediaService?.playSongs(list, idx) },
                                                onRescanMedia = {
                                                    lifecycleScope.launch {
                                                        repository.scanLocalMedia()
                                                        Toast.makeText(this@MainActivity, if (isPersian) "اسکن مجدد انجام شد" else "Media Rescanned", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                            2 -> PlaylistsScreen(
                                                playlists = playlists,
                                                allSongs = allSongs,
                                                recentlyPlayed = recentlyPlayed,
                                                mostPlayed = mostPlayed,
                                                recentlyAdded = recentlyAdded,
                                                longestSongs = longestSongs,
                                                shortestSongs = shortestSongs,
                                                isPersian = isPersian,
                                                onCreatePlaylist = { name ->
                                                    lifecycleScope.launch { repository.createPlaylist(name) }
                                                },
                                                onPlaylistClick = { list -> mediaService?.playSongs(list, 0) }
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
                                            lifecycleScope.launch {
                                                val json = repository.exportLibraryBackupJson()
                                                Toast.makeText(this@MainActivity, "Library Backup Exported!", Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        onImportBackup = {
                                            Toast.makeText(this@MainActivity, "Select JSON file to import", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                composable(Screen.About.route) {
                                    AboutScreen(isPersian = isPersian)
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
                                lifecycleScope.launch { repository.toggleFavorite(song.id, !song.isFavorite) }
                            },
                            onToggleShuffle = { mediaService?.setShuffleMode(!playerState.shuffleMode) },
                            onToggleRepeat = { mediaService?.toggleRepeatMode() },
                            onSetSleepTimer = { mins -> mediaService?.startSleepTimer(mins) },
                            onEditTags = { songToEditTags = it }
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
                }
            }
        }
    }
}
