package com.markrogers.journal.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.markrogers.journal.data.model.JournalEntry
import com.markrogers.journal.data.repo.InMemoryRepository
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Same palette as Metrics
private val colorX = Color(0xFF6EE7B7) // green
private val colorY = Color(0xFFF8D477) // amber
private val colorZ = Color(0xFFFF6B6B) // red
private val colorW = Color(0xFF60A5FA) // blue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    id: Long,
    onBack: () -> Unit
) {
    val entries by InMemoryRepository.entries.collectAsState()
    val entry = entries.firstOrNull { it.id == id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { pad ->
        if (entry == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentAlignment = Alignment.Center
            ) { Text("Entry not found") }
        } else {
            EntryDetailContent(
                entry = entry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun EntryDetailContent(entry: JournalEntry, modifier: Modifier = Modifier) {
    val fmt = DateTimeFormatter.ofPattern("MMM d, h:mm a")

    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Title (left) + mood emojis (right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (entry.title.isNotBlank()) entry.title else "(untitled)",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                entry.moodEmojis.take(3).forEach { e ->
                    Text(e, style = MaterialTheme.typography.titleLarge)
                }
            }
        }

        // Body
        if (entry.body.isNotBlank()) {
            Text(entry.body, style = MaterialTheme.typography.bodyLarge)
        } else {
            Text("(No body text)", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(8.dp))

        // Bottom stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                ToggleDot(entry.toggleX, colorX)
                ToggleDot(entry.toggleY, colorY)
                ToggleDot(entry.toggleZ, colorZ)
                ToggleDot(entry.toggleW, colorW)

                Text(
                    text = "Sleep: ${"%.1f".format(entry.sleepHours)}h",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = entry.createdAt.atZone(ZoneId.systemDefault()).format(fmt),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ToggleDot(checked: Boolean, color: Color) {
    val fill = if (checked) color else color.copy(alpha = 0.25f)
    Box(
        Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(fill)
    )
}
