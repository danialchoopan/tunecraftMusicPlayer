package ir.danialchoopan.tunecraftmusicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity

enum class SearchFilter {
    ALL, TITLE, ALBUM, ARTIST, GENRE, FOLDER, LYRICS, YEAR, BITRATE, FILE_SIZE
}

@Composable
fun SearchScreen(
    allSongs: List<SongEntity>,
    isPersian: Boolean = false,
    onSongClick: (List<SongEntity>, Int) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(SearchFilter.ALL) }

    val filteredSongs = remember(query, filter, allSongs) {
        if (query.isBlank()) allSongs
        else {
            val q = query.trim().lowercase()
            allSongs.filter { song ->
                when (filter) {
                    SearchFilter.ALL -> song.title.lowercase().contains(q) ||
                            song.artist.lowercase().contains(q) ||
                            song.album.lowercase().contains(q) ||
                            song.genre.lowercase().contains(q) ||
                            (song.lrcContent?.lowercase()?.contains(q) == true)
                    SearchFilter.TITLE -> song.title.lowercase().contains(q)
                    SearchFilter.ALBUM -> song.album.lowercase().contains(q)
                    SearchFilter.ARTIST -> song.artist.lowercase().contains(q)
                    SearchFilter.GENRE -> song.genre.lowercase().contains(q)
                    SearchFilter.FOLDER -> song.folder.lowercase().contains(q)
                    SearchFilter.LYRICS -> song.lrcContent?.lowercase()?.contains(q) == true
                    SearchFilter.YEAR -> song.year.toString().contains(q)
                    SearchFilter.BITRATE -> song.bitrate.toString().contains(q)
                    SearchFilter.FILE_SIZE -> song.fileSize.toString().contains(q)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (isPersian) "جستجوی پیشرفته" else "Search Music",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(if (isPersian) "جستجو بر اساس عنوان، متن، سال..." else "Search by title, lyrics, year...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SearchFilter.values()) { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { filter = f },
                    label = { Text(f.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Results
        Text(
            text = "${filteredSongs.size} ${if (isPersian) "نتیجه یافت شد" else "results found"}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredSongs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    onClick = {
                        val index = filteredSongs.indexOf(song)
                        onSongClick(filteredSongs, index)
                    }
                )
            }
        }
    }
}
