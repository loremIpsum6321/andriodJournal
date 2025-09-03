package com.markrogers.journal.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "journal_prefs")

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class AiProvider { NONE, OPENAI, GEMINI }

data class AppPrefs(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val requireBiometric: Boolean = false,
    val openAiKey: String = "",
    val geminiKey: String = "",
    val provider: AiProvider = AiProvider.NONE,
    val quickEmojis: List<String> = listOf("😀","🙂","😐","🙁","😴")
)

class PreferencesRepository(private val context: Context) {
    private object Keys {
        val THEME = intPreferencesKey("theme")
        val BIO = booleanPreferencesKey("bio")
        val OPENAI = stringPreferencesKey("openai_key")
        val GEMINI = stringPreferencesKey("gemini_key")
        val PROVIDER = intPreferencesKey("provider")
        val EM1 = stringPreferencesKey("emoji_1")
        val EM2 = stringPreferencesKey("emoji_2")
        val EM3 = stringPreferencesKey("emoji_3")
        val EM4 = stringPreferencesKey("emoji_4")
        val EM5 = stringPreferencesKey("emoji_5")
    }

    val prefsFlow: Flow<AppPrefs> = context.dataStore.data.map { p ->
        AppPrefs(
            theme = ThemeMode.values().getOrElse(p[Keys.THEME] ?: 0) { ThemeMode.SYSTEM },
            requireBiometric = p[Keys.BIO] ?: false,
            openAiKey = p[Keys.OPENAI] ?: "",
            geminiKey = p[Keys.GEMINI] ?: "",
            provider = AiProvider.values().getOrElse(p[Keys.PROVIDER] ?: 0) { AiProvider.NONE },
            quickEmojis = listOf(
                p[Keys.EM1] ?: "😀",
                p[Keys.EM2] ?: "🙂",
                p[Keys.EM3] ?: "😐",
                p[Keys.EM4] ?: "🙁",
                p[Keys.EM5] ?: "😴"
            )
        )
    }

    suspend fun setTheme(mode: ThemeMode) { context.dataStore.edit { it[Keys.THEME] = mode.ordinal } }
    suspend fun setBiometric(required: Boolean) { context.dataStore.edit { it[Keys.BIO] = required } }
    suspend fun setOpenAiKey(key: String) { context.dataStore.edit { it[Keys.OPENAI] = key } }
    suspend fun setGeminiKey(key: String) { context.dataStore.edit { it[Keys.GEMINI] = key } }
    suspend fun setProvider(p: AiProvider) { context.dataStore.edit { it[Keys.PROVIDER] = p.ordinal } }

    /** Update one of the 5 quick emoji slots (0..4). */
    suspend fun setQuickEmoji(index: Int, emoji: String) {
        val k = listOf(Keys.EM1, Keys.EM2, Keys.EM3, Keys.EM4, Keys.EM5)[index.coerceIn(0, 4)]
        context.dataStore.edit { it[k] = emoji }
    }
}
