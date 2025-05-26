package com.example.notepad

import DataStoreManager
import android.os.Build
import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.notepad.ui.theme.DarkBlue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.notepad.UI_layer.EditNoteScreen
import com.example.notepad.UI_layer.AddNoteScreen
import com.example.notepad.UI_layer.EditNoteScreen
import com.example.notepad.UI_layer.NoteScreen
import com.example.notepad.UI_layer.NoteViewModel
import com.example.notepad.data.NoteDatabase
import com.example.notepad.ui.theme.NotePadTheme

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val bluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: false
        } else true

        if (fine && coarse && bluetooth) {
            // All permissions granted – safe to proceed with Bluetooth
        } else {
            Toast.makeText(this, "Required permissions not granted", Toast.LENGTH_SHORT).show()
        }
    }
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            NoteDatabase::class.java, "notes.db"
        ).build()
    }

    private val viewModel by viewModels<NoteViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val application = this@MainActivity.application
                    val dao = database.dao


                    return NoteViewModel(application, dao) as T
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissions()
        setContent {

            window.statusBarColor = DarkBlue.toArgb()
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = false
            }

            NotePadTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val state by viewModel.state.collectAsState()
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "NoteScreen",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("NoteScreen") {
                            NoteScreen(
                                state = state,
                                navController = navController,
                                onEvent = viewModel::onEvent

                            )
                        }
                        composable("AddNoteScreen") {
                            AddNoteScreen(
                                state = state,
                                navController = navController,
                                onEvent = viewModel::onEvent
                            )
                        }
                        composable("EditNoteScreen") {
                            EditNoteScreen(
                                state = state,
                                navController = navController,
                                onEvent = viewModel::onEvent
                            )
                        }

                    }
                }
            }
        }
    }
    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        permissionLauncher.launch(permissions.toTypedArray())
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NotePadTheme {
        Greeting("Android")
    }
}