package ir.danialchoopan.tunecraftmusicplayer.widgets

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.action.actionStartService
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import ir.danialchoopan.tunecraftmusicplayer.MainActivity
import ir.danialchoopan.tunecraftmusicplayer.service.TuneCraftMediaService

// Widget 1: Minimal Player (1x1 or 2x1)
class MinimalPlayerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceMinimalContent(context)
        }
    }
}

@Composable
fun GlanceMinimalContent(context: Context) {
    val playIntent = Intent(context, TuneCraftMediaService::class.java).apply {
        action = TuneCraftMediaService.ACTION_PLAY_PAUSE
    }
    val openAppIntent = Intent(context, MainActivity::class.java)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(8.dp)
            .clickable(actionStartActivity(openAppIntent)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "TuneCraft", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold))
        Spacer(modifier = GlanceModifier.height(4.dp))
        Row(
            modifier = GlanceModifier
                .padding(6.dp)
                .clickable(actionStartService(playIntent, isForegroundService = true))
        ) {
            Text(text = "⏯ Play/Pause", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold))
        }
    }
}

// Widget 2: Full Player (4x2)
class FullPlayerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceFullPlayerContent(context)
        }
    }
}

@Composable
fun GlanceFullPlayerContent(context: Context) {
    val playIntent = Intent(context, TuneCraftMediaService::class.java).apply {
        action = TuneCraftMediaService.ACTION_PLAY_PAUSE
    }
    val nextIntent = Intent(context, TuneCraftMediaService::class.java).apply {
        action = TuneCraftMediaService.ACTION_NEXT
    }
    val prevIntent = Intent(context, TuneCraftMediaService::class.java).apply {
        action = TuneCraftMediaService.ACTION_PREVIOUS
    }
    val openAppIntent = Intent(context, MainActivity::class.java)

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp)
            .clickable(actionStartActivity(openAppIntent)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(text = "TuneCraft Player", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
            Text(text = "Offline Mode Active", style = TextStyle(fontSize = 12.sp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "⏮ ",
                style = TextStyle(fontSize = 22.sp),
                modifier = GlanceModifier.clickable(actionStartService(prevIntent, isForegroundService = true))
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "⏯ ",
                style = TextStyle(fontSize = 22.sp),
                modifier = GlanceModifier.clickable(actionStartService(playIntent, isForegroundService = true))
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "⏭",
                style = TextStyle(fontSize = 22.sp),
                modifier = GlanceModifier.clickable(actionStartService(nextIntent, isForegroundService = true))
            )
        }
    }
}

// Widget 3: Car Mode Quick Drive Widget (3x2)
class CarModeWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceCarModeContent(context)
        }
    }
}

@Composable
fun GlanceCarModeContent(context: Context) {
    val playIntent = Intent(context, TuneCraftMediaService::class.java).apply {
        action = TuneCraftMediaService.ACTION_PLAY_PAUSE
    }
    val nextIntent = Intent(context, TuneCraftMediaService::class.java).apply {
        action = TuneCraftMediaService.ACTION_NEXT
    }
    val carIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("OPEN_CAR_MODE", true)
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(10.dp)
            .clickable(actionStartActivity(carIntent)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🚗 DRIVE MODE",
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = GlanceModifier.height(8.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏯ Play",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.clickable(actionStartService(playIntent, isForegroundService = true))
            )
            Spacer(modifier = GlanceModifier.width(16.dp))
            Text(
                text = "⏭ Next",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.clickable(actionStartService(nextIntent, isForegroundService = true))
            )
        }
    }
}

// Widget 4: Queue Widget
class QueueWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val openAppIntent = Intent(context, MainActivity::class.java)
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .clickable(actionStartActivity(openAppIntent))
            ) {
                Text(text = "Upcoming Queue", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(text = "1. Active Playlist Item", style = TextStyle(fontSize = 12.sp))
                Text(text = "2. Next Local Track", style = TextStyle(fontSize = 12.sp))
            }
        }
    }
}

// Widget 5: Now Playing Stats
class StatsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val openAppIntent = Intent(context, MainActivity::class.java)
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .clickable(actionStartActivity(openAppIntent))
            ) {
                Text(text = "Listening Stats", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(text = "Local Tunes • Offline Playback", style = TextStyle(fontSize = 12.sp))
            }
        }
    }
}

// Receivers
class MinimalPlayerReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MinimalPlayerWidget()
}

class FullPlayerReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FullPlayerWidget()
}

class VisualizerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CarModeWidget()
}

class QueueWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QueueWidget()
}

class StatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StatsWidget()
}
