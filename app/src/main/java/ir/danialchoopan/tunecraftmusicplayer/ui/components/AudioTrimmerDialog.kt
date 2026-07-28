package ir.danialchoopan.tunecraftmusicplayer.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ir.danialchoopan.tunecraftmusicplayer.data.local.entity.SongEntity
import ir.danialchoopan.tunecraftmusicplayer.service.TuneCraftMediaService
import ir.danialchoopan.tunecraftmusicplayer.util.AudioTrimmerUtil
import kotlinx.coroutines.launch

/**
 * دایالوگ اختصاصی برش فایل صوتی (Audio Trimmer Dialog)
 *
 * امکان انتخاب بازه زمانی، پیش‌نمایش پخش و ساخت رینگتون/کلیپ صوتی بدون کرش.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioTrimmerDialog(
    song: SongEntity,
    isPersian: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // محاسبه دقیق مدت زمان آهنگ جهت جلوگیری از کرش RangeSlider در صورت صفر بودن duration
    val totalDurationMs = song.duration.coerceAtLeast(3000L)
    val maxRangeFloat = totalDurationMs.toFloat()
    
    var rangeValues by remember { mutableStateOf(0f..maxRangeFloat) }
    var fileName by remember { mutableStateOf("${song.title.take(20)}_clip") }
    var isTrimming by remember { mutableStateOf(false) }

    val startMs = rangeValues.start.toLong().coerceIn(0L, totalDurationMs)
    val endMs = rangeValues.endInclusive.toLong().coerceIn(startMs + 500L, totalDurationMs)

    fun formatMs(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%02d:%02d", min, sec)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian) "برش و ساخت رینگتون" else "Audio Trimmer / Ringtone",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // شاخص‌های زمان شروع و پایان
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = if (isPersian) "نقطه شروع" else "Start Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatMs(startMs),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isPersian) "نقطه پایان" else "End Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatMs(endMs),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // اسلایدر بازه زمانی ایمن
                RangeSlider(
                    value = rangeValues,
                    onValueChange = { newRange ->
                        val safeStart = newRange.start.coerceIn(0f, maxRangeFloat - 500f)
                        val safeEnd = newRange.endInclusive.coerceIn(safeStart + 500f, maxRangeFloat)
                        rangeValues = safeStart..safeEnd
                    },
                    valueRange = 0f..maxRangeFloat,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "${if (isPersian) "مدت زمان انتخاب شده:" else "Selected duration:"} ${formatMs((endMs - startMs).coerceAtLeast(0L))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text(if (isPersian) "نام فایل خروجی" else "Output File Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isTrimming) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isPersian) "در حال برش و ذخیره..." else "Trimming & saving...",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                TuneCraftMediaService.instance?.seekTo(startMs)
                            }
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPersian) "پیش‌نمایش" else "Preview")
                        }

                        Button(
                            onClick = {
                                isTrimming = true
                                coroutineScope.launch {
                                    val result = AudioTrimmerUtil.trimAudio(
                                        context = context,
                                        inputUriOrPath = song.path,
                                        startMs = startMs,
                                        endMs = endMs,
                                        outputFileName = fileName.ifBlank { "trimmed_audio" }
                                    )
                                    isTrimming = false
                                    result.fold(
                                        onSuccess = { file ->
                                            Toast.makeText(
                                                context,
                                                if (isPersian) "فایل با موفقیت ذخیره شد:\n${file.name}" else "File saved successfully:\n${file.name}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            onDismiss()
                                        },
                                        onFailure = { err ->
                                            Toast.makeText(
                                                context,
                                                if (isPersian) "خطا در برش فایل: ${err.localizedMessage}" else "Error trimming audio: ${err.localizedMessage}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentCut, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPersian) "ذخیره فایل" else "Trim & Save")
                        }
                    }
                }
            }
        }
    }
}

