package ir.danialchoopan.tunecraftmusicplayer.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import ir.danialchoopan.tunecraftmusicplayer.data.local.dao.*
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.*
import ir.danialchoopan.tunecraftmusicplayer.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.abs

class MusicRepository(
    private val context: Context,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val historyDao: HistoryDao,
    private val equalizerDao: EqualizerDao,
    private val bookmarkDao: BookmarkDao,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    val allSongs: Flow<List<SongEntity>> = songDao.getAllSongs()
    val favoriteSongs: Flow<List<SongEntity>> = songDao.getFavoriteSongs()
    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
    val playbackHistory: Flow<List<PlaybackHistoryEntity>> = historyDao.getAllHistory()
    val equalizerPresets: Flow<List<EqualizerPresetEntity>> = equalizerDao.getAllPresets()

    suspend fun saveEqualizerPreset(
        name: String,
        bandLevels: List<Int>,
        bassBoost: Int = 0,
        virtualizer: Int = 0,
        balance: Float = 0f
    ) {
        equalizerDao.insertPreset(
            EqualizerPresetEntity(
                name = name,
                bandLevels = bandLevels.joinToString(","),
                bassBoost = bassBoost,
                virtualizer = virtualizer,
                balance = balance
            )
        )
    }

    suspend fun deleteEqualizerPreset(preset: EqualizerPresetEntity) {
        equalizerDao.deletePreset(preset)
    }

    // Smart Playlists
    val recentlyAdded: Flow<List<SongEntity>> = songDao.getRecentlyAddedSongs()
    val mostPlayed: Flow<List<SongEntity>> = songDao.getMostPlayedSongs()
    val recentlyPlayed: Flow<List<SongEntity>> = songDao.getRecentlyPlayedSongs()
    val longestSongs: Flow<List<SongEntity>> = songDao.getLongestSongs()
    val shortestSongs: Flow<List<SongEntity>> = songDao.getShortestSongs()

    suspend fun scanLocalMedia() = withContext(Dispatchers.IO) {
        try {
            songDao.deleteSampleSongs()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val scannedSongs = mutableListOf<SongEntity>()
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.SIZE
        )

        val selection = "(${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.DURATION} >= 5000) AND ${MediaStore.Audio.Media.DURATION} >= 5000"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)?.takeIf { it.isNotBlank() } ?: "Unknown Track"
                    val artist = cursor.getString(artistColumn)?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn)?.takeIf { it.isNotBlank() } ?: "Unknown Album"
                    val duration = cursor.getLong(durationColumn)
                    val path = cursor.getString(dataColumn) ?: ""
                    val albumId = cursor.getLong(albumIdColumn)
                    val year = cursor.getInt(yearColumn)
                    val size = cursor.getLong(sizeColumn)

                    val artworkUri = if (albumId > 0) {
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()
                    } else {
                        ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        ).toString()
                    }

                    val folderName = try {
                        File(path).parentFile?.name ?: "Internal Storage"
                    } catch (e: Exception) {
                        "Internal Storage"
                    }

                    scannedSongs.add(
                        SongEntity(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = if (duration > 0) duration else 180000L,
                            path = path,
                            albumArtUri = artworkUri,
                            genre = "Audio",
                            year = if (year > 0) year else 2024,
                            folder = folderName,
                            bitrate = 320,
                            fileSize = size
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (scannedSongs.isNotEmpty()) {
            songDao.insertSongs(scannedSongs)
            val validIds = scannedSongs.map { it.id }
            songDao.deleteSongsNotIn(validIds)
        }
    }

    suspend fun toggleFavorite(songId: Long, isFavorite: Boolean) {
        songDao.updateFavorite(songId, isFavorite)
    }

    suspend fun resolveSongFromUri(uri: Uri): SongEntity = withContext(Dispatchers.IO) {
        val scheme = uri.scheme
        var title = "Audio Track"
        var artist = "Unknown Artist"
        var album = "External Audio"
        var duration = 0L
        var size = 0L

        if (scheme == "content" || scheme == "file") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val titleIdx = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                        if (titleIdx != -1) cursor.getString(titleIdx)?.takeIf { it.isNotBlank() }?.let { title = it }

                        val artistIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                        if (artistIdx != -1) cursor.getString(artistIdx)?.takeIf { it.isNotBlank() }?.let { artist = it }

                        val albumIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                        if (albumIdx != -1) cursor.getString(albumIdx)?.takeIf { it.isNotBlank() }?.let { album = it }

                        val durationIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                        if (durationIdx != -1) duration = cursor.getLong(durationIdx)

                        val sizeIdx = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                        if (sizeIdx != -1) size = cursor.getLong(sizeIdx)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (title == "Audio Track") {
            uri.lastPathSegment?.let { name ->
                val cleanName = name.substringAfterLast("/").substringAfterLast(":")
                if (cleanName.isNotBlank()) {
                    title = cleanName.substringBeforeLast(".")
                }
            }
        }

        if (artist == "Unknown Artist" || duration == 0L) {
            try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val extractedTitle = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE)
                val extractedArtist = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST)
                val extractedAlbum = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM)
                val extractedDur = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)

                if (!extractedTitle.isNullOrBlank()) title = extractedTitle
                if (!extractedArtist.isNullOrBlank()) artist = extractedArtist
                if (!extractedAlbum.isNullOrBlank()) album = extractedAlbum
                if (!extractedDur.isNullOrBlank()) duration = extractedDur.toLongOrNull() ?: duration
                retriever.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val externalId = abs(uri.toString().hashCode().toLong())
        val song = SongEntity(
            id = externalId,
            title = title,
            artist = artist,
            album = album,
            duration = if (duration > 0) duration else 180000L,
            path = uri.toString(),
            albumArtUri = uri.toString(),
            genre = "External",
            year = 2025,
            folder = "External",
            bitrate = 320,
            fileSize = size
        )

        try {
            songDao.insertSongs(listOf(song))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        song
    }

    suspend fun updateSongMetadata(song: SongEntity) {
        songDao.updateSong(song)
    }

    suspend fun recordPlayHistory(songId: Long, durationPlayedMs: Long) {
        songDao.recordPlay(songId)
        historyDao.insertHistory(
            PlaybackHistoryEntity(
                songId = songId,
                durationPlayedMs = durationPlayedMs
            )
        )
    }

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.clearPlaylist(playlistId)
        playlistDao.deletePlaylistById(playlistId)
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) {
        playlistDao.renamePlaylist(playlistId, newName)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long, orderIndex: Int = 0) {
        playlistDao.insertPlaylistSongCrossRef(
            PlaylistSongCrossRef(playlistId, songId, orderIndex)
        )
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<SongEntity>> {
        return playlistDao.getSongsForPlaylist(playlistId)
    }

    suspend fun updateLyrics(songId: Long, lrcContent: String) {
        songDao.updateLyrics(songId, lrcContent)
    }

    suspend fun saveTrackPosition(songId: Long, positionMs: Long) {
        songDao.updateSavedPosition(songId, positionMs)
    }

    suspend fun getSongById(songId: Long): SongEntity? {
        return songDao.getSongById(songId)
    }

    fun getBookmarksForSong(songId: Long): Flow<List<AudioBookmarkEntity>> {
        return bookmarkDao.getBookmarksForSong(songId)
    }

    suspend fun addBookmark(songId: Long, timestampMs: Long, note: String): Long {
        return bookmarkDao.insertBookmark(
            AudioBookmarkEntity(
                songId = songId,
                timestampMs = timestampMs,
                note = note
            )
        )
    }

    suspend fun deleteBookmark(bookmark: AudioBookmarkEntity) {
        bookmarkDao.deleteBookmark(bookmark)
    }

    // Export local library backup to JSON
    suspend fun exportLibraryBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val songsList = songDao.getAllSongsSync()
        val historyList = historyDao.getAllHistorySync()

        val songsArray = JSONArray()
        for (song in songsList) {
            val obj = JSONObject().apply {
                put("id", song.id)
                put("title", song.title)
                put("artist", song.artist)
                put("album", song.album)
                put("isFavorite", song.isFavorite)
                put("playCount", song.playCount)
                put("rating", song.rating.toDouble())
            }
            songsArray.put(obj)
        }
        root.put("songs", songsArray)

        val historyArray = JSONArray()
        for (h in historyList) {
            val obj = JSONObject().apply {
                put("songId", h.songId)
                put("timestamp", h.timestamp)
                put("durationPlayedMs", h.durationPlayedMs)
            }
            historyArray.put(obj)
        }
        root.put("history", historyArray)

        root.toString(2)
    }

    // Import library backup JSON
    suspend fun importLibraryBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (root.has("songs")) {
                val songsArray = root.getJSONArray("songs")
                for (i in 0 until songsArray.length()) {
                    val obj = songsArray.getJSONObject(i)
                    val id = obj.getLong("id")
                    val existing = songDao.getSongById(id)
                    if (existing != null) {
                        val updated = existing.copy(
                            isFavorite = obj.optBoolean("isFavorite", existing.isFavorite),
                            playCount = obj.optInt("playCount", existing.playCount),
                            rating = obj.optDouble("rating", existing.rating.toDouble()).toFloat()
                        )
                        songDao.updateSong(updated)
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
