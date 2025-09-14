package com.yourorg.simplenote.data.guest

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guest_notes")
data class GuestNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val created: Long = System.currentTimeMillis(),
    val updated: Long = System.currentTimeMillis()
)
