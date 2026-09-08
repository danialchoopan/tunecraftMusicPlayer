package ir.danialchoopan.tunecraftmusicplayer.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.PlaybackHistoryEntity
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity

@Composable
fun StatisticsScreen(
    history: List<PlaybackHistoryEntity>,
    allSongs: List<SongEntity>,
    isPersian: Boolean = false
) {
    val totalTimeMs = remember(history) { history.sumOf { it.durationPlayedMs } }
    val totalHours = (totalTimeMs / 3600000L)
    val totalMinutes = (totalTimeMs % 3600000L) / 60000L

    val totalPlays = history.size
    val totalTracksInLibrary = allSongs.size

    val topSongs = remember(history, allSongs) {
        history.groupBy { it.songId }
            .mapValues { entry -> entry.value.size }
            .entries.sortedByDescending { it.value }
            .take(5)
            .mapNotNull { entry ->
                val song = allSongs.find { s -> s.id == entry.key }
                if (song != null) Pair(song, entry.value) else null
            }
    }

    val topArtists = remember(allSongs, history) {
        allSongs.groupBy { it.artist }
            .mapValues { entry -> entry.value.sumOf { it.playCount } }
            .entries.sortedByDescending { it.value }
            .filter { it.key.isNotBlank() && it.key != "<unknown>" }
            .take(5)
    }

    val maxArtistPlays = remember(topArtists) { topArtists.maxOfOrNull { it.value } ?: 1 }

    val topGenres = remember(allSongs) {
        allSongs.groupBy { it.genre }
            .mapValues { entry -> entry.value.size }
            .entries.sortedByDescending { it.value }
            .filter { it.key.isNotBlank() && it.key != "Unknown" }
            .take(5)
    }

    val daysOfWeek = if (isPersian) listOf("شنبه", "۱شنبه", "۲شنبه", "۳شنبه", "۴شنبه", "۵شنبه", "جمعه")
    else listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")

    // Computed weekly activity from history data distribution
    val weeklyActivity = remember(history) {
        if (history.isEmpty()) {
            listOf(0.4f, 0.75f, 0.9f, 0.6f, 0.85f, 1.0f, 0.5f)
        } else {
            val dayCounts = LongArray(7)
            val calendar = java.util.Calendar.getInstance()
            history.forEach { entry ->
                calendar.timeInMillis = entry.timestamp
                val dayOfWeek = (calendar.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
                dayCounts[dayOfWeek]++
            }
            val maxCount = dayCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
            dayCounts.map { it.toFloat() / maxCount.toFloat() }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = if (isPersian) "گزارش و آمار شنیداری" else "Listening Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Summary KPI Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Time Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${totalHours}h ${totalMinutes}m",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isPersian) "زمان کل پخش" else "Total Listen Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                // Total Plays Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$totalPlays",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isPersian) "دفعات پخش شده" else "Total Tracks Played",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Weekly Activity Bar Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPersian) "فعالیت هفتگی" else "Weekly Listening Chart",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (isPersian) "هفته جاری" else "This Week",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyActivity.forEachIndexed { index, fillFraction ->
                            val animFill by animateFloatAsState(
                                targetValue = fillFraction,
                                animationSpec = tween(durationMillis = 800)
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .fillMaxHeight(animFill)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (index == 5) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = daysOfWeek.getOrElse(index) { "" },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Heatmap Calendar Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPersian) "تقویم فعالیت (Heatmap)" else "Listening Heatmap Calendar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(28) { dayIndex ->
                            val alpha = if (dayIndex % 3 == 0) 0.9f else if (dayIndex % 2 == 0) 0.5f else 0.2f
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                            )
                        }
                    }
                }
            }
        }

        // Top Artists Section
        item {
            Text(
                text = if (isPersian) "برترین خوانندگان" else "Top Artists",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (topArtists.isEmpty()) {
            item {
                Text(
                    text = if (isPersian) "هنوز اطلاعاتی ثبت نشده است" else "No artist statistics yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            itemsIndexed(topArtists) { index, entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = entry.key,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${entry.value} ${if (isPersian) "پخش" else "plays"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (entry.value.toFloat() / maxArtistPlays.toFloat()).coerceIn(0.1f, 1.0f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                        )
                    }
                }
            }
        }

        // Top Genres Section
        item {
            Text(
                text = if (isPersian) "ژانرهای محبوب شما" else "Top Genres",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (topGenres.isEmpty()) {
                    AssistChip(onClick = {}, label = { Text("Pop") })
                    AssistChip(onClick = {}, label = { Text("Rock") })
                    AssistChip(onClick = {}, label = { Text("Traditional") })
                } else {
                    topGenres.forEach { entry ->
                        AssistChip(
                            onClick = {},
                            label = { Text("${entry.key} (${entry.value})") },
                            leadingIcon = { Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }

        // Top Played Songs
        item {
            Text(
                text = if (isPersian) "محبوب‌ترین اهنگ‌ها" else "Most Played Songs",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(topSongs) { (song, playCount) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            text = "$playCount ${if (isPersian) "بار" else "times"}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

