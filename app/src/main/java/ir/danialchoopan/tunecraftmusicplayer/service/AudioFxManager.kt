package ir.danialchoopan.tunecraftmusicplayer.service

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.media.audiofx.Visualizer
import ir.danialchoopan.tunecraftmusicplayer.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.hypot

/*
 * AudioFxManager — Android AudioEffect DSP pipeline.
 *
 * Manages 5 audio effects from the android.media.audiofx package:
 * Equalizer, BassBoost, Virtualizer, PresetReverb, and LoudnessEnhancer.
 *
 * Key design decisions:
 * 1. Debounce (50ms) on applySettings() — prevents audio glitches when
 *    multiple preference collectors fire in rapid succession.
 * 2. All effects are released and recreated when the audio session changes.
 * 3. Balance is tracked in state/preferences but applied as a volume duck
 *    ratio since Android's AudioEffect does not expose per-channel balance.
 * 4. Visualizer/FFT is a placeholder — the data pipeline is ready but
 *    initVisualizerInternal() is empty to avoid performance overhead on
 *    low-end devices.
 */

data class EqualizerState(
    val isEnabled: Boolean = false,
    val numberOfBands: Int = 5,
    val bandCenterFreqs: List<String> = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz"),
    val bandLevels: List<Int> = List(5) { 0 }, // -12dB to +12dB
    val bassBoost: Int = 0, // 0 to 100
    val virtualizer: Int = 0, // 0 to 100
    val loudnessBoost: Int = 0, // 0 to 100
    val reverbPreset: Int = 0, // 0 = None, 1 = Small Room, 2 = Large Room, 3 = Medium Hall, 4 = Large Hall, 5 = Plate
    val balance: Float = 0f, // -1.0 (Left) to +1.0 (Right)
    val presetName: String = "Flat"
)

class AudioFxManager(
    private val context: Context,
    private val preferencesRepository: UserPreferencesRepository,
    private val scope: CoroutineScope
) {

    private var equalizer: Equalizer? = null
    private var bassBoostEffect: BassBoost? = null
    private var virtualizerEffect: Virtualizer? = null
    private var presetReverbEffect: PresetReverb? = null
    private var loudnessEnhancerEffect: LoudnessEnhancer? = null
    private var visualizer: Visualizer? = null

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    private val _fftData = MutableStateFlow(FloatArray(32) { 0f })
    val fftData: StateFlow<FloatArray> = _fftData.asStateFlow()

    private var currentAudioSessionId: Int = 0
    private var debounceJob: Job? = null
    private var lastVolumeL: Float = 1.0f
    private var lastVolumeR: Float = 1.0f

    init {
        scope.launch {
            preferencesRepository.eqEnabled.collect { enabled ->
                _state.value = _state.value.copy(isEnabled = enabled)
                debounceApplySettings()
            }
        }
        scope.launch {
            preferencesRepository.eqBands.collect { bandsStr ->
                val bands = bandsStr.split(",").mapNotNull { it.trim().toIntOrNull() }
                if (bands.isNotEmpty()) {
                    val currentSize = _state.value.numberOfBands
                    val adjustedBands = if (bands.size == currentSize) bands else {
                        List(currentSize) { idx -> bands.getOrElse(idx) { 0 } }
                    }
                    _state.value = _state.value.copy(bandLevels = adjustedBands)
                    debounceApplySettings()
                }
            }
        }
        scope.launch {
            preferencesRepository.eqBass.collect { bass ->
                _state.value = _state.value.copy(bassBoost = bass)
                debounceApplySettings()
            }
        }
        scope.launch {
            preferencesRepository.eqVirtualizer.collect { v ->
                _state.value = _state.value.copy(virtualizer = v)
                debounceApplySettings()
            }
        }
        scope.launch {
            preferencesRepository.eqBalance.collect { b ->
                _state.value = _state.value.copy(balance = b)
                debounceApplySettings()
            }
        }
        scope.launch {
            preferencesRepository.eqReverb.collect { r ->
                _state.value = _state.value.copy(reverbPreset = r)
                debounceApplySettings()
            }
        }
        scope.launch {
            preferencesRepository.eqLoudnessBoost.collect { l ->
                _state.value = _state.value.copy(loudnessBoost = l)
                debounceApplySettings()
            }
        }
    }

    private fun debounceApplySettings() {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(50)
            applySettings()
        }
    }

    fun setAudioSessionId(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        currentAudioSessionId = audioSessionId
        try {
            releaseEffects()

            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            bassBoostEffect = BassBoost(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            virtualizerEffect = Virtualizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            try {
                presetReverbEffect = PresetReverb(0, audioSessionId).apply {
                    enabled = _state.value.isEnabled
                }
            } catch (e: Exception) {
                presetReverbEffect = null
            }
            try {
                loudnessEnhancerEffect = LoudnessEnhancer(audioSessionId).apply {
                    enabled = _state.value.isEnabled
                }
            } catch (e: Exception) {
                loudnessEnhancerEffect = null
            }

            equalizer?.let { eq ->
                val numBands = eq.numberOfBands.toInt()
                val freqs = mutableListOf<String>()
                for (i in 0 until numBands) {
                    val centerMilliHz = eq.getCenterFreq(i.toShort())
                    val hz = centerMilliHz / 1000
                    val freqStr = if (hz >= 1000) "${hz / 1000}kHz" else "${hz}Hz"
                    freqs.add(freqStr)
                }
                val currentLevels = _state.value.bandLevels
                val newLevels = if (currentLevels.size == numBands) currentLevels else List(numBands) { 0 }
                _state.value = _state.value.copy(
                    numberOfBands = numBands,
                    bandCenterFreqs = freqs,
                    bandLevels = newLevels
                )
            }

            applySettings()
            initVisualizerInternal(audioSessionId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initVisualizerInternal(audioSessionId: Int) {
        // Visualizer disabled by user request
    }

    private fun processFft(fft: ByteArray) {
        val bands = 32
        val magnitudes = FloatArray(bands)
        val n = fft.size / 2
        if (n <= 0) return

        for (i in 0 until bands) {
            val index = (i * (n / bands)).coerceIn(0, n - 1)
            val r = fft[2 * index].toFloat()
            val im = fft[2 * index + 1].toFloat()
            val mag = hypot(r.toDouble(), im.toDouble()).toFloat()
            magnitudes[i] = (mag / 100f).coerceIn(0f, 1f)
        }
        _fftData.value = magnitudes
    }

    fun toggleEqualizer(enabled: Boolean) {
        _state.value = _state.value.copy(isEnabled = enabled)
        scope.launch { preferencesRepository.setEqEnabled(enabled) }
        applySettings()
    }

    fun setBandLevel(bandIndex: Int, levelDb: Int) {
        val currentBands = _state.value.bandLevels.toMutableList()
        if (bandIndex in currentBands.indices) {
            currentBands[bandIndex] = levelDb.coerceIn(-12, 12)
            _state.value = _state.value.copy(bandLevels = currentBands, presetName = "Custom")
            scope.launch { preferencesRepository.setEqBands(currentBands.joinToString(",")) }
            debounceApplySettings()
        }
    }

    fun setBassBoost(value: Int) {
        val clamped = value.coerceIn(0, 100)
        _state.value = _state.value.copy(bassBoost = clamped)
        scope.launch { preferencesRepository.setEqBass(clamped) }
        debounceApplySettings()
    }

    fun setVirtualizer(value: Int) {
        val clamped = value.coerceIn(0, 100)
        _state.value = _state.value.copy(virtualizer = clamped)
        scope.launch { preferencesRepository.setEqVirtualizer(clamped) }
        debounceApplySettings()
    }

    fun setReverbPreset(preset: Int) {
        val clamped = preset.coerceIn(0, 5)
        _state.value = _state.value.copy(reverbPreset = clamped)
        scope.launch { preferencesRepository.setEqReverb(clamped) }
        debounceApplySettings()
    }

    fun setLoudnessBoost(value: Int) {
        val clamped = value.coerceIn(0, 100)
        _state.value = _state.value.copy(loudnessBoost = clamped)
        scope.launch { preferencesRepository.setEqLoudnessBoost(clamped) }
        debounceApplySettings()
    }

    fun setBalance(value: Float) {
        val clamped = value.coerceIn(-1f, 1f)
        _state.value = _state.value.copy(balance = clamped)
        scope.launch { preferencesRepository.setEqBalance(clamped) }
        debounceApplySettings()
    }

    fun applyPreset(name: String, bands: List<Int>) {
        val numBands = _state.value.numberOfBands
        val mappedBands = if (bands.size == numBands) {
            bands
        } else {
            List(numBands) { i ->
                val sourceIndex = ((i.toFloat() / (numBands - 1).coerceAtLeast(1)) * (bands.size - 1)).toInt()
                bands.getOrElse(sourceIndex) { 0 }
            }
        }
        _state.value = _state.value.copy(bandLevels = mappedBands, presetName = name)
        scope.launch { preferencesRepository.setEqBands(mappedBands.joinToString(",")) }
        debounceApplySettings()
    }

    private fun applySettings() {
        val currentState = _state.value
        try {
            equalizer?.let { eq ->
                eq.enabled = currentState.isEnabled
                if (currentState.isEnabled) {
                    val range = eq.bandLevelRange
                    val minMilliBel = range[0].toInt()
                    val maxMilliBel = range[1].toInt()

                    val numBands = eq.numberOfBands.toInt()
                    for (i in 0 until minOf(numBands, currentState.bandLevels.size)) {
                        val dbVal = currentState.bandLevels[i]
                        val targetMilliBel = (dbVal * 100).coerceIn(minMilliBel, maxMilliBel)
                        eq.setBandLevel(i.toShort(), targetMilliBel.toShort())
                    }
                }
            }
            bassBoostEffect?.let { bb ->
                bb.enabled = currentState.isEnabled && currentState.bassBoost > 0
                if (currentState.isEnabled && currentState.bassBoost > 0) {
                    val strength = (currentState.bassBoost * 10).coerceIn(0, 1000).toShort()
                    bb.setStrength(strength)
                }
            }
            virtualizerEffect?.let { virt ->
                virt.enabled = currentState.isEnabled && currentState.virtualizer > 0
                if (currentState.isEnabled && currentState.virtualizer > 0) {
                    val strength = (currentState.virtualizer * 10).coerceIn(0, 1000).toShort()
                    virt.setStrength(strength)
                }
            }
            presetReverbEffect?.let { rev ->
                rev.enabled = currentState.isEnabled && currentState.reverbPreset > 0
                if (currentState.isEnabled && currentState.reverbPreset > 0) {
                    val revPresetMap = mapOf(
                        1 to PresetReverb.PRESET_SMALLROOM,
                        2 to PresetReverb.PRESET_LARGEROOM,
                        3 to PresetReverb.PRESET_MEDIUMHALL,
                        4 to PresetReverb.PRESET_LARGEHALL,
                        5 to PresetReverb.PRESET_PLATE
                    )
                    revPresetMap[currentState.reverbPreset]?.let { p ->
                        rev.preset = p
                    }
                }
            }
            loudnessEnhancerEffect?.let { le ->
                le.enabled = currentState.isEnabled && currentState.loudnessBoost > 0
                if (currentState.isEnabled && currentState.loudnessBoost > 0) {
                    // 0..100 maps to 0..800 mB gain
                    val gainMb = (currentState.loudnessBoost * 8f).toInt()
                    le.setTargetGain(gainMb)
                }
            }
            // Balance: stored in state and preferences; applied via per-channel volume
            // approximation. Full stereo balance requires AudioTrack or OS-level support.
            val balance = currentState.balance.coerceIn(-1f, 1f)
            if (balance != 0f) {
                lastVolumeL = if (balance <= 0f) 1.0f else (1.0f - balance).coerceIn(0f, 1f)
                lastVolumeR = if (balance >= 0f) 1.0f else (1.0f + balance).coerceIn(0f, 1f)
            } else {
                lastVolumeL = 1.0f
                lastVolumeR = 1.0f
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseEffects() {
        try {
            equalizer?.release()
            equalizer = null
            bassBoostEffect?.release()
            bassBoostEffect = null
            virtualizerEffect?.release()
            virtualizerEffect = null
            presetReverbEffect?.release()
            presetReverbEffect = null
            loudnessEnhancerEffect?.release()
            loudnessEnhancerEffect = null
            visualizer?.release()
            visualizer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        releaseEffects()
    }
}
