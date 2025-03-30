package com.example.notepad.UI_layer

import com.example.notepad.data.Note

sealed interface NotesEvent {
    object SortNotes : NotesEvent
    data class DeleteNote ( var note : Note) : NotesEvent
    data class SaveNotes(
        var title : String,
        var description : String
    ) : NotesEvent
    data class UpdateNote(val note: Note) : NotesEvent


}