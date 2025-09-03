package com.markrogers.journal.ui.editor

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.markrogers.journal.data.prefs.PreferencesRepository
import com.markrogers.journal.data.prefs.AppPrefs
import com.markrogers.journal.data.repo.InMemoryRepository
import kotlinx.coroutines.launch

/**
 * Full-screen editor for creating a new entry:
 * - Mood emoji quick picks (5 slots) – long-press a slot to customize (saved in DataStore).
 * - Title / Body
 * - Toggles: X / Y / Z
 * - Sleep hours slider
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { PreferencesRepository(LocalContext.current) }
    val prefs by prefsRepo.prefsFlow.collectAsState(initial = AppPrefs())

    // Quick-emoji slots from prefs
    var quick by remember(prefs.quickEmojis) { mutableStateOf(prefs.quickEmojis) }
    var mood by remember { mutableStateOf<String?>(null) }

    var title by remember { mutableStateOf(TextFieldValue("")) }
    var body by remember { mutableStateOf(TextFieldValue("")) }
    var x by remember { mutableStateOf(false) }
    var y by remember { mutableStateOf(false) }
    var z by remember { mutableStateOf(false) }
    var sleep by remember { mutableStateOf(7f) } // hours

    var showPickerFor by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        InMemoryRepository.addEntry(
                            title = title.text,
                            body = body.text,
                            moodEmoji = mood,
                            moodRating = moodRatingFromEmoji(mood),
                            toggleX = x,
                            toggleY = y,
                            toggleZ = z,
                            sleepHours = sleep
                        )
                        onBack()
                    }) { Text("Save") }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Mood", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                quick.forEachIndexed { idx, e ->
                    ElevatedButton(
                        onClick = { mood = e },
                        modifier = Modifier
                            .combinedClickable(
                                onClick = { mood = e },
                                onLongClick = { showPickerFor = idx }
                            )
                    ) { Text(if (e.isNotBlank()) e else "🙂") }
                }
                AssistChip(
                    onClick = { mood = null },
                    label = { Text(if (mood == null) "none" else "clear") }
                )
            }

            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = body, onValueChange = { body = it },
                label = { Text("Body") }, modifier = Modifier.fillMaxWidth(), minLines = 6
            )

            Text("Toggles", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("X"); Spacer(Modifier.width(8.dp)); Switch(checked = x, onCheckedChange = { x = it })
                Spacer(Modifier.width(18.dp))
                Text("Y"); Spacer(Modifier.width(8.dp)); Switch(checked = y, onCheckedChange = { y = it })
                Spacer(Modifier.width(18.dp))
                Text("Z"); Spacer(Modifier.width(8.dp)); Switch(checked = z, onCheckedChange = { z = it })
            }

            Text("Sleep hours: ${'$'}{sleep.toInt()}h")
            Slider(value = sleep, onValueChange = { sleep = it }, valueRange = 0f..14f, steps = 14)
        }
    }

    // Emoji picker dialog for replacing a quick slot
    if (showPickerFor != null) {
        AlertDialog(
            onDismissRequest = { showPickerFor = null },
            confirmButton = {},
            text = {
                Column {
                    Text("Pick emoji for slot ${'$'}{showPickerFor!! + 1}")
                    Spacer(Modifier.height(8.dp))
                    EmojiGrid { chosen ->
                        val idx = showPickerFor!!
                        val updated = quick.toMutableList().apply { this[idx] = chosen }
                        quick = updated
                        scope.launch { prefsRepo.setQuickEmoji(idx, chosen) }
                        showPickerFor = null
                    }
                }
            }
        )
    }
}

@Composable
private fun EmojiGrid(onPick: (String) -> Unit) {
    val choices = listOf(
        "😀","🙂","😐","🙁","😢","😴","🔥","💪","🧘","😵","🤒","🤩","😤","🥱","😎","🫠","🤯","💤","🍀","☕"
    )
    LazyVerticalGrid(columns = GridCells.Fixed(6), modifier = Modifier.height(220.dp)) {
        items(choices) { e ->
            TextButton(onClick = { onPick(e) }) { Text(e, style = MaterialTheme.typography.headlineSmall) }
        }
    }
}

private fun moodRatingFromEmoji(e: String?): Int? =
    when (e) {
        "😀","🤩" -> 5
        "🙂","😎" -> 4
        "😐"      -> 3
        "🙁","😤" -> 2
        "😢","😵" -> 1
        else -> null
    }
