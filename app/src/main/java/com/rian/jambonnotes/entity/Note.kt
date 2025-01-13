package com.rian.jambonnotes.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note (
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val title: String,           // Judul catatan
    val note: String,         // Isi catatan
    val imagePath: String?,      // Path gambar (bisa null)
    val audioPath: String?       // Path audio (bisa null)
)



