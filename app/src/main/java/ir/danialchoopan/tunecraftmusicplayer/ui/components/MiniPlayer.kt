package ir.danialchoopan.tunecraftmusicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ir.danialchoopan.tunecraftmusicplayer.R
import ir.danialchoopan.tunecraftmusicplayer.service.PlayerState

/*
 * MiniPlayer - Compact bottom bar showing current track with playback controls.
 *
 * Displays album art, song title, artist, a thin progress line,
 * and play/pause + next buttons. The entire card is wrapped in
 * SwipeableTrackContainer for left/right swipe-to-change-track gestures.
 *
 * Visual notes:
 * - Uses a surface container background for the glass-morphism card effect
 * - Progress indicator uses M3's LinearProgressIndicator with lambda syntax
 * - Album art is 48dp with 8dp rounded corners
 */
@Composable
fun MiniPlayer(
    playerState: PlayerState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit = {},
    onClick: () -> Unit,
    isPersian: Boolean = false,
    modifier: Modifier = Modifier
) {
    val currentSong = playerState.currentSong ?: return
    val context = LocalContext.current

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
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable { onClick() },
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Column {
                // Thin animated progress bar at the very top of the card
                val progress = if (playerState.durationMs > 0) {
                    playerState.currentPositionMs.toFloat() / playerState.durationMs.toFloat()
                } else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album art thumbnail
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(if (!currentSong.albumArtUri.isNullOrEmpty()) currentSong.albumArtUri else currentSong.path)
                            .crossfade(true)
                            .error(R.drawable.blue_album_placeholder_1785090944004)
                            .placeholder(R.drawable.blue_album_placeholder_1785090944004)
                            .build(),
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Song title + artist text (truncated to one line each)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong.title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentSong.artist,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Previous track button (smaller, secondary)
                    IconButton(onClick = onPrevious) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Play/Pause — uses primary tint to draw attention
                    FilledIconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Next track button
                    IconButton(onClick = onNext) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next"
                        )
                    }
                }
            }
        }
    }
}