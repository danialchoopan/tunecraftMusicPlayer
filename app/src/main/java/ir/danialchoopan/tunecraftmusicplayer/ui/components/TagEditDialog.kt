package ir.danialchoopan.tunecraftmusicplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity

@Composable
fun TagEditDialog(
    song: SongEntity,
    isPersian: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (SongEntity) -> Unit
) {
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.album) }
    var genre by remember { mutableStateOf(song.genre) }
    var yearText by remember { mutableStateOf(song.year.toString()) }
    var lyricsText by remember { mutableStateOf(song.lrcContent ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isPersian) "ویرایش اطلاعات آهنگ" else "Edit Song Metadata") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isPersian) "عنوان آهنگ" else "Song Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text(if (isPersian) "خواننده" else "Artist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text(if (isPersian) "آلبوم" else "Album") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text(if (isPersian) "ژانر" else "Genre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { yearText = it },
                    label = { Text(if (isPersian) "سال انتشار" else "Year") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lyricsText,
                    onValueChange = { lyricsText = it },
                    label = { Text(if (isPersian) "متن ترانه (.lrc)" else "Lyrics (.lrc)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = song.copy(
                        title = title.ifBlank { "Unknown Title" },
                        artist = artist.ifBlank { "Unknown Artist" },
                        album = album.ifBlank { "Unknown Album" },
                        genre = genre.ifBlank { "Unknown Genre" },
                        year = yearText.toIntOrNull() ?: song.year,
                        lrcContent = lyricsText.ifBlank { null }
                    )
                    onSave(updated)
                }
            ) {
                Text(if (isPersian) "ذخیره" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isPersian) "انصراف" else "Cancel")
            }
        }
    )
}
