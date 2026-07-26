package ir.danialchoopan.tunecraftmusicplayer

import android.app.Application
import ir.danialchoopan.tunecraftmusicplayer.data.local.TuneCraftDatabase
import ir.danialchoopan.tunecraftmusicplayer.data.preferences.UserPreferencesRepository
import ir.danialchoopan.tunecraftmusicplayer.data.repository.MusicRepository

class TuneCraftApplication : Application() {

    val database by lazy { TuneCraftDatabase.getDatabase(this) }
    val preferencesRepository by lazy { UserPreferencesRepository(this) }
    val musicRepository by lazy {
        MusicRepository(
            context = this,
            songDao = database.songDao(),
            playlistDao = database.playlistDao(),
            historyDao = database.historyDao(),
            equalizerDao = database.equalizerDao(),
            bookmarkDao = database.bookmarkDao(),
            userPreferencesRepository = preferencesRepository
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: TuneCraftApplication
            private set
    }
}
