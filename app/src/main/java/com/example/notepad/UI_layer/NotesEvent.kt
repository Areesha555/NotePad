package com.example.notepad.UI_layer

import android.app.Activity
import com.example.notepad.data.Note

sealed interface NotesEvent {
    object SortNotes : NotesEvent
    data class DeleteNote ( var note : Note) : NotesEvent
    data class GenerateSingleNote(val note: Note) : NotesEvent
    data class SaveNotes(
        var title : String,
        var description : String
    ) : NotesEvent
    data class UpdateNote(val note: Note) : NotesEvent
    data class GenerateImg(val action: PdfAction) : NotesEvent
    data class GeneratePdf(val action:PdfAction) : NotesEvent
   /* data class SavePrinterIp(val ip: String) : NotesEvent

    data class PrintUsingIp(val ip: String) : NotesEvent*/

    data class GenerateBitmap(val activity: Activity) : NotesEvent


}
enum class PdfAction {
    VIEW,
    SHARE
}
