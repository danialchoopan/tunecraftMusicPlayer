package ir.danialchoopan.tunecraftmusicplayer.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
