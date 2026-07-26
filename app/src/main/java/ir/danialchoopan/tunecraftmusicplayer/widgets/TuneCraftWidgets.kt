package ir.danialchoopan.tunecraftmusicplayer.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

// Widget 1: Minimal Player (1x1)
class MinimalPlayerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceMinimalContent()
        }
    }
}

@Composable
fun GlanceMinimalContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "TuneCraft", style = TextStyle(fontSize = 12.sp))
        Text(text = "▶ Play", style = TextStyle(fontSize = 14.sp))
    }
}

// Widget 2: Full Player (4x2)
class FullPlayerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceFullPlayerContent()
        }
    }
}

@Composable
fun GlanceFullPlayerContent() {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(text = "TuneCraft Player", style = TextStyle(fontSize = 16.sp))
            Text(text = "Offline Mode Active", style = TextStyle(fontSize = 12.sp))
        }
        Row {
            Text(text = "⏮ ", style = TextStyle(fontSize = 20.sp))
            Text(text = "⏯ ", style = TextStyle(fontSize = 20.sp))
            Text(text = "⏭", style = TextStyle(fontSize = 20.sp))
        }
    }
}

// Widget 3: Visualizer Widget
class VisualizerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "TuneCraft FFT Spectrum", style = TextStyle(fontSize = 14.sp))
                Text(text = "▮ ❚ █ ▌ ▐ █ ❚ ▮", style = TextStyle(fontSize = 16.sp))
            }
        }
    }
}

// Widget 4: Queue Widget
class QueueWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Column(modifier = GlanceModifier.fillMaxSize().padding(8.dp)) {
                Text(text = "Upcoming Queue", style = TextStyle(fontSize = 14.sp))
                Text(text = "1. Crafting Silence", style = TextStyle(fontSize = 12.sp))
                Text(text = "2. Neon Waveform", style = TextStyle(fontSize = 12.sp))
                Text(text = "3. Acoustic Reflection", style = TextStyle(fontSize = 12.sp))
            }
        }
    }
}

// Widget 5: Now Playing Stats
class StatsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Column(modifier = GlanceModifier.fillMaxSize().padding(8.dp)) {
                Text(text = "Listening Stats", style = TextStyle(fontSize = 14.sp))
                Text(text = "Plays: 128 • Time: 14h", style = TextStyle(fontSize = 12.sp))
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
    override val glanceAppWidget: GlanceAppWidget = VisualizerWidget()
}

class QueueWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QueueWidget()
}

class StatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StatsWidget()
}
