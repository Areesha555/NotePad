package com.example.notepad.UI_layer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.notepad.data.Note
import com.example.notepad.ui.theme.DarkBlue
import com.example.notepad.ui.theme.grey

@Composable
fun EditNoteScreen(
    state: NoteState,
    navController: NavController,
    onEvent: (NotesEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = state.title.value,
            onValueChange = { state.title.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textStyle = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = state.description.value,
            onValueChange = { state.description.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onEvent(NotesEvent.UpdateNote(
                    state.editingNote!!.copy(
                        title = state.title.value,
                        description = state.description.value
                    )
                ))
                        navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth().background(color = DarkBlue,
                shape = RoundedCornerShape(20.dp))
        ) {
            Text("Update Note")
        }
    }
}

