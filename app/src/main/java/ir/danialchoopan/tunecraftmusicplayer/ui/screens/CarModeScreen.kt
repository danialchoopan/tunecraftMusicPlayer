package ir.danialchoopan.tunecraftmusicplayer.ui.screens

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import ir.danialchoopan.tunecraftmusicplayer.ui.components.formatTime
import java.text.SimpleDateFormat
import java.util.*

/**
 * CarModeScreen — Optimized driving interface with large touch targets.
 *
 * Key design principles for automotive use:
 * - Minimum touch target: 64dp (92dp for play/pause)
 * - High contrast: dark background with bright accent colors to fight glare
 * - Voice search: hands-free song lookup via RecognizerIntent
 * - Quick actions: Bass Boost toggle, Favorites, Recently Played, Shuffle All
 * - Live clock in header
 *
 * All colors use MaterialTheme.colorScheme values where possible,
 * with overrides only for the high-contrast AMOLED toggle.
 */
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

    // Live clock — updates every second via a coroutine
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentTimeStr = sdf.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    // Voice search launcher — fires RecognizerIntent, matches result against library
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
                    Toast.makeText(context,
                        if (isPersian) "در حال پخش: ${matched.first().title}" else "Playing: ${matched.first().title}",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context,
                        if (isPersian) "آهنگی با عنوان \"$spokenText\" پیدا نشد" else "No song found for \"$spokenText\"",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val currentSong = playerState.currentSong
    val duration = playerState.durationMs.coerceAtLeast(1L)
    val position = playerState.currentPositionMs.coerceIn(0L, duration)
    val isPlaying = playerState.isPlaying

    // High-contrast mode toggle — switches between pure black AMOLED and theme surface
    var highContrastMode by remember { mutableStateOf(true) }

    val backgroundColor by animateColorAsState(
        if (highContrastMode) Color(0xFF000000) else MaterialTheme.colorScheme.background,
        label = "bgColor"
    )
    val surfaceColor by animateColorAsState(
        if (highContrastMode) Color(0xFF121212) else MaterialTheme.colorScheme.surface,
        label = "surfaceColor"
    )
    // Accent color used for play button and highlights
    val accentColor = if (highContrastMode) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Top Bar: Car Mode badge + clock + actions ──────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Car mode badge with live clock
            Surface(
                color = surfaceColor,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian) "حالت رانندگی" else "CAR MODE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = currentTimeStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Toggle high contrast mode
                IconButton(
                    onClick = { highContrastMode = !highContrastMode },
                    modifier = Modifier
                        .size(48.dp)
                        .background(surfaceColor, CircleShape)
                ) {
                    Icon(
                        imageVector = if (highContrastMode) Icons.Default.WbSunny else Icons.Default.NightsStay,
                        contentDescription = "Toggle Contrast",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Voice search button
                FilledTonalButton(
                    onClick = {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_PROMPT,
                                    if (isPersian) "نام آهنگ یا خواننده را بگو..." else "Say song or artist name...")
                            }
                            voiceLauncher.launch(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Voice search not supported", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Search", modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPersian) "جستجوی صوتی" else "Voice", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                // Exit car mode (red for danger/warning semantics)
                Button(
                    onClick = onExitCarMode,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Exit", modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPersian) "خروج" else "EXIT", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Center: Swipeable track card with album art + progress ─────────
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
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Large album art thumbnail
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
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

                    // Song info + seek bar
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentSong?.title ?: (if (isPersian) "هیچ آهنگی در حال پخش نیست" else "No track selected"),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentSong?.artist ?: (if (isPersian) "هنرمند ناشناس" else "Unknown Artist"),
                            style = MaterialTheme.typography.titleLarge,
                            color = accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Seek bar
                        Slider(
                            value = position.toFloat(),
                            onValueChange = { onSeekTo(it.toLong()) },
                            valueRange = 0f..duration.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = surfaceColor.copy(alpha = 0.5f)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = formatTime(position),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text(text = formatTime(duration),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Large touch controls ────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rewind 10s
            Surface(
                onClick = { onSeekTo((position - 10000L).coerceAtLeast(0L)) },
                shape = CircleShape,
                color = surfaceColor,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Replay10, contentDescription = "-10s",
                        tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(36.dp))
                }
            }

            // Previous
            Surface(
                onClick = onPrevious,
                shape = CircleShape,
                color = surfaceColor,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(44.dp))
                }
            }

            // Play/Pause — 92dp, accent colored
            Surface(
                onClick = onPlayPause,
                shape = CircleShape,
                color = accentColor,
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

            // Next
            Surface(
                onClick = onNext,
                shape = CircleShape,
                color = surfaceColor,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(44.dp))
                }
            }

            // Forward 10s
            Surface(
                onClick = { onSeekTo((position + 10000L).coerceAtMost(duration)) },
                shape = CircleShape,
                color = surfaceColor,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Forward10, contentDescription = "+10s",
                        tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(36.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Quick drive shortcuts ───────────────────────────────────────────
        Text(
            text = if (isPersian) "دسترسی سریع رانندگی:" else "Quick Drive Access:",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            // Bass Boost toggle with visual feedback
            item {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (bassActive) Icons.Default.Equalizer else Icons.Default.Equalizer,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (bassActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isPersian) "بیس قوی" else "Bass Boost",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp
                            )
                        }
                    },
                    modifier = Modifier.height(48.dp)
                )
            }

            // Favorites playlist
            item {
                FilterChip(
                    selected = false,
                    onClick = {
                        if (favoriteSongs.isNotEmpty()) onPlaySongs(favoriteSongs, 0)
                        else Toast.makeText(context,
                            if (isPersian) "علاقه‌مندی خالی است" else "Favorites is empty",
                            Toast.LENGTH_SHORT).show()
                    },
                    label = {
                        Text("❤️ ${if (isPersian) "علاقه‌مندی‌ها" else "Favorites"} (${favoriteSongs.size})",
                            fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    },
                    modifier = Modifier.height(48.dp)
                )
            }

            // Recently Played
            item {
                FilterChip(
                    selected = false,
                    onClick = {
                        if (recentlyPlayed.isNotEmpty()) onPlaySongs(recentlyPlayed, 0)
                        else Toast.makeText(context,
                            if (isPersian) "لیست اخیر خالی است" else "Recently played is empty",
                            Toast.LENGTH_SHORT).show()
                    },
                    label = {
                        Text("🕒 ${if (isPersian) "شنیده‌های اخیر" else "Recent"}",
                            fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    },
                    modifier = Modifier.height(48.dp)
                )
            }

            // Shuffle all
            item {
                FilterChip(
                    selected = false,
                    onClick = { if (allSongs.isNotEmpty()) onPlaySongs(allSongs.shuffled(), 0) },
                    label = {
                        Text("🔀 ${if (isPersian) "پخش تصادفی" else "Shuffle All"}",
                            fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    },
                    modifier = Modifier.height(48.dp)
                )
            }
        }
    }
}