package com.markrogers.journal.ui.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.markrogers.journal.data.model.JournalEntry
import com.markrogers.journal.data.repo.InMemoryRepository
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Color definitions from MetricsScreen for consistency
private val colorX = Color(0xFF6EE7B7)
private val colorY = Color(0xFFF8D477)
private val colorZ = Color(0xFFFF6B6B)
private val colorW = Color(0xFF60A5FA)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onNewEntry: () -> Unit = {}
) {
    val entries by InMemoryRepository.entries.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewEntry,
                icon = { Icon(Icons.Filled.Add, contentDescription = "New") },
                text = { Text("New") }
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            items(entries, key = { it.id }) { e: JournalEntry ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart ||
                            value == SwipeToDismissBoxValue.StartToEnd
                        ) {
                            InMemoryRepository.deleteEntry(e.id)
                            scope.launch {
                                val result = snackbar.showSnackbar(
                                    message = "Entry deleted",
                                    actionLabel = "Undo",
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    InMemoryRepository.restoreEntry(e)
                                }
                            }
                            true
                        } else false
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = { /* optional red background */ }
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    TimelineRow(e, expanded) { expanded = !expanded }
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(
    e: JournalEntry,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(12.dp)) {
            // Title + date
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (e.title.isNotBlank()) e.title else "(untitled)",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val stamp = remember(e.createdAt) {
                    val dt = e.createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime()
                    dt.format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
                }
                Text(
                    text = stamp,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Mood emojis
            if (e.moodEmojis.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    e.moodEmojis.take(3).forEach { emoji ->
                        Text(emoji, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            // Body preview/detail view
            if (e.body.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                AnimatedVisibility(visible = isExpanded) {
                    Text(
                        text = e.body,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                AnimatedVisibility(visible = !isExpanded) {
                    Text(
                        text = e.body,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Toggles + sleep summary (now with colored dots)
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (e.toggleX) ColorDot(color = colorX)
                        if (e.toggleY) ColorDot(color = colorY)
                        if (e.toggleZ) ColorDot(color = colorZ)
                        if (e.toggleW) ColorDot(color = colorW)

                        if (e.toggleX || e.toggleY || e.toggleZ || e.toggleW) {
                            Spacer(Modifier.width(4.dp))
                        }

                        Text(
                            "Sleep: ${"%.1f".format(e.sleepHours)}h",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorDot(color: Color, size: Dp = 12.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(color, shape = CircleShape)
    )
}