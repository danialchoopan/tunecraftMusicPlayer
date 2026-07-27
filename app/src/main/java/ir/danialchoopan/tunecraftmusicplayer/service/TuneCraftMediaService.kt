package ir.danialchoopan.tunecraftmusicplayer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import ir.danialchoopan.tunecraftmusicplayer.MainActivity
import ir.danialchoopan.tunecraftmusicplayer.R
import ir.danialchoopan.tunecraftmusicplayer.TuneCraftApplication
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ShuffleType(val labelEn: String, val labelFa: String) {
    OFF("Shuffle Off", "شافل خاموش"),
    STANDARD("Standard Random", "شافل تصادفی"),
    SMART_ARTIST("Balanced Artists", "شافل متعادل خوانندگان"),
    FAVORITES_FIRST("Favorites Priority", "شافل اولویت علاقه مندی ها"),
    FRESH_TRACKS("Fresh Tracks Priority", "شافل اولویت آهنگ‌های جدید")
}

data class PlayerState(
    val currentSong: SongEntity? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleMode: Boolean = false,
    val shuffleType: ShuffleType = ShuffleType.OFF,
    val repeatMode: Int = Player.REPEAT_MODE_OFF, // OFF, ONE, ALL
    val playbackSpeed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val queue: List<SongEntity> = emptyList(),
    val currentIndex: Int = -1,
    val sleepTimerRemainingSeconds: Int = 0
)

class TuneCraftMediaService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var exoPlayer: ExoPlayer
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var audioFxManager: AudioFxManager? = null

    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                exoPlayer.pause()
            }
        }
    }

    private var sleepTimerJob: Job? = null
    private val progressHandler = Handler(Looper.getMainLooper())
    private var lastSaveTime = 0L
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            if (::exoPlayer.isInitialized) {
                val currentPos = exoPlayer.currentPosition
                val duration = exoPlayer.duration.coerceAtLeast(0L)
                _playerState.value = _playerState.value.copy(
                    currentPositionMs = currentPos,
                    durationMs = if (duration > 0) duration else _playerState.value.currentSong?.duration ?: 0L
                )

                val currentSong = _playerState.value.currentSong
                val now = System.currentTimeMillis()
                if (currentSong != null && currentPos > 2000L && (now - lastSaveTime > 3000L)) {
                    lastSaveTime = now
                    serviceScope.launch(Dispatchers.IO) {
                        TuneCraftApplication.instance.musicRepository.saveTrackPosition(currentSong.id, currentPos)
                    }
                }
            }
            progressHandler.postDelayed(this, 500)
        }
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "ir.danialchoopan.tunecraftmusicplayer.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "ir.danialchoopan.tunecraftmusicplayer.ACTION_NEXT"
        const val ACTION_PREVIOUS = "ir.danialchoopan.tunecraftmusicplayer.ACTION_PREVIOUS"

        private val _playerState = MutableStateFlow(PlayerState())
        val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

        var instance: TuneCraftMediaService? = null
            private set

        /**
         * Dynamically updates the isFavorite state of a song in the current PlayerState
         * and playback queue for instant UI response without waiting for database observers.
         */
        fun updateSongFavoriteStatus(songId: Long, isFavorite: Boolean) {
            val current = _playerState.value
            val updatedSong = if (current.currentSong?.id == songId) {
                current.currentSong.copy(isFavorite = isFavorite)
            } else current.currentSong

            val updatedQueue = current.queue.map { song ->
                if (song.id == songId) song.copy(isFavorite = isFavorite) else song
            }

            _playerState.value = current.copy(
                currentSong = updatedSong,
                queue = updatedQueue
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> playPause()
            ACTION_NEXT -> next()
            ACTION_PREVIOUS -> previous()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        createNotificationChannel()

        setMediaNotificationProvider(
            androidx.media3.session.DefaultMediaNotificationProvider.Builder(this)
                .setChannelId("media_playback_channel")
                .setNotificationId(1001)
                .build()
        )

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        exoPlayer = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(audioAttributes, true)
            setWakeMode(C.WAKE_MODE_LOCAL)
            setHandleAudioBecomingNoisy(true)
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
                    if (isPlaying) {
                        progressHandler.post(updateProgressRunnable)
                    } else {
                        progressHandler.removeCallbacks(updateProgressRunnable)
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val mediaId = mediaItem?.mediaId?.toLongOrNull()
                    if (mediaId != null) {
                        val currentQueue = _playerState.value.queue
                        val index = currentQueue.indexOfFirst { it.id == mediaId }
                        if (index != -1) {
                            val song = currentQueue[index]
                            _playerState.value = _playerState.value.copy(
                                currentSong = song,
                                currentIndex = index,
                                durationMs = song.duration
                            )
                            // Record playback history asynchronously
                            serviceScope.launch {
                                TuneCraftApplication.instance.musicRepository.recordPlayHistory(song.id, song.duration)
                            }
                        }
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        _playerState.value = _playerState.value.copy(isPlaying = false)
                    }
                }
            })
        }

        val sessionActivityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(pendingIntent)
            .build()

        audioFxManager = AudioFxManager(
            this,
            TuneCraftApplication.instance.preferencesRepository,
            serviceScope
        ).apply {
            setAudioSessionId(exoPlayer.audioSessionId)
        }

        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, filter)

        progressHandler.post(updateProgressRunnable)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    fun playSongs(songs: List<SongEntity>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        val targetIndex = startIndex.coerceIn(0, songs.size - 1)
        val targetSong = songs[targetIndex]

        val mediaItems = songs.map { song ->
            val artworkUri = song.albumArtUri?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
                ?: if (song.path.startsWith("content://") || song.path.startsWith("file://")) Uri.parse(song.path) else null

            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.path)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setAlbumArtist(song.artist)
                        .setGenre(song.genre)
                        .setArtworkUri(artworkUri)
                        .setIsPlayable(true)
                        .setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
                        .build()
                )
                .build()
        }

        _playerState.value = _playerState.value.copy(
            queue = songs,
            currentIndex = targetIndex,
            currentSong = targetSong
        )

        val startPositionMs = if (targetSong.savedPositionMs > 5000L && targetSong.savedPositionMs < targetSong.duration - 5000L) {
            targetSong.savedPositionMs
        } else {
            0L
        }

        exoPlayer.setMediaItems(mediaItems, targetIndex, startPositionMs)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun playPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun next() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        }
    }

    fun previous() {
        if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        } else {
            exoPlayer.seekTo(0L)
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _playerState.value = _playerState.value.copy(currentPositionMs = positionMs)
    }

    fun setShuffleMode(enabled: Boolean) {
        if (enabled) {
            cycleShuffleType()
        } else {
            exoPlayer.shuffleModeEnabled = false
            _playerState.value = _playerState.value.copy(shuffleMode = false, shuffleType = ShuffleType.OFF)
        }
    }

    fun cycleShuffleType() {
        val nextType = when (_playerState.value.shuffleType) {
            ShuffleType.OFF -> ShuffleType.STANDARD
            ShuffleType.STANDARD -> ShuffleType.SMART_ARTIST
            ShuffleType.SMART_ARTIST -> ShuffleType.FAVORITES_FIRST
            ShuffleType.FAVORITES_FIRST -> ShuffleType.FRESH_TRACKS
            ShuffleType.FRESH_TRACKS -> ShuffleType.OFF
        }
        applyShuffleType(nextType)
    }

    fun applyShuffleType(type: ShuffleType) {
        if (type == ShuffleType.OFF) {
            exoPlayer.shuffleModeEnabled = false
            _playerState.value = _playerState.value.copy(shuffleMode = false, shuffleType = ShuffleType.OFF)
            return
        }

        exoPlayer.shuffleModeEnabled = true
        _playerState.value = _playerState.value.copy(shuffleMode = true, shuffleType = type)
    }

    fun toggleRepeatMode() {
        val nextMode = when (_playerState.value.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        exoPlayer.repeatMode = nextMode
        _playerState.value = _playerState.value.copy(repeatMode = nextMode)
    }

    fun setPlaybackSpeedAndPitch(speed: Float, pitch: Float) {
        val params = PlaybackParameters(speed, pitch)
        exoPlayer.playbackParameters = params
        _playerState.value = _playerState.value.copy(playbackSpeed = speed, pitch = pitch)
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _playerState.value = _playerState.value.copy(sleepTimerRemainingSeconds = 0)
            return
        }
        var remaining = minutes * 60
        _playerState.value = _playerState.value.copy(sleepTimerRemainingSeconds = remaining)

        sleepTimerJob = serviceScope.launch {
            while (remaining > 0) {
                delay(1000)
                remaining--
                _playerState.value = _playerState.value.copy(sleepTimerRemainingSeconds = remaining)
            }
            exoPlayer.pause()
            _playerState.value = _playerState.value.copy(sleepTimerRemainingSeconds = 0)
        }
    }

    fun getAudioFxManager(): AudioFxManager? = audioFxManager

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "media_playback_channel",
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media controls and playback status"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        progressHandler.removeCallbacks(updateProgressRunnable)
        unregisterReceiver(becomingNoisyReceiver)
        audioFxManager?.release()
        exoPlayer.release()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceScope.cancel()
        instance = null
        super.onDestroy()
    }
}
