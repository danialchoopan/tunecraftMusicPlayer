package ir.danialchoopan.tunecraftmusicplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity

import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.ui.text.style.TextAlign

@Composable
fun LibraryScreen(
    allSongs: List<SongEntity>,
    isPersian: Boolean = false,
    hasAudioPermission: Boolean = true,
    onRequestPermission: () -> Unit = {},
    onSongClick: (List<SongEntity>, Int) -> Unit,
    onRescanMedia: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Songs, 1: Albums, 2: Artists, 3: Genres, 4: Folders, 5: Years

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Title & Rescan Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isPersian) "کتابخانه موسیقی" else "Music Library",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onRescanMedia) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Rescan")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
        } else if (allSongs.isEmpty()) {
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

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                // All Songs
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(allSongs) { song ->
                        SongListItem(
                            song = song,
                            onClick = {
                                val idx = allSongs.indexOf(song)
                                onSongClick(allSongs, idx)
                            }
                        )
                    }
                }
            }
            1 -> {
                // Albums
                val albums = remember(allSongs) { allSongs.groupBy { it.album } }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(albums.keys.toList()) { albumName ->
                        val albumSongs = albums[albumName] ?: emptyList()
                        CategoryCard(
                            title = albumName,
                            subtitle = "${albumSongs.size} tracks",
                            onClick = { if (albumSongs.isNotEmpty()) onSongClick(albumSongs, 0) }
                        )
                    }
                }
            }
            2 -> {
                // Artists
                val artists = remember(allSongs) { allSongs.groupBy { it.artist } }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(artists.keys.toList()) { artistName ->
                        val artistSongs = artists[artistName] ?: emptyList()
                        CategoryCard(
                            title = artistName,
                            subtitle = "${artistSongs.size} tracks",
                            onClick = { if (artistSongs.isNotEmpty()) onSongClick(artistSongs, 0) }
                        )
                    }
                }
            }
            3 -> {
                // Genres
                val genres = remember(allSongs) { allSongs.groupBy { it.genre } }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(genres.keys.toList()) { genreName ->
                        val genreSongs = genres[genreName] ?: emptyList()
                        CategoryCard(
                            title = genreName,
                            subtitle = "${genreSongs.size} tracks",
                            onClick = { if (genreSongs.isNotEmpty()) onSongClick(genreSongs, 0) }
                        )
                    }
                }
            }
            4 -> {
                // Folders
                val folders = remember(allSongs) { allSongs.groupBy { it.folder } }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(folders.keys.toList()) { folderName ->
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
            5 -> {
                // Years
                val years = remember(allSongs) { allSongs.groupBy { it.year } }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(years.keys.toList().sortedDescending()) { year ->
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
