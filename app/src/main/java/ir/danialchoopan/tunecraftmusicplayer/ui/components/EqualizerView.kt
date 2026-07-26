package ir.danialchoopan.tunecraftmusicplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.danialchoopan.tunecraftmusicplayer.service.AudioFxManager
import ir.danialchoopan.tunecraftmusicplayer.service.EqualizerState

val EqualizerPresetsMap = mapOf(
    "Flat" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    "Rock" to listOf(4, 3, 2, 0, -1, 1, 3, 4, 4, 5),
    "Pop" to listOf(-1, 1, 3, 4, 3, 1, -1, -1, 2, 3),
    "Jazz" to listOf(3, 2, 0, 2, 3, 3, 2, 1, 2, 3),
    "Classical" to listOf(5, 4, 3, 2, -1, -1, 0, 2, 3, 4),
    "Heavy Metal" to listOf(5, 4, 2, 0, -1, 2, 4, 5, 4, 3),
    "Hip Hop" to listOf(5, 4, 3, 1, -1, -1, 1, 2, 3, 4),
    "Electronic" to listOf(4, 4, 2, 0, -2, 2, 1, 3, 4, 4),
    "Bass Boost" to listOf(6, 5, 4, 2, 0, 0, 0, 0, 0, 0)
)

@Composable
fun EqualizerView(
    audioFxManager: AudioFxManager?,
    isPersian: Boolean = false,
    modifier: Modifier = Modifier
) {
    val state by audioFxManager?.state?.collectAsState() ?: remember { mutableStateOf(EqualizerState()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isPersian) "اکولایزر حرفه‌ای" else "Audio Equalizer",
                style = MaterialTheme.typography.titleLarge
            )
            Switch(
                checked = state.isEnabled,
                onCheckedChange = { audioFxManager?.toggleEqualizer(it) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Presets List
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(EqualizerPresetsMap.keys.toList()) { presetName ->
                val isSelected = state.presetName == presetName
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        EqualizerPresetsMap[presetName]?.let { bands ->
                            audioFxManager?.applyPreset(presetName, bands)
                        }
                    },
                    label = { Text(presetName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Band Sliders
        Text(
            text = if (isPersian) "باندهای فرکانسی (dB)" else "Frequency Bands (dB)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            items(state.bandLevels.indices.toList()) { index ->
                val level = state.bandLevels[index]
                val freqLabel = state.bandCenterFreqs.getOrElse(index) { "B${index + 1}" }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        text = "${if (level > 0) "+$level" else "$level"}dB",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = level.toFloat(),
                        onValueChange = { newValue ->
                            audioFxManager?.setBandLevel(index, newValue.toInt())
                        },
                        valueRange = -12f..12f,
                        steps = 23,
                        enabled = state.isEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .width(120.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = freqLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bass Boost & Virtualizer & Balance
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Bass Boost
                Text(
                    text = "${if (isPersian) "افزایش بیس" else "Bass Boost"}: ${state.bassBoost}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = state.bassBoost.toFloat(),
                    onValueChange = { audioFxManager?.setBassBoost(it.toInt()) },
                    valueRange = 0f..100f,
                    enabled = state.isEnabled
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Virtualizer
                Text(
                    text = "${if (isPersian) "افکت سه بعدی (Virtualizer)" else "Virtualizer 3D"}: ${state.virtualizer}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = state.virtualizer.toFloat(),
                    onValueChange = { audioFxManager?.setVirtualizer(it.toInt()) },
                    valueRange = 0f..100f,
                    enabled = state.isEnabled
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Balance
                Text(
                    text = "${if (isPersian) "بالانس صدا (چپ / راست)" else "Audio Balance (L / R)"}: ${(state.balance * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = state.balance,
                    onValueChange = { audioFxManager?.setBalance(it) },
                    valueRange = -1f..1f
                )
            }
        }
    }
}
