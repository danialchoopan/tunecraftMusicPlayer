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
import ir.danialchoopan.tunecraftmusicplayer.R
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import ir.danialchoopan.tunecraftmusicplayer.util.BatteryOptimizationHelper
import ir.danialchoopan.tunecraftmusicplayer.ui.components.formatTime

import androidx.compose.ui.text.style.TextAlign
import coil.request.ImageRequest

@Composable
fun HomeScreen(
    allSongs: List<SongEntity>,
    recentlyPlayed: List<SongEntity>,
    favoriteSongs: List<SongEntity>,
    isPersian: Boolean = false,
    hasAudioPermission: Boolean = true,
    onRequestPermission: () -> Unit = {},
    onRescanMedia: () -> Unit = {},
    onSongClick: (List<SongEntity>, Int) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
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

        // Hero Welcome Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = R.drawable.album_art_placeholder_1785083404441,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp)),
                        alpha = 0.35f
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isPersian) "تون‌کرافت موزیک پلیر" else "TuneCraft Music",
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isPersian) "تجربه موزیک کاملاً آفلاین و حرفه‌ای" else "100% Offline Pure Audio Craft",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Button(
                            onClick = { if (allSongs.isNotEmpty()) onSongClick(allSongs, 0) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isPersian) "پخش تصادفی" else "Shuffle All")
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
                    onClick = { if (favoriteSongs.isNotEmpty()) onSongClick(favoriteSongs, 0) }
                )
                QuickCard(
                    title = if (isPersian) "کل آهنگ‌ها" else "All Songs",
                    count = allSongs.size,
                    icon = Icons.Default.MusicNote,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToLibrary
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
                        items(recentlyPlayed) { song ->
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
                TextButton(onClick = onNavigateToLibrary) {
                    Text(if (isPersian) "مشاهده همه" else "View All")
                }
            }
        }

        items(allSongs.take(5)) { song ->
            SongListItem(
                song = song,
                onClick = {
                    val index = allSongs.indexOf(song)
                    onSongClick(allSongs, index)
                }
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
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "$count tracks",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                    .data(if (!song.albumArtUri.isNullOrEmpty()) song.albumArtUri else song.path)
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
