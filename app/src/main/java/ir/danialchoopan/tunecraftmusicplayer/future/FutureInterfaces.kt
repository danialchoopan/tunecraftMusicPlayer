package ir.danialchoopan.tunecraftmusicplayer.future

/**
 * Disabled interfaces for future potential offline/local expansions.
 * Current version operates strictly 100% offline without network operations.
 */

@Suppress("UNUSED")
interface CloudSyncManager {
    suspend fun syncWithCloud(): Result<Unit> = Result.failure(IllegalStateException("Offline mode active. Cloud Sync is disabled."))
}

@Suppress("UNUSED")
interface AlbumArtDownloader {
    suspend fun downloadArtwork(songId: String): Result<String> = Result.failure(IllegalStateException("Offline mode active. Art Download is disabled."))
}

@Suppress("UNUSED")
interface ConcertReminderService {
    suspend fun checkUpcomingConcerts(): List<String> = emptyList()
}

@Suppress("UNUSED")
interface CollaborativePlaylistService {
    suspend fun syncCollaborativePlaylist(id: String): Result<Unit> = Result.failure(IllegalStateException("Offline mode active. Collaborative Playlists disabled."))
}
