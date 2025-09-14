package simplenote.data.db

import retrofit2.Retrofit

object NoteClient {
    lateinit var noteService: NoteService
        private set

    fun init(retrofit: Retrofit) {
        noteService = retrofit.create(NoteService::class.java)
    }
}