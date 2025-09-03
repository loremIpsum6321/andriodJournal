package com.markrogers.journal.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.markrogers.journal.data.model.TodoItem
import com.markrogers.journal.data.repo.InMemoryRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen() {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Simple invalidation key so we can recompute todos after add/toggle.
    var refreshKey by remember { mutableStateOf(0) }
    val todos: List<TodoItem> = remember(selectedDate, refreshKey) {
        InMemoryRepository.todosOn(selectedDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month header / nav
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                        + " " + month.year,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { month = month.minusMonths(1) }) { Text("Prev") }
            TextButton(onClick = { month = month.plusMonths(1) }) { Text("Next") }
        }

        // Days of week header
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DayOfWeek.values().forEach { dow ->
                Text(
                    text = dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Calendar grid (7 columns)
        val days = remember(month) { daysForMonth(month) }
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            userScrollEnabled = false
        ) {
            items(days, key = { it.toString() }) { day ->
                val isInMonth = day.month == month.month
                val isSelected = day == selectedDate
                Surface(
                    tonalElevation = if (isSelected) 2.dp else 0.dp,
                    shape = MaterialTheme.shapes.small,
                    border = if (isSelected)
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else null,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable(enabled = isInMonth) { selectedDate = day }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (!isInMonth)
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.surface
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isInMonth)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Divider()

        // Selected day details
        Text(
            "Todos for $selectedDate",
            style = MaterialTheme.typography.titleMedium
        )

        var newTodo by remember { mutableStateOf("") }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTodo,
                onValueChange = { newTodo = it },
                label = { Text("Add a reminder / todo") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    val text = newTodo.trim()
                    if (text.isNotEmpty()) {
                        InMemoryRepository.addTodo(selectedDate, text)
                        newTodo = ""
                        refreshKey++ // refresh list
                    }
                }
            ) { Text("Add") }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (todos.isEmpty()) {
                Text(
                    "No todos for this day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                todos.forEach { t ->
                    ElevatedCard(
                        shape = RectangleShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = t.done,
                                onCheckedChange = {
                                    InMemoryRepository.toggleTodo(t.id)
                                    refreshKey++ // refresh list
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = t.text,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Build a 6-row grid (42 cells) including leading/trailing days to fill weeks. */
private fun daysForMonth(ym: YearMonth): List<LocalDate> {
    val first = ym.atDay(1)
    // Compose’s idea of week usually starts on Monday; align with ISO.
    val lead = (first.dayOfWeek.value % 7) // Monday=1…Sunday=7 → 0..6
    val start = first.minusDays(lead.toLong())
    return (0 until 42).map { start.plusDays(it.toLong()) }
}
