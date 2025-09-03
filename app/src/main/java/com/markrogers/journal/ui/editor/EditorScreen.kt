package com.markrogers.journal.ui.editor



import androidx.compose.foundation.ExperimentalFoundationApi

import androidx.compose.foundation.border

import androidx.compose.foundation.clickable

import androidx.compose.foundation.combinedClickable

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.width

import androidx.compose.foundation.lazy.grid.GridCells

import androidx.compose.foundation.lazy.grid.LazyVerticalGrid

import androidx.compose.foundation.lazy.grid.items

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material3.AlertDialog

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.Icon

import androidx.compose.material3.IconButton

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Scaffold

import androidx.compose.material3.Slider

import androidx.compose.material3.Surface

import androidx.compose.material3.Switch

import androidx.compose.material3.SwitchDefaults

import androidx.compose.material3.Text

import androidx.compose.material3.TextButton

import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.Composable

import androidx.compose.runtime.collectAsState

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.shadow

import androidx.compose.ui.graphics.Shape

import androidx.compose.ui.graphics.SolidColor

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.input.TextFieldValue

import androidx.compose.ui.unit.dp

import com.markrogers.journal.data.prefs.AppPrefs

import com.markrogers.journal.data.prefs.PreferencesRepository

import com.markrogers.journal.data.repo.InMemoryRepository

import kotlinx.coroutines.launch



/**

 * Full-screen editor for creating a new entry:

 * - Mood emoji quick picks (5 slots) – long-press a slot to customize (saved in DataStore).

 * - Title / Body

 * - Toggles: X / Y / Z / W (colored switches, evenly spaced)

 * - Sleep hours slider

 * - Multiple mood emojis per entry (up to 3; 4th evicts the oldest)

 */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

@Composable

fun EditorScreen(

    onBack: () -> Unit

) {

    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val prefsRepo = remember(context) { PreferencesRepository(context) }

    val prefs by prefsRepo.prefsFlow.collectAsState(initial = AppPrefs())



// Quick-emoji slots (from DataStore)

    var quick by remember(prefs.quickEmojis) { mutableStateOf(prefs.quickEmojis) }

// Multi-select mood (up to 3; oldest evicted)

    var moods by remember { mutableStateOf<List<String>>(emptyList()) }



    var title by remember { mutableStateOf(TextFieldValue("")) }

    var body by remember { mutableStateOf(TextFieldValue("")) }

    var x by remember { mutableStateOf(false) }

    var y by remember { mutableStateOf(false) }

    var z by remember { mutableStateOf(false) }

    var w by remember { mutableStateOf(false) } // fourth toggle

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

                            moodEmojis = moods,

                            moodRating = moodRatingFromEmojis(moods),

                            toggleX = x,

                            toggleY = y,

                            toggleZ = z,

                            toggleW = w,

                            sleepHours = sleep

                        )

                        onBack()

                    }) { Text("Save") }

                }

            )

        }

    ) { pad ->

        Column(

            modifier = Modifier

                .fillMaxSize()

                .padding(pad)

                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(14.dp)

        ) {

            Text("Mood", style = MaterialTheme.typography.titleMedium)



            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),

                verticalAlignment = Alignment.CenterVertically

            ) {

                quick.forEachIndexed { idx, e ->

                    QuickEmojiChip(

                        emoji = if (e.isNotBlank()) e else "🙂",

                        selected = moods.contains(e),

                        onClick = { moods = toggleEmoji(moods, e, limit = 3) },

                        onLongPress = { showPickerFor = idx }

                    )

// Visual grouping: add a little extra gap after the 3rd slot

                    if (idx == 2) Spacer(Modifier.width(8.dp))

                }

                ClearChip(

                    enabled = moods.isNotEmpty(),

                    onClear = { moods = emptyList() }

                )

            }



            OutlinedTextField(

                value = title,

                onValueChange = { title = it },

                label = { Text("Title") },

                modifier = Modifier.fillMaxWidth(),

                singleLine = true

            )



            OutlinedTextField(

                value = body,

                onValueChange = { body = it },

                label = { Text("Body") },

                modifier = Modifier.fillMaxWidth(),

                minLines = 6

            )



            Text("Toggles", style = MaterialTheme.typography.titleMedium)

            Row(

                modifier = Modifier.fillMaxWidth(),

                verticalAlignment = Alignment.CenterVertically,

                horizontalArrangement = Arrangement.SpaceEvenly

            ) {

// X = primary (green in metrics)

                Switch(

                    checked = x, onCheckedChange = { x = it },

                    colors = SwitchDefaults.colors(

                        checkedThumbColor = MaterialTheme.colorScheme.primary,

                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

                    )

                )

// Y = tertiary (amber in metrics)

                Switch(

                    checked = y, onCheckedChange = { y = it },

                    colors = SwitchDefaults.colors(

                        checkedThumbColor = MaterialTheme.colorScheme.tertiary,

                        checkedTrackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)

                    )

                )

// Z = error (red in metrics)

                Switch(

                    checked = z, onCheckedChange = { z = it },

                    colors = SwitchDefaults.colors(

                        checkedThumbColor = MaterialTheme.colorScheme.error,

                        checkedTrackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)

                    )

                )

// W = secondary (indigo in metrics)

                Switch(

                    checked = w, onCheckedChange = { w = it },

                    colors = SwitchDefaults.colors(

                        checkedThumbColor = MaterialTheme.colorScheme.secondary,

                        checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)

                    )

                )

            }



            Text("Sleep hours: ${sleep.toInt()}h")

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

                    Text("Pick emoji for slot ${showPickerFor!! + 1}")

                    LazyVerticalGrid(columns = GridCells.Fixed(6), modifier = Modifier.height(220.dp)) {

                        items(emojiChoices) { e ->

                            TextButton(onClick = {

                                val idx = showPickerFor!!

                                val updated = quick.toMutableList().apply { this[idx] = e }

                                quick = updated

                                scope.launch { prefsRepo.setQuickEmoji(idx, e) }

                                showPickerFor = null

                            }) { Text(e, style = MaterialTheme.typography.headlineSmall) }

                        }

                    }

                }

            }

        )

    }

}



// ----- Helpers & UI pieces -----



private val emojiChoices = listOf(

// Connected / positive (normal → extreme)

    "🙂","😀","😄","😊","🤗","😌","😎","🥰","😍","🥹","🤩","😇",

// Neutral / reflective

    "😐","😶","😑","🤔","😏","🥲","🫨","😳",

// Distressed / self-worth low (normal → extreme)

    "😔","😞","🥺","😣","😩","🙁","😢","😭",

// Anxious / fearful (normal → extreme)

    "😬","😟","😰","😱","😨","😥",

// Overwhelmed / irritable / angry (normal → extreme)

    "😫","😵‍💫","🤯","😒","😤","😠","😡","🤬",

// Uncertain / ashamed / woozy / shocked

    "😕","😖","😓","😧","🥴","😲",

// misc — energy / affection / desire

    "🔥","💪","💋","😘","🥰","😍","💘","💖","💞","💕","😉","😏","😈","🤤","🥵","🫦"

)



@OptIn(ExperimentalFoundationApi::class)

@Composable

private fun QuickEmojiChip(

    emoji: String,

    selected: Boolean,

    onClick: () -> Unit,

    onLongPress: () -> Unit

) {

    val shape: Shape = MaterialTheme.shapes.large

    Surface(

        tonalElevation = if (selected) 4.dp else 0.dp,

        shape = shape,

        modifier = Modifier

            .shadow(if (selected) 8.dp else 0.dp, shape, clip = false)

            .border(

                width = if (selected) 2.dp else 1.dp,

                brush = SolidColor(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),

                shape = shape

            )

            .combinedClickable(onClick = onClick, onLongClick = onLongPress)

            .padding(horizontal = 12.dp, vertical = 8.dp)

    ) {

        Text(emoji, style = MaterialTheme.typography.titleLarge)

    }

}



/** Matches the emoji chips; disabled/greyed until there's something to clear. */

@Composable

private fun ClearChip(

    enabled: Boolean,

    onClear: () -> Unit

) {

    val shape: Shape = MaterialTheme.shapes.large

    val borderColor =

        if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    val xColor =

        if (enabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)



    Surface(

        tonalElevation = 0.dp,

        shape = shape,

        modifier = Modifier

            .border(width = 1.dp, brush = SolidColor(borderColor), shape = shape)

            .then(if (enabled) Modifier.clickable(onClick = onClear) else Modifier)

            .padding(horizontal = 12.dp, vertical = 8.dp)

    ) {

        Text("❌", color = xColor, style = MaterialTheme.typography.titleLarge)

    }

}



/** Toggle/evict with max size. */

private fun toggleEmoji(current: List<String>, emoji: String, limit: Int): List<String> {

    if (current.contains(emoji)) return current.filterNot { it == emoji }

    val appended = current + emoji

    return if (appended.size <= limit) appended else appended.drop(appended.size - limit)

}



/** Very simple mood rating heuristic based on the first (primary) emoji. */

private fun moodRatingFromEmojis(list: List<String>): Int? {

    val e = list.firstOrNull() ?: return null

    return when (e) {

        "😀", "🤩" -> 5

        "🙂", "😎" -> 4

        "😐" -> 3

        "🙁", "😤" -> 2

        "😢", "😵" -> 1

        else -> null

    }

}