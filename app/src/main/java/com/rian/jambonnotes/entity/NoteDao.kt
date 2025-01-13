package com.rian.jambonnotes.entity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update



@Dao
interface NoteDao {

    @Insert
    suspend fun addNote(note:Note)

    @Query("SELECT * FROM note ORDER BY id DESC")
    suspend fun getNotes() : List<Note>

    @Query("SELECT * FROM note WHERE id=:note_id")
    suspend fun getNote(note_id: Int) : List<Note>
    @Update
    suspend fun updateNote(note:Note)

    @Delete
    suspend fun deleteNote(note: Note)




}