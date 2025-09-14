package simplenote.data.db

import com.google.gson.annotations.SerializedName

data class NoteRequest(
    val title: String,
    val description: String
)

data class Note(
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("created_at") val created: String,
    @SerializedName("updated_at") val updated: String
)

data class PaginatedNoteList(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Note>
)