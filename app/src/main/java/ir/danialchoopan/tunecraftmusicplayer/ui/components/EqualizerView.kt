package ir.danialchoopan.tunecraftmusicplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.EqualizerPresetEntity
import ir.danialchoopan.tunecraftmusicplayer.service.AudioFxManager
import ir.danialchoopan.tunecraftmusicplayer.service.EqualizerState
import kotlin.math.roundToInt

val DefaultEqualizerPresetsMap = mapOf(
    "Flat" to listOf(0, 0, 0, 0, 0),
    "Bass Boost" to listOf(6, 4, 2, 0, 0),
    "Rock" to listOf(5, 3, -1, 3, 5),
    "Pop" to listOf(-1, 2, 4, 2, -1),
    "Jazz" to listOf(3, 2, 1, 2, 3),
    "Classical" to listOf(4, 3, -1, 2, 4),
    "Heavy Metal" to listOf(5, 4, 0, 4, 3),
    "Hip Hop" to listOf(5, 3, 0, 2, 4),
    "Electronic" to listOf(4, 2, -1, 3, 4),
    "Vocal Booster" to listOf(-2, 1, 4, 3, 0),
    "Acoustic" to listOf(3, 2, 1, 2, 2)
)

@Composable
fun EqualizerView(
    audioFxManager: AudioFxManager?,
    customPresets: List<EqualizerPresetEntity> = emptyList(),
    onSaveCustomPreset: ((name: String, bandLevels: List<Int>, bassBoost: Int, virtualizer: Int, balance: Float) -> Unit)? = null,
    onDeleteCustomPreset: ((preset: EqualizerPresetEntity) -> Unit)? = null,
    isPersian: Boolean = false,
    modifier: Modifier = Modifier
) {
    val state by audioFxManager?.state?.collectAsState() ?: remember { mutableStateOf(EqualizerState()) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var customPresetNameInput by remember { mutableStateOf("") }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header Row: Power Switch & Reset/Save Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (state.isEnabled) primaryColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = null,
                            tint = if (state.isEnabled) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPersian) "اکولایزر و افکت صوتی" else "Audio Equalizer & FX",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.isEnabled) (if (isPersian) "فعال" else "ON") else (if (isPersian) "غیرفعال" else "OFF"),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.isEnabled) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Reset Button
                    IconButton(
                        onClick = {
                            audioFxManager?.applyPreset("Flat", List(state.numberOfBands) { 0 })
                        },
                        enabled = state.isEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset EQ",
                            tint = if (state.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }

                    // Save Preset Button
                    if (onSaveCustomPreset != null) {
                        IconButton(
                            onClick = { showSaveDialog = true },
                            enabled = state.isEnabled
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "Save Preset",
                                tint = if (state.isEnabled) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Switch(
                        checked = state.isEnabled,
                        onCheckedChange = { audioFxManager?.toggleEqualizer(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Frequency Gain Response Curve Canvas
        FrequencyCurveCanvas(
            bandLevels = state.bandLevels,
            isEnabled = state.isEnabled,
            primaryColor = primaryColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Presets Chips Carousel
        Text(
            text = if (isPersian) "پیش‌فرض‌های اکولایزر" else "Equalizer Presets",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Built-in presets
            items(DefaultEqualizerPresetsMap.keys.toList()) { presetName ->
                val isSelected = state.presetName == presetName
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        DefaultEqualizerPresetsMap[presetName]?.let { bands ->
                            audioFxManager?.applyPreset(presetName, bands)
                        }
                    },
                    label = { Text(presetName) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    enabled = state.isEnabled
                )
            }

            // User Custom Saved Presets
            items(customPresets) { preset ->
                val isSelected = state.presetName == preset.name
                val bandList = preset.bandLevels.split(",").mapNotNull { it.trim().toIntOrNull() }
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (bandList.isNotEmpty()) {
                            audioFxManager?.applyPreset(preset.name, bandList)
                            audioFxManager?.setBassBoost(preset.bassBoost)
                            audioFxManager?.setVirtualizer(preset.virtualizer)
                            audioFxManager?.setBalance(preset.balance)
                        }
                    },
                    label = { Text("★ ${preset.name}") },
                    trailingIcon = {
                        if (onDeleteCustomPreset != null) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete Preset",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onDeleteCustomPreset(preset) }
                            )
                        }
                    },
                    enabled = state.isEnabled
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Vertical Frequency Band Sliders
        Text(
            text = if (isPersian) "تنظیم فرکانس‌ها (dB)" else "Frequency Bands (-12dB to +12dB)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(surfaceVariant)
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            val numBands = state.bandLevels.size
            for (index in 0 until numBands) {
                val level = state.bandLevels.getOrElse(index) { 0 }
                val freqLabel = state.bandCenterFreqs.getOrElse(index) { "B${index + 1}" }

                VerticalBandSlider(
                    levelDb = level,
                    freqLabel = freqLabel,
                    isEnabled = state.isEnabled,
                    primaryColor = primaryColor,
                    onLevelChange = { newLevel ->
                        audioFxManager?.setBandLevel(index, newLevel)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sound Enhancement FX (Bass Boost, Virtualizer 3D, Balance)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = primaryColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian) "بهبود‌دهنده‌های صوتی (DSP Effects)" else "Audio Enhancements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bass Boost Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speaker,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = primaryColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPersian) "افزایش بیس (Bass Boost)" else "Bass Boost",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${state.bassBoost}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                    Slider(
                        value = state.bassBoost.toFloat(),
                        onValueChange = { audioFxManager?.setBassBoost(it.roundToInt()) },
                        valueRange = 0f..100f,
                        enabled = state.isEnabled,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Virtualizer 3D Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SurroundSound,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = primaryColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPersian) "افکت سه بعدی (3D Spatializer)" else "3D Spatializer",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${state.virtualizer}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                    Slider(
                        value = state.virtualizer.toFloat(),
                        onValueChange = { audioFxManager?.setVirtualizer(it.roundToInt()) },
                        valueRange = 0f..100f,
                        enabled = state.isEnabled,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Audio Stereo Balance Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = primaryColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPersian) "بالانس هدفون (چپ / راست)" else "Stereo Balance (L / R)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = when {
                                state.balance < -0.05f -> "L ${((abs(state.balance)) * 100).toInt()}%"
                                state.balance > 0.05f -> "R ${((state.balance) * 100).toInt()}%"
                                else -> if (isPersian) "مرکز (50/50)" else "Center"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                    Slider(
                        value = state.balance,
                        onValueChange = { audioFxManager?.setBalance(it) },
                        valueRange = -1f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Save Preset Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(if (isPersian) "ذخیره پیش‌فرض جدید" else "Save Custom Preset")
            },
            text = {
                OutlinedTextField(
                    value = customPresetNameInput,
                    onValueChange = { customPresetNameInput = it },
                    label = { Text(if (isPersian) "نام پیش‌فرض" else "Preset Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customPresetNameInput.isNotBlank()) {
                            onSaveCustomPreset?.invoke(
                                customPresetNameInput.trim(),
                                state.bandLevels,
                                state.bassBoost,
                                state.virtualizer,
                                state.balance
                            )
                            showSaveDialog = false
                            customPresetNameInput = ""
                        }
                    }
                ) {
                    Text(if (isPersian) "ذخیره" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(if (isPersian) "انصراف" else "Cancel")
                }
            }
        )
    }
}

private fun abs(valF: Float): Float = if (valF < 0f) -valF else valF

/**
 * Custom Vertical Band Slider
 * Correctly renders vertical track, zero-center line, illuminated thumb, gain badge, and frequency label.
 */
@Composable
fun VerticalBandSlider(
    levelDb: Int,
    freqLabel: String,
    isEnabled: Boolean,
    primaryColor: Color,
    onLevelChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val levelText = if (levelDb > 0) "+${levelDb}dB" else "${levelDb}dB"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxHeight()
    ) {
        // Top Level Text Badge
        Text(
            text = levelText,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isEnabled) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Draggable Vertical Track
        Box(
            modifier = Modifier
                .weight(1f)
                .width(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                .pointerInput(isEnabled) {
                    if (!isEnabled) return@pointerInput
                    detectDragGestures { change, _ ->
                        change.consume()
                        val heightPx = size.height.toFloat()
                        val touchY = change.position.y.coerceIn(0f, heightPx)
                        // Touch top (y=0) = +12dB, touch bottom (y=heightPx) = -12dB
                        val norm = 1f - (touchY / heightPx)
                        val calculatedDb = (norm * 24f - 12f).roundToInt().coerceIn(-12, 12)
                        onLevelChange(calculatedDb)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Track background & fill
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val centerX = width / 2f
                val centerY = height / 2f

                // Draw center 0dB line indicator
                drawLine(
                    color = Color.Gray.copy(alpha = 0.4f),
                    start = Offset(4f, centerY),
                    end = Offset(width - 4f, centerY),
                    strokeWidth = 2.dp.toPx()
                )

                if (isEnabled) {
                    // Normalize levelDb from [-12, +12] to [1.0 (top), 0.0 (bottom)]
                    val levelNorm = (levelDb + 12f) / 24f
                    val thumbY = height * (1f - levelNorm)

                    // Draw vertical active bar from 0dB center to thumbY
                    val activeBarTop = minOf(centerY, thumbY)
                    val activeBarBottom = maxOf(centerY, thumbY)
                    val activeBarHeight = maxOf(4.dp.toPx(), activeBarBottom - activeBarTop)

                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(centerX - 3.dp.toPx(), activeBarTop),
                        size = androidx.compose.ui.geometry.Size(6.dp.toPx(), activeBarHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                    )

                    // Draw Glowing Thumb Circle
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.3f),
                        radius = 14.dp.toPx(),
                        center = Offset(centerX, thumbY)
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 9.dp.toPx(),
                        center = Offset(centerX, thumbY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(centerX, thumbY)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Frequency Text Label
        Text(
            text = freqLabel,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Frequency Gain Response Curve Canvas
 * Smooth Bezier curve showing current gain across frequency bands
 */
@Composable
fun FrequencyCurveCanvas(
    bandLevels: List<Int>,
    isEnabled: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        // Grid Lines: +12dB, 0dB, -12dB
        drawLine(
            color = Color.Gray.copy(alpha = 0.2f),
            start = Offset(0f, 10.dp.toPx()),
            end = Offset(width, 10.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color.Gray.copy(alpha = 0.4f),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
        drawLine(
            color = Color.Gray.copy(alpha = 0.2f),
            start = Offset(0f, height - 10.dp.toPx()),
            end = Offset(width, height - 10.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )

        if (bandLevels.isEmpty()) return@Canvas

        val points = mutableListOf<Offset>()
        val count = bandLevels.size
        for (i in 0 until count) {
            val x = if (count > 1) (i.toFloat() / (count - 1)) * (width - 40.dp.toPx()) + 20.dp.toPx() else width / 2f
            val db = if (isEnabled) bandLevels[i] else 0
            val norm = (db + 12f) / 24f
            val y = height * (1f - norm)
            points.add(Offset(x, y))
        }

        val strokePath = Path()
        val fillPath = Path()

        strokePath.moveTo(points.first().x, points.first().y)
        fillPath.moveTo(points.first().x, height)
        fillPath.lineTo(points.first().x, points.first().y)

        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val control1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
            val control2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)

            strokePath.cubicTo(control1.x, control1.y, control2.x, control2.y, p2.x, p2.y)
            fillPath.cubicTo(control1.x, control1.y, control2.x, control2.y, p2.x, p2.y)
        }

        fillPath.lineTo(points.last().x, height)
        fillPath.close()

        val activeColor = if (isEnabled) primaryColor else Color.Gray.copy(alpha = 0.5f)

        // Draw Fill Gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(activeColor.copy(alpha = 0.35f), activeColor.copy(alpha = 0.02f)),
                startY = 0f,
                endY = height
            )
        )

        // Draw Stroke Line
        drawPath(
            path = strokePath,
            color = activeColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Band Points
        for (point in points) {
            drawCircle(
                color = activeColor,
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }
}
