package ir.danialchoopan.tunecraftmusicplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import ir.danialchoopan.tunecraftmusicplayer.service.AudioFxManager
import kotlin.math.cos
import kotlin.math.sin

enum class VisualizerMode {
    WAVEFORM, SPECTRUM, CIRCULAR, BARS
}

@Composable
fun VisualizerCanvas(
    isPlaying: Boolean,
    audioFxManager: AudioFxManager? = null,
    mode: VisualizerMode = VisualizerMode.BARS,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(180.dp),
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary
) {
    val liveFftData by audioFxManager?.fftData?.collectAsState() ?: remember { mutableStateOf(FloatArray(0)) }

    val infiniteTransition = rememberInfiniteTransition(label = "fft")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val hasLiveFft = isPlaying && liveFftData.isNotEmpty() && liveFftData.any { it > 0.01f }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val centerX = width / 2f

        val barCount = 32
        val activeAmp = if (isPlaying) 1f else 0.08f

        // Get magnitude array for 32 bands
        val magnitudes = FloatArray(barCount) { i ->
            if (hasLiveFft) {
                liveFftData.getOrElse(i) { 0f } * activeAmp
            } else {
                if (!isPlaying) {
                    0.05f
                } else {
                    val norm = i.toFloat() / barCount
                    val bass = (sin(time * 8f + norm * 3f) + 1f) / 2f * 0.4f
                    val mid = (cos(time * 12f - norm * 7f) + 1f) / 2f * 0.35f
                    val treble = (sin(time * 20f + norm * 15f) + 1f) / 2f * 0.25f
                    (bass + mid + treble) * activeAmp
                }
            }
        }

        when (mode) {
            VisualizerMode.WAVEFORM -> {
                val path = Path()
                val points = barCount
                for (i in 0 until points) {
                    val x = (width / (points - 1)) * i
                    val mag = magnitudes[i]
                    val sign = if (i % 2 == 0) 1f else -1f
                    val y = centerY + (sign * mag * height * 0.45f)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor)),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
            VisualizerMode.BARS -> {
                val barWidth = (width / barCount) * 0.7f
                val gap = (width / barCount) * 0.3f
                for (i in 0 until barCount) {
                    val mag = magnitudes[i]
                    val barHeight = (mag * height * 0.85f).coerceAtLeast(6.dp.toPx())
                    val x = i * (barWidth + gap)
                    val y = height - barHeight
                    drawRoundRect(
                        brush = Brush.verticalGradient(listOf(primaryColor, secondaryColor)),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                }
            }
            VisualizerMode.SPECTRUM -> {
                val barWidth = width / barCount
                for (i in 0 until barCount) {
                    val mag = magnitudes[i]
                    val h = (mag * height * 0.8f).coerceAtLeast(8.dp.toPx())
                    val x = i * barWidth
                    drawRect(
                        brush = Brush.verticalGradient(listOf(secondaryColor, primaryColor)),
                        topLeft = Offset(x, centerY - h / 2f),
                        size = Size(barWidth * 0.8f, h)
                    )
                }
            }
            VisualizerMode.CIRCULAR -> {
                val radius = minOf(width, height) * 0.32f
                val rayCount = barCount
                for (i in 0 until rayCount) {
                    val angle = (2 * Math.PI / rayCount) * i
                    val mag = magnitudes[i]
                    val len = radius + (mag * radius * 0.8f)
                    val startX = centerX + radius * cos(angle).toFloat()
                    val startY = centerY + radius * sin(angle).toFloat()
                    val endX = centerX + len * cos(angle).toFloat()
                    val endY = centerY + len * sin(angle).toFloat()
                    drawLine(
                        brush = Brush.linearGradient(listOf(primaryColor, secondaryColor)),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }
    }
}
