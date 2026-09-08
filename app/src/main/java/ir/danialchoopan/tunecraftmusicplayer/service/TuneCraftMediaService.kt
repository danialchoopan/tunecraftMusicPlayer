package ir.danialchoopan.tunecraftmusicplayer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.core.graphics.ColorUtils
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import androidx.palette.graphics.Palette
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import ir.danialchoopan.tunecraftmusicplayer.MainActivity
import ir.danialchoopan.tunecraftmusicplayer.R
import ir.danialchoopan.tunecraftmusicplayer.TuneCraftApplication
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.io.File

/*
 * TuneCraftMediaService — Android Media3 MediaLibraryService.
 *
 * This is the heart of the app: it owns the ExoPlayer instance, manages the
 * playback queue, handles shuffle/repeat/sleep-timer, and communicates with
 * Android's system media panel (lock screen, quick settings, Wear OS, Android Auto).
 *
 * Key architecture notes:
 * 1. PlayerState is a single immutable data class exposed as a StateFlow.
 *    All UI reads it via collectAsStateWithLifecycle for consistency.
 * 2. Palette color extraction runs on IO and updates the ExoPlayer media item
 *    for rich notification artwork.
 * 3. The sleep timer is a coroutine-based countdown (not Handler) to avoid
 *    the drift inherent in postDelayed chaining.
 * 4. Smart shuffle types (SMART_ARTIST, FAVORITES_FIRST, FRESH_TRACKS) are
 *    placeholders — they all map to ExoPlayer's built-in shuffle. The queue
 *    reordering logic for "real" smart shuffle is unimplemented.
 * 5. The companion object holds _playerState as a singleton so the Activity
 *    can observe it without coupling to the service lifecycle.
 */

enum class ShuffleType(val labelEn: String, val labelFa: String) {
    OFF("Shuffle Off", "شافل خاموش"),
    STANDARD("Standard Random", "شافل تصادفی"),
    SMART_ARTIST("Balanced Artists", "شافل متعادل خوانندگان"),
    FAVORITES_FIRST("Favorites Priority", "شافل اولویت علاقه مندی ها"),
    FRESH_TRACKS("Fresh Tracks Priority", "شافل اولویت آهنگ‌های جدید")
}

data class PaletteColors(
    val dominantColor: Int = 0xFF121212.toInt(),
    val vibrantColor: Int = 0xFF1DB954.toInt(),
    val darkVibrantColor: Int = 0xFF0D5225.toInt(),
    val lightVibrantColor: Int = 0xFF1ED760.toInt(),
    val mutedColor: Int = 0xFF282828.toInt(),
    val onDominantColor: Int = 0xFFFFFFFF.toInt()
)

data class PlayerState(
    val currentSong: SongEntity? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleMode: Boolean = false,
    val shuffleType: ShuffleType = ShuffleType.OFF,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val playbackSpeed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val queue: List<SongEntity> = emptyList(),
    val currentIndex: Int = -1,
    val sleepTimerRemainingSeconds: Int = 0,
    val paletteColors: PaletteColors = PaletteColors()
)

class TuneCraftMediaService : MediaLibraryService() {

    private var mediaLibrarySession: MediaLibrarySession? = null
    private lateinit var exoPlayer: ExoPlayer
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var audioFxManager: AudioFxManager? = null

    // Smart Pause on disconnect receiver (wired + bluetooth)
    private val disconnectReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == AudioManager.ACTION_AUDIO_BECOMING_NOISY ||
                action == BluetoothDevice.ACTION_ACL_DISCONNECTED ||
                action == BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED
            ) {
                if (::exoPlayer.isInitialized && exoPlayer.isPlaying) {
                    exoPlayer.pause()
                }
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

    private val librarySessionCallback = object : MediaLibrarySession.Callback {

        /**
         * مدیریت اتصال کنترلرها (از جمله پنل مدیای سیستم اندروید ۱۰ به بعد، صفحه قفل و دستگاه‌های بلوتوثی)
         */
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val sessionCommands = connectionResult.availableSessionCommands.buildUpon().build()
            val playerCommands = connectionResult.availablePlayerCommands.buildUpon()
                .add(Player.COMMAND_PLAY_PAUSE)
                .add(Player.COMMAND_SEEK_TO_NEXT)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                .add(Player.COMMAND_STOP)
                .build()
            return MediaSession.ConnectionResult.accept(sessionCommands, playerCommands)
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootItem = MediaItem.Builder()
                .setMediaId("root")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("TuneCraft Music Library")
                        .setIsPlayable(false)
                        .setIsBrowsable(true)
                        .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            // استفاده مستقیم از صف موجود بدون مسدودسازی نخ اصلی (No runBlocking)
            val songs = _playerState.value.queue
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
            return Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.copyOf(mediaItems), params))
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val song = _playerState.value.queue.find { it.id.toString() == mediaId }
            if (song != null) {
                val artworkUri = song.albumArtUri?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
                val item = MediaItem.Builder()
                    .setMediaId(song.id.toString())
                    .setUri(song.path)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setAlbumTitle(song.album)
                            .setArtworkUri(artworkUri)
                            .setIsPlayable(true)
                            .build()
                    )
                    .build()
                return Futures.immediateFuture(LibraryResult.ofItem(item, null))
            }
            return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
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

        // ایجاد کانال نوتیفیکیشن کنترل رسانه
        createNotificationChannel()

        // تنظیم تامین‌کننده نوتیفیکیشن Media3 پیش‌فرض جهت پشتیبانی از MediaStyle، صفحه قفل و پنل مدیای اندروید
        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId("media_playback_channel")
            .setChannelName(R.string.app_name)
            .setNotificationId(1001)
            .build()

        setMediaNotificationProvider(notificationProvider)

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
                            extractPaletteForSong(song)
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

        mediaLibrarySession = MediaLibrarySession.Builder(this, exoPlayer, librarySessionCallback)
            .setSessionActivity(pendingIntent)
            .build()

        // ثبت جلسه‌ی رسانه در MediaSessionService جهت ایجاد خودکار نوتیفیکیشن MediaStyle و کنترل‌های صفحه قفل و Quick Settings
        addSession(mediaLibrarySession!!)

        audioFxManager = AudioFxManager(
            this,
            TuneCraftApplication.instance.preferencesRepository,
            serviceScope
        ).apply {
            setAudioSessionId(exoPlayer.audioSessionId)
        }

        val filter = IntentFilter().apply {
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        }
        registerReceiver(disconnectReceiver, filter)

        progressHandler.post(updateProgressRunnable)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // در صورت بستن کامل برنامه از Recent Apps، اگر اهنگ پخش نمی‌شود سرویس را می‌بندیم
        if (::exoPlayer.isInitialized && (!exoPlayer.playWhenReady || exoPlayer.mediaItemCount == 0 || exoPlayer.playbackState == Player.STATE_ENDED)) {
            stopSelf()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
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

        extractPaletteForSong(targetSong)

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

    /**
     * استخراج تصویر آلبوم و پالت رنگی آهنگ جاری و ارسال مستقیم تصویر به پنل مدیای سیستم اندروید (MediaStyle Notification & Quick Settings)
     */
    private fun extractPaletteForSong(song: SongEntity) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                var bitmap: Bitmap? = null
                var artBytes: ByteArray? = null

                if (!song.albumArtUri.isNullOrBlank()) {
                    try {
                        val uri = Uri.parse(song.albumArtUri)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(contentResolver, uri)
                            bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                                decoder.setTargetSize(400, 400)
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        }
                    } catch (e: Exception) {
                        // Fallback
                    }
                }

                if (bitmap == null && (song.path.startsWith("content://") || song.path.startsWith("file://") || File(song.path).exists())) {
                    try {
                        val retriever = MediaMetadataRetriever()
                        if (song.path.startsWith("content://")) {
                            retriever.setDataSource(this@TuneCraftMediaService, Uri.parse(song.path))
                        } else {
                            retriever.setDataSource(song.path)
                        }
                        artBytes = retriever.embeddedPicture
                        retriever.release()
                        if (artBytes != null) {
                            bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
                        }
                    } catch (e: Exception) {
                        // Fallback
                    }
                }

                if (bitmap != null) {
                    // تبدیل بیت‌مپ به بایت جهت ارسال به MediaSession در سیستم نوتیفیکیشن
                    if (artBytes == null) {
                        val stream = java.io.ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                        artBytes = stream.toByteArray()
                    }

                    // به‌روزرسانی متادیتای ExoPlayer جهت نمایش در نوتیفیکیشن MediaStyle و صفحه قفل اندروید ۱۰ به بعد
                    val currentArtBytes = artBytes
                    withContext(Dispatchers.Main) {
                        if (::exoPlayer.isInitialized) {
                            val currentItem = exoPlayer.currentMediaItem
                            if (currentItem != null && currentItem.mediaId == song.id.toString()) {
                                val updatedMetadata = currentItem.mediaMetadata.buildUpon()
                                    .setArtworkData(currentArtBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                                    .build()
                                val updatedItem = currentItem.buildUpon()
                                    .setMediaMetadata(updatedMetadata)
                                    .build()
                                val currentIndex = exoPlayer.currentMediaItemIndex
                                if (currentIndex != C.INDEX_UNSET) {
                                    exoPlayer.replaceMediaItem(currentIndex, updatedItem)
                                }
                            }
                        }
                    }

                    Palette.from(bitmap).generate { palette ->
                        if (palette != null) {
                            val dominant = palette.getDominantColor(0xFF121212.toInt())
                            val vibrant = palette.getVibrantColor(0xFF1DB954.toInt())
                            val darkVibrant = palette.getDarkVibrantColor(0xFF0D5225.toInt())
                            val lightVibrant = palette.getLightVibrantColor(0xFF1ED760.toInt())
                            val muted = palette.getMutedColor(0xFF282828.toInt())
                            val onDominant = if (ColorUtils.calculateLuminance(dominant) > 0.5) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()

                            val paletteColors = PaletteColors(
                                dominantColor = dominant,
                                vibrantColor = vibrant,
                                darkVibrantColor = darkVibrant,
                                lightVibrantColor = lightVibrant,
                                mutedColor = muted,
                                onDominantColor = onDominant
                            )
                            _playerState.value = _playerState.value.copy(paletteColors = paletteColors)
                        }
                    }
                } else {
                    _playerState.value = _playerState.value.copy(paletteColors = PaletteColors())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
        try {
            unregisterReceiver(disconnectReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioFxManager?.release()
        exoPlayer.release()
        mediaLibrarySession?.run {
            removeSession(this)
            player.release()
            release()
            mediaLibrarySession = null
        }
        serviceScope.cancel()
        instance = null
        super.onDestroy()
    }
}
