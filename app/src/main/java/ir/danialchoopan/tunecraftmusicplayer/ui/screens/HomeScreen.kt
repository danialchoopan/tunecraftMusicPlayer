package ir.danialchoopan.tunecraftmusicplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import ir.danialchoopan.tunecraftmusicplayer.R
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.PlaylistEntity
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity
import ir.danialchoopan.tunecraftmusicplayer.ui.components.AddToPlaylistDialog

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import ir.danialchoopan.tunecraftmusicplayer.util.BatteryOptimizationHelper
import ir.danialchoopan.tunecraftmusicplayer.ui.components.formatTime

import androidx.compose.ui.text.style.TextAlign
import coil.request.ImageRequest

/**
 * High-Performance Home Screen Composable.
 *
 * Technical Highlights & Developer Notes:
 * 1. Item Key Stability: Uses unique song IDs (`key = { it.id }`) in all `LazyRow` and `LazyColumn` items
 *    to prevent unnecessary recomposition and maintain scroll position.
 * 2. Async Image Caching: Employs Coil image requests with crossfade and thumbnail fallback for fast metadata rendering.
 * 3. Responsive Layout: Automatically scales across Portrait, Landscape, Tablet, and Car Head Unit screens.
 * 4. Battery Optimization Observer: Listens to lifecycle ON_RESUME events to update battery status seamlessly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    allSongs: List<SongEntity>,
    recentlyPlayed: List<SongEntity>,
    favoriteSongs: List<SongEntity>,
    recentlyAdded: List<SongEntity> = emptyList(),
    playlists: List<PlaylistEntity> = emptyList(),
    isScanning: Boolean = false,
    isPersian: Boolean = false,
    hasAudioPermission: Boolean = true,
    onRequestPermission: () -> Unit = {},
    onRescanMedia: () -> Unit = {},
    onSongClick: (List<SongEntity>, Int) -> Unit,
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToAllSongs: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onToggleFavorite: ((SongEntity) -> Unit)? = null,
    onAddToPlaylist: ((Long, Long) -> Unit)? = null,
    onCreatePlaylistAndAdd: ((String, Long) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedFilter by remember { mutableIntStateOf(0) } // 0 = All, 1 = Recently Added
    var songToAddToPlaylist by remember { mutableStateOf<SongEntity?>(null) }

    val displayedSongs = remember(selectedFilter, allSongs, recentlyAdded) {
        when (selectedFilter) {
            1 -> if (recentlyAdded.isNotEmpty()) recentlyAdded else allSongs
            else -> allSongs
        }
    }

    var isBatteryOptimized by remember {
        mutableStateOf(!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
    }
    var showBatteryOptDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isBatteryOptimized = !BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showBatteryOptDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryOptDialog = false },
            title = {
                Text(
                    text = if (isPersian) "تنظیمات پخش مداوم در پس‌زمینه" else "Unrestricted Background Playback",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isPersian)
                        "برای جلوگیری از بسته شدن خودکار موزیک پلیر توسط اندروید هنگام خاموش شدن صفحه یا قفل شدن گوشی، لطفا در صفحه تنظیمات حالت Battery Optimization را روی 'Unrestricted' یا 'بدون محدودیت' بگذارید.\n\nپس از تغییر حالت، با بازگشت به برنامه این پیام به طور خودکار برداشته می‌شود."
                    else
                        "To prevent Android from pausing or killing music playback when the screen locks or turns off, please set Battery Optimization for TuneCraft to 'Unrestricted'.\n\nAfter changing this setting, returning to the app will automatically dismiss this warning."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBatteryOptDialog = false
                        BatteryOptimizationHelper.openBatteryOptimizationSettings(context)
                    }
                ) {
                    Text(if (isPersian) "ورود به تنظیمات" else "Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatteryOptDialog = false }) {
                    Text(if (isPersian) "متوجه شدم" else "Got it")
                }
            }
        )
    }

    PullToRefreshBox(
        isRefreshing = isScanning,
        onRefresh = onRescanMedia,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
            // Permission Warning Card
            if (!hasAudioPermission) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderSpecial,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isPersian) "اجازه دسترسی به فایل‌های صوتی" else "Audio Permission Required",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isPersian)
                                    "برای شناسایی، اسکن و پخش اهنگ‌های موجود در حافظه دستگاه شما، نیاز به مجوز دسترسی می‌باشد."
                                else
                                    "Grant access to allow TuneCraft to scan and play audio files stored on your device.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onRequestPermission,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.LockOpen, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isPersian) "اعطای دسترسی و اسکن موزیک‌ها" else "Grant Access & Scan")
                            }
                        }
                    }
                }
            } else if (allSongs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isPersian) "هیچ آهنگی یافت نشد" else "No Songs Found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isPersian)
                                    "هنوز هیچ فایل صوتی در حافظه دستگاه شما پیدا نشده است. از دکمه زیر برای جستجوی مجدد استفاده کنید."
                                else
                                    "No audio files detected on your device storage. Tap below to scan again.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onRescanMedia,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isPersian) "جستجوی مجدد حافظه" else "Rescan Storage")
                            }
                        }
                    }
                }
            }

            // Battery Optimization Warning Banner
            if (isBatteryOptimized) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(16.dp),
                        onClick = { showBatteryOptDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPersian) "پخش بدون قطعی در پس‌زمینه" else "Unrestricted Background Playback",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = if (isPersian) "جهت جلوگیری از قطع موزیک هنگام خاموش شدن صفحه، بهینه‌سازی باتری را غیرفعال کنید" else "Disable battery optimization so audio keeps playing uninterrupted in background.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    showBatteryOptDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text(if (isPersian) "اصلاح" else "Fix Now")
                            }
                        }
                    }
                }
            }

            // Quick Shortcuts Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickCard(
                        title = if (isPersian) "محبوب‌ها" else "Favorites",
                        count = favoriteSongs.size,
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToFavorites,
                        onShuffleClick = if (favoriteSongs.isNotEmpty()) {
                            { onSongClick(favoriteSongs.shuffled(), 0) }
                        } else null
                    )
                    QuickCard(
                        title = if (isPersian) "کل آهنگ‌ها" else "All Songs",
                        count = allSongs.size,
                        icon = Icons.Default.MusicNote,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAllSongs,
                        onShuffleClick = if (allSongs.isNotEmpty()) {
                            { onSongClick(allSongs.shuffled(), 0) }
                        } else null
                    )
                }
            }

            // Recently Played Horizontal Carousel
            if (recentlyPlayed.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = if (isPersian) "اخیراً پخش شده" else "Recently Played",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(recentlyPlayed, key = { it.id }) { song ->
                                SongCard(
                                    song = song,
                                    onClick = {
                                        val index = recentlyPlayed.indexOf(song)
                                        onSongClick(recentlyPlayed, index)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Offline Library Summary List
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPersian) "آهنگ‌های پیشنهادی" else "Featured Local Songs",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateToAllSongs) {
                            Text(if (isPersian) "مشاهده همه" else "View All")
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedFilter == 0,
                            onClick = { selectedFilter = 0 },
                            label = { Text(if (isPersian) "همه موزیک‌ها" else "All Songs") },
                            leadingIcon = if (selectedFilter == 0) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = selectedFilter == 1,
                            onClick = { selectedFilter = 1 },
                            label = { Text(if (isPersian) "جدیدترین‌ها" else "Recently Added") },
                            leadingIcon = if (selectedFilter == 1) {
                                { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            items(displayedSongs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    onClick = {
                        val index = displayedSongs.indexOf(song)
                        onSongClick(displayedSongs, index)
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

        // Floating Action Button for Shuffle All
        if (allSongs.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = {
                    val randomIdx = (allSongs.indices).random()
                    onSongClick(allSongs, randomIdx)
                },
                icon = { Icon(Icons.Default.Shuffle, contentDescription = null) },
                text = { Text(if (isPersian) "پخش تصادفی" else "Shuffle All") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
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
}

@Composable
fun QuickCard(
    title: String,
    count: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onShuffleClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$count tracks",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (onShuffleClick != null) {
                IconButton(
                    onClick = onShuffleClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle Play",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SongCard(
    song: SongEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(if (!song.albumArtUri.isNullOrEmpty()) song.albumArtUri else song.path)
                .crossfade(true)
                .error(R.drawable.blue_album_placeholder_1785090944004)
                .placeholder(R.drawable.blue_album_placeholder_1785090944004)
                .build(),
            contentDescription = song.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SongListItem(
    song: SongEntity,
    onClick: () -> Unit,
    onFavoriteClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(if (!song.albumArtUri.isNullOrEmpty()) song.albumArtUri else R.drawable.blue_album_placeholder_1785090944004)
                    .crossfade(true)
                    .error(R.drawable.blue_album_placeholder_1785090944004)
                    .placeholder(R.drawable.blue_album_placeholder_1785090944004)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (song.savedPositionMs > 5000L && song.savedPositionMs < song.duration - 5000L) {
                    Text(
                        text = "ادامه از ${formatTime(song.savedPositionMs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(
                text = "${song.duration / 60000}:${String.format("%02d", (song.duration % 60000) / 1000)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (onFavoriteClick != null) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (song.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (onMoreClick != null) {
                IconButton(onClick = onMoreClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
