package ir.danialchoopan.tunecraftmusicplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class LrcLine(val timestampMs: Long, val text: String)

fun parseLrc(lrcText: String?): List<LrcLine> {
    if (lrcText.isNullOrBlank()) return emptyList()
    val lines = mutableListOf<LrcLine>()
    val regex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")
    lrcText.lines().forEach { line ->
        val match = regex.find(line.trim())
        if (match != null) {
            val min = match.groupValues[1].toLongOrNull() ?: 0L
            val sec = match.groupValues[2].toLongOrNull() ?: 0L
            val msVal = match.groupValues[3].toLongOrNull() ?: 0L
            val ms = if (match.groupValues[3].length == 2) msVal * 10 else msVal
            val totalMs = min * 60000 + sec * 1000 + ms
            val text = match.groupValues[4].trim()
            if (text.isNotEmpty()) {
                lines.add(LrcLine(totalMs, text))
            }
        }
    }
    return lines.sortedBy { it.timestampMs }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()

@Composable
fun LyricsView(
    lrcContent: String?,
    currentPositionMs: Long,
    isPersian: Boolean = false,
    modifier: Modifier = Modifier
) {
    val lrcLines = remember(lrcContent) { parseLrc(lrcContent) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val currentLineIndex = remember(currentPositionMs, lrcLines) {
        if (lrcLines.isEmpty()) -1
        else {
            val index = lrcLines.indexOfLast { it.timestampMs <= currentPositionMs }
            if (index != -1) index else 0
        }
    }

    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex in lrcLines.indices) {
            coroutineScope.launch {
                listState.animateScrollToItem((currentLineIndex - 2).coerceAtLeast(0))
            }
        }
    }

    if (lrcLines.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isPersian) "متن ترانه در دسترس نیست." else "Lyrics not available.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(lrcLines) { index, line ->
                val isHighlighted = index == currentLineIndex
                Text(
                    text = line.text,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = if (isHighlighted) 22.sp else 16.sp,
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                    color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
