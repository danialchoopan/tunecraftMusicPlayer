package ir.danialchoopan.tunecraftmusicplayer.ui.navigation

/*
 * Navigation route definitions.
 *
 * Each Screen object carries:
 * - route: the NavHost route string
 * - titleEn / titleFa: localized labels for drawer items and bottom nav
 *
 * Main tabs (Home, Library, Playlists) use a HorizontalPager inside the
 * Home route. All other screens are standalone composable destinations
 * in the NavHost.
 */

sealed class Screen(val route: String, val titleEn: String, val titleFa: String) {
    object Home : Screen("home", "Home", "خانه")
    object Library : Screen("library", "Library", "کتابخانه")
    object Search : Screen("search", "Search", "جستجو")
    object Playlists : Screen("playlists", "Playlists", "لیست‌های پخش")
    object Statistics : Screen("statistics", "Statistics", "آمار و ارقام")
    object Settings : Screen("settings", "Settings", "تنظیمات")
    object Equalizer : Screen("equalizer", "Equalizer", "اکولایزر")
    object CarMode : Screen("carmode", "Car Mode", "حالت ماشین")
    object About : Screen("about", "About Us", "درباره ما")
    object FavoritesDetail : Screen("favorites_detail", "Favorites", "علاقه‌مندی‌ها")
    object AllSongsDetail : Screen("all_songs_detail", "All Songs", "همه آهنگ‌ها")
}
