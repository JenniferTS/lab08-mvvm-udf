package com.example.lab08

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lab08.ui.theme.Lab08Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab08Theme {
                // viewModel() reutiliza el mismo ViewModel aunque la pantalla se recomponga
                val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModel.Factory)
                TaskScreen(taskViewModel)
            }
        }
    }
}
