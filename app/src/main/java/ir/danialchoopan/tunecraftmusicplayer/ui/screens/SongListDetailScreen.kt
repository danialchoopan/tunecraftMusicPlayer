package ir.danialchoopan.tunecraftmusicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.PlaylistEntity
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity
import ir.danialchoopan.tunecraftmusicplayer.ui.components.AddToPlaylistDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListDetailScreen(
    title: String,
    songs: List<SongEntity>,
    playlists: List<PlaylistEntity> = emptyList(),
    isPersian: Boolean = false,
    onBack: () -> Unit,
    onSongClick: (List<SongEntity>, Int) -> Unit,
    onToggleFavorite: ((SongEntity) -> Unit)? = null,
    onAddToPlaylist: ((Long, Long) -> Unit)? = null,
    onCreatePlaylistAndAdd: ((String, Long) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var songToAddToPlaylist by remember { mutableStateOf<SongEntity?>(null) }

    val filteredSongs = remember(songs, searchQuery) {
        if (searchQuery.isBlank()) {
            songs
        } else {
            val q = searchQuery.trim().lowercase()
            songs.filter {
                it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = title, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${filteredSongs.size} ${if (isPersian) "موزیک" else "tracks"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (filteredSongs.isNotEmpty()) {
                        IconButton(onClick = { onSongClick(filteredSongs.shuffled(), 0) }) {
                            Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Shuffle")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (filteredSongs.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { onSongClick(filteredSongs, 0) },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    text = { Text(if (isPersian) "پخش همه" else "Play All") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (songs.size > 5) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isPersian) "جستجو در این لیست..." else "Search in this list...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            if (filteredSongs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isPersian) "هیچ آهنگی یافت نشد" else "No songs found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredSongs, key = { it.id }) { song ->
                        SongListItem(
                            song = song,
                            onClick = {
                                val idx = filteredSongs.indexOf(song)
                                onSongClick(filteredSongs, idx)
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
