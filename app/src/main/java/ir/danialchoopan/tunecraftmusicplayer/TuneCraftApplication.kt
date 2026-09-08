package ir.danialchoopan.tunecraftmusicplayer

import android.app.Application
import ir.danialchoopan.tunecraftmusicplayer.data.local.TuneCraftDatabase
import ir.danialchoopan.tunecraftmusicplayer.data.preferences.UserPreferencesRepository
import ir.danialchoopan.tunecraftmusicplayer.data.repository.MusicRepository

/*
 * TuneCraftApplication — Application-level singleton owner.
 *
 * Architecture note: This app uses a lightweight manual DI approach
 * (no Hilt/Koin) by lazy-initializing the database, preferences,
 * and repository here. All dependencies are lazily computed on first
 * access, so app startup is fast.
 *
 * If the project grows beyond ~15 dependencies, consider migrating
 * to Hilt for better testability and lifecycle-scoped injection.
 */

class TuneCraftApplication : Application() {

    // Lazy database instance — created on first access, not at app startup
    val database by lazy { TuneCraftDatabase.getDatabase(this) }

    // DataStore-backed user preferences store
    val preferencesRepository by lazy { UserPreferencesRepository(this) }

    // Central repository bridging local data sources and the UI/service layer
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
