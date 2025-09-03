package com.markrogers.journal.data.repo

import com.markrogers.journal.data.model.JournalEntry
import com.markrogers.journal.data.model.TodoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

object InMemoryRepository {

    // ---- entries store ------------------------------------------------------

    private val genEntry = AtomicLong(0)
    private val _entries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val entries: StateFlow<List<JournalEntry>> = _entries

    fun addEntry(
        title: String,
        body: String,
        moodEmojis: List<String>,
        moodRating: Int?,
        toggleX: Boolean,
        toggleY: Boolean,
        toggleZ: Boolean,
        toggleW: Boolean,
        sleepHours: Float,
        isTest: Boolean = false
    ): Long {
        val id = genEntry.incrementAndGet()
        val now = Instant.now()
        val entry = JournalEntry(
            id = id,
            createdAt = now,
            title = title,
            body = body,
            moodEmojis = moodEmojis,
            moodRating = moodRating,
            toggleX = toggleX,
            toggleY = toggleY,
            toggleZ = toggleZ,
            toggleW = toggleW,
            sleepHours = sleepHours,
            isTest = isTest
        )
        _entries.update { (it + entry).sortedBy { e -> e.createdAt } }
        return id
    }

    fun deleteEntry(id: Long) {
        _entries.update { list -> list.filterNot { it.id == id } }
    }

    /** Undo helper (used by Timeline swipe). */
    fun restoreEntry(entry: JournalEntry) {
        _entries.update { (it + entry).sortedBy { e -> e.createdAt } }
    }
    // Add anywhere inside object InMemoryRepository { ... }
    fun find(id: Long): JournalEntry? = _entries.value.firstOrNull { it.id == id }

    // ---- dummy data helpers (used by Metrics) -------------------------------

    fun generateDummy(start: LocalDate, end: LocalDate) {
        if (end.isBefore(start)) return
        val rng = Random(System.currentTimeMillis())
        val zone = ZoneId.systemDefault()
        val moodPool = listOf("🙂","😀","😐","🙁","😢","😴","🥰","🤩","😤","🤯")

        val toAdd = buildList {
            var day = start
            while (!day.isAfter(end)) {
                repeat(rng.nextInt(0, 3)) {
                    val id = genEntry.incrementAndGet()
                    val createdAt = day.atStartOfDay(zone).toInstant()
                        .plusSeconds(rng.nextLong(0, 60L * 60L * 20L))
                    val moods = moodPool.shuffled(rng).take(rng.nextInt(0, 3))
                    val moodRating: Int? = moods.firstOrNull()?.let { e ->
                        when (e) {
                            "😀","🤩","🥰" -> 5
                            "🙂"           -> 4
                            "😐"           -> 3
                            "🙁","😤"      -> 2
                            "😢","😴","🤯" -> 1
                            else           -> 3
                        }
                    }
                    add(
                        JournalEntry(
                            id = id,
                            createdAt = createdAt,
                            title = "Auto ${day.monthValue}/${day.dayOfMonth}",
                            body = "",
                            moodEmojis = moods,
                            moodRating = moodRating,
                            toggleX = rng.nextBoolean(),
                            toggleY = rng.nextBoolean(),
                            toggleZ = rng.nextBoolean(),
                            toggleW = rng.nextBoolean(),
                            sleepHours = rng.nextDouble(3.5, 9.5).toFloat(),
                            isTest = true
                        )
                    )
                }
                day = day.plusDays(1)
            }
        }
        _entries.update { (it + toAdd).sortedBy { e -> e.createdAt } }
    }

    /** Remove only generated (isTest=true) entries. */
    fun clearTestData() {
        _entries.update { list -> list.filterNot { it.isTest } }
    }

    // ---- todos store (used by Calendar) -------------------------------------

    private val genTodo = AtomicLong(0)
    private val _todos = MutableStateFlow<List<TodoItem>>(emptyList())
    val todos: StateFlow<List<TodoItem>> = _todos

    /** Return todos that fall exactly on [date]. */
    fun todosOn(date: LocalDate): List<TodoItem> =
        _todos.value.filter { it.date == date }

    /** Add a new todo for [date]. */
    fun addTodo(date: LocalDate, text: String) {
        val id = genTodo.incrementAndGet()
        val item = TodoItem(id = id, date = date, text = text, done = false)
        _todos.update { it + item }
    }

    /** Toggle completion for todo with [id]. */
    fun toggleTodo(id: Long) {
        _todos.update { list ->
            list.map { if (it.id == id) it.copy(done = !it.done) else it }
        }
    }
}
