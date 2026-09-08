package ir.danialchoopan.tunecraftmusicplayer.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.danialchoopan.tunecraftmusicplayer.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.launch

/*
 * SettingsScreen — Organized settings with grouped sections.
 *
 * Sections: Themes, Language, Gestures & Driving, Animations,
 * Accessibility, Backup. Each section is a Card with custom header.
 * Reusable SettingsRow composable for consistent toggle/radio/clickable rows.
 */

@Composable
fun SettingsScreen(
    preferencesRepository: UserPreferencesRepository,
    isPersian: Boolean = false,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val themeMode by preferencesRepository.themeMode.collectAsState(initial = "DARK")
    val language by preferencesRepository.language.collectAsState(initial = "EN")
    val doubleTapSkip by preferencesRepository.doubleTapSkip.collectAsState(initial = true)
    val carMode by preferencesRepository.carMode.collectAsState(initial = false)
    val fontScale by preferencesRepository.fontScale.collectAsState(initial = 1.0f)
    val animationSpeed by preferencesRepository.animationSpeed.collectAsState(initial = 1.0f)
    val animationsEnabled by preferencesRepository.animationsEnabled.collectAsState(initial = true)
    val animationStyle by preferencesRepository.animationStyle.collectAsState(initial = "SPRING")
    val hapticsEnabled by preferencesRepository.hapticsEnabled.collectAsState(initial = true)
    val keepScreenOn by preferencesRepository.keepScreenOn.collectAsState(initial = false)
    val highContrast by preferencesRepository.highContrast.collectAsState(initial = false)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // ── Title ──────────────────────────────────────────────────────────
        item {
            Text(
                text = if (isPersian) "تنظیمات برنامه" else "Settings",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // ── Section: Themes ────────────────────────────────────────────────
        item {
            SettingsSection(
                title = if (isPersian) "پوسته و تم‌های رنگی" else "Themes & Color Schemes",
                icon = Icons.Default.Palette
            ) {
                ThemeSelector(
                    currentTheme = themeMode,
                    isPersian = isPersian,
                    onThemeSelected = { coroutineScope.launch { preferencesRepository.setThemeMode(it) } }
                )
            }
        }

        // ── Section: Language ──────────────────────────────────────────────
        item {
            SettingsSection(
                title = if (isPersian) "زبان برنامه" else "App Language",
                icon = Icons.Default.Language
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("English", style = MaterialTheme.typography.bodyLarge)
                    RadioButton(
                        selected = language == "EN",
                        onClick = { coroutineScope.launch { preferencesRepository.setLanguage("EN") } }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("فارسی (Persian)", style = MaterialTheme.typography.bodyLarge)
                    RadioButton(
                        selected = language == "FA",
                        onClick = { coroutineScope.launch { preferencesRepository.setLanguage("FA") } }
                    )
                }
            }
        }

        // ── Section: Gestures & Driving ────────────────────────────────────
        item {
            SettingsSection(
                title = if (isPersian) "ژست‌ها و حالت رانندگی" else "Gestures & Driving",
                icon = Icons.Default.Gesture
            ) {
                SettingsSwitchRow(
                    title = if (isPersian) "دوبار ضربه برای رد کردن" else "Double Tap Skip",
                    subtitle = if (isPersian) "دوبار ضربه روی لبه پخش‌کننده" else "Double-tap player edge to skip",
                    checked = doubleTapSkip,
                    onCheckedChange = { coroutineScope.launch { preferencesRepository.setDoubleTapSkip(it) } }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                SettingsSwitchRow(
                    title = if (isPersian) "حالت خودرو" else "Car Mode",
                    subtitle = if (isPersian) "نمایش دکمه‌های بزرگ برای رانندگی" else "Large buttons for driving",
                    checked = carMode,
                    onCheckedChange = { coroutineScope.launch { preferencesRepository.setCarMode(it) } }
                )
            }
        }

        // ── Section: Animations ────────────────────────────────────────────
        item {
            SettingsSection(
                title = if (isPersian) "انیمیشن‌ها" else "Animations",
                icon = Icons.Default.Animation
            ) {
                SettingsSwitchRow(
                    title = if (isPersian) "فعال بودن انیمیشن" else "Enable Animations",
                    checked = animationsEnabled,
                    onCheckedChange = { coroutineScope.launch { preferencesRepository.setAnimationsEnabled(it) } }
                )

                if (animationsEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isPersian) "سبک انیمیشن:" else "Animation Style:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    val styles = listOf(
                        Triple("SPRING", "فنری و پویا", "Dynamic Spring"),
                        Triple("SLIDE", "کشویی و روان", "Smooth Slide"),
                        Triple("FADE", "محو شدن نرم", "Soft Fade"),
                        Triple("BOUNCE", "جهشی", "Bounce Accent"),
                        Triple("NONE", "خاموش / آنی", "Instant (No Motion)")
                    )
                    styles.forEach { (key, fa, en) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { coroutineScope.launch { preferencesRepository.setAnimationStyle(key) } }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isPersian) fa else en,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (animationStyle == key) FontWeight.Bold else FontWeight.Normal
                            )
                            RadioButton(
                                selected = animationStyle == key,
                                onClick = { coroutineScope.launch { preferencesRepository.setAnimationStyle(key) } }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPersian) "سرعت:" else "Speed:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${String.format("%.1f", animationSpeed)}x",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = animationSpeed,
                        onValueChange = { coroutineScope.launch { preferencesRepository.setAnimationSpeed(it) } },
                        valueRange = 0.5f..2.0f,
                        steps = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ── Section: Accessibility ─────────────────────────────────────────
        item {
            SettingsSection(
                title = if (isPersian) "دسترس‌پذیری" else "Accessibility",
                icon = Icons.Default.Accessibility
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPersian) "اندازه فونت:" else "Font Size:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(fontScale * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = fontScale,
                        onValueChange = { coroutineScope.launch { preferencesRepository.setFontScale(it) } },
                        valueRange = 0.8f..1.4f,
                        steps = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                SettingsSwitchRow(
                    title = if (isPersian) "بازخورد لرزشی" else "Haptic Feedback",
                    checked = hapticsEnabled,
                    onCheckedChange = { coroutineScope.launch { preferencesRepository.setHapticsEnabled(it) } }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                SettingsSwitchRow(
                    title = if (isPersian) "روشن ماندن صفحه در پخش" else "Keep Screen On",
                    subtitle = if (isPersian) "صفحه هنگام پخش موزیک خاموش نشود" else "Stay awake during playback",
                    checked = keepScreenOn,
                    onCheckedChange = { coroutineScope.launch { preferencesRepository.setKeepScreenOn(it) } }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                SettingsSwitchRow(
                    title = if (isPersian) "کنتراست بالای متون" else "High Contrast Text",
                    checked = highContrast,
                    onCheckedChange = { coroutineScope.launch { preferencesRepository.setHighContrast(it) } }
                )
            }
        }

        // ── Section: Backup ────────────────────────────────────────────────
        item {
            SettingsSection(
                title = if (isPersian) "پشتیبان‌گیری" else "Backup & Restore",
                icon = Icons.Default.Backup
            ) {
                Text(
                    text = if (isPersian) "علاقه‌مندی‌ها، آمار و تنظیمات خود را ذخیره کنید" else "Save your favorites, play counts, and ratings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onExportBackup,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPersian) "خروجی" else "Export")
                    }
                    OutlinedButton(
                        onClick = onImportBackup,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPersian) "بازیابی" else "Import")
                    }
                }
            }
        }

        // Bottom spacer
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ─── Reusable Components ─────────────────────────────────────────────────────

/*
 * SettingsSection — Wraps content in a Material3 Card with header icon + title.
 */
@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/*
 * SettingsSwitchRow — A labeled row with a Switch component.
 * Optional subtitle for extra context.
 */
@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/*
 * ThemeSelector — 14 themes displayed in a grid with color swatches.
 * Each row shows the accent color + background color preview.
 */
@Composable
private fun ThemeSelector(
    currentTheme: String,
    isPersian: Boolean,
    onThemeSelected: (String) -> Unit
) {
    Column {
        Text(
            text = if (isPersian) "تم‌های تیره" else "Dark Themes",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        darkThemes.forEach { theme -> ThemeRow(theme, currentTheme, isPersian, onThemeSelected) }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isPersian) "تم‌های روشن" else "Light Themes",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        lightThemes.forEach { theme -> ThemeRow(theme, currentTheme, isPersian, onThemeSelected) }
    }
}

@Composable
private fun ThemeRow(
    theme: ThemeOption,
    currentTheme: String,
    isPersian: Boolean,
    onThemeSelected: (String) -> Unit
) {
    val isSelected = currentTheme == theme.id
    Surface(
        onClick = { onThemeSelected(theme.id) },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(theme.bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(theme.primaryColor)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isPersian) theme.nameFa else theme.nameEn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
            RadioButton(selected = isSelected, onClick = { onThemeSelected(theme.id) })
        }
    }
}

private data class ThemeOption(
    val id: String,
    val nameFa: String,
    val nameEn: String,
    val primaryColor: Color,
    val bgColor: Color
)

private val darkThemes = listOf(
    ThemeOption("DARK", "بنفش نئونی", "Dark Neon Violet", Color(0xFF0088FF), Color(0xFF0B132B)),
    ThemeOption("CYBERPUNK", "سایبرپانک", "Cyberpunk", Color(0xFFFF007F), Color(0xFF0D0624)),
    ThemeOption("GOLD", "طلایی سلطنتی", "Royal Gold", Color(0xFFFFD700), Color(0xFF0F0D0B)),
    ThemeOption("LAVA", "گدازه آتشین", "Crimson Lava", Color(0xFFFF3333), Color(0xFF140808)),
    ThemeOption("SUNSET", "کهربایی غروب", "Sunset Amber", Color(0xFFF59E0B), Color(0xFF181316)),
    ThemeOption("FOREST", "جنگل زمردی", "Emerald Forest", Color(0xFF10B981), Color(0xFF0A1B15)),
    ThemeOption("OCEAN", "اقیانوس آبی", "Ocean Sapphire", Color(0xFF3A86FF), Color(0xFF0B132B)),
    ThemeOption("ROSE", "رز مخملی", "Rose Velvet", Color(0xFFF43F5E), Color(0xFF1C0D18)),
    ThemeOption("AMOLED", "AMOLED مشکی", "AMOLED Black", Color(0xFF38BDF8), Color(0xFF000000))
)

private val lightThemes = listOf(
    ThemeOption("SAKURA", "شکوفه گیلاس", "Sakura Blossom", Color(0xFFE91E63), Color(0xFFFFF0F5)),
    ThemeOption("MINT", "نعناعی", "Mint Breeze", Color(0xFF059669), Color(0xFFF0FDF4)),
    ThemeOption("WARM_PEACH", "هلویی گرم", "Warm Peach", Color(0xFFFF6B4A), Color(0xFFFFF7F2)),
    ThemeOption("NORDIC", "شمالی خاکستری", "Nordic Slate", Color(0xFF0284C7), Color(0xFFF0F4F8)),
    ThemeOption("SYSTEM", "سیستم (Material You)", "System (Material You)", Color(0xFF6750A4), Color(0xFFFFFBFE))
)