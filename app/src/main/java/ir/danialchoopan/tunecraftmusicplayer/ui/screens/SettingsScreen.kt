package ir.danialchoopan.tunecraftmusicplayer.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CarRental
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.danialchoopan.tunecraftmusicplayer.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.launch

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
    val gradientTheme by preferencesRepository.gradientTheme.collectAsState(initial = "OCEAN")
    val language by preferencesRepository.language.collectAsState(initial = "EN")
    val doubleTapSkip by preferencesRepository.doubleTapSkip.collectAsState(initial = true)
    val swipeSeek by preferencesRepository.swipeSeek.collectAsState(initial = true)
    val swipeVolume by preferencesRepository.swipeVolume.collectAsState(initial = true)
    val carMode by preferencesRepository.carMode.collectAsState(initial = false)
    val fontScale by preferencesRepository.fontScale.collectAsState(initial = 1.0f)
    val animationSpeed by preferencesRepository.animationSpeed.collectAsState(initial = 1.0f)
    val animationsEnabled by preferencesRepository.animationsEnabled.collectAsState(initial = true)
    val animationStyle by preferencesRepository.animationStyle.collectAsState(initial = "SPRING")
    val hapticsEnabled by preferencesRepository.hapticsEnabled.collectAsState(initial = true)
    val keepScreenOn by preferencesRepository.keepScreenOn.collectAsState(initial = false)
    val highContrast by preferencesRepository.highContrast.collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = if (isPersian) "تنظیمات برنامه" else "Settings",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )

        // Appearance Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian) "پوسته و تم‌های رنگی" else "Themes & Color Schemes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                data class ThemeOption(
                    val id: String,
                    val nameFa: String,
                    val nameEn: String,
                    val primaryColor: androidx.compose.ui.graphics.Color,
                    val bgColor: androidx.compose.ui.graphics.Color
                )

                val themes = listOf(
                    ThemeOption("DARK", "بنفش نئونی", "Dark Neon Violet", ir.danialchoopan.tunecraftmusicplayer.ui.theme.DarkVioletPrimary, ir.danialchoopan.tunecraftmusicplayer.ui.theme.DarkVioletBackground),
                    ThemeOption("SUNSET", "کهربایی و غروب", "Sunset Amber", ir.danialchoopan.tunecraftmusicplayer.ui.theme.SunsetPrimary, ir.danialchoopan.tunecraftmusicplayer.ui.theme.SunsetBackground),
                    ThemeOption("FOREST", "زمردی و جنگل", "Emerald Forest", ir.danialchoopan.tunecraftmusicplayer.ui.theme.ForestPrimary, ir.danialchoopan.tunecraftmusicplayer.ui.theme.ForestBackground),
                    ThemeOption("OCEAN", "آبی اقیانوسی", "Ocean Sapphire", ir.danialchoopan.tunecraftmusicplayer.ui.theme.OceanPrimary, ir.danialchoopan.tunecraftmusicplayer.ui.theme.OceanBackground),
                    ThemeOption("ROSE", "رز و شرابی", "Rose Velvet", ir.danialchoopan.tunecraftmusicplayer.ui.theme.RosePrimary, ir.danialchoopan.tunecraftmusicplayer.ui.theme.RoseBackground),
                    ThemeOption("AMOLED", "مشکی خالص AMOLED", "AMOLED Pitch Black", ir.danialchoopan.tunecraftmusicplayer.ui.theme.AmoledPrimary, ir.danialchoopan.tunecraftmusicplayer.ui.theme.AmoledBackground),
                    ThemeOption("WARM_PEACH", "هلویی گرم (روشن)", "Warm Peach & Cream", ir.danialchoopan.tunecraftmusicplayer.ui.theme.WarmPeachPrimary, ir.danialchoopan.tunecraftmusicplayer.ui.theme.WarmPeachBackground),
                    ThemeOption("NORDIC", "خاکستری نوردیک (روشن)", "Nordic Slate", ir.danialchoopan.tunecraftmusicplayer.ui.theme.NordicPrimary, ir.danialchoopan.tunecraftmusicplayer.ui.theme.NordicBackground),
                    ThemeOption("SYSTEM", "پویای سیستم (Material You)", "System Material You", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.background)
                )

                themes.forEach { theme ->
                    Surface(
                        onClick = { coroutineScope.launch { preferencesRepository.setThemeMode(theme.id) } },
                        shape = RoundedCornerShape(12.dp),
                        color = if (themeMode == theme.id) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Color preview circles
                                androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
                                    drawCircle(color = theme.bgColor)
                                    drawCircle(color = theme.primaryColor, radius = size.minDimension / 3f)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (isPersian) theme.nameFa else theme.nameEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (themeMode == theme.id) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            RadioButton(
                                selected = themeMode == theme.id,
                                onClick = { coroutineScope.launch { preferencesRepository.setThemeMode(theme.id) } }
                            )
                        }
                    }
                }
            }
        }

        // Language & Localization Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian) "زبان برنامه (Language)" else "App Language",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "English")
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
                    Text(text = "فارسی (Persian)")
                    RadioButton(
                        selected = language == "FA",
                        onClick = { coroutineScope.launch { preferencesRepository.setLanguage("FA") } }
                    )
                }
            }
        }

        // Gestures & Car Mode
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CarRental, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian) "ژست‌های حرکتی و حالت خودرو" else "Gestures & Driving Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (isPersian) "دوبار ضربه برای رد کردن" else "Double Tap Skip")
                    Switch(
                        checked = doubleTapSkip,
                        onCheckedChange = { coroutineScope.launch { preferencesRepository.setDoubleTapSkip(it) } }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (isPersian) "حالت خودرو (دکمه‌های بزرگ)" else "Car Mode UI")
                    Switch(
                        checked = carMode,
                        onCheckedChange = { coroutineScope.launch { preferencesRepository.setCarMode(it) } }
                    )
                }
            }
        }

        // Animation & Visual Styles Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Animation, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian) "انیمیشن‌ها و افکت‌های بصری" else "Animations & Motion Styles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPersian) "فعال بودن انیمیشن‌ها" else "Enable Animations",
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = animationsEnabled,
                        onCheckedChange = { coroutineScope.launch { preferencesRepository.setAnimationsEnabled(it) } }
                    )
                }

                if (animationsEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isPersian) "سبک و افکت انیمیشن:" else "Animation Motion Style:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val styles = listOf(
                        Triple("SPRING", "فنری و پویا", "Dynamic Spring"),
                        Triple("SLIDE", "کشویی و روان", "Smooth Slide"),
                        Triple("FADE", "محو شدن نرم", "Soft Fade"),
                        Triple("BOUNCE", "جهشی", "Bounce Accent"),
                        Triple("NONE", "خاموش / آنی", "Instant (No Motion)")
                    )

                    styles.forEach { (styleKey, nameFa, nameEn) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { coroutineScope.launch { preferencesRepository.setAnimationStyle(styleKey) } }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isPersian) nameFa else nameEn,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (animationStyle == styleKey) FontWeight.Bold else FontWeight.Normal
                            )
                            RadioButton(
                                selected = animationStyle == styleKey,
                                onClick = { coroutineScope.launch { preferencesRepository.setAnimationStyle(styleKey) } }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isPersian) "سرعت انیمیشن: ${String.format("%.1fx", animationSpeed)}" else "Animation Speed: ${String.format("%.1fx", animationSpeed)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
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

        // Accessibility & Persistent Preferences
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian) "دسترس‌پذیری و قابلیت‌ها" else "Accessibility & Options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isPersian) "اندازه فونت و متن: ${(fontScale * 100).toInt()}%" else "Font Scale: ${(fontScale * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = fontScale,
                    onValueChange = { coroutineScope.launch { preferencesRepository.setFontScale(it) } },
                    valueRange = 0.8f..1.4f,
                    steps = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (isPersian) "بازخورد لرزشی (Haptic)" else "Haptic Feedback")
                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = { coroutineScope.launch { preferencesRepository.setHapticsEnabled(it) } }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (isPersian) "روشن ماندن صفحه هنگام پخش" else "Keep Screen On")
                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = { coroutineScope.launch { preferencesRepository.setKeepScreenOn(it) } }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (isPersian) "کنتراست بالای متون" else "High Contrast Text")
                    Switch(
                        checked = highContrast,
                        onCheckedChange = { coroutineScope.launch { preferencesRepository.setHighContrast(it) } }
                    )
                }
            }
        }

        // Local Backup & Export Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Backup, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian) "پشتیبان‌گیری محلی (JSON)" else "Local Backup & Restore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onExportBackup,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isPersian) "خروجی گرفتن" else "Export JSON")
                    }
                    OutlinedButton(
                        onClick = onImportBackup,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isPersian) "بازیابی" else "Import JSON")
                    }
                }
            }
        }
    }
}
