package ir.danialchoopan.tunecraftmusicplayer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import ir.danialchoopan.tunecraftmusicplayer.data.local.dao.*
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.*

/*
 * TuneCraft Room Database — version 2.
 *
 * Entities are keyed by MediaStore audio IDs so re-scans produce upserts.
 * See MusicRepository.scanLocalMedia() for the full scan → insert → cleanup flow.
 *
 * Thread safety: uses the double-checked locking singleton pattern.
 * Schema changes: always add a Migration instead of using fallbackToDestructiveMigration()
 * to preserve user favorites, play history, and playlists across upgrades.
 */

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        PlaybackHistoryEntity::class,
        EqualizerPresetEntity::class,
        AudioBookmarkEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TuneCraftDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): HistoryDao
    abstract fun equalizerDao(): EqualizerDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: TuneCraftDatabase? = null

        fun getDatabase(context: Context): TuneCraftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TuneCraftDatabase::class.java,
                    "tunecraft_music.db"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }

        // v1 → v2: add savedPositionMs column for "resume from last position"
        private val MIGRATION_1_2 = Migration(1, 2) { database ->
            database.execSQL("ALTER TABLE songs ADD COLUMN savedPositionMs INTEGER NOT NULL DEFAULT 0")
        }
    }
}
