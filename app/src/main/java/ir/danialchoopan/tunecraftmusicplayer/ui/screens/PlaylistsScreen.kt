package ir.danialchoopan.tunecraftmusicplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.PlaylistEntity
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity

data class SmartPlaylist(val name: String, val nameFa: String, val filterLambda: (List<SongEntity>) -> List<SongEntity>)

@Composable
fun PlaylistsScreen(
    playlists: List<PlaylistEntity>,
    allSongs: List<SongEntity>,
    recentlyPlayed: List<SongEntity>,
    mostPlayed: List<SongEntity>,
    recentlyAdded: List<SongEntity>,
    longestSongs: List<SongEntity>,
    shortestSongs: List<SongEntity>,
    isPersian: Boolean = false,
    onCreatePlaylist: (String) -> Unit,
    onPlaylistClick: (List<SongEntity>) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    val smartPlaylists = remember(allSongs, recentlyPlayed, mostPlayed, recentlyAdded, longestSongs, shortestSongs) {
        listOf(
            SmartPlaylist(
                "Recently Played", "اخیراً پخش شده",
                filterLambda = { recentlyPlayed }
            ),
            SmartPlaylist(
                "Most Played", "بیشترین پخش",
                filterLambda = { mostPlayed }
            ),
            SmartPlaylist(
                "Recently Added", "اخیراً اضافه شده",
                filterLambda = { recentlyAdded }
            ),
            SmartPlaylist(
                "Longest Songs", "طولانی‌ترین آهنگ‌ها",
                filterLambda = { longestSongs }
            ),
            SmartPlaylist(
                "Shortest Songs", "کوتاه‌ترین آهنگ‌ها",
                filterLambda = { shortestSongs }
            ),
            SmartPlaylist(
                "Morning Energy", "انرژی صبحگاهی",
                filterLambda = { songs -> songs.filter { it.genre.contains("Workout", true) || it.genre.contains("Electronic", true) } }
            ),
            SmartPlaylist(
                "Workout", "تمرین و ورزش",
                filterLambda = { songs -> songs.filter { it.genre.contains("Workout", true) || it.bitrate >= 320 } }
            ),
            SmartPlaylist(
                "Focus", "تمرکز و مطالعه",
                filterLambda = { songs -> songs.filter { it.genre.contains("Ambient", true) || it.genre.contains("Lo-Fi", true) } }
            ),
            SmartPlaylist(
                "Chill", "ارامش بخش",
                filterLambda = { songs -> songs.filter { it.genre.contains("Acoustic", true) || it.duration > 200000L } }
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPersian) "لیست‌های پخش" else "Playlists",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create Playlist")
                }
            }
        }

        // Smart Playlists Section
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPersian) "لیست‌های پخش هوشمند" else "Smart Playlists",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(smartPlaylists) { sp ->
            val list = sp.filterLambda(allSongs)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (list.isNotEmpty()) onPlaylistClick(list) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistPlay,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPersian) sp.nameFa else sp.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${list.size} tracks",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // User Playlists Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isPersian) "لیست‌های پخش سفارشی" else "Custom User Playlists",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (playlists.isEmpty()) {
            item {
                Text(
                    text = if (isPersian) "هیچ لیست پخشی ایجاد نشده است." else "No custom playlists created yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(playlists) { pl ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlaylistClick(allSongs) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = pl.name, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(if (isPersian) "ایجاد لیست پخش جدید" else "Create New Playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text(if (isPersian) "نام لیست پخش" else "Playlist Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(newPlaylistName.trim())
                            newPlaylistName = ""
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text(if (isPersian) "ایجاد" else "Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(if (isPersian) "انصراف" else "Cancel")
                }
            }
        )
    }
}
