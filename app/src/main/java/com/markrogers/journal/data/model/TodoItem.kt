package com.markrogers.journal.data.model
import java.time.LocalDate
data class TodoItem(val id: Long, val date: LocalDate, val text: String, val done: Boolean = false)
