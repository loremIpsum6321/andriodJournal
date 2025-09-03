package com.markrogers.journal.ui.settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markrogers.journal.data.prefs.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(repo: PreferencesRepository) {
    val prefs by repo.prefsFlow.collectAsState(initial = AppPrefs())
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Appearance", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.values().forEach { m ->
                FilterChip(selected = prefs.theme==m, onClick = { scope.launch { repo.setTheme(m) } }, label = { Text(m.name.lowercase().replaceFirstChar { it.uppercase() }) })
            }
        }
        Divider()
        Text("AI Provider", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(AiProvider.NONE, AiProvider.OPENAI, AiProvider.GEMINI).forEach { p ->
                FilterChip(selected = prefs.provider==p, onClick = { scope.launch { repo.setProvider(p) } }, label = { Text(p.name) })
            }
        }
        OutlinedTextField(value = prefs.openAiKey, onValueChange = { v -> scope.launch { repo.setOpenAiKey(v) } }, label = { Text("OpenAI API Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = prefs.geminiKey, onValueChange = { v -> scope.launch { repo.setGeminiKey(v) } }, label = { Text("Gemini API Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Divider()
        Text("Security", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Require biometric to open"); Spacer(Modifier.width(12.dp)); Switch(checked = prefs.requireBiometric, onCheckedChange = { v -> scope.launch { repo.setBiometric(v) } })
        }
    }
}
