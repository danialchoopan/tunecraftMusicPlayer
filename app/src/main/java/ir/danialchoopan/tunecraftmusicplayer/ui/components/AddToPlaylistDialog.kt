package ir.danialchoopan.tunecraftmusicplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.PlaylistEntity
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity

@Composable
fun AddToPlaylistDialog(
    song: SongEntity,
    playlists: List<PlaylistEntity>,
    isPersian: Boolean = false,
    onDismiss: () -> Unit,
    onAddToPlaylist: (Long, Long) -> Unit, // playlistId, songId
    onCreatePlaylistAndAdd: (String, Long) -> Unit // name, songId
) {
    var showCreateSubDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isPersian) "افزودن «${song.title}» به لیست پخش" else "Add \"${song.title}\" to Playlist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = { showCreateSubDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPersian) "ایجاد لیست پخش جدید..." else "Create New Playlist...")
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (playlists.isEmpty()) {
                    Text(
                        text = if (isPersian) "هیچ لیست پخشی وجود ندارد. اول یکی بسازید." else "No playlists available. Create one above.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(playlists) { playlist ->
                            Surface(
                                onClick = {
                                    onAddToPlaylist(playlist.id, song.id)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.PlaylistPlay, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = playlist.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isPersian) "انصراف" else "Cancel")
            }
        }
    )

    if (showCreateSubDialog) {
        AlertDialog(
            onDismissRequest = { showCreateSubDialog = false },
            title = { Text(if (isPersian) "نام لیست پخش جدید" else "New Playlist Name") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    singleLine = true,
                    label = { Text(if (isPersian) "عنوان" else "Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylistAndAdd(newPlaylistName.trim(), song.id)
                            showCreateSubDialog = false
                            onDismiss()
                        }
                    }
                ) {
                    Text(if (isPersian) "ایجاد و افزودن" else "Create & Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateSubDialog = false }) {
                    Text(if (isPersian) "انصراف" else "Cancel")
                }
            }
        )
    }
}
