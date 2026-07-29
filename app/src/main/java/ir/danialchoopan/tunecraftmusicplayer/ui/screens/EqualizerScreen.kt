package ir.danialchoopan.tunecraftmusicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.danialchoopan.tunecraftmusicplayer.data.repository.MusicRepository
import ir.danialchoopan.tunecraftmusicplayer.service.AudioFxManager
import ir.danialchoopan.tunecraftmusicplayer.ui.components.EqualizerView
import kotlinx.coroutines.launch

@Composable
fun EqualizerScreen(
    audioFxManager: AudioFxManager?,
    musicRepository: MusicRepository,
    isPersian: Boolean = false,
    onBack: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val customPresets by musicRepository.equalizerPresets.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        EqualizerView(
            audioFxManager = audioFxManager,
            customPresets = customPresets,
            onSaveCustomPreset = { name, bandLevels, bassBoost, virtualizer, balance ->
                coroutineScope.launch {
                    musicRepository.saveEqualizerPreset(name, bandLevels, bassBoost, virtualizer, balance)
                }
            },
            onDeleteCustomPreset = { preset ->
                coroutineScope.launch {
                    musicRepository.deleteEqualizerPreset(preset)
                }
            },
            isPersian = isPersian
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
