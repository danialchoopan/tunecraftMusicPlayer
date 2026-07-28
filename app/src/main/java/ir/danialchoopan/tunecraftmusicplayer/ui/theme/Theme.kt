package ir.danialchoopan.tunecraftmusicplayer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkVioletColorScheme = darkColorScheme(
    primary = DarkVioletPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = DarkVioletSurfaceVariant,
    onPrimaryContainer = DarkVioletOnBackground,
    secondary = DarkVioletSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    secondaryContainer = DarkVioletSurface,
    onSecondaryContainer = DarkVioletOnBackground,
    background = DarkVioletBackground,
    surface = DarkVioletSurface,
    surfaceVariant = DarkVioletSurfaceVariant,
    onBackground = DarkVioletOnBackground,
    onSurface = DarkVioletOnBackground,
    onSurfaceVariant = DarkVioletOnBackground
)

private val SunsetColorScheme = darkColorScheme(
    primary = SunsetPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    primaryContainer = SunsetSurfaceVariant,
    onPrimaryContainer = SunsetOnBackground,
    secondary = SunsetSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = SunsetSurface,
    onSecondaryContainer = SunsetOnBackground,
    background = SunsetBackground,
    surface = SunsetSurface,
    surfaceVariant = SunsetSurfaceVariant,
    onBackground = SunsetOnBackground,
    onSurface = SunsetOnBackground,
    onSurfaceVariant = SunsetOnBackground
)

private val ForestColorScheme = darkColorScheme(
    primary = ForestPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    primaryContainer = ForestSurfaceVariant,
    onPrimaryContainer = ForestOnBackground,
    secondary = ForestSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    secondaryContainer = ForestSurface,
    onSecondaryContainer = ForestOnBackground,
    background = ForestBackground,
    surface = ForestSurface,
    surfaceVariant = ForestSurfaceVariant,
    onBackground = ForestOnBackground,
    onSurface = ForestOnBackground,
    onSurfaceVariant = ForestOnBackground
)

private val OceanColorScheme = darkColorScheme(
    primary = OceanPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = OceanSurfaceVariant,
    onPrimaryContainer = OceanOnBackground,
    secondary = OceanSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    secondaryContainer = OceanSurface,
    onSecondaryContainer = OceanOnBackground,
    background = OceanBackground,
    surface = OceanSurface,
    surfaceVariant = OceanSurfaceVariant,
    onBackground = OceanOnBackground,
    onSurface = OceanOnBackground,
    onSurfaceVariant = OceanOnBackground
)

private val RoseColorScheme = darkColorScheme(
    primary = RosePrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = RoseSurfaceVariant,
    onPrimaryContainer = RoseOnBackground,
    secondary = RoseSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = RoseSurface,
    onSecondaryContainer = RoseOnBackground,
    background = RoseBackground,
    surface = RoseSurface,
    surfaceVariant = RoseSurfaceVariant,
    onBackground = RoseOnBackground,
    onSurface = RoseOnBackground,
    onSurfaceVariant = RoseOnBackground
)

private val AmoledColorScheme = darkColorScheme(
    primary = AmoledPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    primaryContainer = AmoledSurfaceVariant,
    onPrimaryContainer = AmoledOnBackground,
    secondary = AmoledSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    secondaryContainer = AmoledSurface,
    onSecondaryContainer = AmoledOnBackground,
    background = AmoledBackground,
    surface = AmoledSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onBackground = AmoledOnBackground,
    onSurface = AmoledOnBackground,
    onSurfaceVariant = AmoledOnBackground
)

private val WarmPeachColorScheme = lightColorScheme(
    primary = WarmPeachPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = WarmPeachSurfaceVariant,
    onPrimaryContainer = WarmPeachOnBackground,
    secondary = WarmPeachSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    secondaryContainer = WarmPeachSurface,
    onSecondaryContainer = WarmPeachOnBackground,
    background = WarmPeachBackground,
    surface = WarmPeachSurface,
    surfaceVariant = WarmPeachSurfaceVariant,
    onBackground = WarmPeachOnBackground,
    onSurface = WarmPeachOnBackground,
    onSurfaceVariant = WarmPeachOnBackground
)

private val NordicColorScheme = lightColorScheme(
    primary = NordicPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = NordicSurfaceVariant,
    onPrimaryContainer = NordicOnBackground,
    secondary = NordicSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = NordicSurface,
    onSecondaryContainer = NordicOnBackground,
    background = NordicBackground,
    surface = NordicSurface,
    surfaceVariant = NordicSurfaceVariant,
    onBackground = NordicOnBackground,
    onSurface = NordicOnBackground,
    onSurfaceVariant = NordicOnBackground
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = CyberpunkPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = CyberpunkSurfaceVariant,
    onPrimaryContainer = CyberpunkOnBackground,
    secondary = CyberpunkSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    secondaryContainer = CyberpunkSurface,
    onSecondaryContainer = CyberpunkOnBackground,
    background = CyberpunkBackground,
    surface = CyberpunkSurface,
    surfaceVariant = CyberpunkSurfaceVariant,
    onBackground = CyberpunkOnBackground,
    onSurface = CyberpunkOnBackground,
    onSurfaceVariant = CyberpunkOnBackground
)

private val GoldColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    primaryContainer = GoldSurfaceVariant,
    onPrimaryContainer = GoldOnBackground,
    secondary = GoldSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    secondaryContainer = GoldSurface,
    onSecondaryContainer = GoldOnBackground,
    background = GoldBackground,
    surface = GoldSurface,
    surfaceVariant = GoldSurfaceVariant,
    onBackground = GoldOnBackground,
    onSurface = GoldOnBackground,
    onSurfaceVariant = GoldOnBackground
)

private val LavaColorScheme = darkColorScheme(
    primary = LavaPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = LavaSurfaceVariant,
    onPrimaryContainer = LavaOnBackground,
    secondary = LavaSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    secondaryContainer = LavaSurface,
    onSecondaryContainer = LavaOnBackground,
    background = LavaBackground,
    surface = LavaSurface,
    surfaceVariant = LavaSurfaceVariant,
    onBackground = LavaOnBackground,
    onSurface = LavaOnBackground,
    onSurfaceVariant = LavaOnBackground
)

private val SakuraColorScheme = lightColorScheme(
    primary = SakuraPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = SakuraSurfaceVariant,
    onPrimaryContainer = SakuraOnBackground,
    secondary = SakuraSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = SakuraSurface,
    onSecondaryContainer = SakuraOnBackground,
    background = SakuraBackground,
    surface = SakuraSurface,
    surfaceVariant = SakuraSurfaceVariant,
    onBackground = SakuraOnBackground,
    onSurface = SakuraOnBackground,
    onSurfaceVariant = SakuraOnBackground
)

private val MintColorScheme = lightColorScheme(
    primary = MintPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = MintSurfaceVariant,
    onPrimaryContainer = MintOnBackground,
    secondary = MintSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = MintSurface,
    onSecondaryContainer = MintOnBackground,
    background = MintBackground,
    surface = MintSurface,
    surfaceVariant = MintSurfaceVariant,
    onBackground = MintOnBackground,
    onSurface = MintOnBackground,
    onSurfaceVariant = MintOnBackground
)

@Composable
fun TuneCraftTheme(
    themeMode: String = "FOREST",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()

    val colorScheme = when (themeMode) {
        "FOREST", "EMERALD" -> ForestColorScheme
        "SUNSET" -> SunsetColorScheme
        "OCEAN" -> OceanColorScheme
        "ROSE" -> RoseColorScheme
        "AMOLED" -> AmoledColorScheme
        "WARM_PEACH", "LIGHT" -> WarmPeachColorScheme
        "NORDIC" -> NordicColorScheme
        "DARK_VIOLET", "CYBER" -> DarkVioletColorScheme
        "CYBERPUNK", "SYNTHWAVE" -> CyberpunkColorScheme
        "GOLD", "ROYAL_GOLD" -> GoldColorScheme
        "LAVA", "CRIMSON" -> LavaColorScheme
        "SAKURA", "LAVENDER" -> SakuraColorScheme
        "MINT", "SAGE" -> MintColorScheme
        "SYSTEM" -> if (isSystemDark) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) dynamicDarkColorScheme(context) else ForestColorScheme
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) dynamicLightColorScheme(context) else WarmPeachColorScheme
        }
        else -> ForestColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

