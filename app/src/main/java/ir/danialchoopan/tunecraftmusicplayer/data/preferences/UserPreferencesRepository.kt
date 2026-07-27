package ir.danialchoopan.tunecraftmusicplayer.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "tunecraft_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK", "AMOLED", "GRADIENT"
        val GRADIENT_THEME = stringPreferencesKey("gradient_theme") // "OCEAN", "SUNSET", "FOREST", "NEON"
        val LANGUAGE = stringPreferencesKey("language") // "EN", "FA"
        val FONT_SCALE = floatPreferencesKey("font_scale") // 0.9f, 1.0f, 1.1f, 1.2f
        val ANIMATION_SPEED = floatPreferencesKey("animation_speed") // 0.5f, 1.0f, 1.5f
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val ANIMATION_STYLE = stringPreferencesKey("animation_style") // "SPRING", "SLIDE", "FADE", "BOUNCE", "NONE"
        val DOUBLE_TAP_SKIP = booleanPreferencesKey("double_tap_skip")
        val SWIPE_SEEK = booleanPreferencesKey("swipe_seek")
        val SWIPE_VOLUME = booleanPreferencesKey("swipe_volume")
        val CAR_MODE = booleanPreferencesKey("car_mode")
        val CROSSFADE_ENABLED = booleanPreferencesKey("crossfade_enabled")
        val CROSSFADE_SECONDS = intPreferencesKey("crossfade_seconds")
        val REPLAY_GAIN = booleanPreferencesKey("replay_gain")
        val SLEEP_TIMER_MINUTES = intPreferencesKey("sleep_timer_minutes")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
        val EQ_BANDS = stringPreferencesKey("eq_bands") // "0,0,0,0,0,0,0,0,0,0"
        val EQ_BASS = intPreferencesKey("eq_bass")
        val EQ_VIRTUALIZER = intPreferencesKey("eq_virtualizer")
        val EQ_BALANCE = floatPreferencesKey("eq_balance")
        val LAST_PLAYED_SONG_ID = longPreferencesKey("last_played_song_id")
        val LAST_PLAYED_POSITION = longPreferencesKey("last_played_position")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "FOREST" }
    val gradientTheme: Flow<String> = context.dataStore.data.map { it[GRADIENT_THEME] ?: "OCEAN" }
    val language: Flow<String> = context.dataStore.data.map { it[LANGUAGE] ?: "EN" }
    val fontScale: Flow<Float> = context.dataStore.data.map { it[FONT_SCALE] ?: 1.0f }
    val animationSpeed: Flow<Float> = context.dataStore.data.map { it[ANIMATION_SPEED] ?: 1.0f }
    val animationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[ANIMATIONS_ENABLED] ?: true }
    val animationStyle: Flow<String> = context.dataStore.data.map { it[ANIMATION_STYLE] ?: "SPRING" }
    val doubleTapSkip: Flow<Boolean> = context.dataStore.data.map { it[DOUBLE_TAP_SKIP] ?: true }
    val swipeSeek: Flow<Boolean> = context.dataStore.data.map { it[SWIPE_SEEK] ?: true }
    val swipeVolume: Flow<Boolean> = context.dataStore.data.map { it[SWIPE_VOLUME] ?: true }
    val carMode: Flow<Boolean> = context.dataStore.data.map { it[CAR_MODE] ?: false }
    val crossfadeEnabled: Flow<Boolean> = context.dataStore.data.map { it[CROSSFADE_ENABLED] ?: false }
    val crossfadeSeconds: Flow<Int> = context.dataStore.data.map { it[CROSSFADE_SECONDS] ?: 3 }
    val replayGain: Flow<Boolean> = context.dataStore.data.map { it[REPLAY_GAIN] ?: false }
    val sleepTimerMinutes: Flow<Int> = context.dataStore.data.map { it[SLEEP_TIMER_MINUTES] ?: 0 }
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { it[HAPTICS_ENABLED] ?: true }
    val keepScreenOn: Flow<Boolean> = context.dataStore.data.map { it[KEEP_SCREEN_ON] ?: false }
    val highContrast: Flow<Boolean> = context.dataStore.data.map { it[HIGH_CONTRAST] ?: false }

    val eqEnabled: Flow<Boolean> = context.dataStore.data.map { it[EQ_ENABLED] ?: false }
    val eqBands: Flow<String> = context.dataStore.data.map { it[EQ_BANDS] ?: "0,0,0,0,0,0,0,0,0,0" }
    val eqBass: Flow<Int> = context.dataStore.data.map { it[EQ_BASS] ?: 0 }
    val eqVirtualizer: Flow<Int> = context.dataStore.data.map { it[EQ_VIRTUALIZER] ?: 0 }
    val eqBalance: Flow<Float> = context.dataStore.data.map { it[EQ_BALANCE] ?: 0f }

    val lastSongId: Flow<Long> = context.dataStore.data.map { it[LAST_PLAYED_SONG_ID] ?: -1L }
    val lastPosition: Flow<Long> = context.dataStore.data.map { it[LAST_PLAYED_POSITION] ?: 0L }

    suspend fun setThemeMode(mode: String) { context.dataStore.edit { it[THEME_MODE] = mode } }
    suspend fun setGradientTheme(theme: String) { context.dataStore.edit { it[GRADIENT_THEME] = theme } }
    suspend fun setLanguage(lang: String) { context.dataStore.edit { it[LANGUAGE] = lang } }
    suspend fun setFontScale(scale: Float) { context.dataStore.edit { it[FONT_SCALE] = scale } }
    suspend fun setAnimationSpeed(speed: Float) { context.dataStore.edit { it[ANIMATION_SPEED] = speed } }
    suspend fun setAnimationsEnabled(enabled: Boolean) { context.dataStore.edit { it[ANIMATIONS_ENABLED] = enabled } }
    suspend fun setAnimationStyle(style: String) { context.dataStore.edit { it[ANIMATION_STYLE] = style } }
    suspend fun setDoubleTapSkip(enabled: Boolean) { context.dataStore.edit { it[DOUBLE_TAP_SKIP] = enabled } }
    suspend fun setSwipeSeek(enabled: Boolean) { context.dataStore.edit { it[SWIPE_SEEK] = enabled } }
    suspend fun setSwipeVolume(enabled: Boolean) { context.dataStore.edit { it[SWIPE_VOLUME] = enabled } }
    suspend fun setCarMode(enabled: Boolean) { context.dataStore.edit { it[CAR_MODE] = enabled } }
    suspend fun setCrossfadeEnabled(enabled: Boolean) { context.dataStore.edit { it[CROSSFADE_ENABLED] = enabled } }
    suspend fun setCrossfadeSeconds(seconds: Int) { context.dataStore.edit { it[CROSSFADE_SECONDS] = seconds } }
    suspend fun setReplayGain(enabled: Boolean) { context.dataStore.edit { it[REPLAY_GAIN] = enabled } }
    suspend fun setSleepTimerMinutes(minutes: Int) { context.dataStore.edit { it[SLEEP_TIMER_MINUTES] = minutes } }
    suspend fun setHapticsEnabled(enabled: Boolean) { context.dataStore.edit { it[HAPTICS_ENABLED] = enabled } }
    suspend fun setKeepScreenOn(enabled: Boolean) { context.dataStore.edit { it[KEEP_SCREEN_ON] = enabled } }
    suspend fun setHighContrast(enabled: Boolean) { context.dataStore.edit { it[HIGH_CONTRAST] = enabled } }

    suspend fun setEqEnabled(enabled: Boolean) { context.dataStore.edit { it[EQ_ENABLED] = enabled } }
    suspend fun setEqBands(bands: String) { context.dataStore.edit { it[EQ_BANDS] = bands } }
    suspend fun setEqBass(bass: Int) { context.dataStore.edit { it[EQ_BASS] = bass } }
    suspend fun setEqVirtualizer(v: Int) { context.dataStore.edit { it[EQ_VIRTUALIZER] = v } }
    suspend fun setEqBalance(b: Float) { context.dataStore.edit { it[EQ_BALANCE] = b } }

    suspend fun setLastSession(songId: Long, position: Long) {
        context.dataStore.edit {
            it[LAST_PLAYED_SONG_ID] = songId
            it[LAST_PLAYED_POSITION] = position
        }
    }
}
