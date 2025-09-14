package simplenote.data.db

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface NoteService {
    @GET("/api/notes/")
    suspend fun listNotes(
        @Query("page") page: Int? = null,
        @Query("page_size") page_size: Int? = null
    ): PaginatedNoteList

    @GET("/api/notes/filter")
    suspend fun filterNotes(
        @Query("title") title: String? = null,
        @Query("description") description: String? = null,
        @Query("updated__gte") updated__gte: String? = null,
        @Query("updated__lte") updated__lte: String? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") page_size: Int? = null
    ): PaginatedNoteList

    @GET("/api/notes/{id}/")
    suspend fun retrieveNote(@Path("id") id: Int): Note

    @POST("/api/notes/")
    suspend fun createNote(@Body body: NoteRequest): Note

    @PUT("/api/notes/{id}/")
    suspend fun updateNote(@Path("id") id: Int, @Body body: NoteRequest): Note

    @PATCH("/api/notes/{id}/")
    suspend fun partialUpdateNote(@Path("id") id: Int, @Body body: NoteRequest): Note

    @DELETE("/api/notes/{id}/")
    suspend fun deleteNote(@Path("id") id: Int)
}