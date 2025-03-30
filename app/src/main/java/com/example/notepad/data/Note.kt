package com.example.notepad.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note (
    @PrimaryKey(autoGenerate = true)
    var id : Int=0,
    val title : String,
    val description : String,
    val dateAdded : Long
)