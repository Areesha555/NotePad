package com.example.notepad.UI_layer

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.notepad.data.Note

data class NoteState(
    val notes : List<Note> = emptyList(),
    val title : MutableState<String> = mutableStateOf(""),
    val description : MutableState<String> = mutableStateOf(""),
    var editingNote: Note? = null
)


