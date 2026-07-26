package ir.danialchoopan.tunecraftmusicplayer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ir.danialchoopan.tunecraftmusicplayer.data.local.dao.*
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.*

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
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
