package com.example.notepad.UI_layer

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.example.notepad.data.Note
import com.example.notepad.data.NoteDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteViewModel(
    private  var dao: NoteDao
):ViewModel() {
    private var isSortedBydateAdded = MutableStateFlow(true)
    private var notes = isSortedBydateAdded.flatMapLatest {
        if (it) {
            dao.getOrderByDate()
        } else {
            dao.getOrderByTitle()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    var _state = MutableStateFlow(NoteState())
    var state = combine(_state, isSortedBydateAdded, notes) { state, isSortedBydateAdded, notes ->
        state.copy(
            notes = notes
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NoteState())
    fun onEvent(event : NotesEvent) {
        when(event){
            is NotesEvent.DeleteNote -> {
                viewModelScope.launch {
                    dao.Delete(event.note)
                }
            }
            is NotesEvent.SaveNotes -> {

                    val note = Note(
                        title = state.value.title.value,
                        description = state.value.description.value,
                        dateAdded = System.currentTimeMillis()
                    )
                    viewModelScope.launch {
                        dao.upsertNote(note = note)
                    }
                _state.update { 
                    it.copy(
                        title = mutableStateOf(""),
                        description = mutableStateOf("")
                    )

                }

            }
            is NotesEvent.UpdateNote -> {
                viewModelScope.launch {
                    dao.upsertNote(note = event.note.copy(
                        dateAdded = System.currentTimeMillis()
                    ))
                }
                _state.update {
                    it.copy(
                        title = mutableStateOf(""),
                        description = mutableStateOf(""),
                        editingNote = null
                    )
                }
            }
            NotesEvent.SortNotes -> {
                isSortedBydateAdded.value = ! isSortedBydateAdded.value

            }
        }
    }
}