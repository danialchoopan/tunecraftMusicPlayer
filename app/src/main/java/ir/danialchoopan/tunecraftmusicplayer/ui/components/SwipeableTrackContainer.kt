package ir.danialchoopan.tunecraftmusicplayer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ir.danialchoopan.tunecraftmusicplayer.R
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * SwipeableTrackContainer
 *
 * A high-performance gesture container enabling smooth horizontal dragging (swipe left/right)
 * to skip songs. Displays a real-time live preview card with the title, artist, and album artwork
 * of the next or previous track in the queue.
 *
 * @param onNext Triggered when user swiped left past the threshold.
 * @param onPrevious Triggered when user swiped right past the threshold.
 * @param nextSong Next track in queue for live drag preview.
 * @param previousSong Previous track in queue for live drag preview.
 * @param isPersian Enable RTL string & directional support.
 * @param enabled Whether drag gestures are active.
 */
@Composable
fun SwipeableTrackContainer(
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    nextSong: SongEntity? = null,
    previousSong: SongEntity? = null,
    isPersian: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl || isPersian

    // Drag distance thresholds (px)
    val thresholdPx = with(density) { 80.dp.toPx() }
    val maxDragPx = with(density) { 220.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (enabled) {
                    Modifier.pointerInput(isRtl) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    val currentOffset = offsetX.value
                                    if (currentOffset < -thresholdPx) {
                                        onNext()
                                        offsetX.animateTo(0f, spring())
                                    } else if (currentOffset > thresholdPx) {
                                        onPrevious()
                                        offsetX.animateTo(0f, spring())
                                    } else {
                                        offsetX.animateTo(0f, spring())
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    offsetX.animateTo(0f, spring())
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    val newOffset = offsetX.value + dragAmount
                                    offsetX.snapTo(newOffset.coerceIn(-maxDragPx, maxDragPx))
                                }
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        val currentOffset = offsetX.value
        val absOffset = abs(currentOffset)

        // Main player UI content layer with translation & spring scale
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = currentOffset
                    val progress = (absOffset / thresholdPx).coerceIn(0f, 1f)
                    scaleX = 1f - (progress * 0.04f)
                    scaleY = 1f - (progress * 0.04f)
                },
            content = content
        )

        // Dragging Left -> Next Track Live Preview Card Overlay
        if (currentOffset < -20f) {
            val progress = (absOffset / thresholdPx).coerceIn(0f, 1f)
            Surface(
                modifier = Modifier
                    .align(if (isRtl) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 12.dp)
                    .widthIn(max = 240.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = (progress * 0.95f)),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                tonalElevation = 8.dp,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(if (!nextSong?.albumArtUri.isNullOrEmpty()) nextSong?.albumArtUri else nextSong?.path)
                                .crossfade(true)
                                .error(R.drawable.blue_album_placeholder_1785090944004)
                                .placeholder(R.drawable.blue_album_placeholder_1785090944004)
                                .build(),
                            contentDescription = "Next Track Art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPersian) "آهنگ بعدی" else "Next Track",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = nextSong?.title ?: (if (isPersian) "آهنگ بعدی" else "Next Track"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!nextSong?.artist.isNullOrBlank()) {
                            Text(
                                text = nextSong?.artist ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isRtl) Icons.AutoMirrored.Filled.NavigateBefore else Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else if (currentOffset > 20f) {
            // Dragging Right -> Previous Track Live Preview Card Overlay
            val progress = (absOffset / thresholdPx).coerceIn(0f, 1f)
            Surface(
                modifier = Modifier
                    .align(if (isRtl) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(horizontal = 12.dp)
                    .widthIn(max = 240.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = (progress * 0.95f)),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                tonalElevation = 8.dp,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isRtl) Icons.AutoMirrored.Filled.NavigateNext else Icons.AutoMirrored.Filled.NavigateBefore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(if (!previousSong?.albumArtUri.isNullOrEmpty()) previousSong?.albumArtUri else previousSong?.path)
                                .crossfade(true)
                                .error(R.drawable.blue_album_placeholder_1785090944004)
                                .placeholder(R.drawable.blue_album_placeholder_1785090944004)
                                .build(),
                            contentDescription = "Previous Track Art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPersian) "آهنگ قبلی" else "Previous Track",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = previousSong?.title ?: (if (isPersian) "آهنگ قبلی" else "Previous Track"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!previousSong?.artist.isNullOrBlank()) {
                            Text(
                                text = previousSong?.artist ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}
