package ir.danialchoopan.tunecraftmusicplayer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/*
 * TuneCraft - Theme Engine
 *
 * Maps the 14 color schemes from Color.kt into Material 3 ColorSchemes.
 * Each scheme is registered with both dark and light variants where appropriate.
 *
 * A note on M3 color tokens:
 * - primary / onPrimary → main accent & text-on-accent
 * - primaryContainer / onPrimaryContainer → tinted surface for selected states
 * - secondary / onSecondary → secondary accent for less prominent UI
 * - secondaryContainer / onSecondaryContainer → tinted surface for secondary
 * - background / onBackground → root background & text on it
 * - surface / onSurface → card/sheet backgrounds & text on them
 * - surfaceVariant / onSurfaceVariant → subdued surface (e.g. search bars)
 * - surfaceContainer → M3 "elevated" surface tier (used for bottom bars)
 * - outline → dividers and borders
 */

// ─── Dark Schemes ─────────────────────────────────────────────────────────
private val DarkVioletColorScheme = darkColorScheme(
    primary = DarkVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkVioletSurfaceVariant,
    onPrimaryContainer = DarkVioletOnBackground,
    secondary = DarkVioletSecondary,
    onSecondary = Color.Black,
    secondaryContainer = DarkVioletSurfaceContainer,
    onSecondaryContainer = DarkVioletSecondary,
    tertiary = DarkVioletTertiary,
    background = DarkVioletBackground,
    onBackground = DarkVioletOnBackground,
    surface = DarkVioletSurface,
    onSurface = DarkVioletOnSurface,
    surfaceVariant = DarkVioletSurfaceVariant,
    onSurfaceVariant = DarkVioletOnSurface.copy(alpha = 0.75f),
    surfaceContainer = DarkVioletSurfaceContainer,
    outline = DarkVioletOnSurface.copy(alpha = 0.12f)
)

private val SunsetColorScheme = darkColorScheme(
    primary = SunsetPrimary,
    onPrimary = Color.Black,
    primaryContainer = SunsetSurfaceVariant,
    onPrimaryContainer = SunsetOnBackground,
    secondary = SunsetSecondary,
    onSecondary = Color.White,
    secondaryContainer = SunsetSurfaceContainer,
    onSecondaryContainer = SunsetSecondary,
    tertiary = SunsetTertiary,
    background = SunsetBackground,
    onBackground = SunsetOnBackground,
    surface = SunsetSurface,
    onSurface = SunsetOnSurface,
    surfaceVariant = SunsetSurfaceVariant,
    onSurfaceVariant = SunsetOnSurface.copy(alpha = 0.75f),
    surfaceContainer = SunsetSurfaceContainer,
    outline = SunsetOnSurface.copy(alpha = 0.12f)
)

private val ForestColorScheme = darkColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.Black,
    primaryContainer = ForestSurfaceVariant,
    onPrimaryContainer = ForestOnBackground,
    secondary = ForestSecondary,
    onSecondary = Color.Black,
    secondaryContainer = ForestSurfaceContainer,
    onSecondaryContainer = ForestSecondary,
    tertiary = ForestTertiary,
    background = ForestBackground,
    onBackground = ForestOnBackground,
    surface = ForestSurface,
    onSurface = ForestOnSurface,
    surfaceVariant = ForestSurfaceVariant,
    onSurfaceVariant = ForestOnSurface.copy(alpha = 0.75f),
    surfaceContainer = ForestSurfaceContainer,
    outline = ForestOnSurface.copy(alpha = 0.12f)
)

private val OceanColorScheme = darkColorScheme(
    primary = OceanPrimary,
    onPrimary = Color.White,
    primaryContainer = OceanSurfaceVariant,
    onPrimaryContainer = OceanOnBackground,
    secondary = OceanSecondary,
    onSecondary = Color.Black,
    secondaryContainer = OceanSurfaceContainer,
    onSecondaryContainer = OceanSecondary,
    tertiary = OceanTertiary,
    background = OceanBackground,
    onBackground = OceanOnBackground,
    surface = OceanSurface,
    onSurface = OceanOnSurface,
    surfaceVariant = OceanSurfaceVariant,
    onSurfaceVariant = OceanOnSurface.copy(alpha = 0.75f),
    surfaceContainer = OceanSurfaceContainer,
    outline = OceanOnSurface.copy(alpha = 0.12f)
)

private val RoseColorScheme = darkColorScheme(
    primary = RosePrimary,
    onPrimary = Color.White,
    primaryContainer = RoseSurfaceVariant,
    onPrimaryContainer = RoseOnBackground,
    secondary = RoseSecondary,
    onSecondary = Color.White,
    secondaryContainer = RoseSurfaceContainer,
    onSecondaryContainer = RoseSecondary,
    tertiary = RoseTertiary,
    background = RoseBackground,
    onBackground = RoseOnBackground,
    surface = RoseSurface,
    onSurface = RoseOnSurface,
    surfaceVariant = RoseSurfaceVariant,
    onSurfaceVariant = RoseOnSurface.copy(alpha = 0.75f),
    surfaceContainer = RoseSurfaceContainer,
    outline = RoseOnSurface.copy(alpha = 0.12f)
)

private val AmoledColorScheme = darkColorScheme(
    primary = AmoledPrimary,
    onPrimary = Color.Black,
    primaryContainer = AmoledSurfaceVariant,
    onPrimaryContainer = AmoledOnBackground,
    secondary = AmoledSecondary,
    onSecondary = Color.Black,
    secondaryContainer = AmoledSurfaceContainer,
    onSecondaryContainer = AmoledSecondary,
    tertiary = AmoledTertiary,
    background = AmoledBackground,
    onBackground = AmoledOnBackground,
    surface = AmoledSurface,
    onSurface = AmoledOnSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = AmoledOnSurface.copy(alpha = 0.7f),
    surfaceContainer = AmoledSurfaceContainer,
    outline = AmoledOnSurface.copy(alpha = 0.1f)
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = CyberpunkPrimary,
    onPrimary = Color.White,
    primaryContainer = CyberpunkSurfaceVariant,
    onPrimaryContainer = CyberpunkOnBackground,
    secondary = CyberpunkSecondary,
    onSecondary = Color.Black,
    secondaryContainer = CyberpunkSurfaceContainer,
    onSecondaryContainer = CyberpunkSecondary,
    tertiary = CyberpunkTertiary,
    background = CyberpunkBackground,
    onBackground = CyberpunkOnBackground,
    surface = CyberpunkSurface,
    onSurface = CyberpunkOnSurface,
    surfaceVariant = CyberpunkSurfaceVariant,
    onSurfaceVariant = CyberpunkOnSurface.copy(alpha = 0.75f),
    surfaceContainer = CyberpunkSurfaceContainer,
    outline = CyberpunkOnSurface.copy(alpha = 0.12f)
)

private val GoldColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = GoldSurfaceVariant,
    onPrimaryContainer = GoldOnBackground,
    secondary = GoldSecondary,
    onSecondary = Color.Black,
    secondaryContainer = GoldSurfaceContainer,
    onSecondaryContainer = GoldSecondary,
    tertiary = GoldTertiary,
    background = GoldBackground,
    onBackground = GoldOnBackground,
    surface = GoldSurface,
    onSurface = GoldOnSurface,
    surfaceVariant = GoldSurfaceVariant,
    onSurfaceVariant = GoldOnSurface.copy(alpha = 0.75f),
    surfaceContainer = GoldSurfaceContainer,
    outline = GoldOnSurface.copy(alpha = 0.12f)
)

private val LavaColorScheme = darkColorScheme(
    primary = LavaPrimary,
    onPrimary = Color.White,
    primaryContainer = LavaSurfaceVariant,
    onPrimaryContainer = LavaOnBackground,
    secondary = LavaSecondary,
    onSecondary = Color.Black,
    secondaryContainer = LavaSurfaceContainer,
    onSecondaryContainer = LavaSecondary,
    tertiary = LavaTertiary,
    background = LavaBackground,
    onBackground = LavaOnBackground,
    surface = LavaSurface,
    onSurface = LavaOnSurface,
    surfaceVariant = LavaSurfaceVariant,
    onSurfaceVariant = LavaOnSurface.copy(alpha = 0.75f),
    surfaceContainer = LavaSurfaceContainer,
    outline = LavaOnSurface.copy(alpha = 0.12f)
)

// ─── Light Schemes ────────────────────────────────────────────────────────
private val WarmPeachColorScheme = lightColorScheme(
    primary = WarmPeachPrimary,
    onPrimary = Color.White,
    primaryContainer = WarmPeachSurfaceVariant,
    onPrimaryContainer = WarmPeachOnBackground,
    secondary = WarmPeachSecondary,
    onSecondary = Color.White,
    secondaryContainer = WarmPeachSurfaceContainer,
    onSecondaryContainer = WarmPeachSecondary,
    tertiary = WarmPeachTertiary,
    background = WarmPeachBackground,
    onBackground = WarmPeachOnBackground,
    surface = WarmPeachSurface,
    onSurface = WarmPeachOnSurface,
    surfaceVariant = WarmPeachSurfaceVariant,
    onSurfaceVariant = WarmPeachOnSurface.copy(alpha = 0.7f),
    surfaceContainer = WarmPeachSurfaceContainer,
    outline = WarmPeachOnSurface.copy(alpha = 0.15f)
)

private val NordicColorScheme = lightColorScheme(
    primary = NordicPrimary,
    onPrimary = Color.White,
    primaryContainer = NordicSurfaceVariant,
    onPrimaryContainer = NordicOnBackground,
    secondary = NordicSecondary,
    onSecondary = Color.White,
    secondaryContainer = NordicSurfaceContainer,
    onSecondaryContainer = NordicSecondary,
    tertiary = NordicTertiary,
    background = NordicBackground,
    onBackground = NordicOnBackground,
    surface = NordicSurface,
    onSurface = NordicOnSurface,
    surfaceVariant = NordicSurfaceVariant,
    onSurfaceVariant = NordicOnSurface.copy(alpha = 0.7f),
    surfaceContainer = NordicSurfaceContainer,
    outline = NordicOnSurface.copy(alpha = 0.15f)
)

private val SakuraColorScheme = lightColorScheme(
    primary = SakuraPrimary,
    onPrimary = Color.White,
    primaryContainer = SakuraSurfaceVariant,
    onPrimaryContainer = SakuraOnBackground,
    secondary = SakuraSecondary,
    onSecondary = Color.White,
    secondaryContainer = SakuraSurfaceContainer,
    onSecondaryContainer = SakuraSecondary,
    tertiary = SakuraTertiary,
    background = SakuraBackground,
    onBackground = SakuraOnBackground,
    surface = SakuraSurface,
    onSurface = SakuraOnSurface,
    surfaceVariant = SakuraSurfaceVariant,
    onSurfaceVariant = SakuraOnSurface.copy(alpha = 0.7f),
    surfaceContainer = SakuraSurfaceContainer,
    outline = SakuraOnSurface.copy(alpha = 0.15f)
)

private val MintColorScheme = lightColorScheme(
    primary = MintPrimary,
    onPrimary = Color.White,
    primaryContainer = MintSurfaceVariant,
    onPrimaryContainer = MintOnBackground,
    secondary = MintSecondary,
    onSecondary = Color.White,
    secondaryContainer = MintSurfaceContainer,
    onSecondaryContainer = MintSecondary,
    tertiary = MintTertiary,
    background = MintBackground,
    onBackground = MintOnBackground,
    surface = MintSurface,
    onSurface = MintOnSurface,
    surfaceVariant = MintSurfaceVariant,
    onSurfaceVariant = MintOnSurface.copy(alpha = 0.7f),
    surfaceContainer = MintSurfaceContainer,
    outline = MintOnSurface.copy(alpha = 0.15f)
)

/*
 * TuneCraftTheme - Root composable wrapper.
 *
 * Selects the correct color scheme by themeMode string and wraps
 * content with MaterialTheme (also applies custom Typography).
 *
 * @param themeMode One of the supported theme keys:
 *   "FOREST", "SUNSET", "OCEAN", "ROSE", "AMOLED",
 *   "WARM_PEACH", "NORDIC", "DARK_VIOLET", "CYBERPUNK",
 *   "GOLD", "LAVA", "SAKURA", "MINT", "SYSTEM"
 */
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
            // Material You — dynamic colors from wallpaper (Android 12+ only)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else ForestColorScheme
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                androidx.compose.material3.dynamicLightColorScheme(context)
            } else WarmPeachColorScheme
        }
        else -> DarkVioletColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}