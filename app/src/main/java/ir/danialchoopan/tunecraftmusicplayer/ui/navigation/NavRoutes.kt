package ir.danialchoopan.tunecraftmusicplayer.ui.navigation

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
}
