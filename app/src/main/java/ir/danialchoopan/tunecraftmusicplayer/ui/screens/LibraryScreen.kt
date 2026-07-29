package ir.danialchoopan.tunecraftmusicplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.PlaylistEntity
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity
import ir.danialchoopan.tunecraftmusicplayer.ui.components.AddToPlaylistDialog

enum class SongSortOption(val titleEn: String, val titleFa: String) {
    NEWEST("Recently Added", "جدیدترین‌ها"),
    TITLE_ASC("Title (A-Z)", "الفبایی (الف تا ی)"),
    TITLE_DESC("Title (Z-A)", "الفبایی (ی تا الف)"),
    ARTIST("Artist (A-Z)", "بر اساس خواننده"),
    DURATION_DESC("Longest First", "طولانی‌ترین‌ها"),
    DURATION_ASC("Shortest First", "کوتاه‌ترین‌ها")
}

enum class SongFilterChip(val titleEn: String, val titleFa: String) {
    ALL("All Tracks", "همه آهنگ‌ها"),
    FAVORITES("Favorites", "علاقه‌مندی‌ها"),
    HIGH_QUALITY("320+ kbps", "کیفیت بالا (۳۲۰)"),
    SHORT("Short (<3m)", "کوتاه (<۳ دقیقه)"),
    LONG("Long (>5m)", "طولانی (>۵ دقیقه)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    allSongs: List<SongEntity>,
    playlists: List<PlaylistEntity> = emptyList(),
    isPersian: Boolean = false,
    hasAudioPermission: Boolean = true,
    isScanning: Boolean = false,
    onRequestPermission: () -> Unit = {},
    onSongClick: (List<SongEntity>, Int) -> Unit,
    onRescanMedia: () -> Unit,
    onAddToPlaylist: ((Long, Long) -> Unit)? = null,
    onCreatePlaylistAndAdd: ((String, Long) -> Unit)? = null,
    onToggleFavorite: ((SongEntity) -> Unit)? = null
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Songs, 1: Albums, 2: Artists, 3: Genres, 4: Folders, 5: Years
    var searchQuery by remember { mutableStateOf("") }
    var currentSortOption by remember { mutableStateOf(SongSortOption.NEWEST) }
    var currentFilterChip by remember { mutableStateOf(SongFilterChip.ALL) }
    var showSortMenu by remember { mutableStateOf(false) }

    var songToAddToPlaylist by remember { mutableStateOf<SongEntity?>(null) }

    PullToRefreshBox(
        isRefreshing = isScanning,
        onRefresh = onRescanMedia,
        modifier = Modifier.fillMaxSize()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth > 600.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (isScanning) {
                    Text(
                        text = if (isPersian) "در حال اسکن و بارگذاری موزیک‌ها..." else "Scanning & loading music...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

            if (!hasAudioPermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderSpecial,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isPersian) "اجازه دسترسی به فایل‌های صوتی" else "Audio Permission Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPersian)
                                "برای اسکن و لیست کردن موزیک‌های شما، لطفا دسترسی لازم را صادر کنید."
                            else
                                "Grant access to scan and list audio files stored on your device.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onRequestPermission,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isPersian) "اعطای دسترسی و اسکن" else "Grant Access & Scan")
                        }
                    }
                }
            } else if (allSongs.isEmpty() && !isScanning) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isPersian) "هیچ آهنگی یافت نشد" else "No Songs Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPersian)
                                "هیچ فایل صوتی در حافظه دستگاه یافت نشد. برای جستجوی مجدد دکمه زیر را لمس کنید."
                            else
                                "No audio files found. Tap rescan to search device storage.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onRescanMedia,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isPersian) "اسکن مجدد حافظه" else "Rescan Storage")
                        }
                    }
                }
            }

            // Tabs
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
                val tabs = if (isPersian) listOf("آهنگ‌ها", "آلبوم‌ها", "خوانندگان", "ژانرها", "پوشه‌ها", "سال‌ها")
                else listOf("Songs", "Albums", "Artists", "Genres", "Folders", "Years")

                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // Sort Dropdown Row & Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LazyRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(SongFilterChip.values()) { chip ->
                                FilterChip(
                                    selected = currentFilterChip == chip,
                                    onClick = { currentFilterChip = chip },
                                    label = { Text(if (isPersian) chip.titleFa else chip.titleEn) }
                                )
                            }
                        }

                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(imageVector = Icons.Default.SortByAlpha, contentDescription = "Sort")
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                SongSortOption.values().forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(if (isPersian) opt.titleFa else opt.titleEn) },
                                        onClick = {
                                            currentSortOption = opt
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            if (currentSortOption == opt) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filtered and Sorted Songs List
                    val processedSongs = remember(allSongs, searchQuery, currentSortOption, currentFilterChip) {
                        var list = allSongs

                        // Search filter
                        if (searchQuery.isNotBlank()) {
                            val q = searchQuery.trim().lowercase()
                            list = list.filter {
                                it.title.lowercase().contains(q) ||
                                        it.artist.lowercase().contains(q) ||
                                        it.album.lowercase().contains(q)
                            }
                        }

                        // Filter Chips
                        list = when (currentFilterChip) {
                            SongFilterChip.ALL -> list
                            SongFilterChip.FAVORITES -> list.filter { it.isFavorite }
                            SongFilterChip.HIGH_QUALITY -> list.filter { it.bitrate >= 320 }
                            SongFilterChip.SHORT -> list.filter { it.duration < 180000L }
                            SongFilterChip.LONG -> list.filter { it.duration > 300000L }
                        }

                        // Sort Options (NEWEST is default)
                        when (currentSortOption) {
                            SongSortOption.NEWEST -> list.sortedByDescending { it.id }
                            SongSortOption.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
                            SongSortOption.TITLE_DESC -> list.sortedByDescending { it.title.lowercase() }
                            SongSortOption.ARTIST -> list.sortedBy { it.artist.lowercase() }
                            SongSortOption.DURATION_DESC -> list.sortedByDescending { it.duration }
                            SongSortOption.DURATION_ASC -> list.sortedBy { it.duration }
                        }
                    }

                    Text(
                        text = "${processedSongs.size} ${if (isPersian) "آهنگ پیدا شد" else "tracks found"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (isWideScreen) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(processedSongs, key = { it.id }) { song ->
                                SongListItem(
                                    song = song,
                                    onClick = {
                                        val idx = processedSongs.indexOf(song)
                                        onSongClick(processedSongs, idx)
                                    },
                                    onFavoriteClick = {
                                        onToggleFavorite?.invoke(song)
                                    },
                                    onMoreClick = {
                                        songToAddToPlaylist = song
                                    }
                                )
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(processedSongs, key = { it.id }) { song ->
                                SongListItem(
                                    song = song,
                                    onClick = {
                                        val idx = processedSongs.indexOf(song)
                                        onSongClick(processedSongs, idx)
                                    },
                                    onFavoriteClick = {
                                        onToggleFavorite?.invoke(song)
                                    },
                                    onMoreClick = {
                                        songToAddToPlaylist = song
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Albums
                    val albums = remember(allSongs) { allSongs.groupBy { it.album } }
                    val albumKeys = remember(albums) { albums.keys.toList() }
                    
                    if (isWideScreen) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 250.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(albumKeys) { albumName ->
                                val albumSongs = albums[albumName] ?: emptyList()
                                CategoryCard(
                                    title = albumName,
                                    subtitle = "${albumSongs.size} tracks",
                                    onClick = { if (albumSongs.isNotEmpty()) onSongClick(albumSongs, 0) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(albumKeys) { albumName ->
                                val albumSongs = albums[albumName] ?: emptyList()
                                CategoryCard(
                                    title = albumName,
                                    subtitle = "${albumSongs.size} tracks",
                                    onClick = { if (albumSongs.isNotEmpty()) onSongClick(albumSongs, 0) }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Artists
                    val artists = remember(allSongs) { allSongs.groupBy { it.artist } }
                    val artistKeys = remember(artists) { artists.keys.toList() }

                    if (isWideScreen) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 250.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(artistKeys) { artistName ->
                                val artistSongs = artists[artistName] ?: emptyList()
                                CategoryCard(
                                    title = artistName,
                                    subtitle = "${artistSongs.size} tracks",
                                    onClick = { if (artistSongs.isNotEmpty()) onSongClick(artistSongs, 0) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(artistKeys) { artistName ->
                                val artistSongs = artists[artistName] ?: emptyList()
                                CategoryCard(
                                    title = artistName,
                                    subtitle = "${artistSongs.size} tracks",
                                    onClick = { if (artistSongs.isNotEmpty()) onSongClick(artistSongs, 0) }
                                )
                            }
                        }
                    }
                }
                3 -> {
                    // Genres
                    val genres = remember(allSongs) { allSongs.groupBy { it.genre } }
                    val genreKeys = remember(genres) { genres.keys.toList() }

                    if (isWideScreen) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 250.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(genreKeys) { genreName ->
                                val genreSongs = genres[genreName] ?: emptyList()
                                CategoryCard(
                                    title = genreName,
                                    subtitle = "${genreSongs.size} tracks",
                                    onClick = { if (genreSongs.isNotEmpty()) onSongClick(genreSongs, 0) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(genreKeys) { genreName ->
                                val genreSongs = genres[genreName] ?: emptyList()
                                CategoryCard(
                                    title = genreName,
                                    subtitle = "${genreSongs.size} tracks",
                                    onClick = { if (genreSongs.isNotEmpty()) onSongClick(genreSongs, 0) }
                                )
                            }
                        }
                    }
                }
                4 -> {
                    // Folders
                    val folders = remember(allSongs) { allSongs.groupBy { it.folder } }
                    val folderKeys = remember(folders) { folders.keys.toList() }

                    if (isWideScreen) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 250.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(folderKeys) { folderName ->
                                val folderSongs = folders[folderName] ?: emptyList()
                                CategoryCard(
                                    title = folderName.ifBlank { "Root Storage" },
                                    subtitle = "${folderSongs.size} tracks",
                                    icon = Icons.Default.Folder,
                                    onClick = { if (folderSongs.isNotEmpty()) onSongClick(folderSongs, 0) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(folderKeys) { folderName ->
                                val folderSongs = folders[folderName] ?: emptyList()
                                CategoryCard(
                                    title = folderName.ifBlank { "Root Storage" },
                                    subtitle = "${folderSongs.size} tracks",
                                    icon = Icons.Default.Folder,
                                    onClick = { if (folderSongs.isNotEmpty()) onSongClick(folderSongs, 0) }
                                )
                            }
                        }
                    }
                }
                5 -> {
                    // Years
                    val years = remember(allSongs) { allSongs.groupBy { it.year } }
                    val yearKeys = remember(years) { years.keys.toList().sortedDescending() }

                    if (isWideScreen) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 250.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(yearKeys) { year ->
                                val yearSongs = years[year] ?: emptyList()
                                CategoryCard(
                                    title = if (year > 0) year.toString() else "Unknown Year",
                                    subtitle = "${yearSongs.size} tracks",
                                    onClick = { if (yearSongs.isNotEmpty()) onSongClick(yearSongs, 0) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(yearKeys) { year ->
                                val yearSongs = years[year] ?: emptyList()
                                CategoryCard(
                                    title = if (year > 0) year.toString() else "Unknown Year",
                                    subtitle = "${yearSongs.size} tracks",
                                    onClick = { if (yearSongs.isNotEmpty()) onSongClick(yearSongs, 0) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (songToAddToPlaylist != null && onAddToPlaylist != null && onCreatePlaylistAndAdd != null) {
        val s = songToAddToPlaylist!!
        AddToPlaylistDialog(
            song = s,
            playlists = playlists,
            isPersian = isPersian,
            onDismiss = { songToAddToPlaylist = null },
            onAddToPlaylist = { pId, sId -> onAddToPlaylist(pId, sId) },
            onCreatePlaylistAndAdd = { name, sId -> onCreatePlaylistAndAdd(name, sId) }
        )
    }
    }
}

@Composable
fun CategoryCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
