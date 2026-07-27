package ir.danialchoopan.tunecraftmusicplayer.ui.screens

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ir.danialchoopan.tunecraftmusicplayer.R
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity
import ir.danialchoopan.tunecraftmusicplayer.service.AudioFxManager
import ir.danialchoopan.tunecraftmusicplayer.service.PlayerState
import ir.danialchoopan.tunecraftmusicplayer.ui.components.SwipeableTrackContainer
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CarModeScreen(
    playerState: PlayerState,
    audioFxManager: AudioFxManager?,
    allSongs: List<SongEntity>,
    favoriteSongs: List<SongEntity>,
    recentlyPlayed: List<SongEntity>,
    isPersian: Boolean,
    onExitCarMode: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onPlaySongs: (List<SongEntity>, Int) -> Unit
) {
    val context = LocalContext.current
    var currentTimeStr by remember { mutableStateOf("") }

    // Live Clock Update
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentTimeStr = sdf.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    // Voice recognition launcher for hands-free drive search
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                val matched = allSongs.filter {
                    it.title.contains(spokenText, ignoreCase = true) ||
                            it.artist.contains(spokenText, ignoreCase = true)
                }
                if (matched.isNotEmpty()) {
                    onPlaySongs(matched, 0)
                    Toast.makeText(
                        context,
                        if (isPersian) "در حال پخش: ${matched.first().title}" else "Playing: ${matched.first().title}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        if (isPersian) "آهنگی با عنوان \"$spokenText\" پیدا نشد" else "No song found for \"$spokenText\"",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    val currentSong = playerState.currentSong
    val duration = playerState.durationMs.coerceAtLeast(1L)
    val position = playerState.currentPositionMs.coerceIn(0L, duration)
    val isPlaying = playerState.isPlaying

    var highContrastMode by remember { mutableStateOf(true) }

    val backgroundColor by animateColorAsState(
        if (highContrastMode) Color(0xFF000000) else MaterialTheme.colorScheme.surface
    )
    val cardColor by animateColorAsState(
        if (highContrastMode) Color(0xFF121212) else MaterialTheme.colorScheme.surfaceVariant
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Bar: Clock, Drive Mode Badge, Exit Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian) "حالت رانندگی" else "CAR MODE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = currentTimeStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Toggle contrast/theme
                IconButton(
                    onClick = { highContrastMode = !highContrastMode },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF1E293B), CircleShape)
                ) {
                    Icon(
                        imageVector = if (highContrastMode) Icons.Default.WbSunny else Icons.Default.NightsStay,
                        contentDescription = "Theme",
                        tint = Color.White
                    )
                }

                // Voice Control Button
                Button(
                    onClick = {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_PROMPT, if (isPersian) "نام آهنگ یا خواننده را بگو..." else "Say song or artist name...")
                            }
                            voiceLauncher.launch(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Voice search not supported on this device", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Search",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPersian) "جستجوی صوتی" else "Voice",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                // Exit Car Mode
                Button(
                    onClick = onExitCarMode,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPersian) "خروج" else "EXIT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val nextSong = if (playerState.currentIndex in 0 until playerState.queue.size - 1) {
            playerState.queue.getOrNull(playerState.currentIndex + 1)
        } else playerState.queue.firstOrNull()

        val previousSong = if (playerState.currentIndex > 0) {
            playerState.queue.getOrNull(playerState.currentIndex - 1)
        } else playerState.queue.lastOrNull()

        // Center Media Display Card wrapped with Swipeable Track Gestures
        SwipeableTrackContainer(
            onNext = onNext,
            onPrevious = onPrevious,
            nextSong = nextSong,
            previousSong = previousSong,
            isPersian = isPersian,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album Art (Large)
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1F2937)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(if (!currentSong?.albumArtUri.isNullOrEmpty()) currentSong?.albumArtUri else currentSong?.path)
                                .crossfade(true)
                                .error(R.drawable.blue_album_placeholder_1785090944004)
                                .placeholder(R.drawable.blue_album_placeholder_1785090944004)
                                .build(),
                            contentDescription = "Album Art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Song Info + Visualizer
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentSong?.title ?: (if (isPersian) "هیچ آهنگی در حال پخش نیست" else "No track selected"),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentSong?.artist ?: (if (isPersian) "هنرمند ناشناس" else "Unknown Artist"),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF38BDF8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress slider + times
                        Slider(
                            value = position.toFloat(),
                            onValueChange = { onSeekTo(it.toLong()) },
                            valueRange = 0f..duration.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF38BDF8),
                                activeTrackColor = Color(0xFF0088FF),
                                inactiveTrackColor = Color(0xFF334155)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(position),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = formatTime(duration),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Touch Controls Row for driving
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Seek Back 10s
            Surface(
                onClick = { onSeekTo((position - 10000L).coerceAtLeast(0L)) },
                shape = CircleShape,
                color = Color(0xFF1E293B),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "-10s",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Previous Song
            Surface(
                onClick = onPrevious,
                shape = CircleShape,
                color = Color(0xFF1E293B),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            // Play / Pause GIANT Button (92dp)
            Surface(
                onClick = onPlayPause,
                shape = CircleShape,
                color = Color(0xFF0088FF),
                modifier = Modifier.size(92.dp),
                tonalElevation = 12.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            // Next Song
            Surface(
                onClick = onNext,
                shape = CircleShape,
                color = Color(0xFF1E293B),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            // Seek Forward 10s
            Surface(
                onClick = { onSeekTo((position + 10000L).coerceAtMost(duration)) },
                shape = CircleShape,
                color = Color(0xFF1E293B),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "+10s",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Drive Playlists & Bass Boost Row
        Text(
            text = if (isPersian) "لیست‌های پخش سریع رانندگی:" else "Quick Drive Playlists:",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFF94A3B8),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                // Quick Bass Boost Toggle
                val fxState by audioFxManager?.state?.collectAsState() ?: remember { mutableStateOf(null) }
                val bassActive = (fxState?.bassBoost ?: 0) > 0
                FilterChip(
                    selected = bassActive,
                    onClick = {
                        val targetBass = if (bassActive) 0 else 80
                        audioFxManager?.setBassBoost(targetBass)
                        audioFxManager?.toggleEqualizer(true)
                    },
                    label = {
                        Text(
                            text = if (isPersian) "⚡ بیس قوی رانندگی" else "⚡ Drive Bass Boost",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.height(48.dp)
                )
            }

            item {
                FilterChip(
                    selected = false,
                    onClick = {
                        if (favoriteSongs.isNotEmpty()) {
                            onPlaySongs(favoriteSongs, 0)
                        } else {
                            Toast.makeText(context, if (isPersian) "علاقه‌مندی خالی است" else "Favorites is empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    label = {
                        Text(
                            text = if (isPersian) "❤️ علاقمندی‌ها (${favoriteSongs.size})" else "❤️ Favorites (${favoriteSongs.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.height(48.dp)
                )
            }

            item {
                FilterChip(
                    selected = false,
                    onClick = {
                        if (recentlyPlayed.isNotEmpty()) {
                            onPlaySongs(recentlyPlayed, 0)
                        } else {
                            Toast.makeText(context, if (isPersian) "لیست اخیر خالی است" else "Recently played is empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    label = {
                        Text(
                            text = if (isPersian) "🕒 شنیده‌های اخیر" else "🕒 Recently Played",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.height(48.dp)
                )
            }

            item {
                FilterChip(
                    selected = false,
                    onClick = {
                        if (allSongs.isNotEmpty()) {
                            onPlaySongs(allSongs.shuffled(), 0)
                        }
                    },
                    label = {
                        Text(
                            text = if (isPersian) "🔀 پخش تصادفی همه" else "🔀 Shuffle All Tracks",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.height(48.dp)
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format(Locale.US, "%02d:%02d", min, sec)
}
