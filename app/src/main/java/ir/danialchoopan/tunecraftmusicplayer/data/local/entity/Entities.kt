package ir.danialchoopan.tunecraftmusicplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long, // in ms
    val path: String,
    val albumArtUri: String? = null,
    val genre: String = "Unknown",
    val year: Int = 0,
    val folder: String = "",
    val bitrate: Int = 320, // kbps
    val fileSize: Long = 0L, // bytes
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val rating: Float = 0f,
    val dateAddedTimestamp: Long = System.currentTimeMillis(),
    val lrcContent: String? = null,
    val savedPositionMs: Long = 0L
)

@Entity(tableName = "audio_bookmarks")
data class AudioBookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Long,
    val timestampMs: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val orderIndex: Int
)

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val durationPlayedMs: Long = 0
)

@Entity(tableName = "equalizer_presets")
data class EqualizerPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bandLevels: String, // comma separated decibels
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val balance: Float = 0f
)
