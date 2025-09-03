package com.markrogers.journal.ui.analyze
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markrogers.journal.data.prefs.*
import com.markrogers.journal.data.repo.InMemoryRepository
import com.markrogers.journal.net.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun AnalyzeScreen(prefsRepo: PreferencesRepository) {
    val scope = rememberCoroutineScope()
    val prefs by prefsRepo.prefsFlow.collectAsState(initial = AppPrefs())
    var days by remember { mutableStateOf(7) }
    var result by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Analyze journal with AI")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(7,14,30).forEach { d -> FilterChip(selected = days==d, onClick={ days=d }, label={ Text("Last $d days") }) } }
        Button(onClick={
            loading=true; result=null
            scope.launch {
                val until = Instant.now(); val since = until.minus(days.toLong(), ChronoUnit.DAYS)
                val entries = InMemoryRepository.entries.value.filter { it.createdAt.isAfter(since) }
                val text = buildString {
                    appendLine("Summarize these journal entries and produce insights and 5 todos:")
                    entries.forEach { val ts = it.createdAt.atZone(ZoneId.systemDefault()); appendLine("- [${ts}] ${it.title}: ${it.body.take(500)}") }
                }
                try {
                    result = when (prefs.provider) {
                        AiProvider.OPENAI -> if (prefs.openAiKey.isBlank()) "Set your OpenAI key in Settings." else {
                            val s = openAiRetrofit(prefs.openAiKey); val r = s.chat(OpenAiRequest(messages=listOf(Message("system","You are a concise journaling assistant."), Message("user", text)))); r.choices.firstOrNull()?.message?.content ?: "No response"
                        }
                        AiProvider.GEMINI -> if (prefs.geminiKey.isBlank()) "Set your Gemini key in Settings." else {
                            val s = geminiRetrofit(); val r = s.generate(prefs.geminiKey, GeminiRequest(contents=listOf(GeminiContent(parts=listOf(GeminiPart(text)))))); r.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response"
                        }
                        AiProvider.NONE -> "Choose a provider in Settings."
                    }
                } catch (e: Exception) { result = "Error: " + e.message } finally { loading=false }
            }
        }) { Text(if (loading) "Analyzing..." else "Analyze") }
        if (result!=null) { Divider(); Text(result!!) }
    }
}
