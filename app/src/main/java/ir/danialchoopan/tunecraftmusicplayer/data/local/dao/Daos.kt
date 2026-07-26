package ir.danialchoopan.tunecraftmusicplayer.data.local.dao

import androidx.room.*
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY title ASC")
    suspend fun getAllSongsSync(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY dateAddedTimestamp DESC LIMIT :limit")
    fun getRecentlyAddedSongs(limit: Int = 30): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayedSongs(limit: Int = 30): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentlyPlayedSongs(limit: Int = 30): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY duration DESC LIMIT :limit")
    fun getLongestSongs(limit: Int = 30): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY duration ASC LIMIT :limit")
    fun getShortestSongs(limit: Int = 30): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavorite(songId: Long, isFavorite: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :songId")
    suspend fun recordPlay(songId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE songs SET lrcContent = :lrc WHERE id = :songId")
    suspend fun updateLyrics(songId: Long, lrc: String)

    @Query("UPDATE songs SET savedPositionMs = :positionMs WHERE id = :songId")
    suspend fun updateSavedPosition(songId: Long, positionMs: Long)

    @Query("DELETE FROM songs WHERE id NOT IN (:validIds)")
    suspend fun deleteSongsNotIn(validIds: List<Long>)

    @Query("DELETE FROM songs WHERE path LIKE 'sample://%'")
    suspend fun deleteSampleSongs()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM audio_bookmarks WHERE songId = :songId ORDER BY timestampMs ASC")
    fun getBookmarksForSong(songId: Long): Flow<List<AudioBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: AudioBookmarkEntity): Long

    @Delete
    suspend fun deleteBookmark(bookmark: AudioBookmarkEntity)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("SELECT s.* FROM songs s INNER JOIN playlist_songs ps ON s.id = ps.songId WHERE ps.playlistId = :playlistId ORDER BY ps.orderIndex ASC")
    fun getSongsForPlaylist(playlistId: Long): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)
}

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PlaybackHistoryEntity)

    @Query("SELECT * FROM playback_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history ORDER BY timestamp DESC")
    suspend fun getAllHistorySync(): List<PlaybackHistoryEntity>

    @Query("SELECT SUM(durationPlayedMs) FROM playback_history WHERE timestamp >= :startTime")
    suspend fun getTotalListeningTimeSince(startTime: Long): Long?

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()
}

@Dao
interface EqualizerDao {
    @Query("SELECT * FROM equalizer_presets ORDER BY name ASC")
    fun getAllPresets(): Flow<List<EqualizerPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EqualizerPresetEntity): Long

    @Delete
    suspend fun deletePreset(preset: EqualizerPresetEntity)
}
