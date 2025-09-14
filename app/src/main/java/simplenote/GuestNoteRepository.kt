package com.yourorg.simplenote.data.guest

import simplenote.Note
import simplenote.NoteRequest
import simplenote.PaginatedNoteList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GuestNoteRepository(private val dao: GuestNoteDao) {
    private val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    private fun GuestNoteEntity.toNote(): Note = Note(
        id = id,
        title = title,
        description = description,
        created = fmt.format(Date(created)),
        updated = fmt.format(Date(updated))
    )

    suspend fun listNotes(page: Int? = null, page_size: Int? = null): PaginatedNoteList {
        val all = dao.getAll().map { it.toNote() }
        return PaginatedNoteList(count = all.size, next = null, previous = null, results = all)
    }

    suspend fun filterNotes(
        title: String? = null,
        description: String? = null,
        updated__gte: String? = null,
        updated__lte: String? = null,
        page: Int? = null,
        page_size: Int? = null
    ): PaginatedNoteList {
        val q = (title ?: description ?: "").trim()
        val all = if (q.isEmpty()) dao.getAll() else dao.getAll().filter {
            it.title.contains(q, ignoreCase = true) || it.description.contains(q, ignoreCase = true)
        }
        val res = all.map { it.toNote() }
        return PaginatedNoteList(count = res.size, next = null, previous = null, results = res)
    }

    suspend fun retrieveNote(id: Int): Note {
        val n = dao.getById(id) ?: throw IllegalStateException("Guest note not found")
        return n.toNote()
    }

    suspend fun createNote(note: NoteRequest): Note {
        val now = System.currentTimeMillis()
        val entity = GuestNoteEntity(title = note.title, description = note.description, created = now, updated = now)
        val newId = dao.insert(entity).toInt()
        return entity.copy(id = newId).toNote()
    }

    suspend fun updateNote(id: Int, note: NoteRequest): Note {
        val current = dao.getById(id) ?: throw IllegalStateException("Guest note not found")
        val updated = current.copy(title = note.title, description = note.description, updated = System.currentTimeMillis())
        dao.update(updated)
        return updated.toNote()
    }

    suspend fun partialUpdateNote(id: Int, note: NoteRequest): Note = updateNote(id, note)

    suspend fun deleteNote(id: Int) {
        dao.getById(id)?.let { dao.delete(it) }
    }
}
