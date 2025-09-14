package com.yourorg.simplenote.data.guest

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GuestNoteEntity::class], version = 1, exportSchema = false)
abstract class GuestDatabase : RoomDatabase() {
    abstract fun guestNoteDao(): GuestNoteDao
}

object GuestDatabaseProvider {
    @Volatile private var INSTANCE: GuestDatabase? = null
    fun get(context: Context): GuestDatabase = INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
            context.applicationContext,
            GuestDatabase::class.java,
            "guest_note_db"
        ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
    }
}
