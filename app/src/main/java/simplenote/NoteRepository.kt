package simplenote.data.db

import com.yourorg.simplenote.data.db.NoteDao
import com.yourorg.simplenote.data.db.NoteEntity



class NoteRepository(private val dao: NoteDao) {
    private fun Note.toEntity() = NoteEntity(id, title, description, created, updated)

    suspend fun listNotes(page: Int? = null, page_size: Int? = null): PaginatedNoteList {
        val resp = NoteClient.noteService.listNotes(page, page_size)
        try { dao.upsertAll(resp.results.map { it.toEntity() }) } catch (_: Exception) {}
        return resp
    }

    suspend fun filterNotes(
        title: String? = null,
        description: String? = null,
        updated__gte: String? = null,
        updated__lte: String? = null,
        page: Int? = null,
        page_size: Int? = null
    ): PaginatedNoteList {
        val resp = NoteClient.noteService.filterNotes(title, description, updated__gte, updated__lte, page, page_size)
        try { dao.upsertAll(resp.results.map { it.toEntity() }) } catch (_: Exception) {}
        return resp
    }

    suspend fun retrieveNote(id: Int): Note {
        val n = NoteClient.noteService.retrieveNote(id)
        try { dao.upsert(n.toEntity()) } catch (_: Exception) {}
        return n
    }

    suspend fun createNote(note: NoteRequest): Note {
        val n = NoteClient.noteService.createNote(note)
        try { dao.upsert(n.toEntity()) } catch (_: Exception) {}
        return n
    }

    suspend fun updateNote(id: Int, note: NoteRequest): Note {
        val n = NoteClient.noteService.updateNote(id, note)
        try { dao.upsert(n.toEntity()) } catch (_: Exception) {}
        return n
    }

    suspend fun partialUpdateNote(id: Int, note: NoteRequest): Note {
        val n = NoteClient.noteService.partialUpdateNote(id, note)
        try { dao.upsert(n.toEntity()) } catch (_: Exception) {}
        return n
    }

    suspend fun deleteNote(id: Int) {
        NoteClient.noteService.deleteNote(id)
        try { dao.deleteById(id) } catch (_: Exception) {}
    }
}