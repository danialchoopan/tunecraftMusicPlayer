package ir.danialchoopan.tunecraftmusicplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
 * Room Entity classes representing the 6 local database tables.
 *
 * SongEntity is the core model with 18 fields — the composite primary key
 * matches MediaStore's audio ID so re-scans produce upserts via REPLACE.
 *
 * PlaylistSongCrossRef implements a many-to-many join table between
 * PlaylistEntity and SongEntity. Room does not support List<Long> directly,
 * so EqualizerPresetEntity stores bandLevels as a comma-separated string.
 */

/**
 * Represents a single audio track scanned from device storage or imported externally.
 * [id] maps to MediaStore.Audio.Media._ID for stable identity across scans.
 */
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

/**
 * Timestamped bookmark for a specific playback position within a song.
 */
@Entity(tableName = "audio_bookmarks")
data class AudioBookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Long,
    val timestampMs: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Named playlist containing songs through the PlaylistSongCrossRef join table.
 */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Join table for the many-to-many Playlist ↔ Song relationship.
 * Room composite primary key ensures each song appears once per playlist.
 */
@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val orderIndex: Int
)

/**
 * Records each completed playback event, used for statistics and recommendation.
 */
@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val durationPlayedMs: Long = 0
)

/**
 * User-saved equalizer presets. Band levels are stored as a comma-separated
 * dB string (e.g. "0,3,-2,5,1") since Room does not natively support List<Int>.
 */
@Entity(tableName = "equalizer_presets")
data class EqualizerPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bandLevels: String, // comma separated decibels
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val balance: Float = 0f
)
