package com.example.notepad.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Upsert
    suspend fun upsertNote( note : Note)
    @Delete
    suspend fun Delete(note : Note)
    @Query("select * from note order by title asc")
    fun getOrderByTitle(): Flow<List<Note>>
    @Query("select * from note order by dateAdded")
    fun getOrderByDate():Flow<List<Note>>

}