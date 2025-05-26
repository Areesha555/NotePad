package com.example.notepad.UI_layer

import DataStoreManager
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.notepad.ui.theme.DarkBlue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun NoteScreen(
    state: NoteState,
    navController: NavController,
   /* dataStoreManager: DataStoreManager,*/
    onEvent: (NotesEvent) -> Unit,
   /* savedPrinterIp: String?*/
) {

   /* val context = LocalContext.current
    val printerIp by dataStoreManager.printerIpFlow.collectAsState(initial = "")


    var showPrinterDialog by remember { mutableStateOf(false) }
    var localPrinterIp by remember { mutableStateOf(savedPrinterIp ?: "") }
*/
    var showBottomCard by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? Activity
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .background(DarkBlue)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Note Pad",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                IconButton(onClick = {
                    showBottomCard = !showBottomCard // toggle card visibility
                }) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(35.dp),
                        tint = Color.White
                    )
                }

                IconButton(onClick = { onEvent(NotesEvent.GenerateImg(PdfAction.VIEW)) }) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(35.dp),
                        tint = Color.White
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = DarkBlue,
                onClick = {
                    state.title.value = ""
                    state.description.value = ""
                    navController.navigate("AddNoteScreen")
                }
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(35.dp),
                    tint = Color.White)
            }
        }
    ) { paddingValues ->

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.notes.size) { index ->
                    NoteItem(
                        state = state,
                        index = index,
                        navController = navController,
                        onEvent = onEvent
                    )
                }
            }

            // Bottom card
            if (showBottomCard) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, ),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)

                    ) {
                        Spacer(Modifier.height(8.dp))

                        Button(onClick = { onEvent(NotesEvent.GeneratePdf(PdfAction.SHARE)) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Share PDF")

                        }

                        Button(onClick = { onEvent(NotesEvent.GenerateImg(PdfAction.SHARE)) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Share Img")
                        }

                        Button(onClick = { if (activity != null) {
                            onEvent(NotesEvent.GenerateBitmap(activity))
                        }}, modifier = Modifier.fillMaxWidth()) {
                            Text("Print Info")
                        }
                        Button(onClick = { onEvent(NotesEvent.GeneratePdf(PdfAction.VIEW))}, modifier = Modifier.fillMaxWidth()) {
                            Text("View PDF")
                        }
                        Button(onClick = { onEvent(NotesEvent.GenerateImg(PdfAction.VIEW)) }, modifier = Modifier.fillMaxWidth()) {
                            Text("View Img")
                        }


                        Button(onClick = {
                            showBottomCard = false // hide card
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Cancel")
                        }
                    }
                }
            }

           /* if (showPrinterDialog) {

                AlertDialog(
                    onDismissRequest = { showPrinterDialog = false },
                    confirmButton = {
                        Row {
                            Button(onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    dataStoreManager.savePrinterIp(localPrinterIp)
                                }
                                showPrinterDialog = false
                            }) {
                                Text("Save")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(onClick = {
                                val ipToPrint = if (localPrinterIp.isNotBlank()) localPrinterIp else savedPrinterIp ?: ""
                                onEvent(NotesEvent.PrintUsingIp(ipToPrint))
                                showPrinterDialog = false
                            }) {
                                Text("Print Now")
                            }
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showPrinterDialog = false }) {
                            Text("Cancel")
                        }
                    },

                    title = { Text("Enter Printer IP") }, modifier = Modifier.background(Color.White),
                    text = {
                        OutlinedTextField(
                            value = localPrinterIp,
                            onValueChange = { localPrinterIp = it },
                            label = { Text("Printer IP") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    containerColor = Color.White
                )
            }*/


        }
    }
}



@Composable
fun NoteItem(
    state: NoteState,
    index: Int,
    navController: NavController,
    onEvent: (NotesEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkBlue)
            .padding(12.dp)
            .clickable {
                // Pre-fill the fields with current note data
                state.title.value = state.notes[index].title
                state.description.value = state.notes[index].description
                state.editingNote = state.notes[index]
                navController.navigate("EditNoteScreen")
            }
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.notes[index].title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.notes[index].description,
                fontSize = 16.sp,
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = { onEvent(NotesEvent.DeleteNote(state.notes[index])) }
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "Delete Note",
                modifier = Modifier.size(35.dp),
                tint = Color.White
            )
        }
        IconButton(
            onClick = { onEvent(NotesEvent.GenerateSingleNote(state.notes[index])) }
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "Generate Image for this Note",
                modifier = Modifier.size(35.dp),
                tint = Color.White
            )
        }
    }
}