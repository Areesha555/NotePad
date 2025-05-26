package com.example.notepad.UI_layer

import DataStoreManager
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.content.FileProvider

import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.print.PrintHelper
import com.example.notepad.BluetoothHelper
import com.example.notepad.data.Note
import com.example.notepad.data.NoteDao
import com.example.notepad.util.ImgGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NoteViewModel(
    application: Application,
    private  var dao: NoteDao,
   /* private val dataStoreManager: DataStoreManager,
    private val PrinterManager : PrinterManager*/
): AndroidViewModel(application) {

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

    /*  private val _printerIp = MutableStateFlow<String?>(null)
    val printerIp = _printerIp


    init {

        viewModelScope.launch {
            dataStoreManager.printerIpFlow.collect { ip ->
                _printerIp.value = ip ?: ""
            }
        }
    }
    suspend fun isPrinterAvailable(ip: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val address = java.net.InetAddress.getByName(ip)
            address.isReachable(5000)
        } catch (e: Exception) {
            false
        }
    }*/
    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.DeleteNote -> {
                viewModelScope.launch {
                    dao.Delete(event.note)
                }
            }

            is NotesEvent.GenerateSingleNote -> {
                viewModelScope.launch {
                    val context = getApplication<Application>().applicationContext
                    val note = event.note


                    val file = withContext(Dispatchers.IO) {
                        ImgGenerator.generateImg(context, listOf(note))
                    }


                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "image/jpeg")
                        flags =
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "No image viewer found", Toast.LENGTH_SHORT).show()
                    }
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
                    dao.upsertNote(
                        note = event.note.copy(
                            dateAdded = System.currentTimeMillis()
                        )
                    )
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
                isSortedBydateAdded.value = !isSortedBydateAdded.value

            }

            is NotesEvent.GenerateImg -> {
                viewModelScope.launch {
                    val context = getApplication<Application>().applicationContext
                    val currentNotes = state.value.notes

                    if (currentNotes.isNotEmpty()) {
                        val file = withContext(Dispatchers.IO) {
                            ImgGenerator.generateImg(context, currentNotes)
                        }


                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )

                        val intent = when (event.action) {
                            PdfAction.VIEW -> Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "image/jpeg")
                                flags =
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                            }

                            PdfAction.SHARE -> Intent(Intent.ACTION_SEND).apply {
                                setDataAndType(uri, "image/jpeg")
                                putExtra(Intent.EXTRA_STREAM, uri)
                                flags =
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK

                            }
                        }


                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "No img viewer found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(context, "No notes to export", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            is NotesEvent.GeneratePdf -> {
                viewModelScope.launch {
                    val context = getApplication<Application>().applicationContext
                    val currentNotes = state.value.notes

                    if (currentNotes.isNotEmpty()) {
                        val file = withContext(Dispatchers.IO) {
                            ImgGenerator.generatePdf(context, currentNotes)
                        }

                        val fileSizeInBytes = file.length()
                        val fileSizeInKB = fileSizeInBytes / 1024
                        val fileSizeInMB = fileSizeInBytes / (1024 * 1024)
                        val sizeText = if (fileSizeInMB > 0)
                            "%.2f MB".format(fileSizeInBytes / (1024.0 * 1024.0))
                        else
                            "$fileSizeInKB KB"

                        Toast.makeText(context, "PDF size: $sizeText", Toast.LENGTH_SHORT).show()
                        // Open the PDF
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )


                        val intent = when (event.action) {
                            PdfAction.VIEW -> Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                flags =
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                            }

                            PdfAction.SHARE -> Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                flags =
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        }

                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "No pdf viewer found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(context, "No notes to export", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            is NotesEvent.GenerateBitmap -> {
                viewModelScope.launch {
                    val context = getApplication<Application>().applicationContext
                    val currentNotes = state.value.notes

                    if (currentNotes.isNotEmpty()) {
                        val Bitmap = withContext(Dispatchers.IO) {
                            ImgGenerator.generateBitmap(context, currentNotes, forPrinting = true)
                        }
                        val textBelow = "Printed via Notepad App"



                        withContext(Dispatchers.Main) {
                            val printHelper = PrintHelper(event.activity)
                            printHelper.scaleMode = PrintHelper.SCALE_MODE_FIT
                            printHelper.printBitmap("Notes_Print", Bitmap)
                        }


                    } else {
                        Toast.makeText(context, "No notes to print", Toast.LENGTH_SHORT).show()
                    }

                }
            }

            /*is NotesEvent.SavePrinterIp -> {
                viewModelScope.launch {
                    _printerIp.value = event.ip
                    dataStoreManager.savePrinterIp(event.ip)
                }
            }*/

            /* is NotesEvent.PrintUsingIp -> {
                viewModelScope.launch {
                    val ipToUse = event.ip.ifEmpty { _printerIp.value }
                    val context = getApplication<Application>().applicationContext

                    if (ipToUse != null) {
                        if (ipToUse.isBlank()) {
                            Toast.makeText(context, "Printer IP is empty", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                    }

                    val reachable = ipToUse?.let { PrinterManager.isPrinterReachable(it) }
                    if (!reachable!!) {
                        Toast.makeText(context, "Printer not reachable at $ipToUse", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val currentNotes = state.value.notes
                    if (currentNotes.isEmpty()) {
                        Toast.makeText(context, "No notes to print", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val bitmap = withContext(Dispatchers.IO) {
                        ImgGenerator.generateBitmap(context, currentNotes, forPrinting = true)
                    }

                    val success = PrinterManager.printBitmap(ipToUse, bitmap)
                    if (success) {
                        Toast.makeText(context, "Printed successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to print", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // ... other events ...
        }*/
        }
    }
}

