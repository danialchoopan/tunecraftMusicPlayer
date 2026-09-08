# ─── TuneCraft Music Player — ProGuard / R8 Rules ─────────────────────────
# This file is used for release builds (minifyEnabled = true).

# ─── Jetpack Compose ─────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ─── Room (KSP) ──────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**
-keepclassmembers class * {
    @androidx.room.* <fields>;
}

# ─── Moshi (JSON serialization) ──────────────────────────────────────────
-keep class ir.danialchoopan.tunecraftmusicplayer.data.local.entity.** { *; }
-keep class ir.danialchoopan.tunecraftmusicplayer.service.PlayerState { *; }
-keep class ir.danialchoopan.tunecraftmusicplayer.service.EqualizerState { *; }
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }

# ─── Coil (image loading) ────────────────────────────────────────────────
-dontwarn coil.**
-keep class coil.** { *; }

# ─── Media3 / ExoPlayer ──────────────────────────────────────────────────
-dontwarn androidx.media3.**
-keep class androidx.media3.** { *; }

# ─── Retrofit + OkHttp ───────────────────────────────────────────────────
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**

# ─── Firebase ────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ─── Kotlin Coroutines ───────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ─── Gson / DataStore / misc ─────────────────────────────────────────────
-keep class * implements android.os.Parcelable { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ─── Debug info (optional — keeps line numbers for crash reports) ────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile