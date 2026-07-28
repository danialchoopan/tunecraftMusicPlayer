package ir.danialchoopan.tunecraftmusicplayer.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ir.danialchoopan.tunecraftmusicplayer.R
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity
import ir.danialchoopan.tunecraftmusicplayer.service.AudioFxManager
import ir.danialchoopan.tunecraftmusicplayer.service.PlayerState
import kotlinx.coroutines.launch

fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSheet(
    playerState: PlayerState,
    audioFxManager: AudioFxManager?,
    isPersian: Boolean = false,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleFavorite: (SongEntity) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onEditTags: (SongEntity) -> Unit,
    onAddToPlaylistClick: ((SongEntity) -> Unit)? = null,
    onTrimAudioClick: ((SongEntity) -> Unit)? = null,
    customPresets: List<ir.danialchoopan.tunecraftmusicplayer.data.local.entity.EqualizerPresetEntity> = emptyList(),
    onSaveCustomPreset: ((String, List<Int>, Int, Int, Float) -> Unit)? = null,
    onDeleteCustomPreset: ((ir.danialchoopan.tunecraftmusicplayer.data.local.entity.EqualizerPresetEntity) -> Unit)? = null
) {
    val song = playerState.currentSong ?: return
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0: Player, 1: Lyrics, 2: Equalizer
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Tabs
            SecondaryTabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text(if (isPersian) "پخش‌کننده" else "Player") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text(if (isPersian) "متن ترانه" else "Lyrics") }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text(if (isPersian) "اکولایزر" else "Equalizer") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (activeTab) {
                1 -> {
                    // Lyrics Tab
                    Box(modifier = Modifier.weight(1f)) {
                        LyricsView(
                            lrcContent = song.lrcContent,
                            currentPositionMs = playerState.currentPositionMs,
                            isPersian = isPersian
                        )
                    }
                }
                2 -> {
                    // Equalizer Tab
                    Box(modifier = Modifier.weight(1f)) {
                        EqualizerView(
                            audioFxManager = audioFxManager,
                            customPresets = customPresets,
                            onSaveCustomPreset = onSaveCustomPreset,
                            onDeleteCustomPreset = onDeleteCustomPreset,
                            isPersian = isPersian
                        )
                    }
                }
                else -> {
                    // Default Player Tab with Next/Previous Swipe Previews
                    val nextSong = if (playerState.currentIndex in 0 until playerState.queue.size - 1) {
                        playerState.queue.getOrNull(playerState.currentIndex + 1)
                    } else playerState.queue.firstOrNull()

                    val previousSong = if (playerState.currentIndex > 0) {
                        playerState.queue.getOrNull(playerState.currentIndex - 1)
                    } else playerState.queue.lastOrNull()

                    SwipeableTrackContainer(
                        onNext = onNext,
                        onPrevious = onPrevious,
                        nextSong = nextSong,
                        previousSong = previousSong,
                        isPersian = isPersian,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Large Artwork
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(if (!song.albumArtUri.isNullOrEmpty()) song.albumArtUri else song.path)
                                    .crossfade(true)
                                    .error(R.drawable.blue_album_placeholder_1785090944004)
                                    .placeholder(R.drawable.blue_album_placeholder_1785090944004)
                                    .build(),
                                contentDescription = "Album Art",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(280.dp)
                                    .clip(RoundedCornerShape(24.dp))
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Title, Artist & Tag Edit
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${song.artist} • ${song.album}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(onClick = { onToggleFavorite(song) }) {
                                    Icon(
                                        imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (song.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (onAddToPlaylistClick != null) {
                                    IconButton(onClick = { onAddToPlaylistClick(song) }) {
                                        Icon(
                                            imageVector = Icons.Default.PlaylistAdd,
                                            contentDescription = "Add to Playlist"
                                        )
                                    }
                                }

                                if (onTrimAudioClick != null) {
                                    IconButton(onClick = { onTrimAudioClick(song) }) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCut,
                                            contentDescription = "Trim Audio",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                IconButton(onClick = { onEditTags(song) }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Tags"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Controls (Always accessible)
            Column(modifier = Modifier.fillMaxWidth()) {
                // Seek Bar
                val pos = playerState.currentPositionMs.toFloat()
                val dur = if (playerState.durationMs > 0) playerState.durationMs.toFloat() else 1f

                Slider(
                    value = pos.coerceIn(0f, dur),
                    onValueChange = { onSeekTo(it.toLong()) },
                    valueRange = 0f..dur,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(playerState.currentPositionMs),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = formatTime(playerState.durationMs),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Media Playback Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        onToggleShuffle()
                        val msg = if (isPersian) playerState.shuffleType.labelFa else playerState.shuffleType.labelEn
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (playerState.shuffleMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = {
                        val newPos = (playerState.currentPositionMs - 10000L).coerceAtLeast(0L)
                        onSeekTo(newPos)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10s",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(onClick = onPrevious) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    val dynamicAccent = Color(playerState.paletteColors.lightVibrantColor)
                    val dynamicOnAccent = Color(playerState.paletteColors.onDominantColor)

                    Surface(
                        onClick = onPlayPause,
                        shape = CircleShape,
                        color = dynamicAccent,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = dynamicOnAccent,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    IconButton(onClick = onNext) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(onClick = {
                        val newPos = (playerState.currentPositionMs + 10000L).coerceAtMost(playerState.durationMs)
                        onSeekTo(newPos)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10s",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(onClick = onToggleRepeat) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Repeat",
                            tint = if (playerState.repeatMode != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                var showSpeedDialog by remember { mutableStateOf(false) }

                // Bottom Utilities Row (Sleep timer, Speed, Bookmark, Equalizer shortcut)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    TextButton(onClick = { showSleepTimerDialog = true }) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (playerState.sleepTimerRemainingSeconds > 0) {
                                "${playerState.sleepTimerRemainingSeconds / 60}m"
                            } else {
                                if (isPersian) "تایمر" else "Sleep"
                            }
                        )
                    }

                    TextButton(onClick = { showSpeedDialog = true }) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${playerState.playbackSpeed}x")
                    }

                    TextButton(onClick = {
                        val currentPos = playerState.currentPositionMs
                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            ir.danialchoopan.tunecraftmusicplayer.TuneCraftApplication.instance.musicRepository.addBookmark(
                                song.id,
                                currentPos,
                                "Bookmark at ${formatTime(currentPos)}"
                            )
                        }
                    }) {
                        Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPersian) "نشانه‌گذاری" else "Bookmark")
                    }

                    TextButton(onClick = { activeTab = 3 }) {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPersian) "اکولایزر" else "EQ")
                    }
                }

                if (showSpeedDialog) {
                    AlertDialog(
                        onDismissRequest = { showSpeedDialog = false },
                        title = { Text(if (isPersian) "سرعت پخش" else "Playback Speed") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f).forEach { speed ->
                                    TextButton(
                                        onClick = {
                                            ir.danialchoopan.tunecraftmusicplayer.service.TuneCraftMediaService.instance?.setPlaybackSpeedAndPitch(speed, 1.0f)
                                            showSpeedDialog = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("${speed}x ${if (speed == 1.0f) "(عادی / Normal)" else ""}")
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showSpeedDialog = false }) {
                                Text(if (isPersian) "بستن" else "Close")
                            }
                        }
                    )
                }
            }
        }
    }

    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = { Text(if (isPersian) "تنظیم تایمر خواب" else "Set Sleep Timer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 30, 45, 60, 0).forEach { mins ->
                        TextButton(
                            onClick = {
                                onSetSleepTimer(mins)
                                showSleepTimerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (mins == 0) (if (isPersian) "غیرفعال" else "Off")
                                else "$mins ${if (isPersian) "دقیقه" else "Minutes"}"
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text(if (isPersian) "بستن" else "Close")
                }
            }
        )
    }
}
