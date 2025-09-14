package com.yourorg.simplenote.data.guest

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GuestNoteDao {
    @Query("SELECT * FROM guest_notes ORDER BY updated DESC")
    fun observeAll(): Flow<List<GuestNoteEntity>>


    @Query("SELECT * FROM guest_notes WHERE title LIKE :pattern OR description LIKE :pattern")
    fun observeByLike(pattern: String): Flow<List<GuestNoteEntity>>
    @Query("SELECT * FROM guest_notes ORDER BY updated DESC")
    suspend fun getAll(): List<GuestNoteEntity>

    @Query("SELECT * FROM guest_notes WHERE id = :id")
    suspend fun getById(id: Int): GuestNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: GuestNoteEntity): Long

    @Update
    suspend fun update(note: GuestNoteEntity)

    @Delete
    suspend fun delete(note: GuestNoteEntity)

    @Query("DELETE FROM guest_notes")
    suspend fun clear()
}

