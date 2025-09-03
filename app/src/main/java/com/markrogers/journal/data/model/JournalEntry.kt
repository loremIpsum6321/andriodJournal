package com.markrogers.journal.data.model

import java.time.Instant

data class JournalEntry(
    val id: Long,
    val createdAt: Instant,
    val title: String,
    val body: String,
    val moodEmojis: List<String> = emptyList(),
    val moodRating: Int? = null,
    val toggleX: Boolean = false,
    val toggleY: Boolean = false,
    val toggleZ: Boolean = false,
    val toggleW: Boolean = false,
    val sleepHours: Float = 0f,
    val isPinned: Boolean = false,
    /** Mark entries that were auto-generated or imported as “test” so we can clear them. */
    val isTest: Boolean = false
)
