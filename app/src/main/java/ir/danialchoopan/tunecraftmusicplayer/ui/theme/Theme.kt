package ir.danialchoopan.tunecraftmusicplayer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkVioletColorScheme = darkColorScheme(
    primary = DarkVioletPrimary,
    secondary = DarkVioletSecondary,
    background = DarkVioletBackground,
    surface = DarkVioletSurface,
    surfaceVariant = DarkVioletSurfaceVariant,
    onBackground = DarkVioletOnBackground,
    onSurface = DarkVioletOnBackground
)

private val SunsetColorScheme = darkColorScheme(
    primary = SunsetPrimary,
    secondary = SunsetSecondary,
    background = SunsetBackground,
    surface = SunsetSurface,
    surfaceVariant = SunsetSurfaceVariant,
    onBackground = SunsetOnBackground,
    onSurface = SunsetOnBackground
)

private val ForestColorScheme = darkColorScheme(
    primary = ForestPrimary,
    secondary = ForestSecondary,
    background = ForestBackground,
    surface = ForestSurface,
    surfaceVariant = ForestSurfaceVariant,
    onBackground = ForestOnBackground,
    onSurface = ForestOnBackground
)

private val OceanColorScheme = darkColorScheme(
    primary = OceanPrimary,
    secondary = OceanSecondary,
    background = OceanBackground,
    surface = OceanSurface,
    surfaceVariant = OceanSurfaceVariant,
    onBackground = OceanOnBackground,
    onSurface = OceanOnBackground
)

private val RoseColorScheme = darkColorScheme(
    primary = RosePrimary,
    secondary = RoseSecondary,
    background = RoseBackground,
    surface = RoseSurface,
    surfaceVariant = RoseSurfaceVariant,
    onBackground = RoseOnBackground,
    onSurface = RoseOnBackground
)

private val AmoledColorScheme = darkColorScheme(
    primary = AmoledPrimary,
    secondary = AmoledSecondary,
    background = AmoledBackground,
    surface = AmoledSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onBackground = AmoledOnBackground,
    onSurface = AmoledOnBackground
)

private val WarmPeachColorScheme = lightColorScheme(
    primary = WarmPeachPrimary,
    secondary = WarmPeachSecondary,
    background = WarmPeachBackground,
    surface = WarmPeachSurface,
    surfaceVariant = WarmPeachSurfaceVariant,
    onBackground = WarmPeachOnBackground,
    onSurface = WarmPeachOnBackground
)

private val NordicColorScheme = lightColorScheme(
    primary = NordicPrimary,
    secondary = NordicSecondary,
    background = NordicBackground,
    surface = NordicSurface,
    surfaceVariant = NordicSurfaceVariant,
    onBackground = NordicOnBackground,
    onSurface = NordicOnBackground
)

@Composable
fun TuneCraftTheme(
    themeMode: String = "DARK",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()

    val colorScheme = when (themeMode) {
        "SUNSET" -> SunsetColorScheme
        "FOREST" -> ForestColorScheme
        "OCEAN" -> OceanColorScheme
        "ROSE" -> RoseColorScheme
        "AMOLED" -> AmoledColorScheme
        "WARM_PEACH", "LIGHT" -> WarmPeachColorScheme
        "NORDIC" -> NordicColorScheme
        "SYSTEM" -> if (isSystemDark) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) dynamicDarkColorScheme(context) else DarkVioletColorScheme
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) dynamicLightColorScheme(context) else WarmPeachColorScheme
        }
        else -> DarkVioletColorScheme // "DARK", "DARK_VIOLET"
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

